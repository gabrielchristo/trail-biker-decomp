import pygame

from trail_biker.config import SCALE


def load_logical_image(path) -> pygame.Surface:
    image = pygame.image.load(str(path))
    if pygame.display.get_surface() is not None:
        image = image.convert_alpha()
    w, h = image.get_size()
    if SCALE != 1:
        image = pygame.transform.smoothscale(image, (w // SCALE, h // SCALE))
    return image
