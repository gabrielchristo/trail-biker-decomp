from pathlib import Path

SCALE = 4
DEVICE_PROFILE_ID = 4
LOGIC_TICK_MS = 100
FPS = 60

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "resources"
SAVES = ROOT / "saves"


def default_screen_size() -> tuple[int, int]:
    width, height = 176, 208
    if DEVICE_PROFILE_ID not in (0, 4):
        width += 1
        height += 1
    return width, height
