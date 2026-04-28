import java.util.List;
import java.util.Map;

public class Processo {
    String nome;
    int arrival;
    int periodo;
    int ci;
    List<String> code;
    Map<String, Integer> data;
    Map<String, Integer> labels;
    int pc;
    int acc;
    EstadoProcesso estado;
    int deadline;
    int proximo_arrival;
    int ci_restante;
    int bloqueado_ate;
    boolean deadline_reportado;

    public Processo(String nome, int arrival, int periodo, int ci, List<String> code, Map<String, Integer> data,
            Map<String, Integer> labels) {
        this.nome = nome;
        this.arrival = arrival;
        this.periodo = periodo;
        this.ci = ci;
        this.code = code;
        this.data = data;
        this.labels = labels;
        this.pc = 0;
        this.acc = 0;
        this.estado = EstadoProcesso.PRONTO;
        this.deadline = arrival + periodo;
        this.proximo_arrival = arrival + periodo;
        this.ci_restante = ci;
        this.bloqueado_ate = -1;
        this.deadline_reportado = false;
    }
}
