"""Stunt Cup award screen logic from LevelManager.showMenuScreen case 6 (decomp c)."""

from __future__ import annotations

from trail_biker.game.progress_store import ProgressStore


def apply_stunt_cup_awards(
    progress: ProgressStore,
    lives_lost: int,
) -> tuple[str | None, str | None, bool]:
    """Update cup tier and course unlocks; return (award title, unlock text, changed)."""
    changed = False
    title: str | None = None
    unlock: str | None = None
    unlocked = progress.unlocked_course_count

    if lives_lost == 0:
        if progress.best_cup_tier != 3:
            progress.best_cup_tier = 3
            changed = True
        title = "Gold Cup Awarded!"
        if unlocked < 11:
            if unlocked < 9:
                unlock = "Courses 9, 10 and 11 unlocked."
            elif unlocked == 9:
                unlock = "Courses 10 and 11 unlocked."
            elif unlocked == 10:
                unlock = "Course 11 unlocked."
            progress.unlocked_course_count = 11
            changed = True
    elif lives_lost in (1, 2):
        if progress.best_cup_tier not in (2, 3):
            progress.best_cup_tier = 2
            changed = True
        title = "Silver Cup Awarded!"
        if unlocked < 10:
            if unlocked < 9:
                unlock = "Courses 9 and 10 unlocked."
            elif unlocked == 9:
                unlock = "Course 10 unlocked."
            progress.unlocked_course_count = 10
            changed = True
    elif lives_lost in (3, 4):
        if progress.best_cup_tier == 0:
            progress.best_cup_tier = 1
            changed = True
        title = "Bronze Cup Awarded!"
        if unlocked < 9:
            unlock = "Course 9 unlocked."
            progress.unlocked_course_count = 9
            changed = True

    return title, unlock, changed
