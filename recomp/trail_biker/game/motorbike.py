import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.config import DEVICE_PROFILE_ID
from trail_biker.config import RESOURCES
from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.sprite_sheet import SpriteSheet

BIKE_CRASH_FRAME = 21


class Motorbike:
    def __init__(self, screen_x: int, screen_y: int, metadata: GameMetadata) -> None:
        self.screen_x = screen_x
        self.screen_y = screen_y
        self.game_metadata = metadata
        bikes = load_logical_image(RESOURCES / "bikes.png")
        self.bike_sprites = SpriteSheet(
            bikes,
            int(metadata.data[16]),
            int(metadata.data[17]),
            True,
        )
        self.min_lean = metadata.data[12]
        self.max_lean = metadata.data[13]
        if DEVICE_PROFILE_ID != 0:
            self.min_lean -= 1
            self.max_lean -= 1
        self.sprite_width = self.bike_sprites.frame_width
        self.sprite_height = self.bike_sprites.frame_height
        self.bike_state = 0
        self.animation_tick = 0
        self.is_active = True
        self.pose_frame_index = 0
        self.pose_variant = 0
        self.current_lean = 0
        self.pitch_offset = 0
        self.trick_drift_x = 0
        self.in_air_trick = False
        self.trick_frame_index = 0
        self.wheel_probe_table: list[list[int]] = []
        self._load_wheel_probe_table()
        self.reset_bike()

    def _load_wheel_probe_table(self) -> None:
        n = 21
        self.wheel_probe_table = [[0] * n, [0] * n]
        for i in range(n):
            self.wheel_probe_table[0][i] = self.game_metadata.data[19 + i]
            self.wheel_probe_table[1][i] = self.game_metadata.data[40 + i]

    def lean_forward(self) -> None:
        if self.bike_state != 5:
            if self.current_lean < self.max_lean:
                self.current_lean += 10
            self.bike_state = 1
            self.animation_tick = 0

    def set_neutral(self) -> None:
        if self.bike_state != 5:
            self.bike_state = 0
            self.animation_tick = 0

    def lean_back(self) -> None:
        if self.bike_state != 5:
            if self.current_lean > self.min_lean:
                self.current_lean -= 10
            self.bike_state = 2
            self.animation_tick = 0

    def enter_crash(self) -> None:
        self.bike_state = 5
        self.tick_animation()
        self.animation_tick = 0

    def tick_animation(self) -> None:
        self.animation_tick += 1
        d = self.game_metadata.data
        if self.bike_state == 0:
            if self.animation_tick > d[68]:
                if self.current_lean > self.min_lean:
                    self.current_lean -= 1
                self.animation_tick = 0
        elif self.bike_state == 1:
            if self.animation_tick > d[67]:
                if self.current_lean < self.max_lean:
                    self.current_lean += 1
                self.animation_tick = 0
        elif self.bike_state == 2:
            if self.animation_tick > d[70]:
                if self.current_lean > self.min_lean:
                    self.current_lean -= 1
                self.animation_tick = 0
        elif self.bike_state == 5:
            self.pose_frame_index = BIKE_CRASH_FRAME
            self.pose_variant = 0
            self.current_lean = 0

    def current_frame(self) -> int:
        if self.bike_state == 5:
            return BIKE_CRASH_FRAME
        if self.in_air_trick:
            return self.trick_frame_index
        return self.pose_frame_index + 3

    def paint(self, target: pygame.Surface) -> None:
        self.bike_sprites.draw_frame(
            self.current_frame(),
            target,
            self.screen_x,
            self.screen_y - self.sprite_height,
        )

    def advance_air_trick(self, hop_impulse: int, reset_trick_frame: bool) -> None:
        if not self.in_air_trick:
            self.trick_drift_x = hop_impulse
            self.in_air_trick = True
            if reset_trick_frame:
                self.trick_frame_index = 0
            else:
                self.trick_frame_index = self.pose_frame_index + self.pose_variant
        elif self.trick_frame_index > 0:
            self.trick_frame_index -= 1

    def reset_bike(self) -> None:
        self.current_lean = self.min_lean
        self.pitch_offset = 0
        self.bike_state = 0
        self.is_active = True
        self.animation_tick = 0
        self.pose_frame_index = 7
        self.pose_variant = 3
        self.trick_drift_x = 0
        self.in_air_trick = False

    def get_wheel_probe_offset(self, wheel: int) -> int:
        if self.in_air_trick:
            return self.wheel_probe_table[wheel][self.trick_frame_index]
        return self.wheel_probe_table[wheel][self.pose_frame_index + self.pose_variant]
