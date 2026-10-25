#!/usr/bin/env python3
"""Print OTA decode trace for each slice in sound.json or sound.dat (no pygame)."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from trail_biker.assets.binary_asset import BinaryAsset
from trail_biker.assets.game_sounds import GameSounds
from trail_biker.assets.nokia_ota_sound import OtaDecodeError
from trail_biker.assets.nokia_ota_sound import describe_chunk
from trail_biker.assets.sound_layout import SOUND_FORMAT
from trail_biker.assets.sound_layout import SOUND_JSON_VERSION
from trail_biker.config import SOUND_PATH


def _load_slices(path: Path) -> list[bytes]:
    if path.suffix.lower() == ".json":
        sounds = GameSounds.load(path)
        return sounds.sample_data
    asset = BinaryAsset(path)
    return GameSounds.from_binary_asset_data(asset.data).sample_data


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--input",
        type=Path,
        default=SOUND_PATH,
        help="sound.json (default) or sound.dat",
    )
    args = parser.parse_args()
    if not args.input.is_file():
        print(f"Missing input: {args.input}", file=sys.stderr)
        sys.exit(1)
    if args.input.suffix.lower() == ".json":
        import json

        raw = json.loads(args.input.read_text(encoding="utf-8"))
        version = raw.get("version")
        if raw.get("format") != SOUND_FORMAT or version not in (1, SOUND_JSON_VERSION):
            print(f"Unexpected JSON schema in {args.input}", file=sys.stderr)
            sys.exit(1)
    slices = _load_slices(args.input)
    failed = False
    for i, chunk in enumerate(slices):
        print(f"--- index={i} len={len(chunk)} hex={chunk.hex()}")
        try:
            print(describe_chunk(chunk, i))
        except OtaDecodeError as exc:
            failed = True
            print(f"DECODE ERROR: {exc}")
        print()
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
