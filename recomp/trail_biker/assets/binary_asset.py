from pathlib import Path


def _signed(b: int) -> int:
    return b if b < 128 else b - 256


class BinaryAsset:
    FORMAT_VERSION = 1

    def __init__(self, path: Path | str) -> None:
        path = Path(path)
        raw = path.read_bytes()
        size = (raw[0] << 8) | raw[1]
        version = raw[2]
        if version != self.FORMAT_VERSION:
            raise ValueError(f"unexpected format version {version}")
        payload = raw[3 : 3 + size]
        self.data = [_signed(b) for b in payload]

    def unsigned_byte(self, index: int) -> int:
        b = self.data[index]
        return b & 0xFF

    def uint16(self, index: int) -> int:
        return 256 * self.unsigned_byte(index) + self.unsigned_byte(index + 1)
