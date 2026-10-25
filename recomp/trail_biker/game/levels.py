from trail_biker.assets.game_levels import GameLevels
from trail_biker.config import LEVELS_PATH


def _ubyte(value: int) -> int:
    return value & 0xFF


def _dim_pair(high: int, low: int) -> int:
    return _ubyte(high) * 100 + _ubyte(low)


class LevelStore:
    def __init__(self) -> None:
        if not LEVELS_PATH.is_file():
            raise FileNotFoundError(
                f"Missing {LEVELS_PATH}; run: python tools/levels_dat_to_json.py"
            )
        self.levels_asset = GameLevels.load(LEVELS_PATH)
        self.level_record_offsets: list[int] = []
        self._parse_level_offsets()

    def _parse_level_offsets(self) -> None:
        n = 0
        count = _ubyte(self.levels_asset.data[n])
        n += 1
        self.level_record_offsets = []
        for _ in range(count):
            self.level_record_offsets.append(n)
            cols = _dim_pair(self.levels_asset.data[n + 2], self.levels_asset.data[n + 3])
            rows = _dim_pair(self.levels_asset.data[n + 4], self.levels_asset.data[n + 5])
            n += 6 + cols * rows

    def get_level_column_count(self, index: int) -> int:
        off = self.level_record_offsets[index]
        return _dim_pair(self.levels_asset.data[off + 2], self.levels_asset.data[off + 3])

    def get_level_row_count(self, index: int) -> int:
        off = self.level_record_offsets[index]
        return _dim_pair(self.levels_asset.data[off + 4], self.levels_asset.data[off + 5])

    def build_level_grid(self, course_index: int) -> list[list[int]]:
        cols = self.get_level_column_count(course_index)
        rows = self.get_level_row_count(course_index)
        n = self.level_record_offsets[course_index] + 2
        grid: list[list[int]] = []
        header = [_ubyte(self.levels_asset.data[n + i]) for i in range(4)]
        n += 4
        grid.append(header)
        for _ in range(rows):
            row = [_ubyte(self.levels_asset.data[n + k]) for k in range(cols)]
            n += cols
            grid.append(row)
        return grid

    def get_level_slot_count(self) -> int:
        return _ubyte(self.levels_asset.data[0])

    def get_par_time_ms(self, index: int) -> int:
        off = self.level_record_offsets[index]
        return (_ubyte(self.levels_asset.data[off]) * 60 + _ubyte(self.levels_asset.data[off + 1])) * 1000
