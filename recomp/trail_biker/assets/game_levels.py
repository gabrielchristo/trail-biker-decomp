import json
from pathlib import Path
from typing import Any

from trail_biker.assets.levels_layout import LEVELS_FORMAT
from trail_biker.assets.levels_layout import LEVELS_JSON_VERSION
from trail_biker.assets.levels_layout import document_from_flat
from trail_biker.assets.levels_layout import flatten_document


class GameLevels:
    """levels.dat payload as signed byte array (same as BinaryAsset.data)."""

    def __init__(self, data: list[int]) -> None:
        if not data:
            raise ValueError("levels data must not be empty")
        self.data = data

    @classmethod
    def load(cls, path: Path | str) -> GameLevels:
        path = Path(path)
        raw = json.loads(path.read_text(encoding="utf-8"))
        if isinstance(raw, dict) and raw.get("format") == LEVELS_FORMAT and raw.get("version") == LEVELS_JSON_VERSION:
            return cls(flatten_document(raw))
        if isinstance(raw, dict) and "data" in raw:
            return cls(_coerce_data_list(raw["data"]))
        raise ValueError(f"unrecognized levels JSON schema in {path}")

    @classmethod
    def from_binary_asset_data(cls, data: list[int]) -> GameLevels:
        return cls(list(data))

    def to_document(self, source_dat: str = "levels.json") -> dict[str, Any]:
        return document_from_flat(self.data, source_dat=source_dat)


def _coerce_data_list(values: list[Any]) -> list[int]:
    out: list[int] = []
    for value in values:
        if isinstance(value, bool) or not isinstance(value, int):
            raise TypeError(f"levels byte must be int, got {type(value).__name__}")
        if value < -128 or value > 255:
            raise ValueError(f"levels byte out of range: {value}")
        out.append(value - 256 if value > 127 else value)
    return out
