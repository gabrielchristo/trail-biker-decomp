# `a.a.a.a.a` — Relógio / cronômetro do jogo (GameClock)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `a` | `GameClock` |
| `a.a.a.b.c` (sprite sheet) | `SpriteSheet` |

Arquivo decompilado: `a.java`  
Reconstrução legível: `a-reconstruct.java`

---

## Objetivo geral

Esta classe implementa o **cronômetro exibido na HUD** do Trail Biker: mantém o tempo da partida (em milissegundos), atualiza a cada tick do loop principal, pode **contar para cima** ou **para baixo**, pode ser **pausado**, e desenha o valor formatado com sprites do arquivo `numbers.png`.

O construtor deixa explícito o nome original da classe no log de erro: *"Failed to load numbers.png in **Clock()** constructor."*

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.b.c`](../b/c.java) | **Sprite sheet** de dígitos. Construtor `new c(Image, 1, 11, true)` — 1 linha, 11 colunas (dígitos 0–9 + símbolo extra), modo *clipped* (`true`) sem fatiar em memória. Métodos `a(index, Graphics, x, y)` para desenhar um frame; campos `if` (largura do dígito) e `do` (altura) para layout. |
| [`a.a.a.c.a`](../c/a.java) | **Canvas principal** (`FullCanvas`). Campo `W` é a instância do relógio. Chama `tick` a cada frame (`W.do()`), `setPaused` ao abrir/fechar diálogos, `reset` ao iniciar nível (`case()`), `paint` no canto da tela, e usa `W.int` (tempo atual) para fim de fase, recordes e comparação com `o.char()` (tempo alvo do nível em `a.a.a.c.c`). |
| `/res/numbers.png` | Asset carregado no construtor. |
| [`a.a.a.c.c`](../c/c.java) | Modo de jogo / nível: `o.a` (tipo de fase), `char()` retorna tempo de referência (ex.: countdown inicial), usado em `reset(countdownMode, o.char(), …)`. |

Fluxo resumido no canvas:

1. `new a(false, 0L, true)` no construtor — relógio com centésimos visíveis.
2. Ao começar fase: `W.reset(o.a != 0, o.a != 0 ? o.char() : 0L, true)`.
3. Loop: `W.tick()` enquanto o jogo corre.
4. Pausa de UI: `W.setPaused(true/false)` (ex.: `goto()` / `do()` do canvas).
5. Desenho: `W.paint(graphics, 0, 0)` e `repaint` parcial com `W.getPaintWidth()` × `W.getPaintHeight()`.

---

## Funções

Nomes sugeridos refletem o papel real; assinaturas originais entre parênteses.

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(boolean, long, boolean)` | `reset` | Define modo countdown, valor base (`baseTimeMs`), exibir centésimos; zera relógio de parede (`wallClockStartMs`) e tira da pausa. |
| `do()` | `tick` | Avança o tempo com base em `System.currentTimeMillis()`. Se não pausado: em countdown com tempo &gt; 0 faz `displayTimeMs = baseTimeMs - elapsed`; senão `displayTimeMs = baseTimeMs + elapsed` limitado a `MAX_TIME_MS`. Clamp em 0. |
| `a(boolean)` | `setPaused` | `true` inicia pausa (guarda instante); `false` compensa `wallClockStartMs` pelo tempo pausado para o relógio não “andar” enquanto pausado. |
| `a(Graphics, int, int)` | `paint` | Desenha `MM:SS` ou `MM:SS:CC` (se `showCentiseconds`) na posição `(x, y)` usando índices de sprite 0–9 e `10` como separador `:`. |
| `a()` | `getPaintWidth` | Largura total do texto em pixels (4 “celas” de dígito + espaçamento, ou 7 se centésimos). |
| `if()` | `getPaintHeight` | Altura de um dígito (`spriteSheet.frameHeight`). |

---

## Variáveis

