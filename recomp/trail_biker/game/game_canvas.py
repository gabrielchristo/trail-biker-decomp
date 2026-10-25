from __future__ import annotations

import pygame

from trail_biker.assets.binary_asset import BinaryAsset
from trail_biker.config import DEVICE_PROFILE_ID
from trail_biker.config import LOGIC_TICK_MS
from trail_biker.config import RESOURCES
from trail_biker.config import SCALE
from trail_biker.config import default_screen_size
from trail_biker.game.game_clock import GameClock
from trail_biker.game.levels import LevelStore
from trail_biker.game.motorbike import Motorbike
from trail_biker.game.parallax_background import ParallaxBackground
from trail_biker.game.terrain_level import TerrainLevel
from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.sprite_sheet import SpriteSheet


class GameCanvas:
    def __init__(self, metadata: BinaryAsset, level_store: LevelStore, course_index: int = 0) -> None:
        self.game_metadata = metadata
        self.level_store = level_store
        self.selected_course_index = course_index
        self.game_mode = 0
        self.sound_enabled = True
        self.screen_width, self.screen_height = default_screen_size()
        self.logical = pygame.Surface((self.screen_width, self.screen_height))
        self.game_clock = GameClock(False, 0, True)
        self.splash_phase = 1
        self.splash_frame_counter = 0
        self.splash_sprites: SpriteSheet | None = None
        self.assets_loaded = False
        self.playfield_ready = False
        self.terrain: TerrainLevel | None = None
        self.parallax: ParallaxBackground | None = None
        self.motorbike: Motorbike | None = None
        self.empty_tile_color = 0xFFFFFF
        self.race_end_handled = False
        self.stunt_active = False
        self.ramp_landing_active = False
        self.wheelie_on_log = False
        self.terrain_scroll_period = 0
        self.parallax_scroll_period = 0
        self.terrain_scroll_accum = 0
        self.parallax_scroll_accum = 0
        self.terrain_scroll_threshold = 0
        self.parallax_scroll_threshold = 0
        self.trick_hold_ticks = 0
        self.trick_cooldown_max = 0
        self.air_trick_timer = 0
        self.stunt_air_delay_threshold = 0
        self.hop_impulse = 0
        self.trick_drift_per_tick = 0
        self.stunt_phase_ticks = 0
        self.ramp_stop_world_x = 0
        self._load_splash()
        self._logic_accum = 0.0

    def _load_splash(self) -> None:
        try:
            img = load_logical_image(RESOURCES / "splash.png")
            self.splash_sprites = SpriteSheet(img, 1, 2, False)
        except FileNotFoundError:
            self.splash_sprites = None

    def load_game_resources(self) -> None:
        d = self.game_metadata.data
        self.motorbike = Motorbike(int(d[9]), 0, self.game_metadata)
        if DEVICE_PROFILE_ID == 4:
            self.empty_tile_color = 0x00FF00
        else:
            self.empty_tile_color = 0xFFFFFF
        tiles = load_logical_image(RESOURCES / "tiles.png")
        tile_sprites = SpriteSheet(tiles, int(d[6]), int(d[7]), True)
        self.parallax = ParallaxBackground(RESOURCES / "bg.png", int(d[1]), False)
        n = d[72] * d[73] + 2
        n2 = 72
        slope_bytes = [d[n2 + i] for i in range(n)]
        n2 += n
        n3 = d[n2 + 1] + 2 * d[n2] + 2
        n4 = n3 + (d[n2 + n3] + 1)
        piece_bytes = [d[n2 + i] for i in range(n4)]
        self.terrain = TerrainLevel(
            0,
            int(d[4]),
            self.screen_width,
            int(d[3]),
            tile_sprites,
            int(d[5]),
            None,
            False,
            slope_bytes,
            piece_bytes,
            self.motorbike.sprite_width // 2,
            self.empty_tile_color,
        )
        self.assets_loaded = True

    def start_level(self) -> None:
        grid = self.level_store.build_level_grid(self.selected_course_index)
        if self.terrain is not None:
            self.terrain.load_level(grid)
            self.terrain.set_bike_world_x(int(self.game_metadata.data[8]))
        self.game_clock.reset(self.game_mode != 0, 0, True)
        self.reset_round_state()
        self.playfield_ready = True
        self.splash_phase = -1

    def reset_round_state(self) -> None:
        d = self.game_metadata.data
        self.race_end_handled = False
        self.stunt_active = False
        self.ramp_landing_active = False
        self.wheelie_on_log = False
        self.trick_hold_ticks = 0
        self.air_trick_timer = 0
        self.stunt_phase_ticks = 0
        if self.motorbike:
            self.motorbike.reset_bike()
        if self.terrain:
            self.terrain.scroll_by(1)
            self.motorbike.screen_y = d[4] + self.terrain.camera_ground_offset_y
        self.splash_frame_counter = 0
        self.terrain_scroll_period = d[65]
        self.parallax_scroll_period = d[66]
        self.stunt_air_delay_threshold = d[61]
        self.hop_impulse = d[11]
        self.trick_drift_per_tick = d[10]
        self.trick_cooldown_max = d[62]
        if DEVICE_PROFILE_ID != 0:
            self.terrain_scroll_period >>= 1
            self.parallax_scroll_period >>= 1
            self.stunt_air_delay_threshold >>= 2
            self.trick_cooldown_max >>= 2

    def update_bike_on_terrain(self) -> None:
        if not self.motorbike or not self.terrain:
            return
        d = self.game_metadata.data
        crash = 0
        n2 = self.motorbike.screen_y - d[4]
        a0 = self.motorbike.get_wheel_probe_offset(0)
        a1 = self.motorbike.get_wheel_probe_offset(1)
        n3 = abs(a0 - a1) // 2
        n4 = self.motorbike.sprite_width // 2
        t = self.terrain

        def norm(v: int) -> tuple[int, bool]:
            buried = (v & 0xFFFFFF80) != 0
            if buried:
                v = (v + 128) & 0xFF
                if v == 0:
                    v = 6
            return v, buried

        n5 = t.probe_terrain_at(t.bike_world_x + n4, n2 - a0, not self.motorbike.in_air_trick, False)
        if n5 == 0:
            n5 = t.probe_terrain_at(t.bike_world_x + n4, n2 - self.motorbike.sprite_height, False, False)
        n5, b = norm(n5)
        f = t.probe_terrain_at(t.bike_world_x, n2 - n3, not self.motorbike.in_air_trick, False)
        if f == 0:
            f = t.probe_terrain_at(t.bike_world_x, n2 - self.motorbike.sprite_height, False, False)
        f, b2 = norm(f)
        b3 = t.probe_terrain_at(t.bike_world_x - n4, n2 - a1, not self.motorbike.in_air_trick, False)
        if b3 == 0:
            b3 = t.probe_terrain_at(t.bike_world_x - n4, n2 - self.motorbike.sprite_height, False, False)
        b3, b4 = norm(b3)

        if n5 in (2, 5, 8, 6) or (n5 == 4 and b3 not in (0, 1, 5, 9)):
            crash = 1 if b else 0
        if crash == 0 and f in (5, 8, 6):
            crash = 1 if b2 else 0
        if crash == 0 and b3 == 8:
            crash = 1 if b4 else 0
        if crash:
            self.begin_stunt()
        else:
            if (
                not self.motorbike.in_air_trick
                and t.probe_terrain_at(t.bike_world_x + 2, n2, True, False) == 0
                and t.probe_terrain_at(t.bike_world_x, n2 - n3, True, True) != 4
            ):
                self.wheelie_on_log = True
            if not self.motorbike.in_air_trick and crash == 0 and (f == 9 or b3 == 9 or n5 == 9):
                self.motorbike.current_lean = self.motorbike.min_lean

    def begin_stunt(self) -> None:
        if self.stunt_active or self.ramp_landing_active or not self.motorbike:
            return
        if self.motorbike.bike_state == 5:
            return
        self.stunt_active = True
        self.motorbike.enter_crash()

    def on_logic_tick(self) -> None:
        if self.splash_phase != -1:
            if self.splash_sprites is None:
                self._finish_splash()
            elif self.splash_frame_counter > 30:
                if self.splash_phase == 1:
                    self.splash_phase = 0
                else:
                    self._finish_splash()
                self.splash_frame_counter = 0
            else:
                self.splash_frame_counter += 1
            return
        if not self.playfield_ready or not self.motorbike or not self.terrain or not self.parallax:
            return
        d = self.game_metadata.data
        self.wheelie_on_log = False
        self.game_clock.tick()
        self.motorbike.tick_animation()
        if self.stunt_active:
            if self.stunt_phase_ticks > d[63]:
                self.stunt_phase_ticks = 0
                self.stunt_active = False
                self.ramp_landing_active = True
                self.motorbike.reset_bike()
            else:
                self.stunt_phase_ticks += 1
            if not self.stunt_active and self.ramp_landing_active:
                self.ramp_stop_world_x = self.terrain.find_flat_ground_x(
                    self.terrain.bike_world_x - self.terrain.tile_size_px * 4, 1, self.terrain.tile_size_px * 4
                )
                self.ramp_stop_world_x += d[8]
        else:
            if not self.stunt_active and not self.race_end_handled:
                if self.ramp_landing_active:
                    self.terrain_scroll_threshold = self.terrain_scroll_period - 2 * d[71] // self.terrain_scroll_period
                    self.parallax_scroll_threshold = self.parallax_scroll_period - 2 * d[71] // self.parallax_scroll_period
                else:
                    self.terrain_scroll_threshold = self.terrain_scroll_period - self.motorbike.current_lean // (
                        5 * self.terrain_scroll_period
                    )
                    self.parallax_scroll_threshold = self.parallax_scroll_period - self.motorbike.current_lean // (
                        5 * self.parallax_scroll_period
                    )
                if self.terrain_scroll_accum > self.terrain_scroll_threshold:
                    delta = self.terrain_scroll_accum - self.terrain_scroll_threshold
                    self.terrain.scroll_by(-delta if self.ramp_landing_active else delta)
                    self.terrain_scroll_accum = 0
                self.terrain_scroll_accum += 1
                if self.parallax_scroll_accum > self.parallax_scroll_threshold:
                    delta = self.parallax_scroll_accum - self.parallax_scroll_threshold
                    self.parallax.scroll_by(-delta if self.ramp_landing_active else delta)
                    self.parallax_scroll_accum = 0
                self.parallax_scroll_accum += 1
            if self.ramp_landing_active and self.terrain.bike_world_x <= self.ramp_stop_world_x:
                self.ramp_landing_active = False
                self.motorbike.screen_y = d[4] + self.terrain.camera_ground_offset_y
                self.motorbike.in_air_trick = False
            if self.terrain.composed_tile_id != 10 and self.game_clock.display_time_ms < 599990:
                if not self.stunt_active and not self.ramp_landing_active:
                    self.update_bike_on_terrain()
                if self.wheelie_on_log and not self.motorbike.in_air_trick:
                    self.motorbike.advance_air_trick(0, False)
                if self.motorbike.in_air_trick:
                    if self.trick_hold_ticks > self.trick_cooldown_max:
                        if self.motorbike.trick_frame_index < 20:
                            self.motorbike.trick_frame_index += 1
                        self.trick_hold_ticks = 0
                    if self.air_trick_timer > self.stunt_air_delay_threshold:
                        self.motorbike.trick_drift_x -= self.trick_drift_per_tick
                        self.motorbike.screen_y -= self.motorbike.trick_drift_x
                        ground = d[4] + self.terrain.camera_ground_offset_y
                        if self.motorbike.screen_y >= ground:
                            self.motorbike.screen_y = ground
                            self.motorbike.in_air_trick = False
                        self.air_trick_timer = 0
                    self.air_trick_timer += 1
                    self.trick_hold_ticks += 1
                else:
                    self.motorbike.screen_y = d[4] + self.terrain.camera_ground_offset_y

    def _finish_splash(self) -> None:
        self.splash_phase = -1
        if not self.assets_loaded:
            self.load_game_resources()
            self.reset_round_state()
        self.start_level()

    def handle_event(self, event: pygame.event.Event) -> None:
        if event.type != pygame.KEYDOWN or not self.playfield_ready or not self.motorbike:
            return
        if self.stunt_active or self.ramp_landing_active:
            return
        if event.key in (pygame.K_ESCAPE, pygame.K_p):
            self.game_clock.set_paused(not self.game_clock.paused)
            return
        if event.key in (pygame.K_5, pygame.K_UP):
            if not self.motorbike.in_air_trick:
                self.motorbike.lean_back()
        elif event.key in (pygame.K_2, pygame.K_DOWN):
            if not self.motorbike.in_air_trick:
                self.motorbike.lean_forward()
        elif event.key in (pygame.K_1, pygame.K_3, pygame.K_LEFT, pygame.K_RIGHT):
            self.motorbike.advance_air_trick(self.hop_impulse, True)
            self.trick_hold_ticks = 0

    def handle_key_up(self) -> None:
        if self.playfield_ready and self.motorbike:
            self.motorbike.set_neutral()

    def update(self, dt_ms: float) -> None:
        self._logic_accum += dt_ms
        while self._logic_accum >= LOGIC_TICK_MS:
            self._logic_accum -= LOGIC_TICK_MS
            self.on_logic_tick()

    def draw(self, target: pygame.Surface) -> None:
        self.logical.fill((0, 0, 0))
        if self.splash_phase != -1 and self.splash_sprites:
            y = self.screen_height // 2 - self.splash_sprites.frame_height // 2
            self.splash_sprites.draw_frame(self.splash_phase, self.logical, 0, y)
        elif self.playfield_ready and self.terrain and self.parallax and self.motorbike:
            d = self.game_metadata.data
            self.logical.fill((255, 255, 255))
            self.logical.fill((255, 255, 255), (int(d[9]), 0, self.motorbike.sprite_width, int(d[4])))
            self.parallax.paint(self.logical, 0, 0)
            self.terrain.paint(self.logical)
            if not self.ramp_landing_active:
                self.motorbike.paint(self.logical)
            self.game_clock.paint(self.logical, 0, 0)
        scaled = pygame.transform.scale(self.logical, (self.screen_width * SCALE, self.screen_height * SCALE))
        target.blit(scaled, (0, 0))
