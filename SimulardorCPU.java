import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
// logging done via System.out.println instead of java.util.function.Consumer

public class SimulardorCPU {
    private final Random random = new Random();
    private final Scanner inputScanner;

    public SimulardorCPU() {
        this(new Scanner(System.in));
    }

    public SimulardorCPU(Scanner inputScanner) {
        this.inputScanner = inputScanner;
    }

    public String executarInstrucao(Processo processo, int tempo) {
        if (processo.pc >= processo.code.size()) {
            processo.estado = EstadoProcesso.TERMINADO;
            return "fim";
        }

        String instrucao = processo.code.get(processo.pc);
        List<String> partes = new ArrayList<>();
        try (Scanner sc = new Scanner(instrucao)) {
            while (sc.hasNext()) {
                partes.add(sc.next());
            }
        }
        if (partes.isEmpty()) {
            processo.pc += 1;
            return "ok";
        }

        String operacao = partes.get(0).toUpperCase();
        processo.pc += 1;

        if (operacao.equals("ADD")) {
            processo.acc += resolverOperando(processo, getArg(partes, 1));
        } else if (operacao.equals("SUB")) {
            processo.acc -= resolverOperando(processo, getArg(partes, 1));
        } else if (operacao.equals("MULT")) {
            processo.acc *= resolverOperando(processo, getArg(partes, 1));
        } else if (operacao.equals("DIV")) {
            int divisor = resolverOperando(processo, getArg(partes, 1));
            if (divisor == 0) {
                System.out.println("    [ERRO] " + processo.nome + ": divisao por zero (pc=" + (processo.pc - 1) + ")");
                processo.estado = EstadoProcesso.TERMINADO;
                return "fim";
            }
            processo.acc /= divisor;
        } else if (operacao.equals("LOAD")) {
            processo.acc = resolverOperando(processo, getArg(partes, 1));
        } else if (operacao.equals("STORE")) {
            String varName = getArg(partes, 1);
            processo.data.put(varName.toLowerCase(), processo.acc);
        } else if (operacao.equals("BRANY")) {
            processo.pc = resolverLabel(processo, getArg(partes, 1));
        } else if (operacao.equals("BRPOS")) {
            if (processo.acc > 0) {
                processo.pc = resolverLabel(processo, getArg(partes, 1));
            }
        } else if (operacao.equals("BRZERO")) {
            if (processo.acc == 0) {
                processo.pc = resolverLabel(processo, getArg(partes, 1));
            }
        } else if (operacao.equals("BRNEG")) {
            if (processo.acc < 0) {
                processo.pc = resolverLabel(processo, getArg(partes, 1));
            }
        } else if (operacao.equals("SYSCALL")) {
            int idx;
            try {
                idx = Integer.parseInt(getArg(partes, 1));
            } catch (NumberFormatException e) {
                System.out.println("    [ERRO] " + processo.nome + ": SYSCALL invalido (pc=" + (processo.pc - 1) + ")");
                return "ok";
            }

            if (idx == 0) {
                processo.estado = EstadoProcesso.TERMINADO;
                return "fim";
            } else if (idx == 1) {
                System.out.println("    [print] " + processo.nome + ": " + processo.acc);
                // simula tempo de input aleatorio entre 1 e 3 ticks
                processo.bloqueado_ate = tempo + 1 + random.nextInt(3);
                processo.estado = EstadoProcesso.BLOQUEADO;
                return "bloqueado";
            } else if (idx == 2) {
                System.out.println("    [input] " + processo.nome + " aguarda valor: ");
                String entrada = "";
                while (entrada.isEmpty() && inputScanner.hasNextLine()) {
                    entrada = inputScanner.nextLine().trim();
                }
                try {
                    processo.acc = Integer.parseInt(entrada);
                } catch (NumberFormatException e) {
                    System.out.println("    [ERRO] " + processo.nome + ": input invalido, usando 0");
                    processo.acc = 0;
                }
                // simula tempo de input aleatorio entre 1 e 3 ticks
                processo.bloqueado_ate = tempo + 1 + random.nextInt(3);
                processo.estado = EstadoProcesso.BLOQUEADO;
                return "bloqueado";
            } 
        }
        return "ok";
    }

    private int resolverOperando(Processo proc, String operando) {
        //verifica se é um valor imediato
        if (operando.startsWith("#")) {
            String valorImediato = operando.substring(1);
                return Integer.parseInt(valorImediato);
        }

        String chave = operando.toLowerCase();
        //verifica se é uma variável local do .data
        Integer valor = proc.data.get(chave);
        if (valor == null) {
            System.out.println("    [ERRO] " + proc.nome + ": variavel nao encontrada '" + operando + "'");
            return 0;
        }
        return valor;
    }

    private int resolverLabel(Processo proc, String label) {
        Integer idx = proc.labels.get(label.toLowerCase());
        return idx;
    }

    private String getArg(List<String> partes, int index) {
        if (index >= partes.size()) {
            return "";
        }
        return partes.get(index);
    }    
}
