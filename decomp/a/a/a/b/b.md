# `a.a.a.b.b` — Terreno: peças, mapa expandido e colisão

| Decomp | Reconstruct |
|--------|-------------|
| Classe `b` | `TerrainLevel` |
| `a.a.a.b.a` | `ScrollingTileLayer` |
| `a.a.a.b.c` | `SpriteSheet` |

Arquivo decompilado: `b.java`  
Reconstrução legível: `b-reconstruct.java`

---

## Objetivo geral

Esta classe é o **modelo de terreno jogável** do Trail Biker. Ela:

1. **Interpreta blobs binários** embutidos no nível (inclinômetros / slopes e catálogo de **peças de tile** multi-célula).
2. **Expande** a grade compacta do nível (`byte[][]` vindo de `levels.dat`) em um mapa denso `expandedTileMap` com dimensões em tiles.
3. Delega **scroll e desenho** ao [`ScrollingTileLayer`](a-reconstruct.java) (`d`).
4. Expõe **colisão e amostragem** de superfície (`probeTerrainAt`, inclinação, autotile entre vizinhos) e **scroll da moto** (`bikeWorldX`).

O canvas (`a.a.a.c.a`) guarda a instância em `L` e é a principal consumidora: scroll `L.a(±n)`, paint `L.a(graphics)`, altura da moto via `L.e`, tipo de solo `L.char`, alinhamento de rampas `L.a(...)`.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.b.a`](a.java) | `ScrollingTileLayer scrollLayer` — bitmap de tiles visíveis, scroll, `getTileAt`. |
| [`a.a.a.b.c`](c.java) | Passado ao construir o layer (`tiles.png`). |
| [`a.a.a.c.a`](../c/a.java) | Cria `new TerrainLevel(..., array, array2, ...)` com dados do nível; loop de jogo e input. |
| [`a.a.a.c.e`](../c/e.java) | Motocicleta: `p.void` usa `P.a[4] + L.e` (offset vertical do terreno). |
| [`a.a.a.e.a`](../e/a.java) | Metadata indireta via `P.a[3..5, 8]` no canvas ao construir `L`. |

### Formato dos blobs (resumo)

- **array2** (`parseSlopeMetadata`): largura de amostra `for = tileSize / firstByte`, tabela `null[g][b]` de bytes de inclinação por faixa.
- **array3** (`parseTilePieceCatalog`): lista de peças; cada peça tem largura/altura em `[0]`, linhas de pixels de tile; ao final lista `b[]` de ids **transparentes / pass-through** para colisão.
- **array** (`expandLevelGrid`): linha 0 = dimensões codificadas em decimal compacto (`WW`, `HH`); linhas 1..H = ids de peça por coluna; expansão usa o catálogo `int[][][]`.

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `b(...)` (construtor) | `TerrainLevel(...)` | Parse slopes + peças, expande nível (se `array` não nulo), cria `ScrollingTileLayer`. |
| `a(byte[])` (privado) | `parseSlopeMetadata` | Lê tabela de slopes ligada a `tileSize` (`goto`). |
| `if(byte[])` (privado) | `parseTilePieceCatalog` | Carrega definições de peças e lista `passThroughTileIds`. |
| `if(byte[][])` | `expandLevelGrid` | Converte grade de nível em `expandedTileMap` + dimensões `c`×`long`; calcula `levelWidthPx`. |
| `a(int,int)` | `getTileAtPixels` | Tile em coordenadas mundo (px) via col/row no layer. |
| `a(byte,byte,byte)` | `matchTilePieceId` | Autotile: acha id de peça cujo padrão casa com esquerda/centro/direita. |
| `a(int,byte)` | `getSlopeForTile` | Slope da tabela `null` para posição X e tile id. |
| `a(int)` | `scrollBy` | Avança `bikeWorldX`, varre solo à frente, atualiza `surfaceTileId` / `composedTileId` / `cameraGroundOffsetY`, chama scroll no layer. |
| `a(Graphics)` | `paint` | Delega ao layer. |
| `a(int,int,boolean,boolean)` | `probeTerrainAt` | Colisão vertical: encontra tile sob `(x,y)`, compõe id com vizinhos; pode subtrair 128 se “acima” da superfície. |
| `a(byte[][])` | `loadLevel` | `expandLevelGrid` + `setTileMapData` no layer. |
| `if(int)` | `setBikeWorldX` | Reposiciona moto e `initScrollAt` no layer. |
| `a(byte)` (privado) | `isPassThroughTile` | Tile ignora colisão se estiver em `passThroughTileIds`. |
| `a(int,byte,int)` | `findFlatGroundX` | Alinha X em terreno plano do tipo `b` (stunt / pouso). |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `d` | `scrollLayer` | `ScrollingTileLayer`. |
| `for` | `slopeIndexStride` | `tileSize / slopeSampleWidth` (primeiro byte do blob slope). |
| `g` | `slopeTableHeight` | Linhas da tabela `slopeLookup`. |
| `goto` | `tileSizePx` | Tamanho do tile em pixels (público). |
| `byte` | `tilePieceCount` | Quantidade de tipos de peça no catálogo. |
| `e` | `cameraGroundOffsetY` | Offset Y usado para alinhar moto ao chão (público). |
| `f` | `bikeWorldX` | Posição horizontal da moto no mundo (público). |
| `if` | `minBikeWorldX` | Limite inferior de `bikeWorldX` (spawn / margem). |
| `void` | `surfaceTileId` | Tile de superfície atual (byte). |
| `char` | `composedTileId` | Id composto (autotile) sob a roda (público). |
| `do` | `levelWidthPx` | Largura total do nível em pixels. |
| `null` | `slopeLookup` | Tabela de inclinação por segmento. |
| `int` | `tilePieceDefs` | Catálogo 3D de peças (pixels de tile por peça). |
| `i` | `expandedTileMap` | Mapa denso col×row de ids de tile. |
| `c` | `expandedMapWidthTiles` | Largura do mapa expandido. |
| `long` | `expandedMapHeightTiles` | Altura do mapa expandido. |
| `new` | `sourceLevelWidth` | Largura na grade compacta do `.dat`. |
| `h` | `sourceLevelHeight` | Altura na grade compacta. |
| `else` | `passThroughTileCount` | Tamanho da lista de exclusão de colisão. |
| `b` | `passThroughTileIds` | Ids que não contam como sólido em `probeTerrainAt`. |
| `try`, `case` | `RESERVED_ZERO` | Constantes `0` (não usadas no corpo). |
| `a` (static) | `PROBE_TILE_ADJUST` | `Byte.MIN_VALUE` (-128); definido, sem uso no próprio arquivo. |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/world/terrain_level.py` — classe `TerrainLevel`.
- Depende de `ScrollingTileLayer` e `SpriteSheet` já portados.

