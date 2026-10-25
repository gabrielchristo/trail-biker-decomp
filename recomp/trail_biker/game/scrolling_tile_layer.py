from __future__ import annotations

import pygame

from trail_biker.gfx.sprite_sheet import SpriteSheet


class ScrollingTileLayer:
    TILE_WRAP_MARKER = 128
    NO_TILE = -1

    def __init__(
        self,
        draw_anchor_x: int,
        draw_anchor_y: int,
        track_width_px: int,
        viewport_height_px: int,
        tile_sprites: SpriteSheet,
        tile_size_px: int,
        tile_map: list[list[int]] | None,
        level_width_tiles: int,
        level_height_tiles: int,
        wrap_at_far_edge: bool,
        empty_tile_fill_color: int,
    ) -> None:
        self.draw_anchor_x = draw_anchor_x
        self.draw_anchor_y = draw_anchor_y
        self.track_width_px = track_width_px
        self.tile_sprites = tile_sprites
        self.tile_map = tile_map
        self.level_width_tiles = level_width_tiles
        self.level_height_tiles = level_height_tiles
        self.tile_size_px = tile_size_px
        self.wrap_at_far_edge = wrap_at_far_edge
        self.buffer_column_count = track_width_px // tile_size_px + 2
        if track_width_px % tile_size_px > 0:
            self.buffer_column_count += 1
        self.buffer_width_px = self.buffer_column_count * tile_size_px
        self.buffer_row_count = viewport_height_px // tile_size_px
        self.buffer_height_px = self.buffer_row_count * tile_size_px
        self.column_scan_limit = self.buffer_column_count
        self.row_scan_limit = self.buffer_row_count
        self.empty_tile_fill_color = empty_tile_fill_color
        self.world_column_index = 0
        self.world_row_index = 0
        self.ring_write_offset_px = 0
        self.scroll_offset_secondary = 0
        self.scroll_offset_px = 0
        self.wrap_scroll_active = False
        self.wrap_probe_x = 0
        self.buffer_image: pygame.Surface | None = None
        self.saved_scroll_offset_px = 0
        self.saved_scroll_secondary_px = 0
        self.init_scroll_at(0, 0)

    def _ensure_buffer(self) -> pygame.Surface:
        if self.buffer_image is None:
            self.buffer_image = pygame.Surface((self.buffer_width_px, self.buffer_height_px))
            self.buffer_image.fill((255, 255, 255))
        return self.buffer_image

    def _draw_tile_cell(self, col: int, row: int, dest_x: int, dest_y: int) -> None:
        if self.tile_map is None:
            return
        buf = self._ensure_buffer()
        tile = self.tile_map[row][col]
        if tile == 0:
            buf.fill(self._rgb(self.empty_tile_fill_color), (dest_x, dest_y, self.tile_size_px, self.tile_size_px))
        else:
            self.tile_sprites.draw_frame(tile - 1, buf, dest_x, dest_y)

    @staticmethod
    def _rgb(argb: int) -> tuple[int, int, int]:
        return (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF

    def rebuild_entire_buffer(self) -> None:
        for i in range(self.buffer_column_count):
            for j in range(self.buffer_row_count):
                self._draw_tile_cell(
                    self.world_column_index + i,
                    self.world_row_index + j,
                    i * self.tile_size_px,
                    j * self.tile_size_px,
                )

    def strip_wrap_markers(self, redraw: bool) -> None:
        if self.tile_map is None:
            return
        col = self.world_column_index
        for i in range(self.buffer_column_count):
            if i * self.tile_size_px >= self.ring_write_offset_px and col >= self.world_column_index:
                col = self.column_scan_limit - self.buffer_column_count
            row = self.world_row_index
            for j in range(self.buffer_row_count):
                if (self.tile_map[row][col] & 0x80) != 0:
                    self.tile_map[row][col] = (self.tile_map[row][col] - 128) & 0xFF
                    if redraw:
                        self._draw_tile_cell(col, row, i * self.tile_size_px, j * self.tile_size_px)
                row += 1
            col += 1

    def update_scroll_reverse(self, probe: int, delta: int) -> bool:
        if self.scroll_offset_secondary < 0:
            self.scroll_offset_secondary += self.buffer_width_px
        if (
            self.scroll_offset_secondary > self.ring_write_offset_px
            and self.scroll_offset_secondary <= self.ring_write_offset_px + self.tile_size_px
        ):
            if (
                self.world_column_index + self.ring_write_offset_px // self.tile_size_px
                >= self.level_width_tiles
                and self.wrap_at_far_edge
            ):
                self.world_column_index = 0
                self.column_scan_limit = self.buffer_column_count
                self.ring_write_offset_px = self.buffer_width_px
            if self.world_column_index + self.ring_write_offset_px // self.tile_size_px >= self.level_width_tiles:
                if self.wrap_scroll_active:
                    self.wrap_scroll_active = False
                    self.wrap_probe_x = probe
                self.scroll_offset_px -= delta
                self.scroll_offset_secondary -= delta
                if self.scroll_offset_secondary < 0:
                    self.scroll_offset_secondary += self.buffer_width_px
            else:
                if self.ring_write_offset_px >= self.buffer_width_px:
                    self.ring_write_offset_px = 0
                    self.world_column_index += self.buffer_column_count
                if self.scroll_offset_secondary >= self.buffer_width_px:
                    self.scroll_offset_secondary -= self.buffer_width_px
                ring = self.ring_write_offset_px
                self.ring_write_offset_px += self.tile_size_px
                self.column_scan_limit += 1
                for j in range(self.buffer_row_count):
                    self._draw_tile_cell(
                        self.world_column_index + ring // self.tile_size_px,
                        self.world_row_index + j,
                        ring,
                        j * self.tile_size_px,
                    )
        if self.scroll_offset_px >= self.buffer_width_px:
            self.scroll_offset_px -= self.buffer_width_px
        if self.scroll_offset_px < 0:
            self.scroll_offset_px += self.buffer_width_px
        if (
            self.scroll_offset_px < self.ring_write_offset_px
            and self.scroll_offset_px >= self.ring_write_offset_px - self.tile_size_px
        ):
            if self.world_column_index - (self.buffer_column_count - self.ring_write_offset_px // self.tile_size_px) <= 0:
                if self.wrap_scroll_active:
                    self.wrap_scroll_active = False
                    self.wrap_probe_x = probe
                self.scroll_offset_px -= delta
                self.scroll_offset_secondary -= delta
                if self.scroll_offset_px >= self.buffer_width_px:
                    self.scroll_offset_px -= self.buffer_width_px
            else:
                self.ring_write_offset_px -= self.tile_size_px
                ring = self.ring_write_offset_px
                self.column_scan_limit -= 1
                if self.ring_write_offset_px <= 0:
                    self.ring_write_offset_px = self.buffer_width_px
                    self.world_column_index -= self.buffer_column_count
                for j in range(self.buffer_row_count):
                    self._draw_tile_cell(
                        self.column_scan_limit - self.buffer_column_count,
                        self.world_row_index + j,
                        ring,
                        j * self.tile_size_px,
                    )
        return self.wrap_scroll_active

    def update_scroll_forward(self, probe: int, delta: int) -> bool:
        if not self.wrap_scroll_active:
            left_cols = self.buffer_column_count - self.ring_write_offset_px // self.tile_size_px
            if self.world_column_index - left_cols <= 0 and probe >= self.wrap_probe_x and probe < self.wrap_probe_x + 5:
                self.wrap_scroll_active = True
            if (
                self.world_column_index + self.ring_write_offset_px // self.tile_size_px >= self.level_width_tiles
                and probe <= self.wrap_probe_x
                and probe > self.wrap_probe_x - 5
            ):
                self.wrap_scroll_active = True
        if self.wrap_scroll_active:
            self.scroll_offset_px += delta
            self.scroll_offset_secondary += delta
        return self.wrap_scroll_active

    def paint(self, target: pygame.Surface) -> None:
        buf = self._ensure_buffer()
        ax = self.draw_anchor_x - self.scroll_offset_px
        ay = self.draw_anchor_y
        target.blit(buf, (ax, ay))
        if self.scroll_offset_px >= self.scroll_offset_secondary:
            target.blit(buf, (ax + self.buffer_width_px, ay))

    def get_tile_at(self, col: int, row: int) -> int:
        if col == -1 or row == -1 or self.tile_map is None:
            return self.NO_TILE
        return self.tile_map[row][col]

    def tile_column_from_pixel_x(self, px: int) -> int:
        col = px // self.tile_size_px
        if col < 0 or col >= self.level_width_tiles:
            return -1
        return col

    def tile_row_from_pixel_y(self, py: int) -> int:
        row = py // self.tile_size_px
        if row < 0 or row >= self.level_height_tiles:
            return -1
        return row

    def set_tile_map_data(self, tile_map: list[list[int]], width_tiles: int, height_tiles: int) -> None:
        self._ensure_buffer().fill((255, 255, 255))
        self.tile_map = tile_map
        self.level_width_tiles = width_tiles
        self.level_height_tiles = height_tiles
        self.column_scan_limit = self.buffer_column_count
        self.row_scan_limit = self.buffer_row_count
        self.init_scroll_at(0, 0)

    def init_scroll_at(self, scroll_px: int, row_px: int) -> None:
        self.saved_scroll_offset_px = scroll_px
        self.saved_scroll_secondary_px = self.buffer_width_px - self.tile_size_px
        self.wrap_scroll_active = True
        self.ring_write_offset_px = self.buffer_width_px
        self.scroll_offset_secondary = self.saved_scroll_secondary_px
        self.scroll_offset_px = self.saved_scroll_offset_px
        self.world_column_index = scroll_px // self.tile_size_px
        self.world_row_index = row_px // self.tile_size_px
        self.rebuild_entire_buffer()
