"""Named layout for sound.dat payload (SoundManager / Nokia FORMAT_TONE slices)."""

from __future__ import annotations

from typing import Any

SOUND_FORMAT = "trail_biker_sound"
SOUND_JSON_VERSION = 2

SOUND_LABELS = (
    "log_and_accelerate",
    "crash_stunt",
    "race_finish",
    "stunt_cup_finish_course_8",
)


def _u8(value: int) -> int:
    return value & 0xFF


def _signed_byte(value: int) -> int:
    if value < -128 or value > 255:
        raise ValueError(f"byte out of range: {value}")
    return value - 256 if value > 127 else value


def _uint16(data: list[int], index: int) -> int:
    return 256 * _u8(data[index]) + _u8(data[index + 1])


def parse_slice_offsets(data: list[int]) -> tuple[int, list[int]]:
    if not data:
        raise ValueError("empty sound payload")
    count = _u8(data[0])
    if count == 0:
        raise ValueError("sound count is zero")
    need = 1 + 2 * count
    if len(data) < need:
        raise ValueError(f"sound header needs {need} bytes, have {len(data)}")
    offsets = [_uint16(data, 1 + i * 2) for i in range(count)]
    return count, offsets


def slices_from_flat(data: list[int]) -> list[bytes]:
    count, offsets = parse_slice_offsets(data)
    out: list[bytes] = []
    for index in range(count):
        start = offsets[index]
        end = len(data) if index == count - 1 else offsets[index + 1]
        if start < 0 or end < start or start > len(data):
            raise ValueError(f"sound[{index}] invalid slice {start}..{end}")
        out.append(bytes(_u8(data[i]) for i in range(start, end)))
    return out


def document_from_flat(data: list[int], source_dat: str = "sound.dat") -> dict[str, Any]:
    count, offsets = parse_slice_offsets(data)
    first_offset = offsets[0]
    if first_offset > len(data):
        raise ValueError(f"first sound offset {first_offset} past end {len(data)}")
    header_prefix = [_u8(data[i]) for i in range(first_offset)]
    slices: list[dict[str, Any]] = []
    for index in range(count):
        start = offsets[index]
        end = len(data) if index == count - 1 else offsets[index + 1]
        label = SOUND_LABELS[index] if index < len(SOUND_LABELS) else f"sound_{index}"
        slices.append(
            {
                "index": index,
                "label": label,
                "offset": start,
                "ota_bytes": [_u8(data[i]) for i in range(start, end)],
            }
        )
    last_end = offsets[-1] + len(slices[-1]["ota_bytes"])
    trailer_bytes = [_u8(data[i]) for i in range(last_end, len(data))]
    return {
        "format": SOUND_FORMAT,
        "version": SOUND_JSON_VERSION,
        "playback": "ota",
        "source_dat": source_dat,
        "description": (
            "Nokia OTA (FORMAT_TONE) slices from sound.dat: byte 0 = count, uint16 offsets at "
            "1+i*2, payload bytes per slice. header_prefix preserves count, offset table, and "
            "any padding before the first slice; trailer_bytes preserves trailing bytes after the "
            "last slice for bit-exact round-trip."
        ),
        "sound_count": count,
        "header_prefix": header_prefix,
        "slices": slices,
        "trailer_bytes": trailer_bytes,
    }


def flatten_document(doc: dict[str, Any]) -> list[int]:
    slices = doc["slices"]
    count = int(doc.get("sound_count", len(slices)))
    if len(slices) != count:
        raise ValueError("sound_count != len(slices)")
    header = doc["header_prefix"]
    out = [_signed_byte(_u8(b)) for b in header]
    for entry in sorted(slices, key=lambda s: int(s["index"])):
        for b in entry["ota_bytes"]:
            out.append(_signed_byte(_u8(int(b))))
    for b in doc.get("trailer_bytes") or []:
        out.append(_signed_byte(_u8(int(b))))
    return out
