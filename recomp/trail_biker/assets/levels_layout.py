"""Named layout for levels.dat payload (LevelManager / LevelStore)."""

from __future__ import annotations

from typing import Any

LEVELS_FORMAT = "trail_biker_levels"
LEVELS_JSON_VERSION = 2


def _u8(value: int) -> int:
    return value & 0xFF


def _signed_byte(value: int) -> int:
    if value < -128 or value > 255:
        raise ValueError(f"byte out of range: {value}")
    return value - 256 if value > 127 else value


def _dim_pair(high: int, low: int) -> int:
    return _u8(high) * 100 + _u8(low)


def _encode_dim(n: int) -> tuple[int, int]:
    if n < 0 or n > 9999:
        raise ValueError(f"dimension out of range: {n}")
    return n // 100, n % 100


def record_byte_length(columns: int, rows: int) -> int:
    return 6 + columns * rows


def parse_level_offsets(data: list[int]) -> list[int]:
    n = 0
    count = _u8(data[n])
    n += 1
    offsets: list[int] = []
    for _ in range(count):
        offsets.append(n)
        cols = _dim_pair(data[n + 2], data[n + 3])
        row_count = _dim_pair(data[n + 4], data[n + 5])
        n += record_byte_length(cols, row_count)
    if n != len(data):
        raise ValueError(f"levels payload length {len(data)} != parsed end {n}")
    return offsets


def course_from_record(data: list[int], offset: int, index: int) -> dict[str, Any]:
    par_min = _u8(data[offset])
    par_sec = _u8(data[offset + 1])
    columns = _dim_pair(data[offset + 2], data[offset + 3])
    row_count = _dim_pair(data[offset + 4], data[offset + 5])
    n = offset + 2
    header = [_u8(data[n + i]) for i in range(4)]
    n += 4
    grid: list[list[int]] = []
    for _ in range(row_count):
        row = [_u8(data[n + k]) for k in range(columns)]
        n += columns
        grid.append(row)
    return {
        "index": index,
        "label": f"Course {index + 1}",
        "par_time": {"minutes": par_min, "seconds": par_sec},
        "columns": columns,
        "rows": row_count,
        "compact_header_bytes": header,
        "piece_ids": grid,
    }


def document_from_flat(data: list[int], source_dat: str = "levels.dat") -> dict[str, Any]:
    if not data:
        raise ValueError("empty levels payload")
    count = _u8(data[0])
    offsets = parse_level_offsets(data)
    if len(offsets) != count:
        raise ValueError("course count mismatch")
    courses = [course_from_record(data, offsets[i], i) for i in range(count)]
    return {
        "format": LEVELS_FORMAT,
        "version": LEVELS_JSON_VERSION,
        "source_dat": source_dat,
        "description": (
            "Course records from levels.dat: par time (min/sec), compact grid dimensions, "
            "then piece-id rows (0 = empty column). Header bytes match columns/rows encoding "
            "used by TerrainLevel.expand_level_grid."
        ),
        "course_count": count,
        "courses": courses,
    }


def flatten_course(course: dict[str, Any]) -> list[int]:
    par = course["par_time"]
    columns = int(course["columns"])
    row_count = int(course["rows"])
    piece_ids: list[list[int]] = course["piece_ids"]
    if len(piece_ids) != row_count:
        raise ValueError(f"course {course.get('index')}: piece_ids rows != rows")
    for r, row in enumerate(piece_ids):
        if len(row) != columns:
            raise ValueError(f"course {course.get('index')}: row {r} width != columns")
    col_h, col_l = _encode_dim(columns)
    row_h, row_l = _encode_dim(row_count)
    out = [
        _signed_byte(int(par["minutes"])),
        _signed_byte(int(par["seconds"])),
        _signed_byte(col_h),
        _signed_byte(col_l),
        _signed_byte(row_h),
        _signed_byte(row_l),
    ]
    for row in piece_ids:
        for cell in row:
            out.append(_signed_byte(_u8(int(cell))))
    return out


def flatten_document(doc: dict[str, Any]) -> list[int]:
    courses = doc["courses"]
    count = int(doc.get("course_count", len(courses)))
    if len(courses) != count:
        raise ValueError("course_count != len(courses)")
    data: list[int] = [_signed_byte(count)]
    for course in courses:
        data.extend(flatten_course(course))
    return data
