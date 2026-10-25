# `a.a.a.c.d` — Constantes do jogo (tabela compilada)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `d` | `GameConstants` |

Arquivo decompilado: `d.java`  
Reconstrução legível: `d-reconstruct.java`

---

## Objetivo geral

Classe **`final` utilitária** com **apenas campos `static final`** (boolean, int, byte) e construtor privado — padrão de “constant pool” Java usado no código fonte original antes da ofuscação/compilação.

No decomp **nenhum outro arquivo referencia `a.a.a.c.d`** (sem `import` nem `d.`). Os valores provavelmente foram **inlined** nos `.class` ou o Procyon não recuperou os usos. Ainda assim, muitos inteiros **coincidem** com índices em `metadata_mono.dat` (`P.a[…]` no canvas) e com **magic numbers** em `a.java` / `e.java`.

Serve como **mapa de nomes** ao portar para Python: substituir literais espalhados por constantes nomeadas.

### Interações (por valor, não por referência direta)

| Onde | Relação |
|------|---------|
| [`a.a.a.c.a`](a.java) | `P.a[61..66, 71..73]`, máscaras `H & 1/2/4`, `case 50` no input, tiles `L.char` 9/10/11. |
| [`a.a.a.c.e`](e.java) | `gameMetadata.data[67, 68, 70]` — animação de lean (nomes `null`, `aC`, `a9` nesta classe). |
| [`a.a.a.c.c`](c.java) | Modos `a == 0/1/2` (treino, contra-relógio, etc.) — ver blocos `ae`…`I`. |
| [`Motorbike`](e-reconstruct.java) | Frame de crash `21` (`d` = 21). |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `d()` (privado) | `GameConstants()` | Impede instanciação; só há membros estáticos. |

Não há métodos de instância.

---

## Variáveis (grupos sugeridos)

### Flags booleanas (features / debug)

| Decomp | Reconstruct (sugestão) | Valor |
|--------|------------------------|-------|
| `e`, `H`, `aM`, `if`, `aT` | `FLAG_*` | `false` |
| `ai`, `ay`, `aS`, `aR` | `ENABLE_*` | `true` |
| `y` | `FLAG_Y` | `false` |

Sem uso no decomp; tratar como toggles de build até aparecer referência.

### Modos / categorias (0…4)

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `ae` | `MODE_INDEX_0` | 0 |
| `aj` | `MODE_INDEX_1` | 1 |
| `ac` | `MODE_INDEX_2` | 2 |
| `R` | `MODE_INDEX_3` | 3 |
| `I` | `MODE_INDEX_4` | 4 |

### Escala / tempo

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `a2` | `PERCENT_SCALE` | 100 |
| `ag` | `CONST_4` | 4 |
| `a3` | `CONST_20` | 20 |
| `bc` | `PERCENT_MAX` | 100 |

### Índices em `metadata_mono.dat` (`P.a[i]`)

| Decomp | Reconstruct | Valor | Uso conhecido |
|--------|-------------|-------|----------------|
| `u` | `META_SCROLL_DELAY` | 61 | Canvas `void` |
| `aH` | `META_STUNT_TICKS` | 62 | Canvas `t` |
| `aX` | `META_STUNT_HOLD` | 63 | Contador `X` |
| `a6` | `META_CRASH_FRAMES` | 64 | Fim de animação crash |
| `s` | `META_PHYS_PARAM_65` | 65 | `s` scroll |
| `o` | `META_PHYS_PARAM_66` | 66 | `y` scroll |
| `null` | `META_LEAN_ANIM_FORWARD` | 67 | Motorbike estado 1 |
| `aC` | `META_LEAN_ANIM_NEUTRAL` | 68 | Motorbike estado 0 |
| `int` | `META_IDX_69` | 69 | — |
| `a9` | `META_LEAN_ANIM_BACK` | 70 | Motorbike estado 2 |
| `w` | `META_PHYS_PARAM_71` | 71 | Física scroll |
| `aF`, `aI` | `META_LEVEL_W_MUL`, `META_LEVEL_W_PAD` | 72 | Tamanho blob nível |
| `ad` | `META_LEVEL_H_MUL` | 73 | Tamanho blob nível |

### Tipos de tile / superfície (bytes)

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `n`…`case` | `SURFACE_TILE_0` … `SURFACE_TILE_11` | 0–11 |
| `D`, `byte` | `KEY_CODE_26`, `KEY_CODE_47` | teclas MIDP |

### Input

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `for` | `KEY_CODE_50` | 50 — `switch` no canvas (pulo / terreno) |
| `char`, `aw` | `INVALID_INDEX` | -1 |

### Motorbike / UI

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `d` | `BIKE_CRASH_FRAME` | 21 |
| `O` | `CONST_40` | 40 |

### Máscaras de repaint (`H & bit`)

| Decomp | Reconstruct | Valor |
|--------|-------------|-------|
| `bf` | `REPAINT_BIT_1` | 1 |
| `be` | `REPAINT_BIT_2` | 2 |
| `aW` | `REPAINT_BIT_4` | 4 |
| `x` | `REPAINT_BIT_8` | 8 |
| `goto` | `REPAINT_FLAG_A` | 1 |
| `P` | `REPAINT_FLAG_TERRAIN` | 4 |
| `F` | `REPAINT_FLAG_BIKE` | 8 |

(Valores duplicados entre grupos refletem o decomp; unificar no port Python após validar.)

### Demais inteiros (`c`, `aq`, `f`, `b`, `p`…`m`, `aU`…`a5`)

Prováveis **estados de menu**, **tipos de fase**, **comandos** — manter no reconstruct com prefixo `CONST_` + nome decomp até mapear em `c.java`.

---

## Port — estratégia Python + pygame

### 1. Módulo

- `trail_biker/constants.py` ou `trail_biker/game_constants.py` — espelho de `GameConstants`.

### 2. Não portar a classe Java literalmente

- Usar `enum.IntEnum` / `Final` (typing) para:
  - `MetadataIndex` (61–73, 67–70)
  - `BikeState`, `SurfaceTile`, `KeyCode`
  - `RepaintFlags`

### 3. Migração incremental

1. Ao portar `Motorbike`, trocar `data[67]` por `MetadataIndex.LEAN_ANIM_FORWARD`.
2. Ao portar canvas, trocar `P.a[64]` por `metadata[MetadataIndex.CRASH_FRAMES]`.
3. Documentar novos índices neste `d.md` quando descobertos.

### 4. Flags booleanas

- Ignorar no port até provar efeito; ou mapear para `settings.py` (`ENABLE_AI`, etc.).

### 5. Testes

- Apenas assert de consistência (`BIKE_CRASH_FRAME == 21`, índices únicos onde esperado).

Arquivo de baixa prioridade para runtime, **alta prioridade** para legibilidade do port.
