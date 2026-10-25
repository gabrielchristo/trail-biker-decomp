#!/usr/bin/env python3
"""Export sound.json OTA slices to mono 8-bit WAV (square synth; default 4× note length)."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from trail_biker.assets.game_sounds import GameSounds
from trail_biker.assets.nokia_ota_sound import NOKIA_SAMPLE_RATE
from trail_biker.assets.nokia_ota_sound import ota_to_wav_bytes
from trail_biker.config import SOUND_PATH


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--input",
        type=Path,
        default=SOUND_PATH,
        help="sound.json path",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=ROOT / "output" / "sounds",
        help="directory for .wav files",
    )
    parser.add_argument(
        "--duration-scale",
        type=float,
        default=4.0,
        metavar="N",
        help="multiply each note duration by N (default 4 = four times slower)",
    )
    args = parser.parse_args()
    if not args.input.is_file():
        print(f"Missing input: {args.input}", file=sys.stderr)
        sys.exit(1)
    import json

    doc = json.loads(args.input.read_text(encoding="utf-8"))
    sounds = GameSounds.load(args.input)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    for entry in sorted(doc["slices"], key=lambda s: int(s["index"])):
        index = int(entry["index"])
        label = str(entry.get("label", f"sound_{index}"))
        chunk = sounds.sample_data[index]
        wav = ota_to_wav_bytes(
            chunk,
            NOKIA_SAMPLE_RATE,
            sound_index=index,
            duration_scale=args.duration_scale,
        )
        out_path = args.output_dir / f"{index:02d}_{label}.wav"
        out_path.write_bytes(wav)
        print(
            f"{out_path}  ({len(wav)} bytes, {NOKIA_SAMPLE_RATE} Hz mono 8-bit, "
            f"duration_scale={args.duration_scale})"
        )
    print(f"Done: {len(sounds.sample_data)} files in {args.output_dir}")


if __name__ == "__main__":
    main()