| Original | Sugerido | Tipo | Função |
|----------|----------|------|--------|
| `do` | `countdownMode` | `boolean` | Se `true` e `displayTimeMs > 0`, o tempo **diminui** a partir de `baseTimeMs`; caso contrário **aumenta** (modo corrida contra o relógio / tempo decorrido). |
| `case` | `showCentiseconds` | `boolean` | Se `true`, desenha dois dígitos extras de centésimos após um segundo `:`. |
| `int` | `displayTimeMs` | `long` | Tempo atual mostrado e usado pela lógica do jogo (**milissegundos**). Público no decomp para leitura direta pelo canvas. |
| `else` | `baseTimeMs` | `long` | Ancoragem do tempo no último `reset` (valor inicial ou alvo do countdown). Público no decomp. |
| `byte` | `wallClockStartMs` | `long` | `currentTimeMillis()` no último `reset` (ajustado ao sair da pausa). |
| `a` | `digitSprites` | `c` / `SpriteSheet` | Renderizador da folha `numbers.png`. |
| `char` | `pauseStartedMs` | `long` | Instantâneo em que a pausa começou (privado). |
| `new` | `paused` | `boolean` | Se `true`, `tick` não atualiza `displayTimeMs`. |

### Constantes

| Original | Sugerido | Valor | Função |
|----------|----------|-------|--------|
| `goto` | `SPRITE_COLON` | `10` | Índice do sprite `:` na folha. |
| `if` | `SPRITE_COUNT` (?) | `11` | Número de colunas na folha (0–10); não usado diretamente nesta classe além do construtor do sheet. |
| `try` | `DIGIT_SPACING` | `3` | Espaço horizontal entre “slots” de caractere no desenho e no cálculo de largura. |
| `for` | `MAX_TIME_MS` | `599990` | Teto ~9 min 59,99 s em modo contagem crescente; no modo treino (`o.a == 0`) vitória por tempo máximo usa esse limite. |

---

## Port — estratégia Python + Pygame

### 1. Módulo e classe

- Pacote sugerido: `trail_biker/ui/clock.py` ou `trail_biker/game_clock.py`.
- Classe `GameClock` espelhando campos e métodos do Java (sem herdar de nada).

### 2. Dependência de sprites

- Portar primeiro (ou em paralelo) `a.a.a.b.c` como `SpriteSheet` com `draw_frame(index, surface, x, y)` e propriedades `frame_width`, `frame_height`.
- Carregar `original-files/content/res/numbers.png` com `pygame.image.load`; mesmo layout: 11 colunas × 1 linha, modo clip na textura (equivalente ao terceiro argumento `true`).

### 3. Tempo

- Substituir `System.currentTimeMillis()` por `pygame.time.get_ticks()` (também ms desde init do pygame) **ou** `time.monotonic_ns() // 1_000_000` se quiser independência do pygame no unit test.
- Manter a mesma aritmética de `tick`, `reset` e `setPaused` (incluindo compensação de pausa em `wallClockStartMs`).

### 4. Desenho (`paint`)

- Extrair minutos: `(display_time_ms // 1000) // 60`
- Segundos: `(display_time_ms // 1000) % 60`
- Centésimos (se habilitado): `(display_time_ms // 10) % 100` — igual ao bytecode original.
- Posições: `x + DIGIT_SPACING * slot` como no Java; `blit` com recorte da sub-área da folha para cada índice.

### 5. Integração com o canvas portado

- O equivalente a `a.a.a.c.a` guarda `self.clock = GameClock(...)` e no update do loop chama `clock.tick()` nas mesmas condições (não pausar quando diálogo Nokia, etc.).
- `repaint` parcial: usar `get_paint_width()` / `get_paint_height()` para `pygame.display.update(dirty_rect)` ou marcar só o retângulo do HUD.
- Leituras de `display_time_ms` para game over, estrelas e `level_manager.target_time_ms()` permanecem iguais.

### 6. Testes rápidos sem pygame (opcional)

- Lógica pura de `tick` / `reset` / `setPaused` em funções testáveis com mocks de `now_ms`.
- Teste de formatação: dado `display_time_ms`, assert nos índices de sprite ou num string `09:45:23`.

### 7. Ordem no projeto

1. `SpriteSheet` + asset `numbers.png`
2. `GameClock` com tick/paint
3. Conectar no loop do canvas quando o port de `a.a.a.c.a` existir

Não é necessário Arcade; esta classe é estado + blit, alinhada ao MIDlet original.
