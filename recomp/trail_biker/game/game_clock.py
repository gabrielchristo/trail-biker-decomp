import time

import pygame

from trail_biker.config import RESOURCES
from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.sprite_sheet import SpriteSheet

SPRITE_COLON = 10
MAX_TIME_MS = 599990
DIGIT_SPACING = 3


class GameClock:
    def __init__(self, countdown_mode: bool, base_ms: int, show_centiseconds: bool) -> None:
        numbers = load_logical_image(RESOURCES / "numbers.png")
        self.digit_sprites = SpriteSheet(numbers, 1, 11, True)
        self.countdown_mode = countdown_mode
        self.show_centiseconds = show_centiseconds
        self.base_time_ms = base_ms
        self.display_time_ms = base_ms
        self.wall_clock_start_ms = time.monotonic() * 1000
        self.pause_started_ms = 0.0
        self.paused = False
        self.reset(countdown_mode, base_ms, show_centiseconds)

    def tick(self) -> None:
        if self.paused:
            return
        elapsed = time.monotonic() * 1000 - self.wall_clock_start_ms
        if self.countdown_mode and self.display_time_ms > 0:
            self.display_time_ms = self.base_time_ms - elapsed
        else:
            self.display_time_ms = self.base_time_ms + elapsed
            if self.display_time_ms > MAX_TIME_MS:
                self.display_time_ms = MAX_TIME_MS
        if self.display_time_ms < 0:
            self.display_time_ms = 0

    def reset(self, countdown_mode: bool, base_ms: int, show_centiseconds: bool) -> None:
        self.base_time_ms = base_ms
        self.display_time_ms = base_ms
        self.wall_clock_start_ms = time.monotonic() * 1000
        self.countdown_mode = countdown_mode
        self.show_centiseconds = show_centiseconds
        self.paused = False

    def set_paused(self, paused: bool) -> None:
        if paused:
            if not self.paused:
                self.pause_started_ms = time.monotonic() * 1000
        elif self.paused:
            self.wall_clock_start_ms += time.monotonic() * 1000 - self.pause_started_ms
        self.paused = paused

    def paint(self, target: pygame.Surface, x: int, y: int) -> None:
        seconds = int(self.display_time_ms / 1000) % 60
        minutes = int(self.display_time_ms / 1000) // 60
        self.digit_sprites.draw_frame(minutes, target, x, y)
        self.digit_sprites.draw_frame(SPRITE_COLON, target, x + DIGIT_SPACING, y)
        self.digit_sprites.draw_frame(seconds // 10, target, x + DIGIT_SPACING * 2, y)
        self.digit_sprites.draw_frame(seconds % 10, target, x + DIGIT_SPACING * 3, y)
        if self.show_centiseconds:
            cs = int(self.display_time_ms / 10) % 100
            self.digit_sprites.draw_frame(SPRITE_COLON, target, x + DIGIT_SPACING * 4, y)
            self.digit_sprites.draw_frame(cs // 10, target, x + DIGIT_SPACING * 5, y)
            self.digit_sprites.draw_frame(cs % 10, target, x + DIGIT_SPACING * 6, y)

    def get_paint_width(self) -> int:
        w = self.digit_sprites.frame_width
        width = 4 * (w + DIGIT_SPACING)
        if self.show_centiseconds:
            width += 3 * (w + DIGIT_SPACING)
        return width

    def get_paint_height(self) -> int:
        return self.digit_sprites.frame_height
