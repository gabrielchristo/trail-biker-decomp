import sys

import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.assets.game_sounds import GameSounds
from trail_biker.config import LEVELS_PATH
from trail_biker.config import METADATA_PATH
from trail_biker.config import SOUND_PATH
from trail_biker.config import FPS
from trail_biker.config import RESOURCES
from trail_biker.config import SCALE
from trail_biker.config import default_screen_size
from trail_biker.game.level_manager import LevelManager


def main() -> None:
    if not RESOURCES.is_dir():
        print(f"Missing assets folder: {RESOURCES}", file=sys.stderr)
        print("Copy PNG/DAT files into recomp/resources/ (4x sprites OK).", file=sys.stderr)
        sys.exit(1)
    pygame.mixer.pre_init(frequency=8000, size=-8, channels=1, buffer=512)
    pygame.init()
    if not pygame.mixer.get_init():
        pygame.mixer.init(frequency=8000, size=-8, channels=1, buffer=512)
    screen_w, screen_h = default_screen_size()
    pygame.display.set_mode((screen_w * SCALE, screen_h * SCALE))
    pygame.display.set_caption("Trail Biker")
    if not METADATA_PATH.is_file():
        print(f"Missing metadata: {METADATA_PATH}", file=sys.stderr)
        print("Run: python tools/metadata_mono_to_json.py", file=sys.stderr)
        sys.exit(1)
    if not LEVELS_PATH.is_file():
        print(f"Missing levels: {LEVELS_PATH}", file=sys.stderr)
        print("Run: python tools/levels_dat_to_json.py", file=sys.stderr)
        sys.exit(1)
    if not SOUND_PATH.is_file():
        print(f"Missing sound manifest: {SOUND_PATH}", file=sys.stderr)
        sys.exit(1)
    try:
        GameSounds.load_wav_paths(SOUND_PATH)
    except (OSError, ValueError) as exc:
        print(f"Sound assets: {exc}", file=sys.stderr)
        sys.exit(1)
    metadata = GameMetadata.load(METADATA_PATH)
    manager = LevelManager(metadata)
    window = pygame.display.get_surface()
    clock = pygame.time.Clock()
    running = True
    while running:
        dt = clock.tick(FPS)
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.WINDOWFOCUSLOST:
                manager.handle_midlet_pause()
            elif event.type == pygame.KEYDOWN:
                if manager.showing_menu:
                    manager.handle_event(event)
                else:
                    manager.game_canvas.handle_event(event)
            elif event.type == pygame.KEYUP and not manager.showing_menu:
                if event.key in (pygame.K_2, pygame.K_5, pygame.K_DOWN, pygame.K_UP):
                    manager.game_canvas.handle_key_up()
        manager.update(dt)
        manager.draw(window)
        pygame.display.flip()
    pygame.quit()


if __name__ == "__main__":
    main()
