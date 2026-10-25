# `a.a.a.b.a` — Faixa de terreno com scroll horizontal (ring buffer)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `a` | `ScrollingTileLayer` |
| `a.a.a.b.c` | `SpriteSheet` |

Arquivo decompilado: `a.java`  
Reconstrução legível: `a-reconstruct.java`

---

## Objetivo geral

Esta classe renderiza o **terreno lateral deslocável** do Trail Biker: mantém um **buffer off-screen** (`Image`) com algumas colunas de tiles a mais que a tela, atualiza esse buffer em **anel** conforme a moto avança ou recua, e desenha o resultado com **scroll horizontal** (incluindo segunda cópia da imagem quando o offset cruza o fim do buffer — efeito wrap).

Cada célula do mapa (`tileMap`) guarda um índice de tile (1-based no sprite; `0` = vazio pintado com cor sólida). Valores com **bit 0x80** setado são “marcados” para ajuste de wrap: o bit é removido e o tile redesenhado.

Não faz colisão nem montagem de nível a partir de `levels.dat` — isso fica em [`a.a.a.b.b`](b.java), que contém um `ScrollingTileLayer` no campo `d` e delega scroll, paint e leitura de tile.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.b.b`](b.java) | **Terreno / nível**. Instancia `new ScrollingTileLayer(...)` com tiles expandidos, chama `updateScrollReverse` / `updateScrollForward`, `paint`, `initScrollAt`, `setTileMapData`, `tileColumnFromPixelX` / `tileRowFromPixelY`, `getTileAt`. |
| [`a.a.a.b.c`](c.java) | **Sprite sheet** (`tiles.png`). Desenha tiles com índice `tileId - 1` para células ≠ 0. |
| [`a.a.a.c.a`](../c/a.java) | Canvas: campo `L` é `b`, não `a` diretamente; todo uso do scroll passa por `L.d.*`. |
| [`a.a.a.c.f`](../c/f.java) | Metadata `P.a[3]`, `P.a[4]`, `P.a[5]`, `P.a[8]` alimentam dimensões e tile size na criação de `b`. |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(int,…)` (construtor) | `ScrollingTileLayer(...)` | Calcula largura/altura do buffer em tiles e pixels, guarda mapa, sprites, limites do nível; chama `initScrollAt(0,0)`. |
| `a(int,int,int,int)` (privado) | `drawTileCell` | Desenha uma célula: tipo 0 → `fillRect` com `emptyTileFillColor`; senão blit do `SpriteSheet` com `tile - 1`. |
| `a()` (privado) | `rebuildEntireBuffer` | Percorre colunas/linhas visíveis do anel e redesenha todas as células no bitmap. |
| `a(boolean)` (privado) | `stripWrapMarkers` | Varre o mapa; onde `tile & 0x80`, subtrai 128 e opcionalmente redesenha a célula no buffer. |
| `if(int,int)` | `updateScrollReverse` | Lógica de scroll ao **recuar** (anel: injeta/remove colunas, ajusta `scrollOffsetPx`, `ringWriteOffsetPx`, `worldColumnIndex`); pode ativar `wrapScrollActive`. |
| `a(int,int)` | `updateScrollForward` | Enquanto `wrapScrollActive`, incrementa offsets de scroll (avanço suave após wrap). |
| `if(boolean)` | `shiftVerticalWindow` | Desloca janela vertical no mapa (`worldRowIndex` ± linhas), repõe estado de scroll (início/fim do nível ou posição salva) e reconstrói buffer. |
| `a(Graphics)` | `paint` | `drawImage` do buffer em `drawAnchorX - scrollOffsetPx` (e cópia deslocada se `scrollOffsetPx >= scrollOffsetSecondary`). |
| `for(int,int)` | `getTileAt` | Retorna `tileMap[col][row]` ou `-1` se índices inválidos / mapa nulo. |
| `if(int)` | `tileColumnFromPixelX` | `pixelX / tileSize`, clampado a `[0, levelWidthTiles)` ou `-1`. |
| `a(int)` | `tileRowFromPixelY` | `pixelY / tileSize`, clampado a `[0, levelHeightTiles)` ou `-1`. |
| `a(byte[][],int,int)` | `setTileMapData` | Troca mapa e dimensões; cria ou limpa `bufferImage`, reseta índices e `initScrollAt(0,0)`. |
| `do(int,int)` | `initScrollAt` | Define scroll inicial em pixels: deriva coluna/linha do mundo, ativa wrap, preenche offsets e chama `rebuildEntireBuffer`. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `goto` | `worldColumnIndex` | Índice de coluna do mapa na borda lógica do ring buffer. |
| `char` | `worldRowIndex` | Primeira linha do mapa desenhada no buffer (scroll vertical). |
| `m` | `columnScanLimit` | Limite superior de coluna usado ao iterar o anel. |
| `l` | `rowScanLimit` | Par de `worldRowIndex` ao deslocar verticalmente. |
| `do` | `ringWriteOffsetPx` | Posição horizontal (px) onde a próxima coluna é composta no buffer. |
| `j` | `scrollOffsetSecondary` | Segundo offset de scroll (par com `scrollOffsetPx` no wrap). |
| `byte` | `scrollOffsetPx` | Offset horizontal exibido na `paint` (`drawAnchorX - scrollOffsetPx`). |
| `if` | `wrapScrollActive` | Modo de scroll “especial” após detecção de borda (fim/início do nível). |
| `b` | `wrapProbeX` | Coordenada X de referência para entrar em `wrapScrollActive`. |
| `g` | `bufferImage` | Bitmap off-screen do trecho de terreno. |
| `long` | `bufferGraphics` | `Graphics` sobre `bufferImage` para composição. |
| `a` | `bufferWidthPx` | Largura do ring buffer em pixels. |
| `f` | `bufferHeightPx` | Altura do buffer em pixels. |
| `d` | `savedScrollOffsetPx` | Scroll salvo ao deslocar verticalmente com wrap ativo. |
| `null` | `savedScrollSecondaryPx` | Par salvo de `scrollOffsetSecondary`. |
| `new` | `bufferColumnCount` | Número de colunas de tiles no anel. |
| `int` | `bufferRowCount` | Linhas de tiles no buffer. |
| `o` | `drawAnchorX` | Ancora X na tela ao desenhar. |
| `n` | `drawAnchorY` | Ancora Y na tela. |
| `k` | `trackWidthPx` | Faixa útil de scroll horizontal (largura da “pista” em px). |
| `h` | `levelWidthTiles` | Largura do mapa em tiles. |
| `void` | `levelHeightTiles` | Altura do mapa em tiles. |
| `i` | `tileMap` | Matriz `[col][row]` de ids de tile (público no uso via `b`). |
| `e` | `tileSprites` | Folha de tiles. |
| `p` | `unusedImage` | Campo `Image` **nunca atribuído** neste arquivo (provável resquício). |
| `try` | `wrapAtFarEdge` | Se true, ao atingir o fim do mapa reposiciona o anel no início. |
| `c` | `emptyTileFillColor` | RGB compacto J2ME para células 0. |

