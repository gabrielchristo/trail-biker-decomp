# `a.a.a.c.b` — Textos da UI (strings)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `b` | `UiStrings` |

Arquivo decompilado: `b.java`  
Reconstrução legível: `b-reconstruct.java`

---

## Objetivo geral

Classe utilitária com **apenas constantes `String`** em inglês: título do jogo, itens de menu, instruções, mensagens de fim de fase, copas, desbloqueio de cursos e rótulos de comandos (OK, Back, Exit).

Não contém lógica. O construtor público vazio não é necessário para uso estático.

No decomp **nenhum arquivo importa `a.a.a.c.b`** — [`c.java`](c.java) e [`a.java`](a.java) repetem os mesmos literais (`"Practice"`, `"Time Attack"`, corpo das instruções, etc.). Esta classe é quase certamente o **repositório original de strings** antes da compilação/ofuscação; no port convém **centralizar** aqui e eliminar duplicatas.

Relação com `original-files/content/locale.txt` (apenas `"En "`): o jogo Nokia provavelmente usava locale embutido; este `b.java` é a tabela **en** completa.

### Interações (por conteúdo, não por referência)

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.c`](c.java) / [`c-reconstruct.java`](c-reconstruct.java) | Menus `showMenuScreen` — strings idênticas a `MENU_*`, `INSTRUCTIONS_BODY`, prêmios de copa. |
| [`a.a.a.c.a`](a.java) | Mensagens de jogo (game over, tempo, etc.) — cruzar ao portar canvas. |
| [`a.a.a.c.f`](f.java) | Título MIDlet `Trail Biker` = `GAME_TITLE`. |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `b()` (construtor) | `UiStrings()` | Vazio; classe só para agrupar constantes. |

---

## Variáveis (constantes `String`)

| Decomp | Reconstruct | Texto / uso |
|--------|-------------|-------------|
| `a` | `GAME_TITLE` | Trail Biker |
| `long` | `MENU_PRACTICE` | Practice |
| `if` | `MENU_TIME_ATTACK` | Time Attack |
| `new` | `MENU_STUNT_CUP` | Stunt Cup |
| `b` | `MENU_CONTINUE` | Continue |
| `int` | `MENU_NEW_GAME` | New Game |
| `h` | `MENU_HIGH_SCORES` | High Scores |
| `q` | `MENU_INSTRUCTIONS` | Instructions |
| `e` | `INSTRUCTIONS_BODY` | Bloco longo de controles e modos (`\r\n`) |
| `w` | `MSG_NEW_BEST_TIME` | New Best Time! |
| `char` | `CUP_GOLD` | Gold |
| `g` | `CUP_BRONZE` | Bronze |
| `c` | `CUP_SILVER` | Silver |
| `k` | `MSG_CONGRATULATIONS` | Congratulations! |
| `u` | `MENU_SELECT_COURSE` | Select Course |
| `void` | `MSG_GAME_OVER` | Game Over |
| `i` | `LABEL_OK` | OK |
| `o` | `MSG_OUT_OF_TIME` | Out of Time! |
| `case` | `LABEL_COURSE` | Course |
| `j` | `LABEL_BACK` | Back |
| `byte` | `LABEL_NEXT` | Next |
| `try` | `LABEL_EXIT` | Exit |
| `else` | `UNLOCK_MSG_COURSE_9` | Course 9 unlocked. |
| `f` | `UNLOCK_MSG_COURSE_10` | Course 10 unlocked. |
| `l` | `UNLOCK_MSG_COURSE_11` | Course 11 unlocked. |
| `v` | `MSG_COURSE_COMPLETE` | Course Complete! |
| `do` | `LABEL_BEST_CUP` | Best Cup: |
| `t` | `AWARD_GOLD_CUP` | Gold Cup Awarded! |
| `r` | `AWARD_SILVER_CUP` | Silver Cup Awarded! |
| `n` | `AWARD_BRONZE_CUP` | Bronze Cup Awarded! |
| `null` | `UNLOCK_MSG_COURSES_9_10` | Courses 9 and 10 unlocked. |
| `goto` | `UNLOCK_MSG_COURSES_9_10_11` | Courses 9, 10 and 11 unlocked. |
| `d` | `UNLOCK_MSG_COURSES_10_11` | Courses 10 and 11 unlocked. |
| `for` | `MENU_SETTINGS` | Settings |
| `m` | `SETTINGS_SOUND_ON` | Sound On |
| `p` | `SETTINGS_SOUND_OFF` | Sound Off |
| `x` | `SETTINGS_LIGHTS_ON` | Lights On |
| `s` | `SETTINGS_LIGHTS_OFF` | Lights Off |

---

## Port — estratégia Python + pygame

### 1. Módulo

- `trail_biker/i18n/ui_strings.py` ou `trail_biker/locale/en.py` — classe ou módulo `UiStrings` com as mesmas constantes.

### 2. Uso

- Substituir literais em `level_manager.py` / menus por `UiStrings.MENU_PRACTICE`, etc.
- `INSTRUCTIONS_BODY`: normalizar quebras (`\r\n` → `\n`) como em `LevelManager` quando `device_profile_id == 0`.

### 3. Futuro i18n

- Carregar de `locale.txt` ou JSON por idioma; `En` vira chave `en` com dict espelhando estes campos.

### 4. Pygame

- `font.render(UiStrings.MSG_GAME_OVER, …)` em overlays; listas de menu podem usar os mesmos strings que o MIDP `List`.

### 5. Testes

- Assert que strings críticas batem com trechos hardcoded atuais em `c.java` (diff único ao refatorar).

Prioridade baixa para gameplay, alta para **não espalhar** texto ao portar `LevelManager` e canvas.
