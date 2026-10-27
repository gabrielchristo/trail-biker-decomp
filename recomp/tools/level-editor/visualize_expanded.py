#!/usr/bin/env python3
"""Export expanded course terrain as PNG using in-game tile sprites (TerrainLevel)."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
EDITOR_DIR = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT))
sys.path.insert(0, str(EDITOR_DIR))

import pygame

from trail_biker.assets.game_metadata import GameMetadata
from trail_biker.config import METADATA_PATH
from trail_biker.game.levels import LevelStore

from _level_render import render_course_expanded


def _init_pygame() -> None:
    pygame.init()
    if pygame.display.get_surface() is None:
        flags = getattr(pygame, "HIDDEN", 0)
        pygame.display.set_mode((8, 8), flags=flags)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--course", type=int, default=0, help="course index (0-based)")
    parser.add_argument(
        "--all",
        action="store_true",
        help="export every course in levels.json",
    )
    parser.add_argument(
        "--scale",
        type=int,
        default=4,
        help="PNG upscale factor (logical pixels × scale)",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=EDITOR_DIR,
        help="directory for PNG files",
    )
    args = parser.parse_args()
    if args.scale < 1:
        print("--scale must be >= 1", file=sys.stderr)
        sys.exit(1)
    if not METADATA_PATH.is_file():
        print(f"Missing {METADATA_PATH}", file=sys.stderr)
        sys.exit(1)
    _init_pygame()
    metadata = GameMetadata.load(METADATA_PATH)
    level_store = LevelStore()
    count = level_store.get_level_slot_count()
    indices = range(count) if args.all else [args.course]
    args.output_dir.mkdir(parents=True, exist_ok=True)
    for index in indices:
        if index < 0 or index >= count:
            print(f"Course index {index} out of range (0..{count - 1})", file=sys.stderr)
            sys.exit(1)
        logical = render_course_expanded(index, metadata, level_store)
        if args.scale == 1:
            out_img = logical
        else:
            w, h = logical.get_size()
            out_img = pygame.transform.scale(logical, (w * args.scale, h * args.scale))
        out_path = args.output_dir / f"course_{index:02d}_expanded.png"
        pygame.image.save(out_img, str(out_path))
        tw, th = logical.get_size()
        print(
            f"Wrote {out_path} ({tw}×{th} logical, "
            f"{out_img.get_width()}×{out_img.get_height()} px, scale={args.scale})"
        )
    pygame.quit()


if __name__ == "__main__":
    main()
