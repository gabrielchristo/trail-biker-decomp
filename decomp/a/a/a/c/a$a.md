# `a.a.a.c.a$a` — Timer do loop de jogo (inner class)

| Decomp | Reconstruct |
|--------|-------------|
| Classe interna `a` | `CanvasGameLoopTask` |
| Classe externa `a.a.a.c.a` | `GameCanvas` |

Arquivo decompilado: `a$a.java` (inner class de [`a.java`](a.java))  
Reconstrução legível: `a$a-reconstruct.java`

---

## Objetivo geral

Classe interna **`TimerTask`** que dispara a cada **100 ms** e chama [`GameCanvas.else()`](a.java) no thread do timer. Esse método no canvas:

1. Decrementa o contador de splash/diálogo (`try`) e remove comando quando chega a zero.
2. Invoca **`run()`** do `FullCanvas` — o **loop principal do jogo** (física, scroll, repaint flags).

Instanciada em `GameCanvas.goto()` (`startGameSession`): `this.null = new a.a(this, this)` — campo `null` do canvas guarda a referência. Encerrada em `GameCanvas.do()` (`shutdown`) via `null.a()` (cancela timer).

Substitui um thread dedicado por `java.util.Timer` (comum em MIDlets).

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.a`](a.java) | Classe externa `GameCanvas`. Campos `null` (task), métodos `goto()` / `do()`, `else()` (tick agendado), `run()` (loop). |
| [`a.a.a.c.c`](c.java) | `LevelManager` chama `gameCanvas.goto()` / `do()` ao entrar/sair da partida. |
| `com.nokia.mid.ui.DeviceControl` | Ligado em `goto()` após criar o timer (luzes). |
| `javax.microedition.lcdui.FullCanvas` | `run()` herdado — motor do frame. |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(a, a)` (construtor) | `CanvasGameLoopTask(GameCanvas, GameCanvas)` | Guarda referências ao canvas; cria `Timer` e agenda `this` com período **100 ms** (delay 0). |
| `run()` | `run` | `canvas.else()` — tick MIDlet + loop. |
| `a()` | `stop` | `cancel()` na task, `cancel()` no `Timer`, `timer = null`. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `this$0` | `this$0` | Referência sintética à instância externa `GameCanvas` (igual ao primeiro argumento no decomp). |
| `a` | `canvas` | Canvas cuja `else()` / loop são acionados (no código atual sempre o mesmo que `this$0`). |
| `if` | `timer` | `java.util.Timer` que agenda os ticks. |

---

## Port — estratégia Python + pygame

### 1. Substituir Timer + TimerTask

- Não usar `threading.Timer` em cadeia; usar o **clock do pygame**:

```python
# no loop principal
dt = clock.tick(60)  # ou 10 fps ≈ 100ms se quiser fidelidade: clock.tick(10)
game_canvas.on_timer_tick()  # port de else()
game_canvas.update(dt)
game_canvas.draw()
```

- **100 ms** ≈ **10 Hz** para a parte que só chamava `else()`; o `run()` J2ME provavelmente rodava mais rápido — no port unificar num único `tick()` a **60 FPS** e mover lógica de `else()` + corpo de `run()` para métodos explícitos.

### 2. Ciclo de vida

- `start_game_session()` (port de `goto()`): criar flag `loop_running = True`, acender “luzes” se relevante, pausar relógio HUD se necessário.
- `shutdown()` (port de `do()`): `loop_running = False`, equivalente a `stop()`.

### 3. Splash / overlay

- Contador `splash_ticks` (decomp `try`) decrementado em `on_timer_tick()`.

### 4. Módulo

- `trail_biker/game/game_canvas.py` — incorporar a task como método, não classe separada, **a menos** que queira espelhar o Java para documentação.

### 5. Testes

- Mock: após N ticks, splash removido e loop invocado.

Prioridade: junto com o port de `GameCanvas`; sem isso o jogo não “anda” fora do MIDlet.
