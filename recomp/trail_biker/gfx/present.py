from __future__ import annotations

import math

import pygame

from trail_biker.config import UI_FONT_OVERLAY
from trail_biker.config import WINDOW_HEIGHT
from trail_biker.config import WINDOW_WIDTH
from trail_biker.config import default_screen_size
from trail_biker.config import presentation_layout

_SCALED: dict[tuple[int, float], pygame.Surface] = {}


def _layout(logical_w: int, logical_h: int) -> tuple[float, int, int, int, int]:
    return presentation_layout(logical_w, logical_h)


def _is_logical_surface(target: pygame.Surface, logical_w: int, logical_h: int) -> bool:
    return target.get_width() == logical_w and target.get_height() == logical_h


def _view_scale_for_target(target: pygame.Surface, logical_w: int, logical_h: int) -> float:
    if _is_logical_surface(target, logical_w, logical_h):
        return 1.0
    return _layout(logical_w, logical_h)[0]


def _screen_px(logical: float, view_scale: float) -> int:
    return int(math.floor(logical * view_scale + 0.5))


def blit_logical(
    target: pygame.Surface,
    image: pygame.Surface,
    logical_x: float,
    logical_y: float,
    logical_w: int | None = None,
    logical_h: int | None = None,
) -> None:
    lw, lh = logical_w or default_screen_size()[0], logical_h or default_screen_size()[1]
    if _is_logical_surface(target, lw, lh):
        target.blit(image, (int(logical_x), int(logical_y)))
        return
    view_scale, offset_x, offset_y, _, _ = _layout(lw, lh)
    cache_key = (id(image), view_scale)
    width = max(1, _screen_px(image.get_width(), view_scale))
    height = max(1, _screen_px(image.get_height(), view_scale))
    scaled = _SCALED.get(cache_key)
    if scaled is None or scaled.get_width() != width or scaled.get_height() != height:
        scaled = pygame.transform.scale(image, (width, height))
        _SCALED[cache_key] = scaled
    target.blit(
        scaled,
        (offset_x + _screen_px(logical_x, view_scale), offset_y + _screen_px(logical_y, view_scale)),
    )


def fill_logical(
    target: pygame.Surface,
    color: tuple[int, int, int],
    logical_x: float,
    logical_y: float,
    logical_w: int,
    logical_h: int,
    canvas_logical_w: int | None = None,
    canvas_logical_h: int | None = None,
) -> None:
    clw, clh = canvas_logical_w or default_screen_size()[0], canvas_logical_h or default_screen_size()[1]
    if _is_logical_surface(target, clw, clh):
        target.fill(color, (int(logical_x), int(logical_y), logical_w, logical_h))
        return
    view_scale, offset_x, offset_y, _, _ = _layout(clw, clh)
    target.fill(
        color,
        (
            offset_x + _screen_px(logical_x, view_scale),
            offset_y + _screen_px(logical_y, view_scale),
            _screen_px(logical_w, view_scale),
            _screen_px(logical_h, view_scale),
        ),
    )


def present_logical_frame(
    target: pygame.Surface,
    logical: pygame.Surface,
    logical_w: int,
    logical_h: int,
) -> tuple[float, int, int]:
    """Scale logical framebuffer to fill the window (integer VIEW_SCALE)."""
    view_scale, offset_x, offset_y, game_w, game_h = _layout(logical_w, logical_h)
    scaled = pygame.transform.scale(logical, (game_w, game_h))
    target.blit(scaled, (offset_x, offset_y))
    return view_scale, offset_x, offset_y


def draw_playfield_message(
    target: pygame.Surface,
    text: str | None,
    center_logical: tuple[int, int],
    logical_w: int,
    logical_h: int,
) -> None:
    if not text:
        return
    if target.get_width() != WINDOW_WIDTH or target.get_height() != WINDOW_HEIGHT:
        return
    view_scale, offset_x, offset_y, _, _ = _layout(logical_w, logical_h)
    cx, cy = center_logical
    pad = max(8, int(round(4 * view_scale)))
    font = pygame.font.SysFont(None, UI_FONT_OVERLAY)
    text_surf = font.render(text, True, (0, 0, 0))
    box_w = text_surf.get_width() + pad * 2
    box_h = text_surf.get_height() + pad * 2
    sx = offset_x + _screen_px(cx, view_scale)
    sy = offset_y + _screen_px(cy, view_scale)
    x0 = sx - box_w // 2
    y0 = sy - box_h // 2
    radius = min(pad, box_w // 4, box_h // 4)
    pygame.draw.rect(target, (255, 255, 255), (x0, y0, box_w, box_h), border_radius=radius)
    pygame.draw.rect(target, (0, 0, 0), (x0, y0, box_w, box_h), max(1, pad // 4), border_radius=radius)
    target.blit(text_surf, (x0 + pad, y0 + pad))
