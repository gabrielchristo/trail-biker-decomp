#!/usr/bin/env python3
"""Convert resources/levels.dat to editable levels.json."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from trail_biker.assets.binary_asset import BinaryAsset
from trail_biker.assets.game_levels import GameLevels
from trail_biker.assets.levels_layout import document_from_flat
from trail_biker.config import RESOURCES


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--input",
        type=Path,
        default=RESOURCES / "levels.dat",
        help="levels.dat path",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=RESOURCES / "levels.json",
        help="JSON output path",
    )
    args = parser.parse_args()
    if not args.input.is_file():
        print(f"Missing input: {args.input}", file=sys.stderr)
        sys.exit(1)
    asset = BinaryAsset(args.input)
    doc = document_from_flat(asset.data, source_dat=args.input.name)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(doc, indent=2) + "\n", encoding="utf-8")
    roundtrip = GameLevels.load(args.output).data
    if roundtrip != asset.data:
        print("Round-trip mismatch vs .dat", file=sys.stderr)
        sys.exit(1)
    print(f"Wrote {args.output} ({len(asset.data)} bytes, {doc['course_count']} courses)")


if __name__ == "__main__":
    main()
