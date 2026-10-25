"""Named layout for metadata_mono payload (see decomp/GAMEPLAY.md, a.a.a.c.a)."""

from __future__ import annotations

from typing import Any

METADATA_FORMAT = "trail_biker_metadata"
METADATA_JSON_VERSION = 2
PAYLOAD_BYTE_COUNT = 240

WHEEL_PROBE_LEN = 21
WHEEL_PROBE_FRONT_START = 19
WHEEL_PROBE_REAR_START = 40
TERRAIN_BLOB_START = 72


def _u8(value: int) -> int:
    return value & 0xFF


def _signed_byte(value: int) -> int:
    if value < -128 or value > 255:
        raise ValueError(f"byte out of range: {value}")
    return value - 256 if value > 127 else value


def split_terrain_blobs(data: list[int]) -> tuple[list[int], list[int]]:
    """Split flat payload from index 72 into slope + piece catalog (same as GameCanvas.load)."""
    d = data
    if len(d) < TERRAIN_BLOB_START + 2:
        raise ValueError("metadata too short for terrain blobs")
    n = _u8(d[72]) * _u8(d[73]) + 2
    n2 = TERRAIN_BLOB_START
    if n2 + n > len(d):
        raise ValueError("invalid slope blob length in metadata")
    slope = [_u8(d[n2 + i]) for i in range(n)]
    n2 += n
    if n2 + 2 > len(d):
        raise ValueError("invalid piece catalog header in metadata")
    n3 = _u8(d[n2 + 1]) + 2 * _u8(d[n2]) + 2
    n4 = n3 + (_u8(d[n2 + n3]) + 1)
    if n2 + n4 > len(d):
        raise ValueError("invalid piece catalog length in metadata")
    piece = [_u8(d[n2 + i]) for i in range(n4)]
    tail = n2 + n4
    if tail != len(d):
        raise ValueError(f"metadata length {len(d)} != expected tail {tail}")
    return slope, piece


def flatten_document(doc: dict[str, Any]) -> list[int]:
    data = [0] * PAYLOAD_BYTE_COUNT
    reserved: dict[str, int] = doc.get("_reserved_byte_indices") or {}

    display = doc["display"]
    physics = doc["physics"]
    motorbike = doc["motorbike"]
    timing = doc["timing_ticks"]
    terrain = doc["terrain_level_defaults"]

    scalars: dict[int, int] = {
        1: display["parallax_layer_count"],
        2: display["parallax_band_height_px"],
        3: display["terrain_viewport_height_px"],
        4: display["playfield_baseline_y_px"],
        5: display["tile_size_px"],
        6: display["tiles_sheet_columns"],
        7: display["tiles_sheet_rows"],
        8: display["bike_spawn_world_x_px"],
        9: display["bike_anchor_screen_x_px"],
        10: physics["trick_drift_per_tick"],
        11: physics["hop_impulse"],
        12: motorbike["lean_min"],
        13: motorbike["lean_max"],
        14: motorbike["stunt_lives_total"],
        15: motorbike["stunt_life_recovery_threshold"],
        16: motorbike["bikes_sheet_columns"],
        17: motorbike["bikes_sheet_rows"],
        61: timing["air_trick_gravity_delay_ticks"],
        62: timing["trick_animation_cooldown_ticks"],
        63: timing["stunt_crash_duration_ticks"],
        64: timing["level_start_intro_ticks"],
        65: timing["terrain_scroll_period_ticks"],
        66: timing["parallax_scroll_period_ticks"],
        67: timing["lean_animate_forward_ticks"],
        68: timing["lean_animate_neutral_ticks"],
        69: timing.get("lean_animate_reserved_ticks", 0),
        70: timing["lean_animate_brake_ticks"],
        71: timing["ramp_landing_scroll_numerator"],
    }
    for idx, val in scalars.items():
        data[idx] = _signed_byte(int(val))
    for key, val in reserved.items():
        data[int(key)] = _signed_byte(int(val))

    front = motorbike["wheel_probe_front_y_px"]
    rear = motorbike["wheel_probe_rear_y_px"]
    if len(front) != WHEEL_PROBE_LEN or len(rear) != WHEEL_PROBE_LEN:
        raise ValueError(f"wheel_probe tables must have {WHEEL_PROBE_LEN} entries")
    for i, v in enumerate(front):
        data[WHEEL_PROBE_FRONT_START + i] = _signed_byte(int(v))
    for i, v in enumerate(rear):
        data[WHEEL_PROBE_REAR_START + i] = _signed_byte(int(v))

    slope = [int(x) for x in terrain["slope_metadata_bytes"]]
    piece = [int(x) for x in terrain["tile_piece_catalog_bytes"]]
    blob = slope + piece
    if TERRAIN_BLOB_START + len(blob) != PAYLOAD_BYTE_COUNT:
        raise ValueError(
            f"terrain blobs size {len(blob)} does not fill payload "
            f"(need {PAYLOAD_BYTE_COUNT - TERRAIN_BLOB_START} bytes from index {TERRAIN_BLOB_START})"
        )
    for i, b in enumerate(blob):
        data[TERRAIN_BLOB_START + i] = _signed_byte(b)
    return data


