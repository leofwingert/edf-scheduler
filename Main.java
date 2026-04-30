import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        simular();
    }

    public static void simular() {
        Scanner sc = new Scanner(System.in);

        List<Processo> processos = lerProcessos(sc);
        System.out.println("\nTotal de processos criados: " + processos.size());

        SimuladorCPU cpu = new SimuladorCPU(sc);

        System.out.println("Quantos ciclos(ticks) deseja simular? ");
        int ciclos = sc.nextInt();

        List<Processo> filaProntos = new ArrayList<>(processos.stream()
                .filter(p -> p.estado == EstadoProcesso.PRONTO)
                .sorted((a, b) -> Integer.compare(a.deadline, b.deadline))
                .toList());

        List<Processo> todosProcessos = new ArrayList<>(processos);
        Map<String, Integer> ciclosExecutados = new HashMap<>();
        Map<String, Integer> deadlinePerdidas = new HashMap<>();
        for (Processo p : todosProcessos) {
            ciclosExecutados.put(p.nome, 0);
            deadlinePerdidas.put(p.nome, 0);
        }

        imprimirCabecalho();

        for (int tempoAtual = 0; tempoAtual < ciclos; tempoAtual++) {
            // libera processos que chegaram agora ou sairam de bloqueio para os prontos
            for (int i = processos.size() - 1; i >= 0; i--) {
                Processo p = processos.get(i);
                if ((p.estado == EstadoProcesso.ESPERANDO && p.arrival <= tempoAtual)
                 || (p.estado == EstadoProcesso.BLOQUEADO && p.bloqueado_ate <= tempoAtual)) {
                    p.estado = EstadoProcesso.PRONTO;
                    filaProntos.add(p);
                    processos.remove(i);
                }
            }
            // ordena fila de prontos por menor deadline
            filaProntos.sort((a, b) -> Integer.compare(a.deadline, b.deadline));

            if (filaProntos.isEmpty()) {
                imprimirOcioso(tempoAtual);
                continue;
            }

            // executa processo atual (com menor deadline)
            Processo processoAtual = filaProntos.get(0);
            processoAtual.estado = EstadoProcesso.EXECUTANDO;

            imprimirTick(tempoAtual, processoAtual, filaProntos);

            String resultado = cpu.executarInstrucao(processoAtual, tempoAtual);
            // atualiza variáveis de controle do processo
            processoAtual.ci_restante = processoAtual.ci_restante - 1;
            ciclosExecutados.put(processoAtual.nome, ciclosExecutados.get(processoAtual.nome) + 1);

            // verifica se perdeu deadline, se sim marca como perdido e printa
            if (tempoAtual >= processoAtual.deadline && !processoAtual.deadline_reportado) {
                imprimirDeadlineMiss(processoAtual, tempoAtual);
                processoAtual.deadline_reportado = true;
                deadlinePerdidas.put(processoAtual.nome, deadlinePerdidas.get(processoAtual.nome) + 1);
            }

            // verifica retorno da cpu
            if (resultado.equals("bloqueado")) {
                filaProntos.remove(processoAtual);
                processos.add(processoAtual);
            } else if (resultado.equals("fim") || processoAtual.ci_restante <= 0) {
                filaProntos.remove(processoAtual);
                imprimirEncerrou(processoAtual);
                processoAtual.arrival = processoAtual.proximo_arrival;
                processoAtual.deadline = processoAtual.proximo_arrival + processoAtual.periodo;
                processoAtual.proximo_arrival += processoAtual.periodo;
                processoAtual.ci_restante = processoAtual.ci;
                processoAtual.pc = 0;
                processoAtual.estado = EstadoProcesso.ESPERANDO;
                processoAtual.deadline_reportado = false;
                processos.add(processoAtual);
            } else {
                processoAtual.estado = EstadoProcesso.PRONTO;
            }
        }

        imprimirResumo(todosProcessos, ciclosExecutados, deadlinePerdidas);
        sc.close();
    }

    // input 
    static List<Processo> lerProcessos(Scanner sc) {
        System.out.print("Quantos processos deseja criar? ");
        int quantidade = sc.nextInt();
        sc.nextLine();

        Parser parser = new Parser();
        List<Processo> processos = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {
            System.out.println("\n--- Processo " + (i + 1) + " ---");
            System.out.print("Nome do processo: ");
            String nome = sc.nextLine();
            System.out.println("Qual programa deseja carregar? (ex: prog1.txt, prog2.txt)");
            String nomePrograma = sc.nextLine();
            Parser.Program prog = parser.carregarPrograma(nomePrograma);
            System.out.print("Arrival time: ");
            int arrival = sc.nextInt();
            System.out.print("Ci (unidades de computação): ");
            int ci = sc.nextInt();
            System.out.print("Pi (período): ");
            int periodo = sc.nextInt();
            sc.nextLine();

            Processo p = new Processo(nome, arrival, periodo, ci, prog.code, prog.data, prog.labels);
            processos.add(p);
            System.out.println("Processo criado: " + p.nome);
        }

        return processos;
    }

    // prints
    static void imprimirCabecalho() {
        System.out.println("\nTempo   Executando             Fila de prontos");
        System.out.println("------------------------------------------------------------");
    }

    static void imprimirTick(int tempoAtual, Processo processoAtual, List<Processo> filaProntos) {
        StringBuilder filaDisplay = new StringBuilder();
        for (int i = 1; i < filaProntos.size(); i++) {
            if (i > 1) filaDisplay.append("  ");
            filaDisplay.append(filaProntos.get(i).nome)
                       .append("(d=").append(filaProntos.get(i).deadline).append(")");
        }
        System.out.printf("%-8d%-23s%s%n", tempoAtual,
            processoAtual.nome + "(d=" + processoAtual.deadline + ")",
            filaDisplay.toString());
    }

    static void imprimirOcioso(int tempoAtual) {
        System.out.printf("%-8d(ocioso)%n", tempoAtual);
    }

    static void imprimirEncerrou(Processo p) {
        System.out.println("  >> " + p.nome + " encerrou");
    }

    static void imprimirDeadlineMiss(Processo p, int tempoAtual) {
        System.out.println("  !! " + p.nome + " PERDEU O DEADLINE no tempo " + tempoAtual + "! (deadline=" + p.deadline + ")");
    }

    static void imprimirResumo(List<Processo> processos, Map<String, Integer> ciclosExecutados, Map<String, Integer> deadlinePerdidas) {
        System.out.println("\n--- Simulacao encerrada ---");
        System.out.println("\n=== RESUMO ===\n");
        System.out.printf("  %-12s%-15s%s%n", "Processo", "Ciclos exec.", "Deadline perdidas");
        System.out.println("  ----------------------------------------");
        for (Processo p : processos) {
            System.out.printf("  %-12s%-15d%d%n", p.nome,
                ciclosExecutados.getOrDefault(p.nome, 0),
                deadlinePerdidas.getOrDefault(p.nome, 0));
        }
    }
}
