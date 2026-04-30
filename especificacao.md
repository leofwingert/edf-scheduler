# TP1 — Projeto de Execução Dinâmica de Processos

**Disciplina:** Sistemas Operacionais

O presente trabalho tem por objetivo explorar temas referentes ao escalonamento e troca entre processos que utilizam um dado processador. É previsto o desenvolvimento de um ambiente que empregue uma política de escalonamento específica, bem como gerencie a inclusão e remoção de processos que ocupam o processador. A carga de processos deverá ser realizada a partir de programas que utilizarão uma linguagem assembly hipotética.

---

## 1. Descrição de programas e características de execução

O usuário deverá ser capaz de descrever pequenos programas a serem executados pelo ambiente. O ambiente de execução é baseado em acumulador. Para a execução de um programa, dois registradores estão presentes:

- **(i)** `acc` — acumulador, onde as operações são realizadas
- **(ii)** `pc` — ponteiro da instrução em execução

### 1.1 Conjunto de instruções

**Tabela 1 — Mnemônicos e funções associadas**

| Categoria   | Mnemônico        | Função                              |
|-------------|------------------|-------------------------------------|
| Aritmético  | `ADD op1`        | `acc = acc + (op1)`                 |
|             | `SUB op1`        | `acc = acc – (op1)`                 |
|             | `MULT op1`       | `acc = acc * (op1)`                 |
|             | `DIV op1`        | `acc = acc / (op1)`                 |
| Memória     | `LOAD op1`       | `acc = (op1)`                       |
|             | `STORE op1`      | `(op1) = acc`                       |
| Salto       | `BRANY label`    | `pc ← label`                        |
|             | `BRPOS label`    | Se `acc > 0` então `pc ← label`     |
|             | `BRZERO label`   | Se `acc = 0` então `pc ← label`     |
|             | `BRNEG label`    | Se `acc < 0` então `pc ← label`     |
| Sistema     | `SYSCALL index`  | Chamada de sistema                  |

### 1.2 Modos de endereçamento

As instruções aritméticas e `LOAD` suportam dois modos:

| Modo       | Sintaxe   | Comportamento                                      |
|------------|-----------|----------------------------------------------------|
| **Imediato** | `#valor` | O valor literal é usado diretamente na operação    |
| **Direto**   | `nome`   | O valor da variável declarada em `.data` é usado   |

> `STORE` suporta **apenas modo direto** — um dado só pode ser escrito em variável declarada na área de dados.

### 1.3 Labels

Para desvios, declare um label com nome alfanumérico seguido de `:`:

```
ponto1: SUB #1
```

### 1.4 Chamadas de sistema (SYSCALL)

| Index | Comportamento                                                              |
|-------|----------------------------------------------------------------------------|
| `0`   | Finaliza o programa *(halt)*                                               |
| `1`   | Imprime o valor inteiro do `acc`; **bloqueia o processo por 1–3 unidades** |
| `2`   | Lê um inteiro via teclado para o `acc`; **bloqueia o processo por 1–3 unidades** |

### 1.5 Exemplo de programa

```
.code
    LOAD variable       # acc ← 3
ponto1:
    SUB #1              # acc ← acc - 1
    SYSCALL 1           # imprime acc; bloqueia
    BRPOS ponto1        # se acc > 0, volta ao ponto1
    SYSCALL 0           # fim do programa
.endcode

.data
    Variable 3          # variável inicializada com 3
.enddata
```

### 1.6 Ocupação de memória

- Cada instrução ocupa **uma posição** de memória, independente da categoria.
- Cada variável ocupa **uma posição** de memória.
- Total de posições = nº de instruções + nº de variáveis.
- A organização de ocupação entre processos está **fora do escopo** deste trabalho.
- Valores de variáveis e operandos imediatos podem ser **positivos ou negativos**.
- Cada instrução executa em **uma unidade de tempo**.

---

## 2. Política de escalonamento — EDF

A política implementada é o **EDF (Earliest Deadline First)**: escalonamento preemptivo com prioridades dinâmicas, produzido em tempo de execução.

### 2.1 Premissas do modelo de tarefas

| | Premissa |
|---|---|
| **a** | As tarefas são periódicas e independentes |
| **b** | O deadline coincide com o período: `dᵢ = Pᵢ` |
| **c** | O tempo de computação `Cᵢ` é conhecido e constante *(Worst Case Execution Time)* |
| **d** | O tempo de troca de contexto é **nulo** |

### 2.2 Regra de prioridade

A tarefa mais prioritária é a de **menor deadline absoluto `dᵢ`**. A cada chegada de tarefa, a fila de prontos é reordenada. A cada ativação de `Tᵢ`, um novo deadline absoluto é calculado. O conjunto de tarefas é assumido **escalonável**.

### 2.3 Quando uma tarefa deixa o estado *running*

| Condição | Estado seguinte |
|----------|-----------------|
| **(i)** Tempo de computação `Cᵢ` alcançado *(fim do período `Pᵢ`)* | **Pronto** — nova prioridade EDF |
| **(ii)** Tarefa de maior prioridade ficou pronta | **Pronto** — nova prioridade EDF |
| **(iii)** `SYSCALL 1` ou `SYSCALL 2` | **Bloqueado** por 1–3 unidades aleatórias → depois **Pronto** |

### 2.4 Troca de contexto

Quando uma tarefa A é preemptada por B, ao retomar A ela **continua da última instrução parada** (`pc` e `acc` são preservados). O tempo de troca de contexto é desconsiderado.

---

## 3. Interface da aplicação

O ambiente deve permitir configurar, para cada tarefa:

| Parâmetro | Descrição |
|-----------|-----------|
| Programa  | Arquivo `.txt` com o código assembly |
| *Arrival time* | Instante de carga da tarefa |
| `Cᵢ` | Tempo de computação (unidades) |
| `Pᵢ` | Período *(= deadline, pois `dᵢ = Pᵢ`)* |

**Saída obrigatória:**
- Escalonamento das tarefas ao longo do tempo, de forma **clara e inequívoca**.
- Sinalização de **perda de deadline**, informando o **nome da tarefa** e o **instante de tempo**.

---

## 4. Informações adicionais

| | |
|---|---|
| **Grupos** | 4 alunos (obrigatório) |
| **Entrega** | Código-fonte + manual do usuário em PDF (compilação e execução) |
| **Linguagem** | Livre — deve compilar e executar no ambiente do **prédio 32** |
| **Prazo** | **30/04/2026 até 17h15** |
| **Apresentações** | 30/04/2026 ou 05/05/2026 — horário escolhido no Moodle |
| **Arquivo** | `.zip` com nome contendo nome e sobrenome de todos os integrantes |

> ⚠️ Arquivos corrompidos = não entrega. Erro de compilação = não avaliado. Plágio = **nota zero**.
