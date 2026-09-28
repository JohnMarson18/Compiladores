// Autores: Paulo Fernando P Junior e Diogo Siqueira

import java.io.File;
import java.io.FileReader;

public class Main {
    // Variaveis
    private static final String[] TESTES = {
        "teste1_basico.lisp",
        "teste2_completo.lisp",
        "teste3_erro.lisp",
        "teste4_avancado.lisp"
    };

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String arq : args) {
                testar(arq);
            }
        } else {
            for (String arq : TESTES) {
                testar(arq);
            }
        }
    }

    // Execucao
    private static void testar(String caminho) {
        File file = new File(caminho);
        if (!file.exists()) return;

        System.out.println("\nArquivo: " + caminho);

        try {
            FileReader reader = new FileReader(file);
            LispLexer lexer = new LispLexer(reader);
            Token token;

            while ((token = lexer.yylex()) != null) {
                System.out.println(token);
                if (token.tipo == TokenType.EOF) {
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Erro ao processar arquivo: " + e.getMessage());
        }
    }
}
