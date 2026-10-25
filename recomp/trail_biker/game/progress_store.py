import json
from pathlib import Path

from trail_biker.config import SAVES
from trail_biker.game.levels import LevelStore


class ProgressStore:
    def __init__(self, level_store: LevelStore) -> None:
        self.level_store = level_store
        self.path = SAVES / "times.json"
        self.best_times_ms: list[int] = []
        self.unlocked_course_count = 2
        self.best_cup_tier = 0
        self.sound_enabled = True
        self.volume_percent = 100
        self._load()

    def _load(self) -> None:
        count = self.level_store.get_level_slot_count()
        self.best_times_ms = [self.level_store.get_par_time_ms(i) for i in range(count)]
        if not self.path.is_file():
            return
        try:
            data = json.loads(self.path.read_text(encoding="utf-8"))
            self.unlocked_course_count = max(1, int(data.get("unlocked_course_count", 2)))
            self.best_cup_tier = int(data.get("best_cup_tier", 0))
            self.sound_enabled = bool(data.get("sound_enabled", True))
            self.volume_percent = int(data.get("volume_percent", 100))
            saved = data.get("best_times_ms", [])
            for i in range(min(count, len(saved))):
                self.best_times_ms[i] = int(saved[i])
        except (json.JSONDecodeError, OSError, TypeError, ValueError):
            pass

    def save(self) -> None:
        SAVES.mkdir(parents=True, exist_ok=True)
        payload = {
            "unlocked_course_count": self.unlocked_course_count,
            "best_cup_tier": self.best_cup_tier,
            "sound_enabled": self.sound_enabled,
            "volume_percent": self.volume_percent,
            "best_times_ms": self.best_times_ms,
        }
        self.path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
