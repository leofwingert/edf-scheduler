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

        System.out.println("\nTotal de processos criados: " + processos.size());

        SimulardorCPU cpu = new SimulardorCPU(sc);

        System.out.println("Quantos ciclos(ticks) deseja simular? ");
        int ciclos = sc.nextInt();

        List<Processo> filaProntos = new ArrayList<>(processos.stream()
                .filter(p -> p.estado == EstadoProcesso.PRONTO)
                .sorted((a, b) -> Integer.compare(a.deadline, b.deadline))
                .toList());

        List<Processo> todosProcessos = new ArrayList<>(processos);
        Map<String, Integer> ticksExec = new HashMap<>();
        Map<String, Integer> deadlineMisses = new HashMap<>();
        for (Processo p : todosProcessos) {
            ticksExec.put(p.nome, 0);
            deadlineMisses.put(p.nome, 0);
        }

        System.out.println("\nTempo   Executando             Fila de prontos");
        System.out.println("------------------------------------------------------------");

        for (int tempoAtual = 0; tempoAtual < ciclos; tempoAtual++) {
            // libera processos que chegaram agora ou sairam de bloqueio
            for (int i = processos.size() - 1; i >= 0; i--) {
                Processo p = processos.get(i);
                if ((p.estado == EstadoProcesso.ESPERANDO && p.arrival <= tempoAtual)
                 || (p.estado == EstadoProcesso.BLOQUEADO && p.bloqueado_ate <= tempoAtual)) {
                    p.estado = EstadoProcesso.PRONTO;
                    filaProntos.add(p);
                    processos.remove(i);
                }
            }

            filaProntos.sort((a, b) -> Integer.compare(a.deadline, b.deadline));

            if (filaProntos.isEmpty()) {
                System.out.printf("%-8d(ocioso)%n", tempoAtual);
                continue;
            }

            Processo processoAtual = filaProntos.get(0);
            processoAtual.estado = EstadoProcesso.EXECUTANDO;

            StringBuilder filaDisplay = new StringBuilder();
            for (int i = 1; i < filaProntos.size(); i++) {
                if (i > 1) filaDisplay.append("  ");
                filaDisplay.append(filaProntos.get(i).nome)
                           .append("(d=").append(filaProntos.get(i).deadline).append(")");
            }
            System.out.printf("%-8d%-23s%s%n", tempoAtual,
                processoAtual.nome + "(d=" + processoAtual.deadline + ")",
                filaDisplay.toString());

            String resultado = cpu.executarInstrucao(processoAtual, tempoAtual);
            processoAtual.ci_restante = processoAtual.ci_restante - 1;
            ticksExec.merge(processoAtual.nome, 1, Integer::sum);

            if (resultado.equals("bloqueado")) {
                filaProntos.remove(processoAtual);
                processos.add(processoAtual);
            } else if (resultado.equals("fim")) {
                processoAtual.estado = EstadoProcesso.TERMINADO;
                filaProntos.remove(processoAtual);
                System.out.println("  >> " + processoAtual.nome + " encerrou");
            } else if (processoAtual.ci_restante <= 0) {
                processoAtual.estado = EstadoProcesso.TERMINADO;
                filaProntos.remove(processoAtual);
                System.out.println("  >> " + processoAtual.nome + " encerrou");
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

            if (tempoAtual > processoAtual.deadline && !processoAtual.deadline_reportado) {
                System.out.println("  !! " + processoAtual.nome + " PERDEU O DEADLINE! (deadline=" + processoAtual.deadline + ")");
                processoAtual.deadline_reportado = true;
                deadlineMisses.merge(processoAtual.nome, 1, Integer::sum);
            }
        }

        System.out.println("\n--- Simulacao encerrada ---");
        System.out.println("\n=== RESUMO ===\n");
        System.out.printf("  %-12s%-15s%s%n", "Processo", "Ticks exec.", "Deadline misses");
        System.out.println("  ----------------------------------------");
        for (Processo p : todosProcessos) {
            System.out.printf("  %-12s%-15d%d%n", p.nome,
                ticksExec.getOrDefault(p.nome, 0),
                deadlineMisses.getOrDefault(p.nome, 0));
        }

        sc.close();
    }
}
