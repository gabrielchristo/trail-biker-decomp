# `a.a.a.c.a` — Canvas principal do jogo (FullCanvas)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `a` | `GameCanvas` |
| `a.a` (inner) | `CanvasGameLoopTask` |
| `a.a.a.a.a` | `GameClock` |
| `a.a.a.c.e` | `Motorbike` |
| `a.a.a.b.b` | `TerrainLevel` |
| `a.a.a.b.d` | `ParallaxBackground` |
| `a.a.a.b.c` | `SpriteSheet` |
| `a.a.a.d.a` | `SoundManager` |
| `a.a.a.e.a` | `BinaryAsset` |
| `a.a.a.c.c` | `LevelManager` |
| `a.a.a.c.f` | `TrailBikerMidlet` |

Arquivo decompilado: `a.java`  
Reconstrução legível: `a-reconstruct.java`

---

## Objetivo geral

**Tela de jogo** Trail Biker: estende `FullCanvas` e implementa `Runnable`. Responsável por:

- **Splash** inicial (`splash.png`) e transição para a partida.
- **Loop** (`run`): física da moto, scroll de terreno (`TerrainLevel`) e fundo (`ParallaxBackground`), relógio (`GameClock`), colisão de tiles, stunts, vidas (modo Stunt Cup), sons, fim de corrida e `repaint` parcial.
- **Input** (`keyPressed` / `keyReleased`): acelerar, frear, bunny-hop, pausa.
- **Paint**: camadas HUD, terreno, moto, mensagens (“Game Over”, “Course Complete”, etc.).
- **Timer** 100 ms via [`a$a`](a$a.java) → `onTimerTick()` chama `run()`.

Criado por [`LevelManager`](c.java); referência cruzada `o` / `levelManager` para fim de fase e `buildLevelGrid()`.

### Interações

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.c`](c.java) | Menus, `startLevel` (`a()`), `onRaceFinished` via `o.a(time)`, `buildLevelGrid`, pausa `try()`. |
| [`a.a.a.c.f`](f.java) | `TrailBikerMidlet` (`B`), metadata `P` = `midlet.gameMetadata`, `deviceProfileId` (tamanho tela). |
| [`a.a.a.c.e`](e.java) | Motocicleta `p`. |
| [`a.a.a.b.b`](b.java) | Terreno `L`. |
| [`a.a.a.b.d`](d.java) | Fundo `K`. |
| [`a.a.a.a.a`](../a/a.java) | Relógio `W`. |
| [`a.a.a.d.a`](../d/a.java) | Som `Q`. |
| Assets | `splash.png`, `tiles.png`, `bg.png`, `lives.png`, `sound.dat`. |
| Nokia | `DeviceControl.setLights` em `goto()`. |

---

## Funções (principais)

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(f, c)` | `GameCanvas` | Inicializa dimensões, `GameClock`, splash, metadata. |
| `if(Graphics)` | `fillBackgroundWhite` | Limpa framebuffer branco. |
| `else()` | `onTimerTick` | Splash tick; chama `run()`. |
| `a(Graphics,…,String,…)` | `drawCenteredBanner` | Balão de texto + comando Dismiss opcional. |
| `try()` | `loadGameResources` | Moto, tiles, parallax, terreno, sons. |
| `null()` | `resetRoundState` | Zera contadores, `motorbike.resetBike`, scroll terreno, metadata de scroll. |
| `run()` | `run` | Loop principal (splash ou simulação). |
| `a(int)` | `addEndGameFlag` | Bits em `endGameFlags` (1=game over, 2=best time, 4=timeout, 8=complete, 16=…). |
| `long()` | `tickEndGameSequence` | Consome flags de fim com delay. |
| `int()` | `getActiveEndGameFlag` | Maior flag de fim pendente. |
| `paint(Graphics)` | `paint` | Composição visual. |
| `do(Graphics)` | `drawEndGameMessage` | Overlay de fim de fase. |
| `a(Graphics)` | `drawStuntLives` | Ícones de vidas (modo 2). |
| `keyPressed` / `keyReleased` | idem | Teclas Nokia (2,5,1,3, soft keys). |
| `void()` | `dismissSplashOrOpenPause` | Splash → jogo ou pausa `levelManager`. |
| `for()` | `updateBikeOnTerrain` | Colisão tiles, crash, som, `wheelie` flag `A`. |
| `a()` | `startLevel` | Carrega grid do nível, reseta relógio, inicia timer. |
| `case()` | `configureGameClock` | Reset do `GameClock` conforme modo. |
| `if()` | `resetStuntLives` | `stuntLivesRemaining`, metadata. |
| `new()` | `tickStuntCupLife` | Perde vida no stunt cup. |
| `goto()` | `startGameLoopTimer` | `CanvasGameLoopTask`, luzes, relógio. |
| `do()` | `shutdownGameLoopTimer` | Para task e relógio. |
| `byte()` | `beginStunt` | Ativa stunt, `motorbike.enterCrash` path / contador. |
| `hideNotify` | idem | Pausa MIDlet ao perder foco. |
| `a(String)` | `noop` | Vazio. |
| `char()` | `onCharHook` | Vazio no decomp. |

