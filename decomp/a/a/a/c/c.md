# `a.a.a.c.c` — Fluxo de jogo, menus e progressão (LevelManager)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `c` | `LevelManager` |
| `a.a.a.c.f` | `TrailBikerMidlet` |
| `a.a.a.c.a` | `GameCanvas` |
| `a.a.a.e.a` | `BinaryAsset` |

Arquivo decompilado: `c.java`  
Reconstrução legível: `c-reconstruct.java`

---

## Objetivo geral

Esta classe é o **controlador de fluxo** entre menus MIDP e a partida:

- Carrega **`levels.dat`** e monta índices para cada percurso.
- Gerencia **modo de jogo** (`gameMode`: Practice `0`, Time Attack `1`, Stunt Cup `2`).
- Mantém **recordes** e progressão (cursos desbloqueados, copa bronze/prata/ouro) em **RMS** (`RecordStore` `"times"`).
- Constrói **listas e forms** (menu principal, escolha de pista, pause, instruções, high scores, prêmios de copa).
- Dono do **`GameCanvas`** (`int` no decomp): mostra/esconde o canvas, repassa pausa do MIDlet, processa **fim de corrida** (`onRaceFinished`).

Implementa `CommandListener` para OK / Back / Exit e seleção de listas.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.f`](f.java) | `TrailBikerMidlet` (`for`). Recebe no construtor; `destroyApp` no Exit; metadata global em `midlet.gameMetadata`. |
| [`a.a.a.c.a`](a.java) | `gameCanvas`. Criado aqui; `showCanvas`, `pauseGameCanvas`, `enterPlayMode` → `a()` / `goto()`; fim de fase chama `onRaceFinished` com tempo. |
| [`a.a.a.e.a`](../e/a.java) | `levelsAsset` — `/res/levels.dat`. |
| [`a.a.a.c.f`](f.java) | `TrailBikerMidlet.deviceProfileId` — quebra de linha em high scores / instruções. |
| RMS `"times"` | Persistência: nível desbloqueado, copa, som, percent, tempos por curso. |

### `menuScreenId` (`long` no decomp)

| Valor | Tela |
|-------|------|
| `0` | Jogo (canvas visível) |
| `1` | Menu principal |
| `2` | Seleção de curso |
| `3` | Continue / New / Instructions |
| `4` | High scores |
| `5` | Instruções |
| `6` | Prêmio de copa (Stunt Cup) |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `c(f)` (construtor) | `LevelManager` | Cria canvas, carrega levels, RMS, comandos, abre menu. |
| `for()` (privado) | `parseLevelOffsets` | Lê cabeçalho de `levels.dat` e tabela `levelRecordOffsets[]`. |
| `do(int)` | `getLevelColumnCount` | Largura da grade do nível `n`. |
| `a(int)` | `getLevelRowCount` | Altura da grade do nível `n`. |
| `case()` | `getLevelSlotCount` | Byte 0 do dat = número de percursos no arquivo. |
| `if()` | `getUnlockedLevelCount` | Retorna `unlockedCourseCount` (`d`). |
| `if(int)` | `getTargetTimeMs` | Modo 2: par do dat; senão recorde RMS. |
| `char()` | `getCurrentTargetTimeMs` | `getTargetTimeMs(selectedCourseIndex)`. |
| `int(int)` | `getParTimeFromDat` | Tempo alvo embutido no nível (min/seg → ms). |
| `for(int)` | `getBestTimeMs` | Recorde armazenado para o curso. |
| `do()` | `buildLevelGrid` | `byte[][]` do curso atual para `TerrainLevel`. |
| `a(long)` | `onRaceFinished` | Atualiza recorde, avança Stunt Cup, salva RMS, abre menu/copa. |
| `byte()` (privado) | `loadProgressFromRms` | Inicializa tempos padrão + lê `"times"`. |
| `goto()` (privado) | `saveProgressToRms` | Regrava `"times"`. |
| `else()` | `showMenuScreen` | Monta List/Form conforme `menuScreenId`. |
| `commandAction` | `commandAction` | Trata menus e navegação. |
| `new()` (privado) | `enterPlayMode` | Inicia ou retoma partida no canvas. |
| `int()` | `pauseGameCanvas` | Fecha sessão de jogo (`gameCanvas.do()`), ajusta menu. |
| `a()` (privado) | `showCanvas` | `setCurrent(gameCanvas)`. |
| `try()` | `handleMidletPause` | Pausa: overlay de menu se ainda não pausado. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `c` | `levelRecordOffsets` | Offset de cada nível em `levels.dat`. |
| `if` | `bestTimesMs` | Melhores tempos por curso (ms). |
| `d` | `unlockedCourseCount` | Quantos cursos estão liberados (progressão). |
| `new` | `selectedCourseIndex` | Curso selecionado / atual. |
| `byte` | `levelsAsset` | `BinaryAsset` de `levels.dat`. |
| `null` | `unusedLevelRef` | Campo **nunca usado** no decomp. |
| `e` | `unusedFlagE` | Boolean público **não lido** no arquivo. |
| `b` | `menuList` | `List` MIDP atual (ou null). |
| `long` | `menuScreenId` | Id da tela de menu (ver tabela). |
| `a` | `gameMode` | 0 Practice, 1 Time Attack, 2 Stunt Cup. |
| `goto` | `bestCupTier` | 0 nenhuma, 1 bronze, 2 silver, 3 gold. |
| `for` | `midlet` | Referência ao `TrailBikerMidlet`. |
| `int` | `gameCanvas` | Instância do jogo. |
| `do` | `cmdOk` | Command "OK". |
| `void` | `cmdBack` | Command "Back". |
| `f` | `cmdExit` | Command "Exit". |
| `else` | `hideContinueItem` | Se true, menu não mostra "Continue". |
| `try` | `soundEnabled` | Flag salva no RMS. |
| `case` | `volumePercent` | Volume (default 100). |
| `char` | `pauseOverlayActive` | Evita reentrância em pausa. |

---

## Port — estratégia Python + pygame

### 1. Módulo

- `trail_biker/game/level_manager.py` — classe `LevelManager`.
- `trail_biker/game/menu_screens.py` — estados 1–6 (pygame UI ou texto simples no primeiro protótipo).

### 2. Dados

- `BinaryAsset("levels.dat")` + mesma lógica de `parseLevelOffsets`, `buildLevelGrid`, `getParTimeFromDat`.
- Substituir RMS por `save.json` ou `~/.trail-biker/times` com mesmo layout de bytes (ou JSON explícito).

### 3. Estado

- `menu_screen_id`, `game_mode`, `selected_course_index`, `unlocked_course_count`, `best_cup_tier`, `best_times_ms[]`, `sound_enabled`.

### 4. Loop principal

```text
if menu_screen_id == 0: game_canvas.update/draw
else: menu_ui.handle_input → transições iguais a commandAction
```

### 5. `onRaceFinished(elapsed_ms)`

- Portar ramos `game_mode == 2` (vidas/copa, `gameCanvas.x` / `I`), unlock de cursos 9–11, `saveProgress`.

### 6. Integração

- `TrailBikerMidlet` / `main` cria `LevelManager(midlet)`.
- Pausa: `handleMidletPause()` como no MIDlet.

### 7. Testes

- Parse de `levels.dat` real → número de offsets == `data[0]`.
- Round-trip RMS/json de um tempo fictício.

Prioridade: após `BinaryAsset` e esboço de `GameCanvas`; bloqueia menu jogável até existir fluxo mínimo de `enterPlayMode`.
