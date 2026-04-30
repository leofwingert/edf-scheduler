import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Parser {

    public static class Program {
        public final List<String> code = new ArrayList<>();
        public final Map<String, Integer> data = new HashMap<>();
        public final Map<String, Integer> labels = new HashMap<>();
    }

    private String removerComentario(String linha) {
        if (linha == null)
            return null;
        int len = linha.length();
        for (int i = 0; i < len; i++) {
            char c = linha.charAt(i);
            if (c == '#') {
                char proximo = (i + 1 < len) ? linha.charAt(i + 1) : '\0';
                if (Character.isDigit(proximo) || proximo == '-') {
                    // é operando imediato, ignora este '#' e continua procurando
                    continue;
                }
                // é comentário, retorna até aqui
                return linha.substring(0, i);
            }
        }
        return linha;
    }

    public Program carregarPrograma(String arquivoPath) {
        Program p = new Program();
        File arquivo = new File(arquivoPath);
        String modo = null;

        try (Scanner sc = new Scanner(arquivo)) {
            while (sc.hasNextLine()) {
                String linha = sc.nextLine();
                linha = removerComentario(linha);
                if (linha == null)
                    continue;
                linha = linha.trim();
                if (linha.isEmpty())
                    continue;

                String l = linha.toLowerCase();
                if (l.equals(".code")) {
                    modo = "code";
                    continue;
                } else if (l.equals(".endcode")) {
                    modo = null;
                    continue;
                } else if (l.equals(".data")) {
                    modo = "data";
                    continue;
                } else if (l.equals(".enddata")) {
                    modo = null;
                    continue;
                }

                if ("code".equals(modo)) {
                    if (linha.contains(":")) {
                        int idx = linha.indexOf(":");
                        String label = linha.substring(0, idx).trim().toLowerCase();
                        String resto = linha.substring(idx + 1).trim();
                        p.labels.put(label, p.code.size());
                        if (!resto.isEmpty()) {
                            p.code.add(resto);
                        }
                    } else {
                        p.code.add(linha);
                    }
                } else if ("data".equals(modo)) {
                    java.util.List<String> partesList = new ArrayList<>();
                    try (Scanner lsc = new Scanner(linha)) {
                        while (lsc.hasNext()) {
                            partesList.add(lsc.next());
                        }
                    }
                    if (partesList.size() >= 2) {
                        try {
                            int valor = Integer.parseInt(partesList.get(1));
                            p.data.put(partesList.get(0).toLowerCase(), valor);
                        } catch (NumberFormatException e) {
                            System.out.println("  [AVISO] valor nao inteiro em .data: '" + linha + "'");
                        }
                    } else {
                        System.out.println("  [AVISO] linha malformada em .data: '" + linha + "'");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + arquivoPath);
        }

        return p;
    }
}
