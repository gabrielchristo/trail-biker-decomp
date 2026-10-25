# `a.a.a.e.a` — Carregador de assets binários (.dat)

| Decomp | Reconstruct |
|--------|-------------|
| Classe `a` | `BinaryAsset` |

Arquivo decompilado: `a.java`  
Reconstrução legível: `a-reconstruct.java`

---

## Objetivo geral

Esta classe **carrega arquivos de dados empacotados** do classpath J2ME (caminhos como `/res/metadata_mono.dat`), interpreta um **cabeçalho fixo** e expõe o payload como um array de bytes em memória, com utilitários para ler bytes e inteiros de 16 bits sem sinal.

Não parseia semântica de nível, som ou UI: só I/O + acesso tipado ao buffer. Quem consome interpreta índices e layouts (tabelas de inteiros de 1 byte, offsets em `levels.dat`, fatias de `sound.dat`, etc.).

### Formato do arquivo (inferido do loader)

| Offset | Tamanho | Significado |
|--------|---------|-------------|
| 0 | 2 | Comprimento do payload `n` (big-endian: `(b0 << 8) + b1`) |
| 2 | 1 | Versão/formato; se `== 1`, seguem `n` bytes de dados |
| 3 | `n` | Conteúdo bruto (`data[]`) |

Se a versão não for `1`, o array `data` pode ficar `null` (o construtor não aloca).

### Interações com outros arquivos

| Arquivo | Relação |
|---------|---------|
| [`a.a.a.c.f`](../c/f.java) | **MIDlet** principal. Cria `new BinaryAsset("/res/metadata_mono.dat")` no campo `for`; referência global de **parâmetros de jogo** (layout da tela, tiles, física, tempos, etc.). O canvas usa `this.P` apontando para o mesmo objeto. |
| [`a.a.a.c.a`](../c/a.java) | **Canvas** (`FullCanvas`). Campo `P` é a instância de metadata (`b.for`). Dezenas de leituras `P.a[índice]` para dimensões, HUD, scroll, animação, limites. |
| [`a.a.a.c.c`](../c/c.java) | **Menu / progressão de níveis**. `new BinaryAsset("/res/levels.dat")` no campo `byte`; navega no blob para metadados de fases, tempos alvo, recordes em RMS. |
| [`a.a.a.c.e`](../c/e.java) | **Motocicleta / jogador**. Recebe referência à metadata no construtor (`do`) e lê `do.a[12]`, `do.a[16]`, etc. para sprite e hitbox. |
| [`a.a.a.d.a`](../d/a.java) | **Áudio Nokia**. `new BinaryAsset(s)` com `s = "/res/sound.dat"`; byte 0 = quantidade de samples; offsets uint16 no índice `1 + i*2` para fatiar `data[]` em `byte[][]`. |
| `original-files/content/res/*.dat` | Assets reais: `metadata_mono.dat`, `levels.dat`, `sound.dat`. |

---

## Funções

| Original | Sugerido | Descrição |
|----------|----------|-----------|
| `a(String)` (construtor) | `BinaryAsset(String)` | Guarda o caminho do recurso e chama o carregamento. |
| `a(String)` (privado) | `loadFromResource` | Abre `getResourceAsStream(resourcePath)`, lê cabeçalho, preenche `data` se versão == 1; em erro lança `RuntimeException("load failed")`. |
| `a(int)` | `getUnsignedByte` | Retorna `data[index]` como 0–255 (corrige byte Java assinado). |
| `if(int)` | `getUInt16` | Lê uint16 big-endian em `index` e `index+1` via `getUnsignedByte`. |

---

## Variáveis

| Original | Sugerido | Tipo | Função |
|----------|----------|------|--------|
| `if` | `resourcePath` | `String` | Caminho passado ao construtor (ex. `/res/levels.dat`). |
| `a` | `data` | `byte[]` | Payload carregado; **público** no decomp para acesso direto `asset.data[i]` em todo o jogo. |
| `do` | `FORMAT_VERSION` | `int` (const) | Valor `1` esperado no terceiro byte do arquivo. |

Parâmetros e locais do decomp permanecem genéricos (`if1`, `n`, `s`, `b`, `i`) no reconstruct, como no original.

---

## Port — estratégia Python + Pygame

### 1. Módulo

- `trail_biker/data/binary_asset.py` — classe `BinaryAsset` espelhando construtor e três operações de leitura.

### 2. Carregamento

- Em vez de `getResourceAsStream`, abrir arquivos a partir de `original-files/content/res/` ou `assets/res/` no disco:
  - `path = ASSETS_DIR / "metadata_mono.dat"` (sem barra inicial ou normalizar `/res/foo` → `foo`).
- Ler cabeçalho com `struct.unpack(">H", ...)` para `n`, depois `version = read(1)`, depois `data = read(n)` se `version == 1`.
- Armazenar `self.data: bytes` (imutável) ou `bytearray`; expor `data` como no Java para portes mecânicos.

### 3. API

```python
def get_unsigned_byte(self, index: int) -> int:
    return self.data[index] & 0xFF

def get_uint16(self, index: int) -> int:
    return self.get_unsigned_byte(index) * 256 + self.get_unsigned_byte(index + 1)
```

### 4. Instâncias globais (espelho do MIDlet)

- Um `BinaryAsset("metadata_mono.dat")` carregado no bootstrap do jogo (equivalente a `f.for` / `P`).
- `levels.dat` no módulo de menu/níveis (`LevelStore` / `c.c`).
- `sound.dat` no módulo de áudio (`pygame.mixer` ou chunks raw conforme parser de `d.a`).

### 5. Próximos passos após esta classe

- Documentar **mapa de índices** de `metadata_mono.dat` (hoje espalhado em `a.a.a.c.a` como `P.a[9]`, `P.a[72]`, …) — pode virar `dataclass GameConstants` com nomes quando você inventariar os índices.
- Parser de `levels.dat` permanece em `c.c`; só depende de `BinaryAsset`.

### 6. Testes

- Unit test: carregar os três `.dat` reais, assert `len(data) == n` do header, smoke `get_uint16(1)` em `sound.dat` consistente com número de efeitos.

Não depende de Pygame; pode viver em `data/` e ser testado com `pytest` antes do loop gráfico.
