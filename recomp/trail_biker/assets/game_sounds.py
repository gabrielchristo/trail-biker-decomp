import json
from pathlib import Path
from typing import Any

from trail_biker.assets.sound_layout import SOUND_FORMAT
from trail_biker.assets.sound_layout import SOUND_JSON_VERSION
from trail_biker.assets.sound_layout import slices_from_flat
from trail_biker.config import RESOURCES


class GameSounds:
    """sound.json manifest: WAV playback paths and optional OTA slices for tooling."""

    def __init__(self, sample_data: list[bytes]) -> None:
        if not sample_data:
            raise ValueError("sound data must contain at least one slice")
        self.sample_data = sample_data

    @classmethod
    def load(cls, path: Path | str) -> GameSounds:
        path = Path(path)
        raw = json.loads(path.read_text(encoding="utf-8"))
        if isinstance(raw, dict) and raw.get("format") == SOUND_FORMAT and _version_ok(raw.get("version")):
            return cls(_slices_from_document(raw))
        raise ValueError(f"unrecognized sound JSON schema in {path}")

    @classmethod
    def load_wav_paths(cls, path: Path | str, resources_dir: Path | None = None) -> list[Path]:
        path = Path(path)
        base = resources_dir or RESOURCES
        raw = json.loads(path.read_text(encoding="utf-8"))
        if not (isinstance(raw, dict) and raw.get("format") == SOUND_FORMAT and _version_ok(raw.get("version"))):
            raise ValueError(f"unrecognized sound JSON schema in {path}")
        playback = raw.get("playback", "ota")
        if playback != "wav":
            raise ValueError(f"sound.json playback={playback!r}; expected 'wav' for file playback")
        paths: list[Path] = []
        for entry in _sorted_slices(raw):
            wav_name = entry.get("wav_file") or _default_wav_name(entry)
            resolved = (base / wav_name).resolve()
            if not resolved.is_file():
                raise FileNotFoundError(f"missing sound wav: {resolved}")
            paths.append(resolved)
        count = int(raw.get("sound_count", len(paths)))
        if len(paths) != count:
            raise ValueError("sound_count != number of wav files")
        return paths

    @classmethod
    def from_binary_asset_data(cls, data: list[int]) -> GameSounds:
        return cls(slices_from_flat(data))


def _version_ok(version: Any) -> bool:
    return version in (1, SOUND_JSON_VERSION)


def _sorted_slices(doc: dict[str, Any]) -> list[dict[str, Any]]:
    return sorted(doc["slices"], key=lambda s: int(s["index"]))


def _default_wav_name(entry: dict[str, Any]) -> str:
    index = int(entry["index"])
    label = str(entry.get("label", f"sound_{index}"))
    return f"{index:02d}_{label}.wav"


def _slices_from_document(doc: dict[str, Any]) -> list[bytes]:
    slices = _sorted_slices(doc)
    count = int(doc.get("sound_count", len(slices)))
    if len(slices) != count:
        raise ValueError("sound_count != len(slices)")
    return [bytes(int(b) & 0xFF for b in entry["ota_bytes"]) for entry in slices]
