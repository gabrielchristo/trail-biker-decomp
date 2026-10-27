# Level editor (tools)

Ferramentas em torno de `levels.json` / `levels.dat` e do terreno in-game (`TerrainLevel.expand_level_grid`).

## Visualizador expandido (sprites)

Desenha o **mapa expandido** como no jogo: `piece_ids` → grade densa, sprites de `tiles.png` (modo clipped), vazio = cor de tile vazio do perfil (verde no profile 4).

```bash
cd recomp
python tools/level-editor/visualize_expanded.py --course 0
python tools/level-editor/visualize_expanded.py --all --scale 4
```

Saída: `tools/level-editor/course_XX_expanded.png` (gitignore local).

Requer `resources/tiles.png`, `metadata_mono.json`, `levels.json` e venv com pygame-ce.

---

## Estrutura do arquivo de níveis

### `levels.dat` (binário via `BinaryAsset`)

| Offset (por arquivo) | Conteúdo |
|----------------------|----------|
| Payload `[0]` | **N** = número de cursos (`course_count`) |
| Registros | **N** blocos concatenados, um por curso, **sem** tabela de offsets (diferente de `sound.dat`) |

Cada **registro de curso** (bytes consecutivos):

| Bytes | Campo | Significado |
|-------|--------|-------------|
| 0 | `par_min` | Minutos do par (Stunt Cup / referência) |
| 1 | `par_sec` | Segundos do par |
| 2–3 | `col_hi`, `col_lo` | Largura da grade compacta: `columns = col_hi × 100 + col_lo` |
| 4–5 | `row_hi`, `row_lo` | Linhas de `piece_ids`: `rows = row_hi × 100 + row_lo` |
| 6 … 6 + `columns × rows − 1` | grade | Uma linha após a outra, **row-major**: linha 0, linha 1, … |

No JSON (`levels.json`, schema `trail_biker_levels` v2), o mesmo registro vira:

- `par_time.minutes` / `par_time.seconds`
- `columns`, `rows`
- `compact_header_bytes`: `[0, col_lo, 0, row_lo]` quando `col_hi`/`row_hi` são 0 (caso usual: 70 colunas → `[0, 70, 0, 3]`)
- `piece_ids`: matriz **`rows × columns`**, valores **0–255** (0 = célula vazia na pilha)

Ferramenta de conversão: `python tools/levels_dat_to_json.py` (round-trip com o `.dat`).

### O que **não** está no `.dat`

- Sprites, slopes e catálogo de peças vêm de **`metadata_mono.json`** (`terrain_level_defaults.slope_metadata_bytes` + `tile_piece_catalog_bytes`), igual ao MIDlet.
- O `.dat` só guarda **qual peça** (1–11) empilhar em cada coluna da grade compacta.

---

## Grade compacta → mapa expandido

1. Cada **coluna** da grade compacta é uma pilha vertical de **peças** (não tiles soltos).
2. Por coluna, o jogo percorre as linhas de **`piece_ids` de baixo para cima** (última linha = chão, primeira linha = topo da pilha).
3. Célula **`0`**: nada nessa camada (não ocupa largura).
4. Célula **`k`** com **1 ≤ k ≤ 11**: instancia a **peça k** do catálogo global (11 tipos). Cada peça tem largura × altura em **tiles de 12 px** (ver metadata `tile_size_px`) e preenche várias células do mapa expandido com **índices de sprite** (1-based no mapa; `0` = vazio).
5. A largura da coluna compacta avança pela **largura em tiles** da peça colocada (peças são sempre 4 tiles de largura no catálogo atual).
6. O PNG do visualizador mostra esse **`expanded_tile_map`** completo (eixo X = progresso da pista, Y = linhas de tile).

Referência de código: `trail_biker/game/terrain_level.py` → `expand_level_grid`.

---

## `piece_ids` vs jogo (`composedTileId`)

| Valor em `piece_ids` | Significado na grade |
|----------------------|----------------------|
| **0** | Vazio nesta camada da pilha (sem peça) |
| **1–11** | Índice da **peça do catálogo** (mesma ordem abaixo) |

