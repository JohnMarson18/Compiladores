import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String[] ARQUIVOS_PADRAO = {
        "teste1_basico.lisp",
        "teste2_completo.lisp",
        "teste3_avancado.lisp",
        "teste4_erro_parentese_aberto.lisp",
        "teste5_erro_parentese_extra.lisp",
        "teste6_erro_token_invalido.lisp"
    };

    public static void main(String[] args) {
        List<String> alvosCli = new ArrayList<String>();
        for (String arg : args) {
            if (!"--nopause".equalsIgnoreCase(arg) && !"--no-pause".equalsIgnoreCase(arg)) {
                alvosCli.add(arg);
            }
        }
        String[] alvos = alvosCli.isEmpty() ? ARQUIVOS_PADRAO : alvosCli.toArray(new String[0]);
        int validos = 0;
        int comErro = 0;

        for (String caminho : alvos) {
            if (analisarArquivo(caminho)) validos++;
            else comErro++;
        }

        System.out.println("\nResumo: " + alvos.length + " processados, " + validos + " validos, " + comErro + " com erro.");
    }

    private static File resolverArquivo(String caminho) {
        File direto = new File(caminho);
        if (direto.exists()) return direto;
        File emTestes = new File("testes", caminho);
        if (emTestes.exists()) return emTestes;
        File naRaiz = new File("Outubro/completa/testes", caminho);
        if (naRaiz.exists()) return naRaiz;
        return emTestes;
    }

    private static boolean analisarArquivo(String caminho) {
        File arquivo = resolverArquivo(caminho);
        System.out.println("\nArquivo: " + caminho);

        if (!arquivo.exists()) {
            System.out.println("Erro: arquivo nao encontrado (" + arquivo.getPath() + ")");
            return false;
        }

        try {
            String codigo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
            List<Token> tokens = new ScannerSimples(codigo).escanear();

            System.out.print("Tokens (" + tokens.size() + "): ");
            for (int i = 0; i < tokens.size(); i++) {
                System.out.print(tokens.get(i) + (i < tokens.size() - 1 ? " | " : "\n"));
            }

            ParserRecursivo parser = new ParserRecursivo(tokens);
            NoSintatico arvore = parser.parsePrograma();

            List<String> passos = parser.getPassosDerivacao();
            System.out.println("Derivacao (" + passos.size() + " passos):");
            for (int i = 0; i < passos.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + passos.get(i));
            }

            System.out.println("Arvore:");
            arvore.imprimirRaiz();

            if (!parser.temErros()) {
                System.out.println("Resultado: VALIDO");
                return true;
            }
            System.out.println("Resultado: ERRO (" + parser.getErros().size() + ")");
            for (String erro : parser.getErros()) {
                System.out.println("  " + erro);
            }
            return false;
        } catch (Exception e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            return false;
        }
    }
}
