# `a.a.a.c.e` — Motocicleta (física, pose e desenho)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `e` | `Motorbike` |
| `a.a.a.b.c` | `SpriteSheet` |
| `a.a.a.e.a` | `BinaryAsset` |

Arquivo decompilado: `e.java`  
Reconstrução legível: `e-reconstruct.java`

O construtor referencia *"Motorbike() constructor"* no log de erro ao carregar `bikes.png`.

---

## Objetivo geral

Esta classe modela a **moto do jogador**: inclinação (`currentLean`), **estado de pilotagem** (neutro, acelerar, frear, crash), **animação de manobra no ar** (`inAirTrick`), offsets de **rodas** para colisão (tabelas na metadata), e **desenho** a partir de `bikes.png`.

O canvas (`a.a.a.c.a`, campo `p`) atualiza a cada frame (`tickAnimation`), lê `screenY` / `currentLean` para física e terreno, chama `paint`, e mapeia teclas para `leanForward`, `setNeutral`, `leanBack`, `enterCrash`.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.a`](a.java) | Dono da instância `p`. Posiciona `screenY` com terreno (`L.e`), scroll, colisão via `getWheelProbeOffset`, repaint parcial na largura `spriteWidth`. |
| [`a.a.a.e.a`](../e/a.java) / metadata | `gameMetadata.data[12..13]` limites de lean; `[16..17]` grade do sprite; `[19..39]` e `[40..60]` tabelas de probe; `[67,68,70]` tempos de animação por estado. |
| [`a.a.a.c.f`](f.java) | Se `deviceProfileId != 0`, decrementa limites de lean e ajustes de hitbox. |
| [`a.a.a.b.b`](b.java) | Spawn horizontal: `bikeWorldX` inicial `p.spriteWidth / 2`. |
| `/res/bikes.png` | Sprite sheet da moto (modo clip). |

### Estados (`bikeState` / `c`)

| Valor | Uso no código |
|-------|----------------|
| `0` | Neutro (`do`) |
| `1` | Acelerar / inclinar para frente (`for`) |
| `2` | Frear / inclinar para trás (`try`) |
| `5` | Crash (`if`) — pose fixa, lean zerado |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `e(int, int, a)` (construtor) | `Motorbike` | Posição X/Y, metadata; carrega `bikes.png`; lê limites de lean; `loadWheelProbeTable`; `resetBike`. |
| `for()` | `leanForward` | Se não crash: aumenta `currentLean` até `maxLean`, estado `1`. |
| `do()` | `setNeutral` | Estado `0`, zera tick de animação. |
| `try()` | `leanBack` | Diminui `currentLean` até `minLean`, estado `2`. |
| `if()` | `enterCrash` | Estado `5`, chama `tickAnimation`, zera tick. |
| `new()` | `tickAnimation` | Por estado, após N frames (`metadata`) ajusta lean; em crash fixa pose. |
| `a(Graphics)` | `paint` | Escolhe índice de frame (crash / trick / normal) e desenha com `SpriteSheet`. |
| `a(int, boolean)` | `advanceAirTrick` | Inicia ou avança animação de manobra (`inAirTrick`, `trickFrameIndex`). |
| `a()` | `resetBike` | Lean mínimo, pose padrão, flags de trick, estado neutro. |
| `int()` (privado) | `loadWheelProbeTable` | Preenche `wheelProbeTable[2][21]` da metadata. |
| `a(int)` | `getWheelProbeOffset` | Offset Y da roda `0` ou `1` conforme pose/trick atual. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `c` | `bikeState` | Estado de pilotagem (0/1/2/5). |
| `if` | `animationTick` | Contador de frames dentro do estado para animar lean. |
| `a` | `isActive` | Moto “viva” na pista após `resetBike`. |
| `d` | `bikeSprites` | `SpriteSheet` de `bikes.png`. |
| `b` | `screenX` | Posição X na tela (ancora do sprite). |
| `void` | `screenY` | Posição Y (acompanha terreno + offset). |
| `try` | `spriteWidth` | Largura do frame (colisão / repaint). |
| `g` | `spriteHeight` | Altura do frame. |
| `null` | `poseFrameIndex` | Índice base de pose no sheet (normalmente `+ f`). |
| `char` | `minLean` | Limite inferior de inclinação. |
| `e` | `maxLean` | Limite superior de inclinação. |
| `long` | `currentLean` | Inclinação atual (física / visual). |
| `goto` | `pitchOffset` | Zerado no reset; reservado no fluxo de estados. |
| `for` | `trickDriftX` | Deslocamento horizontal durante trick no ar. |
| `case` | `inAirTrick` | Em animação de manobra aérea. |
| `byte` | `trickFrameIndex` | Frame da sequência de trick. |
| `new` | `unusedNew` | Declarado, **não lido** no arquivo. |
| `f` | `poseVariant` | Deslocamento de índice de pose (`+3` no paint normal). |
| `else` | `wheelProbeTable` | `[wheel][frame]` offsets verticais para amostragem do terreno. |
| `do` | `gameMetadata` | Referência à metadata (`BinaryAsset`). |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/entities/motorbike.py` — classe `Motorbike`.

### 2. Dependências

- `SpriteSheet` para `bikes.png` (`metadata[16]`, `[17]`).
- `BinaryAsset` / `game_metadata` para índices 12–13, 19–60, 67–70.
- `device_profile_id` para o mesmo `--char`/`--e` que `f.a != 0`.

### 3. Loop

- Cada frame do canvas: `motorbike.tick_animation()` (equivalente a `p.new()`).
- Input: mapear teclas para `lean_forward`, `set_neutral`, `lean_back`.
- Colisão: `get_wheel_probe_offset(0|1)` como no canvas (`p.a(0)`, `p.a(1)`).

### 4. Desenho

- `paint(surface)`: índice de frame = `21` se crash; senão `trickFrameIndex` se `in_air_trick`; senão `pose_frame_index + pose_variant + 3`.
- Posição: `(screen_x, screen_y - sprite_height)` como no Java.

### 5. Tricks / ar

- Portar `advance_air_trick(axis, reset)` ligado aos mesmos pontos do canvas (`p.a(c, true)`, decremento de `trickFrameIndex`, `trickDriftX`).

### 6. Testes

- `reset_bike()` → `current_lean == min_lean`, `bike_state == 0`.
- `lean_forward` até cap; `tick_animation` após `metadata[67]` frames incrementa lean.

Implementar após `SpriteSheet` + metadata; em paralelo ao port parcial do canvas.
