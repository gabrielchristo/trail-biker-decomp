"""Port of a.a.a.d.a: play indexed effects (recomp uses pre-rendered WAV in resources/)."""

from __future__ import annotations

from pathlib import Path

import pygame

from trail_biker.assets.game_sounds import GameSounds
from trail_biker.config import RESOURCES
from trail_biker.config import SOUND_PATH


class SoundPlaybackError(ValueError):
    pass


class _SoundPlayer:
    """One pygame channel, swap clip like com.nokia.mid.sound.Sound.init + play."""

    def __init__(self) -> None:
        self._gain = 255
        self._clip: pygame.mixer.Sound | None = None
        self._channel: pygame.mixer.Channel | None = None

    def use_clip(self, clip: pygame.mixer.Sound) -> None:
        self._clip = clip
        self._clip.set_volume(self._gain / 255.0)

    def set_gain(self, gain: int) -> None:
        self._gain = 0 if gain < 0 else 255 if gain > 255 else gain
        if self._clip is not None:
            self._clip.set_volume(self._gain / 255.0)

    def play(self, loop: int) -> None:
        if self._clip is None or loop < 0:
            raise SoundPlaybackError(f"Sound.play loop={loop}")
        if self._channel is None:
            self._channel = pygame.mixer.find_channel(True)
        if self._channel is None:
            self._clip.play(loops=-1 if loop == 0 else loop - 1)
            return
        self._channel.stop()
        self._channel.play(self._clip, loops=-1 if loop == 0 else loop - 1)


class SoundManager:
    def __init__(self, resource: str = "/res/sound.dat") -> None:
        self._resource_path = resource
        self._wav_paths: list[Path] = []
        self._clips: list[pygame.mixer.Sound | None] = []
        self._player: _SoundPlayer | None = None
        self._resolve_wav_paths()

    def _resolve_wav_paths(self) -> None:
        _ = self._resource_path
        self._wav_paths = GameSounds.load_wav_paths(SOUND_PATH, RESOURCES)
        self._clips = [None] * len(self._wav_paths)

    def preload_clips(self) -> None:
        """Optional warm-up after display/assets are ready (same timing as old OTA init)."""
        for index, path in enumerate(self._wav_paths):
            if self._clips[index] is None:
                self._clips[index] = pygame.mixer.Sound(str(path))

    def _clip_for_index(self, index: int) -> pygame.mixer.Sound:
        if index < 0 or index >= len(self._wav_paths):
            raise SoundPlaybackError(f"sound index {index} out of range")
        clip = self._clips[index]
        if clip is None:
            clip = pygame.mixer.Sound(str(self._wav_paths[index]))
            self._clips[index] = clip
        return clip

    def play_sound(self, index: int, enabled: bool = True, volume_percent: int = 100) -> None:
        if not enabled:
            return
        if self._player is None:
            self._player = _SoundPlayer()
        self._player.use_clip(self._clip_for_index(index))
        gain = int(255 * max(0, min(100, volume_percent)) / 100)
        self._player.set_gain(gain)
        self._player.play(1)