### 2. Dados de nível

- Ao portar `levels.dat` / `c.c`, produzir os mesmos `array`, `array2`, `array3` que o Java passa ao construtor (bytes Python `list[int]` ou `bytearray`).
- Implementar `parse_slope_metadata`, `parse_tile_piece_catalog`, `expand_level_grid` **linha a linha** antes de otimizar — a expansão usa ordem bottom-up por coluna.

### 3. Colisão

- `probe_terrain_at(x, y, ignore_pass_through, allow_above)` — mesma lógica de laço em degraus de `tileSizePx` e subtração 128.
- `is_pass_through_tile` — membership na lista.
- Canvas port chama os mesmos pontos que `a.a.a.c.a` (rodas, rampas, tile 9/10, etc.).

### 4. Scroll

- `scroll_by(dx)` atualiza `bike_world_x`, recalcula tiles de superfície, depois `scroll_layer.update_scroll_forward(0, dx)` e `update_scroll_reverse(0, dx)`.

### 5. Render

- `paint(surface)` → `scroll_layer.paint(surface)` (converter `Graphics` J2ME para `Surface`).

### 6. Testes

- Fixture mínima: catálogo 1 peça 1×1, nível 2×2, expandir e assert dimensões `expanded_map_*`.
- Colisão: moto sobre tile sólido vs pass-through.
- Opcional: comparar `expanded_tile_map` com dump do Java para um nível real.

Ordem sugerida: `SpriteSheet` → `ScrollingTileLayer` → **`TerrainLevel`** → canvas.