Em runtime, a moto usa sobretudo **`composedTileId`** (1–11): autotile que combina tile de superfície com vizinhos (`matchTilePieceId`). Ao editar níveis, colocar a peça **N** no compacto corresponde à peça **N** do catálogo; na maioria dos trechos planos, **`composedTileId == N`** sobre essa peça.

Sprites **pass-through** (sem colisão sólida): tile indices **20, 21, 22, 23** (parte superior da peça 11). Definidos no fim do blob `tile_piece_catalog_bytes`.

---

## Catálogo de peças (metadata) — referência para `piece_ids`

Peças fixas do jogo (11 entradas). **Índice = valor em `piece_ids`.**  
Tiles listados são **índices no mapa expandido** (sprite `tiles.png` frame = valor − 1).

| ID | Tamanho (tiles) | Sprites (resumo) | Papel in-game | `composedTileId` / gameplay |
|----|-----------------|------------------|---------------|-----------------------------|
| **1** | 4×1 | 1, 1, 1, 1 | Chão plano | **1** — pose normal, terreno padrão |
| **2** | 4×1 | 10, 10, 10, 10 | Faixa de **chegada** | **10** — fim de corrida (`composedTileId == 10` para a simulação) |
| **3** | 4×2 | topo: 0,0,0,**5**; base: 2,3,4,10 | Subida / transição com **tronco** no topo | **3** — superfície baixa; pose 0 ao pousar; limiar de air trick diferente |
| **4** | 4×2 | topo: **9**,0,0,0; base: 10,8,7,6 | Rampa / descida com tile **9** no canto | **4** / **9** na superfície — banco/rampa; pose **16**; regras de colisão madeira |
| **5** | 4×1 | 11, 12, 12, 13 | **Tronco** / obstáculo estreito | **5** — tecla acelerar (**2**) → `beginStunt()` + som 0; wheelie no tronco |
| **6** | 4×2 | blocos 14–16 (banco) | Obstáculo **madeira** | **4** — pose 16; capsizes especiais entre rodas |
| **7** | 4×2 | espelho da peça 6 | Variante do banco | **4** / **7** — mesmo grupo “madeira” nas regras de pose |
| **8** | 4×1 | 1, 1, 1, 1 | Chão plano (duplicata da peça 1) | **1** / **8** — grupo “normal” com 5–9 em `switch` de pose |
| **9** | 4×1 | 17, 17, 17, 17 | Faixa especial (aterro / transição) | **9** — ao pousar pode forçar `minLean`; air trick não punido como em outros tiles |
| **10** | 4×2 | topo: **19**,0,0,0; base: **18**,1,1,1 | Entrada de **rampa** | Usada em sequências de salto; compõe com vizinhos |
| **11** | 4×2 | topo: **20–23**; base: vazia | Topo de rampa (decorativo) | **11** — pass-through em cima; tratamento especial no ar (`inAirTrick`) |

Detalhe de colisão, stunts e sons: [`decomp/GAMEPLAY.md`](../../../decomp/GAMEPLAY.md) (secções *Terreno* e *composedTileId*).

---

## Ordem das linhas em `piece_ids`

```
piece_ids[0][col]     ← topo da pilha (desenhada por cima ao expandir)
piece_ids[1][col]
…
piece_ids[rows-1][col] ← base / chão da pilha na coluna
```

Exemplo: curso 1 no JSON tem `rows: 3` — três camadas empilháveis por coluna; a linha 2 (índice 2) encosta no “solo” do mapa expandido antes das linhas 0–1.

---

## Fluxo rápido (autor)

1. Editar `resources/levels.json` → `courses[i].piece_ids` (e par time se necessário).
2. Validar visualmente: `python tools/level-editor/visualize_expanded.py --course i`.
3. Regenerar `.dat` (quando houver conversor JSON→dat) ou confiar no loader do recomp via `LEVELS_PATH` = JSON.

O recomp carrega só `levels.json` em runtime (`config.LEVELS_PATH`).
