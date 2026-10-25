# `a.a.a.b.d` — Fundo em parallax (faixa `bg.png`)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `d` | `ParallaxBackground` |
| `a.a.a.b.c` | `SpriteSheet` |

Arquivo decompilado: `d.java`  
Reconstrução legível: `d-reconstruct.java`

---

## Objetivo geral

Esta classe desenha o **fundo deslocável** do jogo a partir de `bg.png`: uma coluna vertical de `layerCount` frames no [`SpriteSheet`](c-reconstruct.java) (`1 × N`, em geral **pré-fatia** `clippedMode = false` no canvas).

Mantém um **offset de scroll horizontal** e, ao ultrapassar a largura de um segmento, **rotaciona** qual frame cada “faixa” usa — efeito de parallax em camadas. Com uma única camada (`layerCount == 1`), desenha o mesmo frame **duas vezes** lado a lado para loop contínuo.

Não lê física nem tiles; só reage a `scrollBy(±delta)` e `paint`.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.a`](../c/a.java) | Campo `K`. Criado com `new ParallaxBackground("/res/bg.png", P.a[1], false)`. No loop, `K.scrollBy(±n)` junto com o terreno (`L`); `K.paint(graphics, 0, 0)` antes/depois do terreno na composição. |
| [`a.a.a.e.a`](../e/a.java) | `P.a[1]` = número de camadas / linhas na folha de fundo. |
| `/res/bg.png` | Asset; altura da imagem = `layerCount × frameHeight`. |
| [`a.a.a.b.c`](c.java) | Renderiza cada frame via `drawFrame`. |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `d(String, int, boolean)` (construtor) | `ParallaxBackground(...)` | Carrega imagem, cria `SpriteSheet(1, layerCount, clipped)`, inicializa `layerFrameIndices[i]=i`, `scrollOffsetPx=0`, `scrollStepSign=1`, `segmentWidthPx=frameWidth`. |
| `a(int)` | `scrollBy` | Soma `delta` em `scrollOffsetPx`; atualiza `scrollStepSign` se offset ficar negativo; ao atingir ±`segmentWidthPx`, incrementa cada índice de frame (com wrap 0..layerCount-1) e zera offset. |
| `a(Graphics, int, int)` | `paint` | Se `layerCount > 1`: uma faixa por camada em `x = anchorX - scrollOffsetPx + i * scrollStepSign * segmentWidthPx`. Se `layerCount == 1`: dois blits do frame 0 para tiling horizontal. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `new` | `spriteSheet` | Folha `bg.png` (1 coluna × `layerCount` linhas). |
| `do` | `layerCount` | Número de camadas / frames na faixa vertical. |
| `if` | `layerFrameIndices` | Qual índice de frame cada camada exibe; rotaciona no wrap. |
| `a` | `scrollOffsetPx` | Deslocamento horizontal acumulado dentro do segmento atual. |
| `for` | `segmentWidthPx` | Largura de um frame (= `spriteSheet.frameWidth`). |
| `int` | `scrollStepSign` | `1` ou `-1`; sentido da rotação de frames e espaçamento entre camadas no paint. |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/gfx/parallax_background.py` — classe `ParallaxBackground`.

### 2. Construtor

- `SpriteSheet.load("bg.png", grid_columns=1, grid_rows=layer_count, clipped_mode=False)` (igual ao Java `false` no canvas).
- `layer_frame_indices = list(range(layer_count))`.
- `segment_width_px = sprite_sheet.frame_width`.

### 3. `scroll_by(delta)`

- Portar o loop de wrap **literalmente** (sensível a sinal e comparação `<= -segment` / `>= segment`).
- `scroll_step_sign = -1` se `scroll_offset_px < 0` após somar, senão `1`.

### 4. `paint(surface, x, y)`

- `layer_count > 1`: para cada `i`, `draw_frame(layer_frame_indices[i], surface, x - scroll_offset_px + i * scroll_step_sign * segment_width_px, y)`.
- `layer_count == 1`: dois `draw_frame` em `x - scroll_offset` e `x - scroll_offset + scroll_step_sign * segment_width_px`.

### 5. Integração

- Instanciar com `metadata.data[1]` (equivalente a `P.a[1]`).
- Chamar `scroll_by` com o **mesmo delta** passado ao terreno quando o canvas faz `K.a(±n)` e `L.a(±n)`.
- Ordem de desenho: fundo (`K`) → terreno (`L`) → HUD (como no `paint` do canvas).

### 6. Testes

- `layer_count=2`, `scroll_by(segment_width)` → índices `[1,0]` ou rotação equivalente.
- `layer_count=1`, scroll contínuo sem gap visual entre os dois blits.

Depende apenas de `SpriteSheet`; bom candidato a portar logo após o sprite sheet.
