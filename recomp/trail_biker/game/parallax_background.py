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

    def _blit_tiled(self, target: pygame.Surface, frame_index: int, origin_x: int, y: int) -> None:
        width = target.get_width()
        seg = self.segment_width_px
        step = self.scroll_step_sign * seg
        px = origin_x
        while px < width:
            if px + seg > 0:
                self.sprite_sheet.draw_frame(frame_index, target, px, y)
            px += step
        px = origin_x - step
        while px + seg > 0:
            self.sprite_sheet.draw_frame(frame_index, target, px, y)
            px -= step

    def paint(self, target: pygame.Surface, x: int, y: int) -> None:
        width = target.get_width()
        seg = self.segment_width_px
        if self.layer_count > 1:
            span = self.layer_count * seg
            base = x - self.scroll_offset_px
            for k in range(-1, 3):
                block_x = base + k * self.scroll_step_sign * span
                for i in range(self.layer_count):
                    px = block_x + i * self.scroll_step_sign * seg
                    if px < width and px + seg > 0:
                        self.sprite_sheet.draw_frame(self.layer_frame_indices[i], target, px, y)
        else:
            origin = x - self.scroll_offset_px
            idx = self.layer_frame_indices[0]
            self._blit_tiled(target, idx, origin, y)
