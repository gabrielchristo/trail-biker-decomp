"""Render expanded terrain tiles the same way TerrainLevel + SpriteSheet do in-game."""

from __future__ import annotations

import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.assets.metadata_layout import split_terrain_blobs
from trail_biker.config import DEVICE_PROFILE_ID
from trail_biker.config import RESOURCES
from trail_biker.config import default_screen_size
from trail_biker.game.levels import LevelStore
from trail_biker.game.terrain_level import TerrainLevel
from trail_biker.gfx.images import load_logical_image
from trail_biker.gfx.sprite_sheet import SpriteSheet


def _empty_tile_color() -> int:
    return 0x00FF00 if DEVICE_PROFILE_ID == 4 else 0xFFFFFF


def _rgb(argb: int) -> tuple[int, int, int]:
    return (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF


def build_terrain_for_course(
    course_index: int,
    metadata: GameMetadata,
    level_store: LevelStore,
) -> TerrainLevel:
    d = metadata.data
    screen_w, _ = default_screen_size()
    tiles = load_logical_image(RESOURCES / "tiles.png")
    tile_sprites = SpriteSheet(tiles, int(d[6]), int(d[7]), True)
    slope_bytes, piece_bytes = split_terrain_blobs(d)
    grid = level_store.build_level_grid(course_index)
    spawn_x = int(d[8])
    return TerrainLevel(
        0,
        int(d[4]),
        screen_w,
        int(d[3]),
        tile_sprites,
        int(d[5]),
        grid,
        False,
        slope_bytes,
        piece_bytes,
        spawn_x,
        _empty_tile_color(),
    )


def render_expanded_surface(terrain: TerrainLevel) -> pygame.Surface:
    tile_map = terrain.expanded_tile_map
    if tile_map is None:
        raise ValueError("terrain has no expanded tile map")
    tile = terrain.tile_size_px
    cols = terrain.expanded_map_width_tiles
    rows = terrain.expanded_map_height_tiles
    empty = _rgb(terrain.scroll_layer.empty_tile_fill_color)
    sprites = terrain.scroll_layer.tile_sprites
    surface = pygame.Surface((cols * tile, rows * tile))
    surface.fill(empty)
    for row in range(rows):
        for col in range(cols):
            tile_id = tile_map[row][col] & 0xFF
            if tile_id == 0:
                continue
            surface.blit(sprites.frame_surface(tile_id - 1), (col * tile, row * tile))
    return surface


def render_course_expanded(
    course_index: int,
    metadata: GameMetadata,
    level_store: LevelStore,
) -> pygame.Surface:
    terrain = build_terrain_for_course(course_index, metadata, level_store)
    return render_expanded_surface(terrain)
