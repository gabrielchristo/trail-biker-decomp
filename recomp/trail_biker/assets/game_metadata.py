import json
from pathlib import Path
from typing import Any

from trail_biker.assets.metadata_layout import METADATA_FORMAT
from trail_biker.assets.metadata_layout import METADATA_JSON_VERSION
from trail_biker.assets.metadata_layout import document_from_flat
from trail_biker.assets.metadata_layout import flatten_document


class GameMetadata:
    """Global game parameters (port of J2ME metadata_mono.dat payload)."""

    def __init__(self, data: list[int]) -> None:
        if not data:
            raise ValueError("metadata data must not be empty")
        self.data = data

    @classmethod
    def load(cls, path: Path | str) -> GameMetadata:
        path = Path(path)
        raw = json.loads(path.read_text(encoding="utf-8"))
        if isinstance(raw, list):
            data = [_coerce_signed(v) for v in raw]
            return cls(data)
        if not isinstance(raw, dict):
            raise ValueError(f"unexpected metadata JSON in {path}")
        if raw.get("format") == METADATA_FORMAT and raw.get("version") == METADATA_JSON_VERSION:
            data = flatten_document(raw)
            return cls(data)
        if "data" in raw:
            data = [_coerce_signed(v) for v in raw["data"]]
            return cls(data)
        raise ValueError(f"unrecognized metadata JSON schema in {path}")

    @classmethod
    def from_flat_bytes(cls, data: list[int]) -> GameMetadata:
        return cls(list(data))

    def to_document(self, source_dat: str = "metadata_mono.json") -> dict[str, Any]:
        return document_from_flat(self.data, source_dat=source_dat)

    def unsigned_byte(self, index: int) -> int:
        return self.data[index] & 0xFF

    def uint16(self, index: int) -> int:
        return 256 * self.unsigned_byte(index) + self.unsigned_byte(index + 1)


def _coerce_signed(value: Any) -> int:
    if isinstance(value, bool) or not isinstance(value, int):
        raise TypeError(f"metadata entry must be int, got {type(value).__name__}")
    if value < -128 or value > 255:
        raise ValueError(f"metadata byte out of range: {value}")
    if value > 127:
        return value - 256
    return value