def document_from_flat(data: list[int], source_dat: str = "metadata_mono.dat") -> dict[str, Any]:
    if len(data) != PAYLOAD_BYTE_COUNT:
        raise ValueError(f"expected {PAYLOAD_BYTE_COUNT} bytes, got {len(data)}")
    slope, piece = split_terrain_blobs(data)
    reserved: dict[str, int] = {}
    known = {
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        *range(WHEEL_PROBE_FRONT_START, WHEEL_PROBE_FRONT_START + WHEEL_PROBE_LEN),
        *range(WHEEL_PROBE_REAR_START, WHEEL_PROBE_REAR_START + WHEEL_PROBE_LEN),
        61,
        62,
        63,
        64,
        65,
        66,
        67,
        68,
        69,
        70,
        71,
    }
    for i in range(TERRAIN_BLOB_START):
        if i not in known:
            reserved[str(i)] = data[i]

    return {
        "format": METADATA_FORMAT,
        "version": METADATA_JSON_VERSION,
        "source_dat": source_dat,
        "description": (
            "Global MIDlet parameters (metadata_mono.dat). Values at 19–60 are wheel probe "
            "offsets; bytes from 72 are default slope + tile-piece blobs for TerrainLevel."
        ),
        "display": {
            "parallax_layer_count": data[1],
            "parallax_band_height_px": data[2],
            "terrain_viewport_height_px": data[3],
            "playfield_baseline_y_px": data[4],
            "tile_size_px": data[5],
            "tiles_sheet_columns": data[6],
            "tiles_sheet_rows": data[7],
            "bike_spawn_world_x_px": data[8],
            "bike_anchor_screen_x_px": data[9],
        },
        "physics": {
            "trick_drift_per_tick": data[10],
            "hop_impulse": data[11],
        },
        "motorbike": {
            "lean_min": data[12],
            "lean_max": data[13],
            "stunt_lives_total": data[14],
            "stunt_life_recovery_threshold": data[15],
            "bikes_sheet_columns": data[16],
            "bikes_sheet_rows": data[17],
            "wheel_probe_front_y_px": [data[WHEEL_PROBE_FRONT_START + i] for i in range(WHEEL_PROBE_LEN)],
            "wheel_probe_rear_y_px": [data[WHEEL_PROBE_REAR_START + i] for i in range(WHEEL_PROBE_LEN)],
        },
        "timing_ticks": {
            "air_trick_gravity_delay_ticks": data[61],
            "trick_animation_cooldown_ticks": data[62],
            "stunt_crash_duration_ticks": data[63],
            "level_start_intro_ticks": data[64],
            "terrain_scroll_period_ticks": data[65],
            "parallax_scroll_period_ticks": data[66],
            "lean_animate_forward_ticks": data[67],
            "lean_animate_neutral_ticks": data[68],
            "lean_animate_reserved_ticks": data[69],
            "lean_animate_brake_ticks": data[70],
            "ramp_landing_scroll_numerator": data[71],
        },
        "terrain_level_defaults": {
            "slope_metadata_bytes": slope,
            "tile_piece_catalog_bytes": piece,
        },
        "_reserved_byte_indices": reserved,
    }