### Constantes

| Original | Sugerido | Valor | Função |
|----------|----------|-------|--------|
| `case` | `TILE_WRAP_MARKER` | `128` | Máscara: tile marcado para processamento de borda. |
| `for` | `NO_TILE` | `-1` | Retorno de `getTileAt` inválido. |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/world/scrolling_tile_layer.py` — classe `ScrollingTileLayer`.
- `trail_biker/world/terrain.py` — port de `b` que compõe o layer (manter separação igual ao Java).

### 2. Superfície off-screen

- `buffer_surface = pygame.Surface((buffer_width_px, buffer_height_px))`.
- `drawTileCell`: `surface.fill(color, rect)` ou `SpriteSheet.draw_frame(tile_id - 1, surface, x, y)`.

### 3. Scroll / ring buffer

- Portar **na ordem** `updateScrollReverse` e `updateScrollForward` com os mesmos campos; são sensíveis a ordem e a off-by-one.
- `paint`: um ou dois `blit` do buffer com `x = draw_anchor_x - scroll_offset_px`; segundo blit em `x + buffer_width_px` quando necessário (equivalente ao segundo `drawImage`).

### 4. Wrap marker 0x80

- Mesma lógica: `if tile & 0x80: tile -= 128` e redesenho da célula.
- Colisão em `b` também subtrai 128 ao marcar tile “aéreo” — manter convenção.

### 5. Coordenadas

- `tile_column_from_pixel_x` / `tile_row_from_pixel_y` com divisão inteira por `tile_size` (de metadata).
- `get_tile_at(col, row)` com bounds check.

### 6. Integração Pygame

- O loop do canvas chama `terrain.scroll_by(dx)` (port de `b.a(int)`) que internamente chama os dois updates no layer.
- Dirty rects: o Java usa `repaint` parcial na faixa de terreno; em Pygame pode blitar só a faixa `P.a[4]` de altura.

### 7. Testes

- Mapa 4×2 fixo em memória, avançar scroll N pixels, assert coluna visível mudou.
- Golden screenshot opcional do buffer após `rebuildEntireBuffer`.

Depende de `SpriteSheet` e de `b` para dados reais de nível; o layer isolado é testável com mapa mínimo.
