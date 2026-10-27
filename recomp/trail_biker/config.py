from pathlib import Path

# Source PNGs are 4×; loaders downscale to logical pixels.
ASSET_SCALE = 4
SCALE = ASSET_SCALE
# Window = logical framebuffer × VIEW_SCALE (same as in-game area, no letterbox).
VIEW_SCALE = ASSET_SCALE

DEVICE_PROFILE_ID = 4
LOGIC_TICK_MS = 100
FPS = 60
VISUAL_INTERPOLATION = False

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "resources"
SAVES = ROOT / "saves"
METADATA_PATH = RESOURCES / "metadata_mono.json"
LEVELS_PATH = RESOURCES / "levels.json"
SOUND_PATH = RESOURCES / "sound.json"

UI_FONT_MENU_TITLE = VIEW_SCALE * 8
UI_FONT_MENU_BODY = VIEW_SCALE * 6
UI_FONT_MENU_HINT = VIEW_SCALE * 5
UI_FONT_OVERLAY = VIEW_SCALE * 6


def default_screen_size() -> tuple[int, int]:
    width, height = 176, 208
    if DEVICE_PROFILE_ID not in (0, 4):
        width += 1
        height += 1
    return width, height


def window_size_from_logical(logical_w: int, logical_h: int) -> tuple[int, int]:
    return logical_w * VIEW_SCALE, logical_h * VIEW_SCALE


def presentation_layout(logical_w: int, logical_h: int) -> tuple[float, int, int, int, int]:
    """Integer upscale; window size matches the presented game area."""
    game_w, game_h = window_size_from_logical(logical_w, logical_h)
    return float(VIEW_SCALE), 0, 0, game_w, game_h


def letterbox_layout(logical_w: int, logical_h: int) -> tuple[float, int, int, int, int]:
    """Alias for presentation_layout (legacy name)."""
    return presentation_layout(logical_w, logical_h)


_lw, _lh = default_screen_size()
WINDOW_WIDTH, WINDOW_HEIGHT = window_size_from_logical(_lw, _lh)
