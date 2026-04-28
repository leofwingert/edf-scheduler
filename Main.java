import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
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
        sc.close();

        SimulardorCPU cpu = new SimulardorCPU();
    }
}