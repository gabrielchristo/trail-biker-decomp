# Trail Biker — Jogabilidade original (J2ME)

Documento derivado do bytecode decompilado em `decomp/a/a/a/` (reconstruções `*-reconstruct.java` e notas `*.md`). Descreve o comportamento **como implementado no MIDlet Nokia**, não o port Python.

---

## Visão geral

**Trail Biker** é um side-scroller de moto trial em terreno 2,5D: a moto avança automaticamente (scroll horizontal) enquanto o jogador inclina para frente/trás, freia e faz *bunny hop* / *wheelie* no ar. Cada **curso** é um percurso em `levels.dat` (11 cursos no asset atual). O objetivo varia por **modo de jogo**: treino livre, bater recorde contra countdown, ou completar a **Stunt Cup** em sequência com vidas limitadas.

Classes principais:

| Papel | Pacote decomp | Reconstruct |
|-------|-------------|-------------|
| MIDlet | `a.a.a.c.f` | `TrailBikerMidlet` |
| Menus / progressão | `a.a.a.c.c` | `LevelManager` |
| Partida | `a.a.a.c.a` | `GameCanvas` |
| Moto | `a.a.a.c.e` | `Motorbike` |
| Terreno | `a.a.a.b.b` | `TerrainLevel` |
| Relógio HUD | `a.a.a.a.a` | `GameClock` |
| Som | `a.a.a.d.a` | `SoundManager` |

Timer de simulação: **100 ms** por tick (`CanvasGameLoopTask` → `onTimerTick` → `run()`).

---

## Modos de jogo (`LevelManager.gameMode`)

| Valor | Nome (UI) | Relógio | Vidas | Objetivo |
|-------|-----------|---------|-------|----------|
| `0` | **Practice** | Contagem **crescente** a partir de 0 (centésimos visíveis) | Ilimitadas (stunts não encerram a campanha) | Completar o curso; tentar **melhor tempo** vs recorde salvo |
| `1` | **Time Attack** | **Countdown** a partir do recorde atual do curso (`bestTimesMs`) | Ilimitadas | Terminar o curso **antes** do countdown chegar a zero; opcionalmente **New Best Time** |
| `2` | **Stunt Cup** | **Countdown** a partir do **par** do curso em `levels.dat` (`getParTimeFromDat`) | **4 vidas** (`metadata_mono[14]`; texto de instruções) | Completar **8 cursos em sequência** (índices `0`–`7`) dentro do par de cada um; ao terminar o 8.º curso com tempo restante, tela de **prêmio de copa** |

Constantes de modo e strings: `a.a.a.c.b` / `UiStrings` (`INSTRUCTIONS_BODY`).

### Practice (`gameMode == 0`)

- `GameClock.reset(false, 0, true)` — tempo sobe até teto `599990` ms (~9:59,99).
- A simulação continua enquanto `composedTileId != 10` e `displayTimeMs < 599990`.
- Ao fim: se `displayTimeMs >= 599990` → **Out of Time!**; senão, se `displayTimeMs < getCurrentTargetTimeMs()` (recorde) → **New Best Time!**; sempre **Course Complete!** (flags acumuladas).
- `onRaceFinished` recebe `elapsed_ms = displayTimeMs` (tempo decorrido).

### Time Attack (`gameMode == 1`)

- Countdown inicial = `getBestTimeMs(curso)` (recorde RMS; default = par do `.dat` em save novo).
- Vitória na linha de chegada com `displayTimeMs > 0`: **New Best Time!** se ainda há tempo no relógio; senão só **Course Complete!**
- `displayTimeMs == 0` antes de chegar → **Out of Time!**
- `onRaceFinished`: `elapsed_ms = target - displayTimeMs`.

### Stunt Cup (`gameMode == 2`)

- Ao escolher **Stunt Cup** no menu principal: `selectedCourseIndex = 0`, menu **Continue / New Game / Instructions** (tela 3).
- **New Game**: `resetStuntLives()` (`stuntLivesLost = 0`, `stuntLivesTotal = metadata[14]`).
- Countdown = **par oficial** do curso (`getParTimeFromDat`), não o recorde pessoal.
- Condição de “passou no tempo” ao cruzar a meta (`composedTileId == 10`):

  `getBestTimeMs(curso) - displayTimeMs < getParTimeFromDat(curso)`

  Com recorde inicial = par, equivale a terminar com **tempo restante > 0** no countdown.

