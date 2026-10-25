import math

import pygame

from trail_biker.config import SCALE

_SCALED: dict[int, pygame.Surface] = {}


def _screen_px(logical: float) -> int:
    return int(math.floor(logical * SCALE + 0.5))


def blit_logical(target: pygame.Surface, image: pygame.Surface, logical_x: float, logical_y: float) -> None:
    scaled = _SCALED.get(id(image))
    width = image.get_width() * SCALE
    height = image.get_height() * SCALE
    if scaled is None or scaled.get_width() != width or scaled.get_height() != height:
        scaled = pygame.transform.scale(image, (width, height))
        _SCALED[id(image)] = scaled
    target.blit(scaled, (_screen_px(logical_x), _screen_px(logical_y)))


def fill_logical(
    target: pygame.Surface,
    color: tuple[int, int, int],
    logical_x: float,
    logical_y: float,
    logical_w: int,
    logical_h: int,
) -> None:
    target.fill(
        color,
        (_screen_px(logical_x), _screen_px(logical_y), logical_w * SCALE, logical_h * SCALE),
    )
