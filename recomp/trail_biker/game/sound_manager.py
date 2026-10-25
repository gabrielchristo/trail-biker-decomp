"""Port of a.a.a.d.a: slice sound.dat, then Sound(data, FORMAT_TONE)."""

from __future__ import annotations

import pygame

from trail_biker.assets.binary_asset import BinaryAsset
from trail_biker.assets.nokia_ota_sound import FORMAT_TONE
from trail_biker.assets.nokia_ota_sound import NOKIA_SAMPLE_RATE
from trail_biker.assets.nokia_ota_sound import OtaDecodeError
from trail_biker.assets.nokia_ota_sound import ota_to_wav_bytes
from trail_biker.config import RESOURCES


class _NokiaSound:
    """com.nokia.mid.sound.Sound for FORMAT_TONE, one clip at a time."""

    def __init__(self) -> None:
        self._data: bytes | None = None
        self._type = 0
        self._gain = 255
        self._clip: pygame.mixer.Sound | None = None
        self._channel: pygame.mixer.Channel | None = None

    def init(self, data: bytes, sound_type: int) -> None:
        if data is None:
            raise OtaDecodeError("Sound.init data is null")
        if sound_type != FORMAT_TONE:
            raise OtaDecodeError(f"unsupported sound type {sound_type}")
        self._data = data
        self._type = sound_type
        self._clip = pygame.mixer.Sound(buffer=ota_to_wav_bytes(data, NOKIA_SAMPLE_RATE))
        self._clip.set_volume(self._gain / 255.0)

    def set_gain(self, gain: int) -> None:
        self._gain = 0 if gain < 0 else 255 if gain > 255 else gain
        if self._clip is not None:
            self._clip.set_volume(self._gain / 255.0)

    def play(self, loop: int) -> None:
        if self._clip is None or loop < 0:
            raise OtaDecodeError(f"Sound.play loop={loop}")
        if self._channel is None:
            self._channel = pygame.mixer.find_channel(True)
        if self._channel is None:
            self._clip.play(loops=-1 if loop == 0 else loop - 1)
            return
        self._channel.stop()
        self._channel.play(self._clip, loops=-1 if loop == 0 else loop - 1)


class SoundManager:
    def __init__(self, resource: str = "/res/sound.dat") -> None:
        self._reserved_zero = 0
        self._sound_data_type = FORMAT_TONE
        self.sample_data: list[bytes] = []
        self._nokia_sound: _NokiaSound | None = None
        self._load_sounds_from_resource(resource)

    def _load_sounds_from_resource(self, resource: str) -> None:
        # a.java: new BinaryAsset(s); count = data[0]; uint16 offsets at 1+i*2.
        name = resource.rsplit("/", 1)[-1]
        asset = BinaryAsset(RESOURCES / name)
        count = asset.data[0]
        if count < 0:
            raise OtaDecodeError(f"sound count byte is negative ({count})")
        self.sample_data = []
        for index in range(count):
            start = asset.uint16(1 + index * 2)
            if index == count - 1:
                end = len(asset.data)
            else:
                end = asset.uint16(1 + (index + 1) * 2)
            length = end - start
            if length < 0:
                raise OtaDecodeError(f"sound[{index}] negative slice {start}..{end}")
            sample = bytearray(length)
            for i in range(length):
                sample[i] = asset.data[start + i] & 0xFF
            self.sample_data.append(bytes(sample))

    def play_sound(self, index: int, enabled: bool = True, volume_percent: int = 100) -> None:
        if not enabled:
            return
        if index < 0 or index >= len(self.sample_data):
            raise OtaDecodeError(f"sound index {index} out of range")
        data = self.sample_data[index]
        if self._nokia_sound is None:
            self._nokia_sound = _NokiaSound()
            self._nokia_sound.init(data, self._sound_data_type)
        else:
            self._nokia_sound.init(data, self._sound_data_type)
        gain = int(255 * max(0, min(100, volume_percent)) / 100)
        self._nokia_sound.set_gain(gain)
        self._nokia_sound.play(1)
