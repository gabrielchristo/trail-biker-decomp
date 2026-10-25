from __future__ import annotations

import pygame

from trail_biker.game.levels import _dim_pair
from trail_biker.game.levels import _ubyte
from trail_biker.game.scrolling_tile_layer import ScrollingTileLayer
from trail_biker.gfx.sprite_sheet import SpriteSheet

PROBE_TILE_ADJUST = -128


class TerrainLevel:
    def __init__(
        self,
        anchor_x: int,
        anchor_y: int,
        screen_width: int,
        viewport_height: int,
        tile_sprites: SpriteSheet,
        tile_size_px: int,
        level_grid: list[list[int]] | None,
        wrap_at_far_edge: bool,
        slope_bytes: list[int],
        piece_catalog_bytes: list[int],
        bike_world_x: int,
        empty_tile_color: int,
    ) -> None:
        self.tile_size_px = tile_size_px
        self._parse_slope_metadata(slope_bytes)
        self._parse_tile_piece_catalog(piece_catalog_bytes)
        self.expanded_tile_map: list[list[int]] | None = None
        self.expanded_map_width_tiles = 0
        self.expanded_map_height_tiles = 0
        self.bike_world_x = bike_world_x
        self.min_bike_world_x = bike_world_x
        self.surface_tile_id = 0
        self.composed_tile_id = 0
        self.level_width_px = 0
        self.camera_ground_offset_y = 0
        if level_grid is not None:
            self.expand_level_grid(level_grid)
        self.scroll_layer = ScrollingTileLayer(
            anchor_x,
            anchor_y,
            screen_width,
            viewport_height,
            tile_sprites,
            tile_size_px,
            self.expanded_tile_map,
            self.expanded_map_width_tiles,
            self.expanded_map_height_tiles,
            wrap_at_far_edge,
            empty_tile_color,
        )

    def _parse_slope_metadata(self, array: list[int]) -> None:
        n = 0
        stride_divisor = array[n]
        n += 1
        self.slope_index_stride = self.tile_size_px // stride_divisor
        self.slope_table_height = array[n]
        n += 1
        self.slope_lookup: list[list[int]] = []
        for _ in range(self.slope_table_height):
            row = []
            for _ in range(stride_divisor):
                row.append(array[n] & 0xFF)
                n += 1
            self.slope_lookup.append(row)

    def _parse_tile_piece_catalog(self, array: list[int]) -> None:
        n = 0
        self.tile_piece_count = array[n] & 0xFF
        n += 1
        n += 1  # catalog stride byte (read by MIDlet, unused in piece loop)
        self.tile_piece_defs: list[list[list[int]]] = []
        for _ in range(self.tile_piece_count):
            width = array[n] & 0xFF
            n += 1
            height = array[n] & 0xFF
            n += 1
            piece: list[list[int]] = [[width, height]]
            for _ in range(1, height + 1):
                row = []
                for _ in range(width):
                    row.append(array[n] & 0xFF)
                    n += 1
                piece.append(row)
            self.tile_piece_defs.append(piece)
        self.pass_through_tile_count = array[n] & 0xFF
        n += 1
        self.pass_through_tile_ids = [array[n + i] & 0xFF for i in range(self.pass_through_tile_count)]

    def expand_level_grid(self, array: list[list[int]]) -> None:
        self.expanded_map_width_tiles = 0
        self.expanded_map_height_tiles = 0
        source_w = _dim_pair(array[0][0], array[0][1])
        source_h = _dim_pair(array[0][2], array[0][3])
        for i in range(source_w):
            height_sum = 0
            for j in range(1, source_h + 1):
                cell = _ubyte(array[j][i])
                if cell != 0:
                    height_sum += self.tile_piece_defs[cell - 1][0][1]
            self.expanded_map_height_tiles = max(self.expanded_map_height_tiles, height_sum)
        for k in range(1, source_h + 1):
            width_sum = 0
            for col in range(source_w):
                cell = _ubyte(array[k][col])
                if cell != 0:
                    width_sum += self.tile_piece_defs[cell - 1][0][0]
            self.expanded_map_width_tiles = max(self.expanded_map_width_tiles, width_sum)
        self.expanded_tile_map = [[0] * self.expanded_map_width_tiles for _ in range(self.expanded_map_height_tiles)]
        col_offset = 0
        for n3 in range(source_w):
            row_ptr = self.expanded_map_height_tiles - 1
            run_width = 0
            for n6 in range(source_h - 1, -1, -1):
                cell = _ubyte(array[n6 + 1][n3])
                if cell != 0:
                    piece = self.tile_piece_defs[cell - 1]
                    w = piece[0][0]
                    h = piece[0][1]
                    for b4 in range(h):
                        for b5 in range(w):
                            self.expanded_tile_map[row_ptr - b4][col_offset + b5] = piece[h - b4][b5]
                    row_ptr -= h
                    run_width = w
            col_offset += run_width
        self.level_width_px = self.expanded_map_height_tiles * self.tile_size_px

    def get_tile_at_pixels(self, x: int, y: int) -> int:
        return self.scroll_layer.get_tile_at(
            self.scroll_layer.tile_column_from_pixel_x(x),
            self.scroll_layer.tile_row_from_pixel_y(y),
        )

    def match_tile_piece_id(self, left: int, center: int, right: int) -> int:
        if center == 0:
            return 0
        for i in range(self.tile_piece_count):
            pw = self.tile_piece_defs[i][0][0]
            ph = self.tile_piece_defs[i][0][1]
            for b7 in range(1, ph + 1):
                for b8 in range(pw):
                    ok_left = b8 == 0 or self.tile_piece_defs[i][b7][b8 - 1] == left
                    ok_mid = self.tile_piece_defs[i][b7][b8] == center
                    ok_right = b8 == pw - 1 or self.tile_piece_defs[i][b7][b8 + 1] == right
                    if ok_left and ok_mid and ok_right:
                        return i + 1
        return 0

    def get_slope_for_tile(self, x: int, tile_id: int) -> int:
        if tile_id == 0:
            return 0
        return self.slope_lookup[tile_id - 1][(x % self.tile_size_px) // self.slope_index_stride]

    def scroll_by(self, delta: int) -> None:
        if self.bike_world_x + delta < self.min_bike_world_x:
            delta = self.min_bike_world_x - self.bike_world_x
        self.bike_world_x += delta
        self.camera_ground_offset_y = 0
        self.surface_tile_id = 0
        while self.surface_tile_id == 0 and self.camera_ground_offset_y < self.level_width_px:
            self.surface_tile_id = self.get_tile_at_pixels(self.bike_world_x, self.camera_ground_offset_y)
            if self._is_pass_through(self.surface_tile_id):
                self.surface_tile_id = 0
            if self.surface_tile_id == 0:
                self.camera_ground_offset_y += self.tile_size_px
        if self.surface_tile_id != 0:
            self.composed_tile_id = self.match_tile_piece_id(
                self.get_tile_at_pixels(self.bike_world_x - self.tile_size_px, self.camera_ground_offset_y),
                self.surface_tile_id,
                self.get_tile_at_pixels(self.bike_world_x + self.tile_size_px, self.camera_ground_offset_y),
            )
            row_y = self.scroll_layer.tile_row_from_pixel_y(self.camera_ground_offset_y) * self.tile_size_px
            self.camera_ground_offset_y = row_y + self.get_slope_for_tile(self.bike_world_x, self.surface_tile_id)
        self.scroll_layer.update_scroll_forward(0, delta)
        self.scroll_layer.update_scroll_reverse(0, delta)

    def paint(self, target: pygame.Surface) -> None:
        self.scroll_layer.paint(target)

    def probe_terrain_at(self, x: int, y: int, ignore_pass_through: bool, allow_above: bool) -> int:
        result = 0
        buried = False
        scan_y = 0
        if x < 0 or y < 0:
            return 0
        surface = 0
        while surface == 0 and scan_y < self.level_width_px:
            surface = self.get_tile_at_pixels(x, scan_y)
            if ignore_pass_through and self._is_pass_through(surface):
                surface = 0
            if surface == 0:
                scan_y += self.tile_size_px
            elif y <= scan_y + self.tile_size_px:
                # Probe is inside this cell: this is the surface (wood bench, ground, …).
                # The decompiled `continue` never advanced the scan and either hung or
                # skipped the obstacle and collided with the tile underneath.
                break
            else:
                scan_y += self.tile_size_px
                surface = 0
        if surface != 0:
            ground_y = self.scroll_layer.tile_row_from_pixel_y(scan_y) * self.tile_size_px + self.get_slope_for_tile(
                x, surface
            )
            if y >= ground_y or allow_above:
                result = self.match_tile_piece_id(
                    self.get_tile_at_pixels(x - self.tile_size_px, ground_y),
                    surface,
                    self.get_tile_at_pixels(x + self.tile_size_px, ground_y),
                )
                if y > ground_y and not allow_above:
                    buried = True
        if buried:
            result = (result - 128) & 0xFF
        return result

    def load_level(self, grid: list[list[int]]) -> None:
        self.expand_level_grid(grid)
        self.scroll_layer.set_tile_map_data(
            self.expanded_tile_map,
            self.expanded_map_width_tiles,
            self.expanded_map_height_tiles,
        )

    def set_bike_world_x(self, world_x: int) -> None:
        self.bike_world_x = world_x
        self.surface_tile_id = 0
        self.composed_tile_id = 0
        self.scroll_layer.init_scroll_at(world_x - self.min_bike_world_x, 0)

    def find_flat_ground_x(self, x: int, tile_kind: int, width: int) -> int:
        left = self.probe_terrain_at(x - width, 0, True, True)
        mid = self.probe_terrain_at(x - width // 2, 0, True, True)
        right = self.probe_terrain_at(x, 0, True, True)
        while (left == 0 or mid == 0 or right == 0) and x > 0:
            x -= 1
            left = self.probe_terrain_at(x - width, 0, True, True)
            mid = self.probe_terrain_at(x - width // 2, 0, True, True)
            right = self.probe_terrain_at(x, 0, True, True)
        if left != 0 and mid != 0 and right != 0:
            n3 = x - self.tile_piece_defs[left - 1][0][0]
            while (left != tile_kind or mid != tile_kind or right != tile_kind) and n3 > 0:
                n3 -= self.tile_size_px
                left = self.probe_terrain_at(n3 - width, 0, True, True)
                mid = self.probe_terrain_at(n3 - width // 2, 0, True, True)
                right = self.probe_terrain_at(n3, 0, True, True)
            n4 = n3 - width
            return max(n4, 0)
        return max(x - width, 0)

    def _is_pass_through(self, tile_id: int) -> bool:
        return tile_id in self.pass_through_tile_ids