- Cada **stunt/crash** (`beginStunt`) incrementa `stuntLivesLost`. Com `stuntLivesLost >= stuntLivesTotal` → **Game Over** (flag 1) e fluxo de fim trata como derrota.
- HUD: ícones em `lives.png` — quantidade exibida = `stuntLivesTotal - stuntLivesLost - 1` (canto superior direito).
- Ao completar curso **com tempo** (`n > 0` em `onRaceFinished`) e ainda com vidas:
  - Cursos **1–7** (índice `< 7`): avança `selectedCourseIndex++`, chama `tickStuntCupLife()`, entra direto na próxima corrida (`enterPlayMode`).
  - **8.º curso da copa** (índice `7` concluído com sucesso na sequência até índice `7`, ou seja após completar o curso 8): `menuScreenId = 6` — formulário de **Gold/Silver/Bronze** e desbloqueio de cursos **9–11**.
- Se falhar (sem tempo ou sem vidas): volta ao menu 3 (Continue / New Game / …).

#### Prêmio de copa (menu 6)

Baseado em `stuntLivesLost` **no fim da copa** (quantas vidas “gastas”):

| `stuntLivesLost` | Prêmio | `bestCupTier` | Desbloqueio típico |
|------------------|--------|---------------|-------------------|
| `0` | Gold Cup Awarded! | `3` | Cursos 9, 10 e 11 (ajusta `unlockedCourseCount` até `11`) |
| `1`–`2` | Silver Cup Awarded! | `2` (se ainda não Gold) | 9–10 ou só 10, conforme progresso |
| `3`–`4` | Bronze Cup Awarded! | `1` (se ainda não tinha copa) | Curso 9 se ainda bloqueado |

Mensagens exatas: `UiStrings` (`AWARD_*`, `UNLOCK_MSG_*`).

#### `tickStuntCupLife()` (entre cursos)

Se `stuntLivesTotal - stuntLivesLost < metadata[15]`, executa `--stuntLivesLost` (recupera uma vida “perdida” na contagem interna — limiar definido na metadata global).

---

## Menus e fluxo (`menuScreenId`)

| ID | Tela |
|----|------|
| `0` | Jogo (`GameCanvas` visível) |
| `1` | Menu principal: Practice, Time Attack, Stunt Cup, High Scores (+ Exit) |
| `2` | Select Course (só cursos `1 .. unlockedCourseCount`) |
| `3` | Continue / New Game / Instructions (Back) |
| `4` | High Scores (Form) |
| `5` | Instructions (Form) |
| `6` | Prêmio Stunt Cup (Form) |

**Practice / Time Attack**: Main → modo → escolha curso → menu 3 → New Game ou Continue.

**Stunt Cup**: Main → Stunt Cup → menu 3 (sem escolher curso; sempre sequência desde o curso 1).

**Continue**: `hideContinueItem == false` mostra item **Continue**; `enterPlayMode()` **sem** `startLevel()` — só religa o timer (`startGameLoopTimer()`), mantendo estado da corrida pausada.

**Pausa in-game**: soft keys / tecla `0` → `handleMidletPause()` → `pauseGameCanvas()` + menu 3. Perder foco do MIDlet (`pauseApp`, `hideNotify`) faz o mesmo, salvo transição splash (`suppressPauseOnce`).

**High Scores**: lista cada curso desbloqueado com melhor tempo `MM:SS'CC` e, se houver, **Best Cup** (Bronze / Silver / Gold).

Strings **Settings** (Sound On/Off, Lights On/Off) existem em `UiStrings`, mas o **menu principal decompilado não inclui Settings**; `soundEnabled` e `volumePercent` são persistidos no RMS e usados no jogo (`soundEnabled` gateia `playSound`; `volumePercent` vai para `DeviceControl.setLights(0, volumePercent)` ao iniciar o loop).

---

## Controles (teclado Nokia)

Mapeamento em `GameCanvas.keyPressed` / `keyReleased`:

