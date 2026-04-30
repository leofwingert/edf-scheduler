# Manual do Usuário — EDF Scheduler

## Requisitos

- **Java 16+** instalado (`java -version` para verificar)

---

## Como executar

O projeto já vem com o `edf-scheduler.jar` pré-compilado. Basta executar:

```bash
java -jar edf-scheduler.jar
```

> Os arquivos de programa (`.txt`) devem estar na **mesma pasta** de onde o comando é executado.

### Caso queira recompilar

```bash
javac *.java
jar cfe edf-scheduler.jar Main *.class
```

---

## Fluxo de uso

Ao iniciar, o programa solicita as informações interativamente:

### 1. Número de processos

```
Quantos processos deseja criar? 2
```

### 2. Dados de cada processo

```
--- Processo 1 ---
Nome do processo: t1
Qual programa deseja carregar? (ex: prog1.txt, prog2.txt)
prog1.txt
Arrival time: 0
Ci (unidades de computação): 25
Pi (período): 50
```

| Campo | Descrição |
|-------|-----------|
| **Nome** | Identificador do processo na saída |
| **Programa** | Arquivo `.txt` com o código assembly |
| **Arrival time** | Tick em que o processo entra na fila pela primeira vez |
| **Ci** | Tempo máximo de CPU por período (Worst Case Execution Time) |
| **Pi** | Período — também é o deadline (`dᵢ = Pᵢ`) |

### 3. Número de ciclos

```
Quantos ciclos(ticks) deseja simular? 100
```

### 4. Entradas de SYSCALL 2

Se algum programa usar `SYSCALL 2` (leitura de teclado), o simulador pausará e pedirá um valor inteiro:

```
[input] t2 aguarda valor:
42
```

---

## Saída

### Tabela de execução

```
Tempo   Executando             Fila de prontos
------------------------------------------------------------
0       t1(d=50)               t2(d=80)
1       t1(d=50)               t2(d=80)
    [print] t1: 9
2       t2(d=80)
  >> t1 encerrou
  !! t2 PERDEU O DEADLINE no tempo 80! (deadline=80)
```

| Símbolo | Significado |
|---------|-------------|
| `(d=N)` | Deadline absoluto do processo |
| `[print] nome: X` | Processo executou SYSCALL 1 — imprimiu valor X |
| `[input] nome aguarda valor:` | Processo executou SYSCALL 2 — aguarda entrada |
| `>> nome encerrou` | Processo completou o período, aguarda próxima ativação |
| `!! nome PERDEU O DEADLINE no tempo T` | Deadline miss detectado no tick T |
| `(ocioso)` | Nenhum processo pronto para executar |

### Resumo final

```
=== RESUMO ===

  Processo    Ciclos exec.   Deadline perdidas
  ----------------------------------------
  t1          50             0
  t2          35             1
```

---

## Formato dos arquivos de programa (`.txt`)

```
.code
    LOAD x        # acc = valor de x
    ADD #1        # acc = acc + 1  (modo imediato)
    STORE x       # x = acc
loop:
    SUB #1        # acc = acc - 1
    BRPOS loop    # se acc > 0, volta ao label "loop"
    SYSCALL 1     # imprime acc (bloqueia 1-3 ticks)
    SYSCALL 0     # encerra o processo
.endcode

.data
    x 10          # variável x inicializada com 10
.enddata
```

### Instruções disponíveis

| Instrução | Operação |
|-----------|----------|
| `ADD op` | `acc = acc + op` |
| `SUB op` | `acc = acc - op` |
| `MULT op` | `acc = acc * op` |
| `DIV op` | `acc = acc / op` |
| `LOAD op` | `acc = op` |
| `STORE var` | `var = acc` |
| `BRANY label` | `pc = label` (sempre) |
| `BRPOS label` | `pc = label` (se `acc > 0`) |
| `BRZERO label` | `pc = label` (se `acc = 0`) |
| `BRNEG label` | `pc = label` (se `acc < 0`) |
| `SYSCALL 0` | Encerra o processo |
| `SYSCALL 1` | Imprime `acc`; bloqueia 1–3 ticks |
| `SYSCALL 2` | Lê inteiro do teclado para `acc`; bloqueia 1–3 ticks |

### Modos de endereçamento

| Sintaxe | Modo | Exemplo |
|---------|------|---------|
| `#valor` | Imediato — usa o valor literal | `ADD #5` |
| `nome` | Direto — usa o valor da variável | `ADD x` |

> `STORE` aceita **apenas modo direto**.

### Comentários

Use `#` para comentários — exceto quando seguido de dígito ou `-` (operando imediato):

```
ADD #5      # isso é comentário, o #5 é operando imediato
LOAD x      # carrega x
```

---

## Exemplo completo

```bash
$ java -jar edf-scheduler.jar
Quantos processos deseja criar? 2

--- Processo 1 ---
Nome do processo: P1
Qual programa deseja carregar? prog1.txt
Arrival time: 0
Ci (unidades de computação): 6
Pi (período): 15

--- Processo 2 ---
Nome do processo: P2
Qual programa deseja carregar? prog2.txt
Arrival time: 0
Ci (unidades de computação): 4
Pi (período): 20

Quantos ciclos(ticks) deseja simular? 30
```
