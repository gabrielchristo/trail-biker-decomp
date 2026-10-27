from __future__ import annotations

import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.config import UI_FONT_MENU_BODY
from trail_biker.config import UI_FONT_MENU_HINT
from trail_biker.config import UI_FONT_MENU_TITLE
from trail_biker.config import WINDOW_HEIGHT
from trail_biker.config import WINDOW_WIDTH
from trail_biker.config import default_screen_size
from trail_biker.game.game_canvas import GameCanvas
from trail_biker.game.levels import LevelStore
from trail_biker.game.progress_store import ProgressStore
from trail_biker.game.sound_manager import SoundManager
from trail_biker.game.stunt_cup_awards import apply_stunt_cup_awards
from trail_biker import ui_strings as ui


def _format_time_ms(ms: int) -> str:
    mins = int(ms // 1000 // 60)
    secs = int(ms // 1000 % 60)
    centis = int(ms // 10 % 100)
    return f"{mins // 10}{mins % 10}:{secs // 10}{secs % 10}'{centis // 10}{centis % 10}"


def _cup_tier_label(tier: int) -> str | None:
    if tier == 1:
        return ui.CUP_TIER_BRONZE
    if tier == 2:
        return ui.CUP_TIER_SILVER
    if tier == 3:
        return ui.CUP_TIER_GOLD
    return None


class LevelManager:
    def __init__(self, metadata: GameMetadata) -> None:
        self.level_store = LevelStore()
        self.progress = ProgressStore(self.level_store)
        self.sound_manager = SoundManager()
        self.selected_course_index = 0
        self.game_mode = 0
        self.menu_screen_id = 1
        self.showing_menu = False
        self.pause_overlay_active = False
        self.hide_continue_item = True
        self.menu_index = 0
        self._menu_items: list[str] = []
        self._form_lines: list[str] = []
        self.screen_width, self.screen_height = default_screen_size()
        self.game_canvas = GameCanvas(metadata, self.level_store, self)
        self._font: pygame.font.Font | None = None
        self._sync_audio_settings()

    def _sync_audio_settings(self) -> None:
        self.game_canvas.sound_enabled = self.progress.sound_enabled

    def get_best_time_ms(self, index: int) -> int:
        return self.progress.best_times_ms[index]

    def get_par_time_from_dat(self, index: int) -> int:
        return self.level_store.get_par_time_ms(index)

    def get_target_time_ms(self, index: int) -> int:
        if self.game_mode == 2:
            return self.get_par_time_from_dat(index)
        return self.get_best_time_ms(index)

    def get_current_target_time_ms(self) -> int:
        return self.get_target_time_ms(self.selected_course_index)

    def get_unlocked_level_count(self) -> int:
        return self.progress.unlocked_course_count

    def pause_game_canvas(self) -> None:
        self.game_canvas.game_clock.set_paused(True)
        if self.menu_screen_id == 0:
            self.menu_screen_id = 3

    def enter_play_mode(self) -> None:
        self.menu_screen_id = 0
        self.showing_menu = False
        self.pause_overlay_active = False
        if self.hide_continue_item:
            self.game_canvas.start_level()
        else:
            self.game_canvas.game_clock.set_paused(False)
        self.hide_continue_item = False

    def pause_from_game(self) -> None:
        self.pause_game_canvas()
        self._build_menu_items()
        self.showing_menu = True
        self.pause_overlay_active = True

    def handle_midlet_pause(self) -> None:
        if not self.pause_overlay_active:
            self.pause_overlay_active = True
            self.pause_game_canvas()
            self._build_menu_items()
            self.showing_menu = True

    def _open_stunt_cup_award_screen(self) -> None:
        title, unlock, changed = apply_stunt_cup_awards(
            self.progress,
            self.game_canvas.stunt_lives_lost,
        )
        if changed:
            self.progress.save()
        self._form_lines = []
        if title:
            self._form_lines.append(title)
        if unlock:
            self._form_lines.append(unlock)
        if not self._form_lines:
            self._form_lines.append("Stunt Cup complete!")
        self.menu_screen_id = 6
        self._menu_items = [ui.LABEL_BACK]
        self.menu_index = 0
        self.showing_menu = True
        self.pause_overlay_active = True

    def on_race_finished(self, time_delta: int) -> None:
        saved = False
        idx = self.selected_course_index
        if time_delta != -1 and 0 <= idx < len(self.progress.best_times_ms):
            if time_delta < self.progress.best_times_ms[idx]:
                self.progress.best_times_ms[idx] = time_delta
                saved = True
        canvas = self.game_canvas
        if (
            self.game_mode == 2
            and idx < 7
            and canvas.stunt_lives_lost < canvas.stunt_lives_total
            and time_delta > 0
        ):
            if idx == self.progress.unlocked_course_count - 1:
                self.progress.unlocked_course_count += 1
            saved = True
        if saved:
            self.progress.save()
        self.hide_continue_item = True
        self.pause_game_canvas()
        if self.game_mode == 2 and canvas.stunt_lives_lost < canvas.stunt_lives_total:
            if idx < 7:
                if time_delta > 0:
                    self.selected_course_index += 1
                    canvas.tick_stunt_cup_life()
                self.enter_play_mode()
                return
            if time_delta > 0:
                self._open_stunt_cup_award_screen()
                return
            self.enter_play_mode()
            return
        self.menu_screen_id = 3
        self._build_menu_items()
        self.showing_menu = True
        self.pause_overlay_active = True

    def _build_high_score_lines(self) -> list[str]:
        lines: list[str] = []
        tier = _cup_tier_label(self.progress.best_cup_tier)
        if tier:
            lines.append(f"Best Cup: {tier}")
            lines.append("")
        for i in range(self.get_unlocked_level_count()):
            t = self.progress.best_times_ms[i]
            lines.append(f"Course {i + 1}      {_format_time_ms(t)}")
        return lines

    def _build_menu_items(self) -> None:
        self._form_lines = []
        if self.menu_screen_id == 1:
            self._menu_items = [
                ui.MENU_PRACTICE,
                ui.MENU_TIME_ATTACK,
                ui.MENU_STUNT_CUP,
                ui.MENU_HIGH_SCORES,
                ui.LABEL_EXIT,
            ]
        elif self.menu_screen_id == 2:
            self._menu_items = [f"Course {i + 1}" for i in range(self.get_unlocked_level_count())]
        elif self.menu_screen_id == 3:
            self._menu_items = []
            if not self.hide_continue_item:
                self._menu_items.append(ui.MENU_CONTINUE)
            self._menu_items.extend(["New Game", ui.MENU_INSTRUCTIONS])
        elif self.menu_screen_id == 4:
            self._menu_items = [ui.LABEL_BACK]
            self._form_lines = self._build_high_score_lines()
        elif self.menu_screen_id == 5:
            self._menu_items = [ui.LABEL_BACK]
            self._form_lines = list(ui.INSTRUCTIONS_LINES)
        elif self.menu_screen_id == 6:
            if not self._form_lines:
                self._form_lines = ["Stunt Cup complete!"]
            self._menu_items = [ui.LABEL_BACK]
        else:
            self._menu_items = []
        self.menu_index = min(self.menu_index, max(0, len(self._menu_items) - 1))

    def open_main_menu(self) -> None:
        self.menu_screen_id = 1
        self._build_menu_items()
        self.showing_menu = True

    def handle_event(self, event: pygame.event.Event) -> None:
        if event.type != pygame.KEYDOWN:
            return
        if event.key == pygame.K_ESCAPE:
            if self.showing_menu:
                self._menu_back()
            else:
                self.pause_from_game()
            return
        if not self.showing_menu:
            return
        if self.menu_screen_id in (4, 5, 6):
            if event.key in (pygame.K_RETURN, pygame.K_SPACE):
                self._menu_back()
            return
        if event.key in (pygame.K_UP, pygame.K_w):
            self.menu_index = (self.menu_index - 1) % len(self._menu_items)
        elif event.key in (pygame.K_DOWN, pygame.K_s):
            self.menu_index = (self.menu_index + 1) % len(self._menu_items)
        elif event.key in (pygame.K_RETURN, pygame.K_SPACE):
            self._menu_select()

    def _menu_select(self) -> None:
        if not self._menu_items:
            return
        label = self._menu_items[self.menu_index]
        if self.menu_screen_id == 1:
            if label == ui.MENU_PRACTICE:
                self.game_mode = 0
                self.menu_screen_id = 2
            elif label == ui.MENU_TIME_ATTACK:
                self.game_mode = 1
                self.menu_screen_id = 2
            elif label == ui.MENU_STUNT_CUP:
                self.game_mode = 2
                self.selected_course_index = 0
                self.menu_screen_id = 3
            elif label == ui.MENU_HIGH_SCORES:
                self.menu_screen_id = 4
            elif label == ui.LABEL_EXIT:
                pygame.event.post(pygame.event.Event(pygame.QUIT))
                return
        elif self.menu_screen_id == 2:
            self.selected_course_index = self.menu_index
            self.menu_screen_id = 3
        elif self.menu_screen_id == 3:
            if label == ui.MENU_CONTINUE:
                self.hide_continue_item = True
                self.enter_play_mode()
                return
            if label == "New Game":
                self.hide_continue_item = True
                self.game_canvas.reset_stunt_lives()
                if self.game_mode == 2:
                    self.selected_course_index = 0
                self.enter_play_mode()
                return
            if label == ui.MENU_INSTRUCTIONS:
                self.menu_screen_id = 5
        elif self.menu_screen_id in (4, 5, 6):
            self.menu_screen_id = 1
        self._build_menu_items()

    def _menu_back(self) -> None:
        if self.menu_screen_id == 2:
            self.menu_screen_id = 1
        elif self.menu_screen_id == 3:
            if self.game_mode in (0, 1):
                self.menu_screen_id = 2
            else:
                self.menu_screen_id = 1
            self.hide_continue_item = True
        elif self.menu_screen_id in (4, 5, 6):
            self.menu_screen_id = 1
        else:
            self.showing_menu = False
            self.pause_overlay_active = False
            self.game_canvas.game_clock.set_paused(False)
            return
        self._build_menu_items()

    def draw_menu(self, target: pygame.Surface) -> None:
        if not self.showing_menu:
            return
        game_w, game_h = WINDOW_WIDTH, WINDOW_HEIGHT
        panel = pygame.Surface((game_w, game_h), pygame.SRCALPHA)
        panel.fill((32, 32, 64, 235))
        title_font = pygame.font.SysFont(None, UI_FONT_MENU_TITLE)
        body_font = pygame.font.SysFont(None, UI_FONT_MENU_BODY)
        hint_font = pygame.font.SysFont(None, UI_FONT_MENU_HINT)
        margin = max(16, game_w // 22)
        title = ui.GAME_TITLE
        if self.menu_screen_id == 2:
            title = ui.MENU_SELECT_COURSE
        elif self.menu_screen_id == 3:
            title = "Pause"
        elif self.menu_screen_id == 4:
            title = ui.MENU_HIGH_SCORES
        elif self.menu_screen_id == 5:
            title = ui.MENU_INSTRUCTIONS
        elif self.menu_screen_id == 6:
            title = ui.MENU_STUNT_CUP
        panel.blit(title_font.render(title, True, (255, 255, 255)), (margin, margin))
        line_gap = UI_FONT_MENU_BODY + 6
        y = margin + UI_FONT_MENU_TITLE + 8
        for line in self._form_lines:
            panel.blit(body_font.render(line, True, (210, 210, 220)), (margin, y))
            y += line_gap
            if y > game_h - margin * 2:
                break
        if self._form_lines:
            y += 8
        for i, item in enumerate(self._menu_items):
            color = (255, 220, 80) if i == self.menu_index else (200, 200, 200)
            prefix = "> " if i == self.menu_index else "  "
            panel.blit(body_font.render(prefix + item, True, color), (margin, y))
            y += line_gap
        hint = "Enter/Esc=Back" if self.menu_screen_id in (4, 5, 6) else "Up/Down Enter  Esc=Back"
        hint_surf = hint_font.render(hint, True, (140, 140, 160))
        panel.blit(hint_surf, (margin, game_h - margin - hint_surf.get_height()))
        if target.get_width() == WINDOW_WIDTH and target.get_height() == WINDOW_HEIGHT:
            target.blit(panel, (0, 0))
        else:
            target.blit(pygame.transform.scale(panel, (target.get_width(), target.get_height())), (0, 0))

    def update(self, dt_ms: float) -> None:
        if not self.showing_menu:
            self.game_canvas.update(dt_ms)

    def draw(self, target: pygame.Surface) -> None:
        self.game_canvas.draw(target)
        # Keep end-game overlays visible (do not cover with pause/menu chrome).
        if self.showing_menu and not self.game_canvas.end_game_flags:
            self.draw_menu(target)