| Tecla | Código | Ação |
|-------|--------|------|
| **2** | `50` | **Accelerate** — `leanForward()`; exceção: se `composedTileId == 5` (tronco/log), chama **`beginStunt()`** e som **0** |
| **5** | `53` | **Brake** — `leanBack()` (não durante `inAirTrick`) |
| **1** ou **3** | `49`, `51` | **Bunny hop / wheelie no ar** — `advanceAirTrick(hopImpulse, true)`; reseta `trickHoldTicks` |
| **0**, soft **Left/Right/Center** | `48`, `-5`, `-6`, `-7` | Splash: avança para jogo + abre menu; in-game: **pausa** |
| (soltar tecla) | `keyReleased` | `setNeutral()` — estado neutro da moto |

Instruções oficiais (`INSTRUCTIONS_BODY`):

- Não acelerar em **logs**; pousar em **duas rodas** ou **roda traseira** para não perder o equilíbrio.
- **Cup Mode**: bater o tempo em cada curso para avançar; **quatro vidas**.
- **Practice**: recordes por curso; vidas infinitas.
- **Time Attack**: bater seu melhor tempo; vidas infinitas.

---

## Ciclo de vida de uma partida

### Splash

- Ao criar o canvas: `splashPhase = 1`; folha `splash.png` (2 frames).
- A cada **> 30 ticks** de 100 ms: fase `1 → 0 → dismiss` (abre menu via `dismissSplashOrOpenPause`).
- Até carregar recursos: primeiro dismiss chama `loadGameResources()` + `resetRoundState()`.

### `startLevel()` (New Game)

1. Carrega grade do curso (`buildLevelGrid()` → `terrain.loadLevel`).
2. Reposiciona moto (`metadata[8]`, spawn).
3. `configureGameClock()` conforme modo.
4. **`crashAnimActive = true`** por **`metadata[64]` ticks** (~2 s com valor típico `20`): durante isso **não** roda `gameClock.tick()` nem física normal; o relógio **também não avança** nesse bloco.
5. `resetRoundState()` — inclui flag de banner inicial **Course N** (`addEndGameFlag(16)`), scroll inicial, parâmetros de scroll/trick da metadata.

### Loop principal (`run()`)

Ordem resumida (com `splashPhase == -1` e após intro `crashAnimActive`):

1. Sequência de mensagens de fim (`endGameFlags`) com pausa do relógio e `endGameAnimTimer > 20` entre mensagens.
2. `gameClock.tick()` + `motorbike.tickAnimation()`.
3. **Scroll**: acumuladores de terreno e parallax; velocidade efetiva ligada a `currentLean` e, em pouso pós-stunt, a `metadata[71]`.
4. Se **não** `stuntActive` / `rampLandingActive` e condições de modo/vidas/tempo: **`updateBikeOnTerrain()`**, lógica de **air trick**, alinhamento Y ao `cameraGroundOffsetY`.
5. Se **`composedTileId == 10`** (meta), tempo esgotado, vidas esgotadas (copa), ou Practice no teto de tempo: **`raceEndHandled`** — pausa relógio, define flags de overlay, toca som **2** ou **3** (curso 8 da copa, índice `8`, em modo 2), chama `levelManager.onRaceFinished(...)`.
6. Repaint parcial (`repaintFlags`: `1` parallax, `2` terreno, `4` moto / relógio).

---

## Movimento e câmera

- A moto tem **X de tela** fixo (`metadata[9]`); o mundo move-se com `TerrainLevel.scrollBy(±n)` e `ParallaxBackground.scrollBy(±n)`.
- `bikeWorldX` avança com o scroll; inclinação **para frente** aumenta frequência de scroll (threshold diminui com `currentLean`).
- **Pouso pós-stunt** (`rampLandingActive`): scroll **invertido** temporariamente até `bikeWorldX <= rampStopWorldX` (posição calculada com `findFlatGroundX` + `metadata[8]`); moto oculta no paint durante ramp landing até reassentar.

---

## Motocicleta (`Motorbike`)

### Estados (`bikeState`)

| Valor | Comportamento |
|-------|----------------|
| `0` | Neutro — lean relaxa aos poucos (`metadata[68]`) |
| `1` | Acelerar — lean sobe em passos (`metadata[67]`, +10 por input) |
| `2` | Frear — lean desce (`metadata[70]`) |
| `5` | Crash/stunt — pose fixa (`enterCrash`) |

Limites `minLean` / `maxLean`: `metadata[12–13]`; perfis Nokia não-default decrementam ambos.

### Colisão visual / probes