---

## Variáveis (seleção)

| Original | Sugerido | Função |
|----------|----------|--------|
| `do` | `screenWidth` | Largura canvas (ajuste Nokia). |
| `M` | `screenHeight` | Altura canvas. |
| `try` | `bannerTickCount` | Ticks do banner/dismiss. |
| `U` | `bannerDismissed` | Splash/message dismissed. |
| `e` | `dismissCommand` | Command "Dismiss". |
| `null` | `gameLoopTask` | `CanvasGameLoopTask`. |
| `L` | `terrain` | `TerrainLevel`. |
| `p` | `motorbike` | `Motorbike`. |
| `K` | `parallax` | `ParallaxBackground`. |
| `W` | `gameClock` | `GameClock`. |
| `Q` | `soundManager` | `SoundManager`. |
| `P` | `gameMetadata` | `BinaryAsset` mono. |
| `B` | `midlet` | `TrailBikerMidlet`. |
| `o` | `levelManager` | `LevelManager`. |
| `int` | `tileSprites` | `SpriteSheet` tiles. |
| `w` | `splashSprites` | `SpriteSheet` splash. |
| `h` | `livesImage` | `lives.png`. |
| `else` | `splashPhase` | 1→0→-1 (jogo). |
| `for` | `emptyTileColorArgb` | Cor fundo tile vazio. |
| `V` | `playfieldReady` | Recursos carregados e nível ativo. |
| `j` | `needsBackgroundClear` | Primeiro frame limpa fundo. |
| `T` | `assetsLoaded` | `loadGameResources` executado. |
| `g` | `endGameFlags` | Bits de fim de jogo. |
| `long` | `endGameAnimTimer` | Contador animação fim. |
| `H` | `repaintFlags` | Máscara repaint parcial (1=top, 2=terrain, 4=bike). |
| `R` | `crashAnimActive` | Animação pós-crash. |
| `new` | `crashAnimTicks` | Tick crash. |
| `case` | `stuntActive` | Período pós-stunt no ar. |
| `d` | `rampLandingBoost` | Pouso em rampa calculada. |
| `q` | `raceFinishedHandled` | Evita reentrância fim de corrida. |
| `x` | `stuntLivesLost` | Vidas perdidas stunt cup. |
| `I` | `stuntLivesTotal` | Total vidas (metadata). |
| `goto` | `scrollTickCounter` | Usado em `resetStuntLives` path. |
| `if` | `suppressPauseOnce` | `hideNotify` flag. |
| Demais (`r`,`E`,`v`,`J`,…) | — | Contadores de scroll suave terreno/parallax, stunt air timers (`char`,`l`,`void`), etc. |

---

## Port — estratégia Python + pygame

### 1. Módulo

- `trail_biker/game/game_canvas.py` — classe `GameCanvas` (não herda MIDlet; recebe `surface`, `clock`, refs).

### 2. Loop

- Unificar `onTimerTick` + `run()` em `update(dt)` chamado a 60 FPS (ou 10 Hz só para lógica legacy, depois unificar).
- `paint` → `draw(surface)`.

### 3. Subsistemas já mapeados

- Instanciar `Motorbike`, `TerrainLevel`, `ParallaxBackground`, `GameClock`, `SoundManager` como nos reconstructs de `b`/`e`/`d`/`a`/`d`.

### 4. Input

- Mapear key codes Nokia para pygame (`K_2`, `K_5`, etc.).

### 5. Splash

- Estado `splash_phase`; desenhar `splashSprites`; após N frames chamar `load_game_resources` + `reset_round_state`.

### 6. Fim de fase

- Portar `addEndGameFlag` / `getActiveEndGameFlag` / `drawEndGameMessage`; chamar `level_manager.on_race_finished(elapsed_ms)`.

### 7. Ordem de implementação

1. Recursos + `start_level` sem física  
2. Scroll + draw terreno/moto  
3. Colisão (`update_bike_on_terrain`)  
4. Relógio + `on_race_finished`  
5. Input e stunt  

Maior arquivo do port; usar `a-reconstruct.java` como checklist linha a linha.
