import pygame

from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.sprite_sheet import SpriteSheet


class ParallaxBackground:
    def __init__(self, image_path, layer_count: int, clipped_mode: bool) -> None:
        self.layer_count = layer_count
        source = load_logical_image(image_path)
        self.sprite_sheet = SpriteSheet(source, 1, layer_count, clipped_mode)
        self.layer_frame_indices = list(range(layer_count))
        self.scroll_offset_px = 0
        self.scroll_step_sign = 1
        self.segment_width_px = self.sprite_sheet.frame_width

    def scroll_by(self, delta: int) -> None:
        self.scroll_offset_px += delta
        self.scroll_step_sign = -1 if self.scroll_offset_px < 0 else 1
        if self.scroll_offset_px <= -self.segment_width_px or self.scroll_offset_px >= self.segment_width_px:
            for i in range(self.layer_count):
                self.layer_frame_indices[i] += self.scroll_step_sign
                if self.layer_frame_indices[i] < 0:
                    self.layer_frame_indices[i] = self.layer_count - 1
                elif self.layer_frame_indices[i] >= self.layer_count:
                    self.layer_frame_indices[i] = 0
            self.scroll_offset_px = 0

    def paint(self, target: pygame.Surface, x: int, y: int) -> None:
        if self.layer_count > 1:
            for i in range(self.layer_count):
                px = x - self.scroll_offset_px + i * self.scroll_step_sign * self.segment_width_px
                self.sprite_sheet.draw_frame(self.layer_frame_indices[i], target, px, y)
        else:
            self.sprite_sheet.draw_frame(self.layer_frame_indices[0], target, x - self.scroll_offset_px, y)
            self.sprite_sheet.draw_frame(
                self.layer_frame_indices[0],
                target,
                x - self.scroll_offset_px + self.scroll_step_sign * self.segment_width_px,
                y,
            )
