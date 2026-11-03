import java.io.File;
import java.io.FileReader;

public class Main {
    private static final String[] TESTES = {
        "testes/teste1_basico.lisp",
        "testes/teste2_completo.lisp",
        "testes/teste3_erro.lisp",
        "testes/teste4_avancado.lisp"
    };

    public static void main(String[] args) {
        String[] arquivos = args.length > 0 ? args : TESTES;
        for (String arq : arquivos) {
            testar(arq);
        }
    }

    private static void testar(String caminho) {
        File file = new File(caminho);
        if (!file.exists()) file = new File("testes", caminho);
        if (!file.exists()) return;

        System.out.println("\nArquivo: " + file.getName());
        try (FileReader reader = new FileReader(file)) {
            LispLexer lexer = new LispLexer(reader);
            Token token;
            while ((token = lexer.yylex()) != null) {
                System.out.println(token);
                if (token.tipo == TokenType.EOF) break;
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
