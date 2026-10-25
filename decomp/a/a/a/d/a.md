# `a.a.a.d.a` — Reprodução de efeitos sonoros (Nokia Sound)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `a` | `SoundManager` |
| `a.a.a.e.a` | `BinaryAsset` |

Arquivo decompilado: `a.java`  
Reconstrução legível: `a-reconstruct.java`

---

## Objetivo geral

Esta classe **carrega e toca efeitos sonoros** empacotados em `/res/sound.dat`, usando a API proprietária **`com.nokia.mid.sound.Sound`** dos aparelhos Nokia série 30/40. Um único objeto `Sound` é reutilizado: a cada pedido de toque, o buffer do efeito é trocado com `init` e disparado com ganho máximo.

O parsing do `.dat` segue o layout descrito em [`a.a.a.e.a`](../e/a.md): primeiro byte = quantidade de samples; pares de uint16 a partir do offset `1` definem **offsets de fatia** no array `data` (último sample vai até `data.length`).

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.e.a`](../e/a.java) / `BinaryAsset` | Loader do blob `sound.dat`. Construtor interno `new BinaryAsset(s)`; leitura `data[0]`, `getUInt16(1 + i*2)`, cópia de subarrays para `sampleData[][]`. |
| [`a.a.a.c.a`](../c/a.java) | **Canvas** do jogo. Campo `Q`; criado em `new SoundManager("/res/sound.dat")` ao montar o nível. Chama `playSound` quando `o.try` (som ligado nas opções) e em eventos de gameplay. |
| [`a.a.a.c.c`](../c/c.java) | Campo `try` no gerenciador de níveis — flag “sound enabled”; o canvas só chama `Q` se `this.o.try`. |
| `/res/sound.dat` | Asset binário (via `BinaryAsset`). |
| `com.nokia.mid.sound.Sound` | Runtime Nokia; **não existe no Java SE** — no port PC substituir por `pygame.mixer`. |

### Índices de som usados no canvas (referência)

| Índice | Contexto aproximado em `a.a.a.c.a` |
|--------|--------------------------------------|
| `0` | Colisão com terreno / evento tecla 50, tipo de tile 5 |
| `1` | “Crash” / `byte()` (stunt ou impacto) com som habilitado |
| `2` | Fim de fase, vitória/derrota, recordes (`Q.a` após `a(2)` / `a(8)`) |
| `3` | Variante de fim (modo especial, ex. `o.a == 2` e `o.new == 8`) |

(Confirme ouvindo os samples após o port; os índices vêm só das chamadas no decomp.)

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(String)` (construtor) | `SoundManager(String)` | Inicializa constantes internas, chama `loadSoundsFromResource`, deixa `nokiaSound` nulo até o primeiro `playSound`. |
| `a(String)` (protected) | `loadSoundsFromResource` | Abre `sound.dat` via `BinaryAsset`, lê contagem e fatias, preenche `sampleData[][]`. |
| `a(byte)` | `playSound` | Seleciona `sampleData[b]`, cria ou reinicializa `Sound`, `setGain(255)`, `play(1)`. |

---

## Variáveis

| Original | Sugerido | Tipo | Função |
|----------|----------|------|--------|
| `a` | `nokiaSound` | `Sound` | Instância Nokia reutilizada entre efeitos. |
| `for` | `sampleData` | `byte[][]` | Um buffer PCM (ou formato Nokia) por índice de efeito. `protected` no decomp (possível subclasse; nenhuma no repo). |
| `if` | `reservedZero` | `int` | Constante `0` no construtor; **não lida** no restante da classe (provável resquício ou uso futuro no bytecode). |
| `do` | `soundDataType` | `int` | Constante `1` no construtor; espelha o literal passado a `Sound(..., 1)` e `play(1)` (tipo de dado / modo de playback Nokia). |

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/audio/sound_manager.py` — classe `SoundManager` com a mesma assinatura lógica: `__init__(path)`, `play_sound(index: int)`.

### 2. Carregar `sound.dat`

- Reutilizar `BinaryAsset` do port de `a.a.a.e.a`.
- Replicar o loop de fatias:

```python
count = asset.data[0]
samples: list[bytes] = []
for i in range(count):
    start = asset.get_uint16(1 + i * 2)
    end = asset.get_uint16(1 + (i + 1) * 2) if i < count - 1 else len(asset.data)
    samples.append(bytes(asset.data[start:end]))
```

### 3. Formato de áudio Nokia

- Os buffers em `Sound` Nokia costumam ser **mono 8 kHz** (ou conforme documentação MIDP Nokia Sound). Antes de `pygame.mixer.Sound`, pode ser necessário:
  - inspecionar cabeçalho nos primeiros bytes de cada fatia, ou
  - exportar uma vez com ferramenta / trial-and-error (`frequency=8000`, `size=-8`, `channels=1`).
- Se for PCM cru sem cabeçalho, construir `io.BytesIO` + `wave` ou usar `numpy` + `sndarray.make_sound`.

### 4. Pygame

- `pygame.mixer.init(frequency=8000, size=-8, channels=1)` (ajustar após validar).
- Pré-decodificar cada fatia em `list[pygame.mixer.Sound]` no load.
- `play_sound(i)`: `self.sounds[i].play()` (permite overlap; o original corta/reinicia o mesmo canal — opcional usar um único canal com `stop()` antes para imitar).

### 5. Integração

- Instanciar uma vez no bootstrap do nível (equivalente a `this.Q = new SoundManager(...)`).
- Respeitar flag `level_manager.sound_enabled` antes de tocar (equivalente a `o.try`).
- Mapear índices 0–3 a nomes (`SOUND_LAND`, `SOUND_CRASH`, …) quando validar no ouvido.

### 6. Testes sem janela

- Teste de carga: `len(samples) == data[0]`, somas de tamanhos coerentes com offsets.
- Teste opcional de playback com `python -m trail_biker.tools.play_sound 1`.

Depende de `BinaryAsset`; não depende do canvas até a integração de eventos.
