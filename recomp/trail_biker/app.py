import sys

import pygame

from trail_biker.assets.binary_asset import BinaryAsset
from trail_biker.config import FPS
from trail_biker.config import RESOURCES
from trail_biker.config import SCALE
from trail_biker.config import default_screen_size
from trail_biker.game.game_canvas import GameCanvas
from trail_biker.game.levels import LevelStore


def main() -> None:
    if not RESOURCES.is_dir():
        print(f"Missing assets folder: {RESOURCES}", file=sys.stderr)
        print("Copy PNG/DAT files into recomp/resources/ (4x sprites OK).", file=sys.stderr)
        sys.exit(1)
    pygame.init()
    screen_w, screen_h = default_screen_size()
    pygame.display.set_mode((screen_w * SCALE, screen_h * SCALE))
    pygame.display.set_caption("Trail Biker")
    metadata = BinaryAsset(RESOURCES / "metadata_mono.dat")
    levels = LevelStore()
    canvas = GameCanvas(metadata, levels, course_index=0)
    window = pygame.display.get_surface()
    clock = pygame.time.Clock()
    running = True
    while running:
        dt = clock.tick(FPS)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                canvas.handle_event(event)
            elif event.type == pygame.KEYUP and event.key in (
                pygame.K_2,
                pygame.K_5,
                pygame.K_DOWN,
                pygame.K_UP,
            ):
                canvas.handle_key_up()
        canvas.update(dt)
        canvas.draw(window)
        pygame.display.flip()
    pygame.quit()


if __name__ == "__main__":
    main()
