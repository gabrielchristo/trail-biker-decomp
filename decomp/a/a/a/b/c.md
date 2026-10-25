# `a.a.a.b.c` — Sprite sheet (grade de frames)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `c` | `SpriteSheet` |

Arquivo decompilado: `c.java`  
Reconstrução legível: `c-reconstruct.java`

---

## Objetivo geral

Esta classe encapsula uma **imagem única fatiada em uma grade** de sprites (colunas × linhas). Suporta dois modos:

1. **Pré-fatia** (`clippedMode == false`): no construtor, cada célula vira um `Image` separado; desenho com `drawImage` direto.
2. **Clipe na folha** (`clippedMode == true`): mantém a imagem original; cada frame é desenhado com `setClip` + offset negativo (economia de memória em MIDlets).

Índice de frame `n` percorre a grade em ordem **coluna maior, linha menor**: `col = n / gridRows`, `row = n % gridRows`.

Usada em todo o jogo para **tiles**, **motos**, **dígitos do relógio**, **splash**, **fundo** (`b.d`), etc.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.a.a`](../a/a.java) | `numbers.png` — `1×11`, clipped `true`. |
| [`a.a.a.c.a`](../c/a.java) | `tiles.png`, `splash.png` (splash `1×2`, pré-fatia `false`). |
| [`a.a.a.c.e`](../c/e.java) | `bikes.png` — grid de metadata `P.a[16]` × `P.a[17]`, clipped. |
| [`a.a.a.b.a`](a.java) / [`a.a.a.b.b`](b.java) | Terreno: desenha tile `id - 1` no buffer off-screen. |
| [`a.a.a.b.d`](d.java) | `bg.png` — faixa vertical `1×N` de frames de parallax. |
| [`a.a.a.c.f`](../c/f.java) | Campo estático `f.a` (variante de dispositivo Nokia). Se `== 1`, expande clip em +1 px ao desenhar (workaround de render). |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `c(Image, int, int, boolean)` (construtor) | `SpriteSheet(...)` | Calcula `frameWidth`/`frameHeight`, `frameCount`; pré-fatia ou guarda `sourceImage`. |
| `a(int, Graphics, int, int)` | `drawFrame` | Desenha frame `n` em `(x, y)`. Modo clipe: salva clip, ajusta retângulo se `(x,y)` negativos, opcional +1 px se `TrailBikerMidlet.deviceProfileId == 1`, desenha folha deslocada, restaura clip. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `case` | `gridColumns` | Colunas na folha (eixo X da grade). |
| `byte` | `gridRows` | Linhas na folha (eixo Y da grade). |
| `int` | `frameCount` | `gridColumns * gridRows` — número de frames. |
| `do` | `frameHeight` | Altura de cada célula em pixels (`sheetHeight / gridRows`). Público. |
| `if` | `frameWidth` | Largura de cada célula (`sheetWidth / gridColumns`). Público. |
| `char` | `sheetWidth` | Largura total da imagem. |
| `for` | `sheetHeight` | Altura total da imagem. |
| `a` | `slicedFrames` | Array de imagens pré-cortadas (modo não-clipe). |
| `new` | `sourceImage` | Imagem completa (modo clipe). |
| `try` | `clippedMode` | `true` = desenhar com clip; `false` = usar `slicedFrames`. |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/gfx/sprite_sheet.py` — classe `SpriteSheet`.

### 2. Carregamento

- `pygame.image.load(path).convert_alpha()` (ou `convert()` se sem alpha).
- Mesmos parâmetros: `grid_columns`, `grid_rows`, `clipped_mode`.

### 3. Modo pré-fatia (`clipped_mode=False`)

- No `__init__`, para cada índice `n`, `surface.subsurface(Rect(col * fw, row * fh, fw, fh))` e guardar em `list[Surface]`.
- `draw_frame(n, target, x, y)`: `target.blit(sliced_frames[n], (x, y))`.

### 4. Modo clipe (`clipped_mode=True`)

- Guardar `source_surface` inteira.
- `draw_frame`: calcular `col, row` como no Java; `src_rect = Rect(col * fw, row * fh, fw, fh)`; `dest` em `(x, y)`.
- Se `x < 0` ou `y < 0`, recortar `src_rect` e ajustar dest (equivalente ao ajuste de `if1`/`do1`/`n4`/`n6`).
- Flag de dispositivo: portar `f.a == 1` como config em `settings.py` (`nokia_clip_inset_fix`) se ainda notar buracos entre tiles.

### 5. API pública

- Propriedades `frame_width`, `frame_height`, `frame_count` (usadas por `GameClock`, `TerrainLevel`, moto, etc.).

### 6. Assets

- Caminhos: `res/tiles.png`, `bikes.png`, `numbers.png`, `splash.png`, `bg.png` — mesma grade que no Java (metadata ou literais no construtor).

### 7. Testes

- Carregar `numbers.png` 1×11; `draw_frame(10, ...)` desenha o separador.
- Comparar dimensões `frame_width` com `P.a[5]` / metadata após port do MIDlet.

Primeira dependência gráfica comum a quase todos os módulos — convém portar **antes** ou junto com `GameClock` e `ScrollingTileLayer`.