- Tabela `wheelProbeTable[2][21]` da metadata; `getWheelProbeOffset(0|1)` amostra terreno nas rodas dianteira/traseira.
- Desenho: frame **21** em crash; sequência de trick se `inAirTrick`; senão `poseFrameIndex + poseVariant + 3`.

---

## Terreno e tipos de tile

- Nível compacto no `.dat` expande-se em `expandedTileMap` via catálogo de **peças** (multi-célula) e lista de tiles **pass-through** (sem colisão).
- **`surfaceTileId`**: tile bruto sob a moto; **`composedTileId`**: id composto por autotile (`matchTilePieceId` vizinhos esquerda/centro/direita).
- Inclinação: tabela `slopeLookup` + `getSlopeForTile` ajusta `cameraGroundOffsetY`.

### `composedTileId` relevante no gameplay

| Id | Efeito (resumo) |
|----|-----------------|
| **1, 5–9** | Pose “normal” (`poseFrameIndex = 7`); **5** + tecla **2** = stunt forçado + som 0 |
| **3** | Superfície baixa / pose 0 |
| **4** | Banco/obstáculo madeira — pose 16; regras especiais de colisão entre rodas |
| **9** | Pode forçar `currentLean = minLean` ao pousar |
| **10** | **Linha de chegada** — para simulação de corrida e dispara fim de fase |
| **11** | Tratamento especial durante `inAirTrick` (evita crash em alguns probes) |

### Colisão (`updateBikeOnTerrain`)

Três probes (frente, centro, trás) com ajuste **−128** quando a sonda está “acima” da superfície (`probeTerrainAt`).

Dispara **`beginStunt()`** + som **1** se combinações de tiles nas rodas indicam capsize (tiles **2, 4, 5, 6, 8** com flags de altura, com exceções para madeira composta **4** vs vizinhos **0, 1, 5, 9**).

**Wheelie em tronco**: se probes indicam condição de “front wheel up” em certos tiles, `wheelieOnLog = true` e animação de trick no chão pode ser iniciada sem hop.

### Pouso após air trick

Quando `inAirTrick` termina (Y volta ao chão):

- Limiares de `trickFrameIndex` dependem do tile (**3**, **4**, default **7**), +4 frames de tolerância.
- Se excedeu → **stunt** + som crash; senão repõe pose conforme tile.
- **`composedTileId == 9`** ou **`wheelieOnLog`** alteram se conta como falha.

---

## Stunt / crash (`beginStunt`)

- Só se não já em `stuntActive`, `rampLandingActive`, e `bikeState != 5`.
- Ativa `stuntActive`, `enterCrash()`, **`++stuntLivesLost`**, repaint.
- Após **`metadata[63]`** ticks: fim do stunt → `rampLandingActive`, `resetBike()`, sequência de pouso.

Em **Practice**, vidas de copa não limitam jogo, mas stunts ainda consomem contador se modo 2.

---

## Manobras aéreas

- Teclas **1/3**: entram em `inAirTrick` com impulso `metadata[11]` (`hopImpulse`).
- No ar: `trickFrameIndex` avança até 20 com cadência `trickCooldownMax` (`metadata[62]`); `trickDriftPerTick` (`metadata[10]`) desloca Y/X (`trickDriftX`).
- Atraso antes de aplicar gravidade/drift: `stuntAirDelayThreshold` (`metadata[61]`).
- Perfis `deviceProfileId != 0`: divisores nos períodos de scroll e trick (metade ou ¼) para aparelhos mais lentos.

---

## Fim de corrida — flags e mensagens

`endGameFlags` é soma de bits; exibição por prioridade em `getActiveEndGameFlag`:

| Bit | Valor | Mensagem |
|-----|-------|----------|
| 1 | 1 | **Game Over** |
| 2 | 2 | **New Best Time!** |
| 4 | 4 | **Out of Time!** |
| 8 | 8 | **Course Complete!** |
| 16 | 16 | **Course** *N* (banner ao iniciar rodada) |

Sequência: consome uma flag a cada ~20 ticks com relógio pausado; quando zera flags pendentes, `onRaceFinished` já foi chamado no fechamento da corrida.

---

## Progressão e save (RMS `"times"`)

Registro único; layout (`loadProgressFromRms` / `saveProgressToRms`):

