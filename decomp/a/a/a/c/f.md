# `a.a.a.c.f` — MIDlet principal (Trail Biker)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `f` | `TrailBikerMidlet` |
| `a.a.a.e.a` | `BinaryAsset` |
| `a.a.a.c.c` | `LevelManager` |

Arquivo decompilado: `f.java`  
Reconstrução legível: `f-reconstruct.java`  
Entrada no JAR: `META-INF/MANIFEST.MF` → `MIDlet-1: …, a.a.a.c.f`

---

## Objetivo geral

Esta classe é o **ponto de entrada J2ME** do jogo: estende `MIDlet`, carrega a **metadata global** (`metadata_mono.dat`), detecta o **modelo Nokia** para ajustes de layout, e cria o **gerenciador de fluxo** (`LevelManager`, classe `c`) que monta o canvas de jogo e os menus.

Ciclo de vida MIDlet:

- **Construtor** — assets + `LevelManager`.
- **startApp** — vazio (a UI já sobe no construtor de `c`).
- **pauseApp** — repassa pausa ao fluxo do jogo.
- **destroyApp** — encerra canvas, limpa referências, `notifyDestroyed`.

O campo estático `a` (`deviceProfileId`) é lido em todo o projeto (`SpriteSheet`, canvas, moto, menus) para ramificações de UI/tamanho.

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.c`](c.java) | `LevelManager`. Criado com `new LevelManager(this)`; `pauseApp` chama `handleMidletPause()`. |
| [`a.a.a.c.a`](a.java) | Canvas em `LevelManager.gameCanvas`. `destroyApp` chama `gameCanvas.shutdownGameLoopTimer()`. |
| [`a.a.a.e.a`](../e/a.java) | `BinaryAsset for` — tabela `metadata_mono.dat` compartilhada com o canvas (`P`). |
| [`a.a.a.b.c`](../b/c.java) | Lê `f.a == 1` para hack de clip (valor **não** atribuído neste `f.java`; ramo provavelmente morto ou de outra build). |
| [`a.a.a.c.e`](e.java), [`a.a.a.c.a`](a.java), [`a.a.a.c.c`](c.java) | Comparam `f.a` com `0`, `2`, `4` para layout e comportamento. |

### Valores de `deviceProfileId` (`f.a`)

| Valor | Origem |
|-------|--------|
| `4` | Padrão (`static` initializer) |
| `0` | `microedition.platform` começa com `Nokia6310i` |
| `2` | `microedition.platform` começa com `Nokia3410` |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `f()` (construtor) | `TrailBikerMidlet()` | Carrega metadata, detecta plataforma, instancia `LevelManager`. |
| `startApp()` | `startApp` | Vazio (MIDlet). |
| `pauseApp()` | `pauseApp` | `levelManager.handleMidletPause()`; `notifyPaused()`. |
| `destroyApp(boolean)` | `destroyApp` | `levelManager.gameCanvas.shutdownGameLoopTimer()`; `setCurrent(null)`; anula refs; `gc`; `notifyDestroyed()`. |
| `a(String)` (static) | `noop` | Corpo vazio (placeholder / obfuscação). |
| bloco `static` | `static` | Inicializa `deviceProfileId = 4`. |

---

## Variáveis

| Original | Sugerido | Função |
|----------|----------|--------|
| `do` | `unusedList` | `List` declarada, **nunca atribuída** no decomp. |
| `if` | `levelManager` | Instância de `LevelManager` (`c`). |
| `for` | `gameMetadata` | `BinaryAsset` de `/res/metadata_mono.dat`. |
| `a` (static) | `deviceProfileId` | Perfil de layout / dispositivo Nokia. |

---

## Port — estratégia Python + Pygame

### 1. Papel no port

Substituir `MIDlet` por um **`main.py`** (ou `trail_biker/__main__.py`) que:

1. Inicializa `pygame`.
2. Carrega `BinaryAsset("metadata_mono.dat")` → `game_metadata`.
3. Define `device_profile_id` (config/env, default `4`; opcional emular `0` / `2`).
4. Constrói `LevelManager(midlet_ref, game_metadata)` equivalente a `c`.

Não é necessário portar `MIDlet`/`Display` — só o **bootstrap** e o **ciclo de vida** (quit, pause opcional).

### 2. Metadata global

- Um objeto `GameMetadata` ou dict indexado como `metadata.data[i]` até mapear nomes (`P.a[9]`, etc.).

### 3. `device_profile_id`

- Módulo `trail_biker/platform.py`:
  - `DEVICE_PROFILE_DEFAULT = 4`
  - `detect_device_profile()` lê env `TRAIL_BIKER_PROFILE` ou fica no default.
  - Substituir todas as leituras `f.a` por `platform.device_profile_id`.

### 4. Pausa / saída

- `pauseApp` → flag `game_paused` no loop pygame (evento `APP_WILLENTERBACKGROUND` se mobile; no desktop, tecla P).
- `destroyApp` → `pygame.quit()`, liberar `RecordStore` equivalente se houver save.

### 5. `noop(String)`

- Omitir no port ou manter stub vazio se algum callback legado for descoberto.

### 6. Ordem de implementação

1. `BinaryAsset` + carregar metadata  
2. Loop pygame mínimo  
3. `LevelManager` (port de `c`)  
4. Conectar `GameCanvas` (`a`)

O MIDlet em si é fino; o trabalho pesado está em `c` e `a`.
