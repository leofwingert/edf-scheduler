# TP1 - Projeto de Execução Dinâmica de Processos
**Sistemas Operacionais**

O presente trabalho tem por objetivo explorar temas referentes ao escalonamento e troca entre processos que utilizam um dado processador. É previsto o desenvolvimento de um ambiente que empregue uma política de escalonamento específica, bem como gerencie a inclusão e remoção de processos que ocupam o processador. A carga de processos deverá ser realizada a partir de programas que utilizarão uma linguagem assembly hipotética.

---

## Descrição de programas e características de execução e ocupação

O usuário deverá ser capaz de descrever pequenos programas a serem executados pelo ambiente. O ambiente de execução é baseado em acumulador. Assim, para a execução de um programa, dois registradores estão presentes:
- **(i)** o acumulador (`acc`) onde as operações são realizadas
- **(ii)** o ponteiro da instrução em execução (`pc`)

### Tabela 1 - Mnemônicos e funções associadas

| Categoria  | Mnemônico      | Função                             |
|------------|----------------|------------------------------------|
| Aritmético | `ADD op1`      | `acc = acc + (op1)`                |
| Aritmético | `SUB op1`      | `acc = acc – (op1)`                |
| Aritmético | `MULT op1`     | `acc = acc * (op1)`                |
| Aritmético | `DIV op1`      | `acc = acc / (op1)`                |
| Memória    | `LOAD op1`     | `acc = (op1)`                      |
| Memória    | `STORE op1`    | `(op1) = acc`                      |
| Salto      | `BRANY label`  | `pc ← label`                       |
| Salto      | `BRPOS label`  | Se `acc > 0` então `pc ← label`    |
| Salto      | `BRZERO label` | Se `acc = 0` então `pc ← label`    |
| Salto      | `BRNEG label`  | Se `acc < 0` então `pc ← label`    |
| Sistema    | `SYSCALL index`| Chamada de sistema                 |

### Modos de Endereçamento

- **Imediato**: prefixo `#` antes do operando (ex: `SUB #1`) — usa o valor literal
- **Direto**: sem prefixo (ex: `LOAD variable`) — usa o valor na memória de dados

Instruções aritméticas e `LOAD` suportam ambos os modos. `STORE` suporta **apenas modo direto**.

Labels são definidas com o formato `nome:` (alfanumérico seguido de dois pontos).

### Figura 1 - Código exemplo

```asm
.code
    LOAD variable     # Carrega em acc o conteúdo de variable
ponto1: SUB #1        # Subtrai do acc um valor constante (i.e. 1)
    SYSCALL 1         # Imprime na tela o conteúdo do acumulador
    BRPOS ponto1      # Caso acc>0 deve voltar à linha marcada por "ponto1"
    SYSCALL 0         # Sinaliza o fim do programa
.endcode

.data
    Variable 3        # Conteúdo da posição 1 da área de dados é 3
.enddata
```

### Chamadas de sistema (`SYSCALL`)

| Index | Comportamento |
|-------|---------------|
| `0`   | Finalização/encerramento do programa (halt) |
| `1`   | Impressão de valor inteiro na tela — **bloqueia processo** |
| `2`   | Leitura de valor inteiro via teclado — **bloqueia processo** |

> Para `SYSCALL 1` e `SYSCALL 2`: o processo é bloqueado por um tempo **aleatório entre 1 e 3 unidades de tempo**.

### Características de memória e execução

- Cada instrução ocupa **uma posição** de memória (independente da categoria)
- Cada variável ocupa **uma posição** de memória
- Total de posições = número de instruções + número de variáveis
- Organização/layout de memória está **fora do escopo** do trabalho
- Valores de variáveis e operandos imediatos podem ser **positivos ou negativos**
- Cada instrução executa em **uma unidade de tempo**

---

## Política de Escalonamento

Deve ser implementada a política **EDF (Earliest Deadline First)**.

> Inicialize uma variável no programa principal chamada `sch_pol` com valor `1`.

O EDF é um esquema de **prioridades dinâmicas** com escalonador preemptivo. As premissas do modelo de tarefas:

- **a.** As tarefas são periódicas e independentes
- **b.** O deadline de cada tarefa coincide com seu período: `di = Pi`
- **c.** O tempo de computação `Ci` é conhecido e constante (WCCT)
- **d.** Tempo de chaveamento entre tarefas é **nulo**

A tarefa mais prioritária é a que tem o **deadline absoluto mais próximo** do tempo atual. A cada chegada de tarefa, a fila de prontos é reordenada.

### Transições de estado

Uma tarefa deixa o estado **running** quando:
1. Seu tempo de computação `Ci` é alcançado (fim do período `Pi`) → volta à fila de prontos
2. Uma tarefa de maior prioridade está pronta → volta à fila de prontos
3. Realiza uma chamada de sistema → vai para **bloqueado** por 1–3 unidades de tempo

Após o bloqueio, a tarefa avança para **pronto** e sua prioridade é recalculada conforme EDF.

Ao ser retomada, a tarefa **continua do último ponto de parada** (contexto preservado).

---

## Interface da Aplicação

O ambiente deve permitir definir:
- Qual(is) tarefa(s) será(ão) carregada(s)
- O(s) instante(s) de carga (*arrival time*)
- O tempo de computação de cada tarefa (`Ci`)
- O período de cada tarefa (`Pi`) — onde `deadline = Pi`

Como saída, o sistema deve mostrar de forma **clara e inequívoca**:
- O escalonamento das tarefas ao longo do tempo
- Sinalização de **perda de deadline**, informando o nome da tarefa e o instante de tempo

---

## Informações Adicionais

- **Grupos:** 4 alunos (obrigatoriamente)
- **Entrega:** código fonte + manual do usuário em PDF (como compilar e executar)
- **Linguagem:** livre, desde que compile e execute nos laboratórios do prédio 32
- **Data de entrega:** 30/04/2026 até às 17h15
- **Apresentações:** 30/04/2026 ou 05/05/2026 (horários disponíveis no Moodle)
- **Entrega no Moodle:** arquivo `.zip` com nome contendo nome e sobrenome de todos os integrantes
- Arquivos corrompidos = não entrega; erro de compilação = não aceito; plágio = nota zero
