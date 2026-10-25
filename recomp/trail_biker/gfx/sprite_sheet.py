import pygame

from trail_biker.config import DEVICE_PROFILE_ID


class SpriteSheet:
    TILE_WRAP_MARKER = 128

    def __init__(
        self,
        source: pygame.Surface,
        grid_columns: int,
        grid_rows: int,
        clipped_mode: bool,
    ) -> None:
        self.clipped_mode = clipped_mode
        self.sheet_width = source.get_width()
        self.sheet_height = source.get_height()
        self.grid_columns = grid_columns
        self.grid_rows = grid_rows
        self.frame_count = grid_rows * grid_columns
        self.frame_height = self.sheet_height // grid_rows
        self.frame_width = self.sheet_width // grid_columns
        self.source_image = source
        self.sliced_frames: list[pygame.Surface] | None = None
        self._frame_surfaces: dict[int, pygame.Surface] = {}
        if not clipped_mode:
            self.sliced_frames = []
            index = 0
            for col in range(grid_columns):
                for row in range(grid_rows):
                    rect = pygame.Rect(
                        col * self.frame_width,
                        row * self.frame_height,
                        self.frame_width,
                        self.frame_height,
                    )
                    frame = pygame.Surface((self.frame_width, self.frame_height), pygame.SRCALPHA)
                    frame.blit(source, (0, 0), rect)
                    self.sliced_frames.append(frame)
                    index += 1

    def frame_surface(self, frame_index: int) -> pygame.Surface:
        cached = self._frame_surfaces.get(frame_index)
        if cached is not None:
            return cached
        surf = pygame.Surface((self.frame_width, self.frame_height), pygame.SRCALPHA)
        if self.clipped_mode:
            col = frame_index // self.grid_rows
            row = frame_index % self.grid_rows
            surf.blit(
                self.source_image,
                (0, 0),
                pygame.Rect(col * self.frame_width, row * self.frame_height, self.frame_width, self.frame_height),
            )
        elif self.sliced_frames is not None:
            surf.blit(self.sliced_frames[frame_index], (0, 0))
        self._frame_surfaces[frame_index] = surf
        return surf

    def draw_frame(self, frame_index: int, target: pygame.Surface, x: int, y: int) -> None:
        if not self.clipped_mode:
            target.blit(self.sliced_frames[frame_index], (x, y))
            return
        clip_x = x
        draw_x = x - (frame_index // self.grid_rows) * self.frame_width
        clip_y = y
        draw_y = y - self.frame_height * (frame_index % self.grid_rows)
        saved = target.get_clip()
        clip_w = self.frame_width
        clip_h = self.frame_height
        if clip_x < 0:
            clip_w += clip_x
            clip_x = 0
        if clip_y < 0:
            clip_h += clip_y
            clip_y = 0
        if DEVICE_PROFILE_ID == 1:
            clip_w += 1
            clip_h += 1
        target.set_clip(pygame.Rect(clip_x, clip_y, clip_w, clip_h))
        target.blit(self.source_image, (draw_x, draw_y))
        target.set_clip(saved)
