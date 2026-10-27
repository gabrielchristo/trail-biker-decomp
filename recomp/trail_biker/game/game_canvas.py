from __future__ import annotations

from dataclasses import dataclass
from typing import TYPE_CHECKING

import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.config import DEVICE_PROFILE_ID
from trail_biker.config import LOGIC_TICK_MS
from trail_biker.config import VISUAL_INTERPOLATION
from trail_biker.config import RESOURCES
from trail_biker.config import SCALE
from trail_biker.config import default_screen_size
from trail_biker.game.game_clock import GameClock
from trail_biker.game.levels import LevelStore
from trail_biker.game.motorbike import Motorbike
from trail_biker.game.parallax_background import ParallaxBackground
from trail_biker.game.terrain_level import TerrainLevel
from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.present import blit_logical
from trail_biker.gfx.sprite_sheet import SpriteSheet
from trail_biker import ui_strings as ui

if TYPE_CHECKING:
    from trail_biker.game.level_manager import LevelManager


def _ub(value: int) -> int:
    return value & 0xFF


def _lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


@dataclass
class _Present:
    world_x: float
    bike_y: float
    parallax: float


class GameCanvas:
    def __init__(
        self,
        metadata: GameMetadata,
        level_store: LevelStore,
        level_manager: LevelManager | None = None,
        course_index: int = 0,
    ) -> None:
        self.game_metadata = metadata
        self.level_store = level_store
        self.level_manager = level_manager
        self.selected_course_index = course_index
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
        self.lives_image: pygame.Surface | None = None
        self.sound_manager = None
        self.empty_tile_color = 0xFFFFFF
        self.end_game_flags = 0
        self.end_game_anim_timer = 0
        self.race_end_handled = False
        self.stunt_active = False
        self.ramp_landing_active = False
        self.wheelie_on_log = False
        self.crash_anim_active = False
        self.crash_anim_ticks = 0
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
        self.stunt_lives_lost = 0
        self.stunt_lives_total = 0
        self.race_finish_dispatched = False
        self.needs_background_clear = True
        self._banner_font: pygame.font.Font | None = None
        self._hud = pygame.Surface((self.screen_width, self.screen_height), pygame.SRCALPHA)
        self._load_splash()
        self._logic_accum = 0.0
        self._present_prev = _Present(0.0, 0.0, 0.0)
        self._present_curr = _Present(0.0, 0.0, 0.0)

    @property
    def game_mode(self) -> int:
        if self.level_manager:
            return self.level_manager.game_mode
        return 0

    def _load_splash(self) -> None:
        try:
            img = load_logical_image(RESOURCES / "splash.png")
            self.splash_sprites = SpriteSheet(img, 1, 2, False)
        except FileNotFoundError:
            self.splash_sprites = None

    def load_game_resources(self) -> None:
        from trail_biker.game.sound_manager import SoundManager

        d = self.game_metadata.data
        self.motorbike = Motorbike(int(d[9]), 0, self.game_metadata)
        if DEVICE_PROFILE_ID == 4:
            self.empty_tile_color = 0x00FF00
        else:
            self.empty_tile_color = 0xFFFFFF
        tiles = load_logical_image(RESOURCES / "tiles.png")
        tile_sprites = SpriteSheet(tiles, int(d[6]), int(d[7]), True)
        self.parallax = ParallaxBackground(RESOURCES / "bg.png", int(d[1]), False)
        try:
            self.lives_image = load_logical_image(RESOURCES / "lives.png")
            self.lives_image.set_colorkey((255, 255, 255))
        except FileNotFoundError:
            self.lives_image = None
        n = _ub(d[72]) * _ub(d[73]) + 2
        n2 = 72
        slope_bytes = [_ub(d[n2 + i]) for i in range(n)]
        n2 += n
        n3 = _ub(d[n2 + 1]) + 2 * _ub(d[n2]) + 2
        n4 = n3 + (_ub(d[n2 + n3]) + 1)
        piece_bytes = [_ub(d[n2 + i]) for i in range(n4)]
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
        if self.level_manager:
            self.sound_manager = self.level_manager.sound_manager
        else:
            self.sound_manager = SoundManager()
        self.sound_manager.preload_clips()
        self.assets_loaded = True

    def configure_game_clock(self) -> None:
        timed = self.game_mode != 0
        target = self.level_manager.get_current_target_time_ms() if timed and self.level_manager else 0
        self.game_clock.reset(timed, target, True)

    def start_level(self) -> None:
        grid = self.level_store.build_level_grid(self.selected_course_index)
        if self.level_manager:
            self.selected_course_index = self.level_manager.selected_course_index
            grid = self.level_store.build_level_grid(self.selected_course_index)
        if self.terrain is not None:
            self.terrain.load_level(grid)
            self.terrain.set_bike_world_x(int(self.game_metadata.data[8]))
        if self.level_manager:
            self.configure_game_clock()
        else:
            self.game_clock.reset(self.game_mode != 0, 0, True)
        self.needs_background_clear = True
        self.reset_round_state()
        self.crash_anim_active = True
        self.crash_anim_ticks = 0
        self.game_clock.set_paused(True)
        self.playfield_ready = True
        self.splash_phase = -1
        self.race_end_handled = False
        self.race_finish_dispatched = False

    def reset_round_state(self) -> None:
        d = self.game_metadata.data
        self.race_end_handled = False
        self.stunt_active = False
        self.ramp_landing_active = False
        self.wheelie_on_log = False
        self.trick_hold_ticks = 0
        self.air_trick_timer = 0
        self.stunt_phase_ticks = 0
        self.end_game_flags = 0
        self.end_game_anim_timer = 0
        self.crash_anim_ticks = 0
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
        # a.java null(): "Course N" overlay at round start (flag 16).
        self.add_end_game_flag(16)
        self._sync_present()

    def _capture_present(self) -> _Present:
        if not self.motorbike or not self.terrain or not self.parallax:
            return _Present(0.0, 0.0, 0.0)
        return _Present(
            float(self.terrain.bike_world_x),
            float(self.motorbike.screen_y),
            float(self.parallax.scroll_unwrapped_px),
        )

    def _sync_present(self) -> None:
        snap = self._capture_present()
        self._present_prev = snap
        self._present_curr = snap

    def _present_t(self) -> float:
        if LOGIC_TICK_MS <= 0:
            return 1.0
        return max(0.0, min(1.0, self._logic_accum / LOGIC_TICK_MS))

    def _use_presentation(self) -> bool:
        return (
            VISUAL_INTERPOLATION
            and self.splash_phase == -1
            and self.playfield_ready
            and self.motorbike is not None
            and self.terrain is not None
            and self.parallax is not None
            and not self.crash_anim_active
            and not self.end_game_flags
        )

    def reset_stunt_lives(self) -> None:
        self.stunt_lives_lost = 0
        self.stunt_lives_total = self.game_metadata.data[14]

    def tick_stunt_cup_life(self) -> None:
        d = self.game_metadata.data
        if self.stunt_lives_total - self.stunt_lives_lost < d[15]:
            self.stunt_lives_lost -= 1

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

        def norm_front(v: int) -> tuple[int, bool]:
            buried = (v & 0xFFFFFF80) != 0
            if buried:
                v = (v + 128) & 0xFF
                if v == 0:
                    v = 6
            return v, buried

        def norm_wheel(v: int) -> tuple[int, bool]:
            buried = (v & 0xFFFFFF80) != 0
            if buried:
                v = (v + 128) & 0xFF
            return v, buried

        n5 = t.probe_terrain_at(t.bike_world_x + n4, n2 - a0, not self.motorbike.in_air_trick, False)
        if n5 == 0:
            n5 = t.probe_terrain_at(t.bike_world_x + n4, n2 - self.motorbike.sprite_height, False, False)
        n5, b = norm_front(n5)
        f = t.probe_terrain_at(t.bike_world_x, n2 - n3, not self.motorbike.in_air_trick, False)
        if f == 0:
            f = t.probe_terrain_at(t.bike_world_x, n2 - self.motorbike.sprite_height, False, False)
        f, b2 = norm_wheel(f)
        b3 = t.probe_terrain_at(t.bike_world_x - n4, n2 - a1, not self.motorbike.in_air_trick, False)
        if b3 == 0:
            b3 = t.probe_terrain_at(t.bike_world_x - n4, n2 - self.motorbike.sprite_height, False, False)
        b3, b4 = norm_wheel(b3)

        skip_front_crash = (
            n5 != 2
            and n5 != 5
            and n5 != 8
            and n5 != 6
            and (n5 != 4 or b3 != 0)
            and (n5 != 4 or b3 != 1)
            and (n5 != 4 or b3 != 5)
            and (n5 != 4 or b3 != 9)
        )
        if skip_front_crash:
            if n5 == 11 and self.motorbike.in_air_trick:
                pass
        else:
            crash = 1 if b else 0
        if crash == 0:
            if f != 5 and f != 8 and f != 6:
                if f == 11 and self.motorbike.in_air_trick:
                    pass
            else:
                crash = 1 if b2 else 0
        if crash == 0:
            if b3 != 8:
                if b3 == 11 and self.motorbike.in_air_trick:
                    pass
            else:
                crash = 1 if b4 else 0
        if crash:
            self.begin_stunt()
            self._play_sound(1)
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
        self.stunt_lives_lost += 1

    def _play_sound(self, index: int) -> None:
        if not self.sound_manager:
            return
        enabled = self.sound_enabled
        vol = 100
        if self.level_manager:
            enabled = self.level_manager.progress.sound_enabled
            vol = self.level_manager.progress.volume_percent
        self.sound_manager.play_sound(index, enabled, vol)

    def add_end_game_flag(self, flag: int) -> None:
        if (self.end_game_flags & flag) == 0:
            self.end_game_flags += flag
            self.end_game_anim_timer = 0

    def get_active_end_game_flag(self) -> int:
        if self.end_game_flags & 0x10:
            return 16
        if self.end_game_flags & 0x4:
            return 4
        if self.end_game_flags & 0x8:
            return 8
        if self.end_game_flags & 0x2:
            return 2
        if self.end_game_flags & 0x1:
            return 1
        return 0

    def tick_end_game_sequence(self) -> None:
        if self.end_game_flags:
            self.end_game_flags -= self.get_active_end_game_flag()
            self.end_game_anim_timer = 0

    def _end_game_message(self) -> str | None:
        flag = self.get_active_end_game_flag()
        if flag == 4:
            return ui.MSG_OUT_OF_TIME
        if flag == 2:
            return ui.MSG_NEW_BEST_TIME
        if flag == 1:
            return ui.MSG_GAME_OVER
        if flag == 8:
            return ui.MSG_COURSE_COMPLETE
        if flag == 16 and self.level_manager:
            return f"Course {self.level_manager.selected_course_index + 1}"
        return None

    def _handle_race_end(self) -> None:
        if self.race_end_handled:
            return
        self.race_end_handled = True
        self.game_clock.set_paused(True)
        lm = self.level_manager
        if lm and lm.game_mode == 2 and self.stunt_lives_lost >= self.stunt_lives_total:
            self.add_end_game_flag(1)
            lm.pause_overlay_active = True
        elif lm and lm.game_mode != 0:
            if self.game_clock.display_time_ms == 0:
                self.add_end_game_flag(4)
            else:
                best = lm.get_best_time_ms(lm.selected_course_index)
                par = lm.get_par_time_from_dat(lm.selected_course_index)
                remaining = self.game_clock.display_time_ms
                # a.java: mode 1 → countdown left; mode 2 → par - remaining < best (elapsed beats save)
                if (lm.game_mode == 1 and remaining > 0) or (
                    lm.game_mode == 2 and par - remaining < best
                ):
                    self.add_end_game_flag(2)
                else:
                    self.add_end_game_flag(8)
                if lm.game_mode == 2 and lm.selected_course_index == 8:
                    self._play_sound(3)
                else:
                    self._play_sound(2)
        else:
            if self.game_clock.display_time_ms >= 599990:
                self.add_end_game_flag(4)
            else:
                target = lm.get_current_target_time_ms() if lm else 0
                if self.game_clock.display_time_ms < target:
                    self.add_end_game_flag(2)
                self.add_end_game_flag(8)
                self._play_sound(2)

    def _finish_race_callback(self) -> None:
        if self.end_game_flags != 0 or not self.level_manager or self.race_finish_dispatched:
            return
        if not self.race_end_handled:
            return
        int3 = self.game_clock.display_time_ms
        lm = self.level_manager
        if lm.game_mode == 2 and (
            self.stunt_lives_lost >= self.stunt_lives_total or self.game_clock.display_time_ms == 0
        ):
            int3 = -1
        elif lm.game_mode != 0:
            int3 = lm.get_current_target_time_ms() - int3
        self.race_finish_dispatched = True
        lm.on_race_finished(int3)

    def _should_trigger_race_end(self, can_play: bool) -> bool:
        if self.stunt_active or self.ramp_landing_active:
            return False
        if self.terrain.composed_tile_id == 10:
            return True
        if not can_play:
            return True
        if self.game_mode == 0 and self.game_clock.display_time_ms >= 599990:
            return True
        return False

    def _run_playfield_tick(self) -> None:
        if not self.motorbike or not self.terrain or not self.parallax:
            return
        d = self.game_metadata.data
        self.wheelie_on_log = False
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
            return
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
        can_play = (
            self.terrain.composed_tile_id != 10
            and (self.game_mode == 0 or self.game_clock.display_time_ms > 0)
            and (self.game_mode != 2 or self.stunt_lives_lost < self.stunt_lives_total)
        )
        play_frame_active = False
        if self.ramp_landing_active:
            play_frame_active = True
        elif can_play and not self.race_end_handled and self.game_clock.display_time_ms < 599990:
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
                        n3 = 7
                        if self.terrain.composed_tile_id == 3:
                            n3 = 0
                        elif self.terrain.composed_tile_id == 4:
                            n3 = 16
                        threshold = n3 + 4
                        if (
                            self.motorbike.trick_frame_index > threshold
                            and not self.wheelie_on_log
                            and self.terrain.composed_tile_id != 9
                        ):
                            self.begin_stunt()
                            self._play_sound(1)
                        else:
                            if self.terrain.composed_tile_id == 3:
                                self.motorbike.pose_frame_index = 0
                            elif self.terrain.composed_tile_id == 4:
                                self.motorbike.pose_frame_index = 16
                            else:
                                self.motorbike.pose_frame_index = 7
                    self.air_trick_timer = 0
                self.air_trick_timer += 1
                self.trick_hold_ticks += 1
                play_frame_active = True
            else:
                self.motorbike.screen_y = d[4] + self.terrain.camera_ground_offset_y
                cid = self.terrain.composed_tile_id
                if cid in (1, 5, 6, 7, 8, 9):
                    self.motorbike.pose_frame_index = 7
                    play_frame_active = True
                elif cid == 3:
                    self.motorbike.pose_frame_index = 0
                    play_frame_active = True
                elif cid == 4:
                    self.motorbike.pose_frame_index = 16
                    play_frame_active = True
                elif self.terrain.surface_tile_id != 0:
                    self.motorbike.pose_frame_index = 7
                    play_frame_active = True
        if self._should_trigger_race_end(can_play) and not self.race_end_handled:
            self._handle_race_end()
        self._finish_race_callback()

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
        if self.end_game_flags:
            self.game_clock.set_paused(True)
            if self.end_game_anim_timer > 20:
                self.tick_end_game_sequence()
                if self.end_game_flags == 0 and not self.race_end_handled:
                    self.game_clock.set_paused(False)
                self._finish_race_callback()
            else:
                self.end_game_anim_timer += 1
            # a.java: intro crash (R) still runs while course banner (g) ticks down.
            if not self.crash_anim_active:
                return
        if self.crash_anim_active:
            intro_ticks = int(self.game_metadata.data[64])
            if self.crash_anim_ticks > intro_ticks:
                self.crash_anim_active = False
                self.game_clock.set_paused(False)
            else:
                self.crash_anim_ticks += 1
            return
        self.game_clock.tick()
        self.motorbike.tick_animation()
        self._run_playfield_tick()

    def _finish_splash(self) -> None:
        self.splash_phase = -1
        if not self.assets_loaded:
            self.load_game_resources()
            self.reset_stunt_lives()
            self.reset_round_state()
        self.start_level()

    def dismiss_splash_or_open_pause(self) -> None:
        if self.splash_phase != -1:
            self.splash_phase = -1
            if not self.assets_loaded:
                self.load_game_resources()
                self.reset_stunt_lives()
                self.reset_round_state()
            self.start_level()
        elif self.level_manager:
            self.level_manager.pause_from_game()

    def handle_event(self, event: pygame.event.Event) -> None:
        if event.type != pygame.KEYDOWN:
            return
        if self.splash_phase != -1:
            if event.key in (pygame.K_RETURN, pygame.K_SPACE, pygame.K_0):
                self.dismiss_splash_or_open_pause()
            return
        if not self.playfield_ready or not self.motorbike:
            return
        if event.key in (pygame.K_RETURN, pygame.K_SPACE, pygame.K_0):
            self.dismiss_splash_or_open_pause()
            return
        if self.stunt_active or self.ramp_landing_active:
            return
        if event.key in (pygame.K_ESCAPE, pygame.K_p):
            if self.level_manager:
                self.level_manager.pause_from_game()
            else:
                self.game_clock.set_paused(not self.game_clock.paused)
            return
        if self.game_clock.paused:
            return
        if event.key in (pygame.K_5, pygame.K_UP):
            if not self.motorbike.in_air_trick:
                self.motorbike.lean_back()
        elif event.key in (pygame.K_2, pygame.K_DOWN):
            if self.motorbike.in_air_trick:
                return
            if self.terrain and self.terrain.composed_tile_id == 5:
                self.begin_stunt()
                self._play_sound(0)
            else:
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
            if VISUAL_INTERPOLATION:
                self._present_prev = self._capture_present()
            self.on_logic_tick()
            if VISUAL_INTERPOLATION:
                self._present_curr = self._capture_present()
            self._logic_accum -= LOGIC_TICK_MS

    def _in_game_overlay_center_logical(self) -> tuple[int, int]:
        """Center on the live playfield (parallax + terrain), not the full 208px logical canvas."""
        d = self.game_metadata.data
        terrain_bottom = int(d[4]) + int(d[3])
        visible_bottom = max(terrain_bottom, int(d[2]))
        return self.screen_width // 2, visible_bottom // 2

    def draw_centered_banner(self, text: str, surface: pygame.Surface | None = None) -> None:
        """MIDlet a.java do() + a(Graphics,...): small box on top of live gameplay."""
        dest = self.logical if surface is None else surface
        on_logical = dest.get_width() == self.screen_width and dest.get_height() == self.screen_height
        scale = 1 if on_logical else max(1, dest.get_width() // self.screen_width)
        pad = 4 * scale
        font_px = max(8, 8 * scale)
        font = pygame.font.SysFont(None, font_px)
        text_surf = font.render(text, True, (0, 0, 0))
        box_w = text_surf.get_width() + pad * 2
        box_h = text_surf.get_height() + pad * 2
        cx, cy = self._in_game_overlay_center_logical()
        if on_logical:
            x0 = cx - box_w // 2
            y0 = cy - box_h // 2
        else:
            x0 = cx * scale - box_w // 2
            y0 = cy * scale - box_h // 2
        radius = min(4 * scale, box_w // 4, box_h // 4)
        pygame.draw.rect(dest, (255, 255, 255), (x0, y0, box_w, box_h), border_radius=radius)
        pygame.draw.rect(
            dest,
            (0, 0, 0),
            (x0, y0, box_w, box_h),
            max(1, scale),
            border_radius=radius,
        )
        dest.blit(text_surf, (x0 + pad, y0 + pad))

    def draw_stunt_lives(self, surface: pygame.Surface | None = None) -> None:
        dest = self.logical if surface is None else surface
        if self.game_mode != 2 or not self.lives_image:
            return
        n = self.stunt_lives_total - self.stunt_lives_lost - 1
        n2 = self.lives_image.get_width() + 2
        x = self.screen_width - n * n2
        for _ in range(n):
            dest.blit(self.lives_image, (x, 0))
            x += n2

    def _draw_presented(self, target: pygame.Surface) -> None:
        assert self.motorbike is not None and self.terrain is not None and self.parallax is not None
        t = self._present_t()
        prev = self._present_prev
        curr = self._present_curr
        world_x = _lerp(prev.world_x, curr.world_x, t)
        bike_y = _lerp(prev.bike_y, curr.bike_y, t)
        parallax = _lerp(prev.parallax, curr.parallax, t)
        target.fill((255, 255, 255))
        self.parallax.paint_unwrapped(target, 0, 0, parallax, self.screen_width)
        self.terrain.paint_camera(target, world_x)
        if not self.ramp_landing_active:
            frame = self.motorbike.bike_sprites.frame_surface(self.motorbike.current_frame())
            blit_logical(target, frame, self.motorbike.screen_x, bike_y - self.motorbike.sprite_height)
        self._hud.fill((0, 0, 0, 0))
        self.game_clock.paint(self._hud, 0, 0)
        self.draw_stunt_lives(self._hud)
        hud = pygame.transform.scale(self._hud, (self.screen_width * SCALE, self.screen_height * SCALE))
        target.blit(hud, (0, 0))
        self._draw_end_game_overlay(target)
        self.needs_background_clear = False

    def _draw_end_game_overlay(self, surface: pygame.Surface) -> None:
        msg = self._end_game_message()
        if msg and self.end_game_flags:
            self.draw_centered_banner(msg, surface)

    def draw(self, target: pygame.Surface) -> None:
        if self._use_presentation():
            self._draw_presented(target)
            return
        self.logical.fill((0, 0, 0))
        if self.splash_phase != -1 and self.splash_sprites:
            y = self.screen_height // 2 - self.splash_sprites.frame_height // 2
            self.splash_sprites.draw_frame(self.splash_phase, self.logical, 0, y)
        elif self.playfield_ready and self.terrain and self.parallax and self.motorbike:
            d = self.game_metadata.data
            if self.needs_background_clear:
                self.logical.fill((255, 255, 255))
                self.needs_background_clear = False
            self.logical.fill((255, 255, 255), (int(d[9]), 0, self.motorbike.sprite_width, int(d[4])))
            self.parallax.paint(self.logical, 0, 0)
            self.terrain.paint(self.logical)
            if not self.ramp_landing_active:
                self.motorbike.paint(self.logical)
            self.game_clock.paint(self.logical, 0, 0)
            self.draw_stunt_lives()
            self._draw_end_game_overlay(self.logical)
        scaled = pygame.transform.scale(self.logical, (self.screen_width * SCALE, self.screen_height * SCALE))
        target.blit(scaled, (0, 0))
