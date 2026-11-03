import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Main {

    private static final String[] TESTES_PADRAO = {
        "testes/teste1_basico.lisp",
        "testes/teste2_lista_vazia.lisp",
        "testes/teste3_completo.lisp",
        "testes/teste4_erro_parentese_aberto.lisp",
        "testes/teste5_erro_parentese_extra.lisp",
        "testes/teste6_erro_lexico.lisp"
    };

    public static void main(String[] args) {
        ParserLL1 parser = new ParserLL1(true);
        parser.getTabela().imprimirResumoCompleto();

        String[] arquivos = (args.length > 0) ? args : TESTES_PADRAO;
        int aceitos = 0, rejeitados = 0;

        for (String caminho : arquivos) {
            File arquivo = new File(caminho);
            if (!arquivo.exists()) {
                File alt = new File("testes", caminho);
                if (alt.exists()) {
                    arquivo = alt;
                } else {
                    System.out.println("\nArquivo nao encontrado: " + caminho);
                    continue;
                }
            }

            System.out.println("\nArquivo: " + caminho);
            try {
                String codigo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
                System.out.println("Codigo: " + codigo.trim().replace("\r", "").replace("\n", " "));
                List<Token> tokens = LispLexer.tokenizar(codigo);
                if (parser.analisar(tokens)) {
                    aceitos++;
                    System.out.println("Resultado: VALIDO (" + parser.getTotalPassos() + " passos)");
                } else {
                    rejeitados++;
                    System.out.println("Resultado: INVALIDO - " + parser.getUltimoErro());
                }
            } catch (Exception e) {
                rejeitados++;
                System.out.println("Erro ao ler arquivo: " + e.getMessage());
            }
        }

        System.out.println("\nResumo final: " + aceitos + " aceitos, " + rejeitados + " com erro");
    }
}
