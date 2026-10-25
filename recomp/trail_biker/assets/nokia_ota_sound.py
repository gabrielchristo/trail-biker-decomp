"""FORMAT_TONE playback for the slices SoundManager copies out of sound.dat.

Mirrors KEmulator Sound.init(byte[], FORMAT_TONE) -> RingtoneParser + MidiBuilder.
The bit window, the +10 byte pad, and tempo*4 are part of that parser. A decode
that does not finish raises OtaDecodeError.
"""

from __future__ import annotations

import io
import wave
from dataclasses import dataclass

_BPM = (
    25, 28, 31, 35, 40, 45, 50, 56, 63, 70, 80, 90, 100, 112, 125, 140,
    160, 180, 200, 225, 250, 285, 320, 355, 400, 450, 500, 565, 635, 715,
    800, 900,
)

# MidiBuilder writes time division 240. A quarter note is 240 ticks.
_DIVISION = 240
NOKIA_SAMPLE_RATE = 8000
FORMAT_TONE = 1


class OtaDecodeError(ValueError):
    pass


@dataclass(frozen=True)
class NoteEvent:
    frequency_hz: float
    duration_ms: int
    volume: float
    sounding_ms: int


class _RingtoneBits:
    """RingtoneParser.readBits: 16-bit window, MSB first, one byte step when offset > 7."""

    def __init__(self, data: bytes) -> None:
        self.data = bytes(data) + bytes(10)
        self.length = len(data)
        self.bit_index = 0
        self.bit_offset = 0

    def read(self, count: int) -> int:
        if self.bit_index + 1 >= len(self.data):
            raise OtaDecodeError(
                f"unexpected end of OTA bitstream at byte {self.bit_index}"
            )
        window = ((self.data[self.bit_index] & 0xFF) << 8) | (self.data[self.bit_index + 1] & 0xFF)
        value = (window >> (16 - (count + self.bit_offset))) & ((1 << count) - 1)
        self.bit_offset += count
        if self.bit_offset > 7:
            self.bit_offset -= 8
            self.bit_index += 1
        return value


def _midi_hz(midi_note: int) -> float:
    if midi_note <= 0:
        return 0.0
    return 440.0 * (2.0 ** ((midi_note - 69) / 12.0))


def _duration_ms(duration_type: int, note_type: int, bpm: int) -> int:
    if duration_type < 0 or duration_type > 7:
        raise OtaDecodeError(f"invalid duration type {duration_type}")
    if bpm <= 0:
        raise OtaDecodeError("tempo is zero")
    base = int(_DIVISION * (4.0 / (1 << duration_type)))
    if note_type == 1:
        ticks = base + base // 2
    elif note_type == 2:
        ticks = base + base // 2 + base // 4
    else:
        ticks = base
    return max(1, int(ticks * 60000 / (_DIVISION * bpm)))


def decode_ota_chunk(chunk: bytes) -> list[NoteEvent]:
    if not chunk:
        raise OtaDecodeError("empty tone buffer")
    bits = _RingtoneBits(chunk)
    bits.read(8)
    bits.read(8)
    bits.read(7)
    ringtone_type = bits.read(3)
    if ringtone_type not in (1, 2):
        raise OtaDecodeError(f"unsupported ringtone type {ringtone_type}")
    title_length = bits.read(4)
    for _ in range(title_length):
        bits.read(8)
    pattern_count = bits.read(8)
    if pattern_count <= 0:
        raise OtaDecodeError("pattern count is zero")

    tempo_bpm = 0
    octave = 0
    volume = 0
    events: list[NoteEvent] = []
    while bits.bit_index < bits.length and pattern_count > 0:
        bits.read(3)
        bits.read(2)
        bits.read(4)
        instruction_count = bits.read(8)
        for _ in range(instruction_count):
            if bits.bit_index >= bits.length:
                break
            command = bits.read(3)
            if command == 0:
                bits.read(2)
            elif command == 1:
                note = bits.read(4)
                duration_type = bits.read(3)
                note_type = bits.read(2)
                bpm = tempo_bpm if tempo_bpm else 250
                midi_note = 0 if note == 0 else 60 + (note - 1) + octave * 12
                events.append(
                    NoteEvent(
                        frequency_hz=_midi_hz(midi_note),
                        duration_ms=_duration_ms(duration_type, note_type, bpm),
                        volume=1.0 if note else 0.0,
                        sounding_ms=_duration_ms(duration_type, note_type, bpm),
                    )
                )
            elif command == 2:
                octave = bits.read(2)
                if octave > 0:
                    octave -= 1
            elif command == 3:
                bits.read(2)
            elif command == 4:
                tempo_index = bits.read(5)
                if tempo_index >= len(_BPM):
                    raise OtaDecodeError(f"tempo index {tempo_index} out of range")
                tempo_bpm = _BPM[tempo_index] * 4
            elif command == 5:
                volume = bits.read(4)
            else:
                raise OtaDecodeError(
                    f"unknown instruction {command} at byte {bits.bit_index} offset {bits.bit_offset}"
                )
        pattern_count -= 1
    if volume:
        events = [
            NoteEvent(e.frequency_hz, e.duration_ms, min(1.0, volume / 15.0), e.sounding_ms)
            for e in events
        ]
    if not events:
        raise OtaDecodeError("no notes decoded")
    return events


def _synth_square(events: list[NoteEvent], sample_rate: int) -> bytes:
    pcm = bytearray()
    for ev in events:
        samples = max(1, int(sample_rate * ev.duration_ms / 1000))
        if ev.frequency_hz <= 0 or ev.volume <= 0:
            pcm.extend(b"\x80" * samples)
            continue
        period = sample_rate / ev.frequency_hz
        for i in range(samples):
            phase = (i / period) % 1.0
            amp = ev.volume if phase < 0.5 else -ev.volume
            pcm.append(max(0, min(255, int(128 + amp * 127))))
    return bytes(pcm)


def ota_to_pcm(chunk: bytes, sample_rate: int = NOKIA_SAMPLE_RATE) -> bytes:
    return _synth_square(decode_ota_chunk(chunk), sample_rate)


def ota_to_wav_bytes(chunk: bytes, sample_rate: int = NOKIA_SAMPLE_RATE, sound_index: int = -1) -> bytes:
    try:
        pcm = ota_to_pcm(chunk, sample_rate)
    except OtaDecodeError as exc:
        label = f"sound[{sound_index}]" if sound_index >= 0 else "sound"
        raise OtaDecodeError(f"{label}: {exc} (hex={chunk.hex()})") from exc
    buf = io.BytesIO()
    with wave.open(buf, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(1)
        wf.setframerate(sample_rate)
        wf.writeframes(pcm)
    return buf.getvalue()


def describe_chunk(chunk: bytes, sound_index: int = -1) -> str:
    events = decode_ota_chunk(chunk)
    total_ms = sum(e.duration_ms for e in events)
    lines = [f"sound_index={sound_index} notes={len(events)} total_ms={total_ms}"]
    for i, ev in enumerate(events):
        lines.append(f"  [{i}] {ev.frequency_hz:.1f} Hz  {ev.duration_ms} ms  vol={ev.volume:.2f}")
    return "\n".join(lines)