1. `unlockedCourseCount` (default **2** — cursos 1 e 2 jogáveis em Practice/Time Attack).
2. `bestCupTier` (0–3).
3. `soundEnabled` (byte).
4. `volumePercent` codificado (100% = byte `1`).
5. Por curso (11×): minutos, segundos, centésimos do **melhor tempo**.

Atualização de recorde: `onRaceFinished` se `elapsed < bestTimesMs[curso]`.

**Stunt Cup (durante sequência)**: ao completar curso `k` (`k < 7`) com tempo, se `k == unlockedCourseCount - 1`, incrementa `unlockedCourseCount` (libera próximo curso para modos livres).

Cursos **9–11** dependem sobretudo do prêmio de copa (menu 6).

---

## Áudio (`sound.dat`)

- Quatro efeitos fatiados; API Nokia `Sound(bytes, 1)` (**FORMAT_TONE** / OTA).
- Índices usados no canvas:

| Índice | Uso |
|--------|-----|
| **0** | Tronco + acelerar (tecla 2 em tile 5) |
| **1** | Crash / stunt por colisão ou pouso ruim |
| **2** | Fim de corrida (vitória/complete/record) |
| **3** | Variante fim Stunt Cup no curso índice **8** |

Só toca se `levelManager.soundEnabled`.

---

## Metadata global (`metadata_mono.dat`)

Parâmetros lidos via `gameMetadata.data[i]` (índices usados no canvas/moto):

| Índices | Uso |
|---------|-----|
| 1 | Camadas parallax (`bg.png`) |
| 2–5, 9 | Layout HUD, altura playfield, spawn, tile size |
| 6–7 | Grid `tiles.png` |
| 8 | Offset spawn / pouso |
| 10–11 | Drift e impulso do hop |
| 12–13 | Lean min/max |
| 14–15 | Vidas totais e limiar `tickStuntCupLife` |
| 16–17 | Grid `bikes.png` |
| 61–63 | Delays ar, cooldown trick, duração stunt |
| 64 | Ticks intro `crashAnimActive` |
| 65–66 | Período base scroll terreno/parallax |
| 67–70 | Ticks animação lean por estado |
| 71 | Boost scroll pouso rampa |
| 72–73+ | Offsets blobs slope/catálogo de peças por nível |

Blob por curso também em `levels.dat`: par em min/seg nos bytes do registro do nível.

---

## Perfis de dispositivo (`TrailBikerMidlet.deviceProfileId`)

| ID | Detecção |
|----|----------|
| **4** | Default |
| **0** | `Nokia6310i` — quebras `\r\n` em forms; tile vazio verde (`65280`) |
| **2** | `Nokia3410` |

Efeitos: dimensões de tela (+1 px em alguns casos), lean/hitbox, velocidade de scroll/trick (÷2 ou ÷4), instruções com ou sem `\r`.

---

## Cursos (`levels.dat`)

- **`data[0]`** = número de slots ( **11** no asset embarcado).
- Cada registro: par (min, seg), dimensões da grade compacta, bytes da grade + blobs slope/peças referenciados pela metadata.
- **Stunt Cup** usa sequencialmente os **8 primeiros** percursos da numeração interna (índices `0–7` = Course 1–8).
- **9–11** desbloqueiam via copa/prêmio, não via sequência normal da copa.

---

## Referências rápidas no repositório

- Fluxo menus / copa: `a/a/a/c/c-reconstruct.java`, `c.md`
- Gameplay frame a frame: `a/a/a/c/a-reconstruct.java`, `a.md`
- Strings e instruções: `a/a/a/c/b-reconstruct.java`, `b.md`
- Terreno/colision: `a/a/a/b/b-reconstruct.java`, `b.md`
- Relógio: `a/a/a/a/a-reconstruct.java`, `a.md`
- Som: `a/a/a/d/a-reconstruct.java`, `a.md`

Para validar paridade no port Python, compare com `recomp/trail_biker/` — divergências conhecidas (save JSON vs RMS, menu Settings) devem ser documentadas no README do port. O port mantém `metadata[64]` ticks de intro com física parada e **só despausa o `GameClock` ao fim da intro** (o MIDlet original também não chama `tick()` nesse intervalo, mas o relógio de parede ancorado no `reset()` faria o primeiro `tick()` “pular” ~2 s se não houvesse compensação).
