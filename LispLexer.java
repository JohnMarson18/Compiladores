import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * PROJETO DE COMPILADORES - MES 1 (SETEMBRO)
 * Analisador Lexico (Scanner) para a Gramatica da Linguagem Lisp
 * 
 * Implementacao em Java pura (100% compativel com Java 8+).
 * Rastreia linha e coluna de cada token e trata erros lexicos sem quebrar
 * a execucao (fail-safe error recovery).
 * ============================================================================
 */
public class LispLexer {

    // ------------------------------------------------------------------------
    // 1. Enumeracao dos Tipos de Tokens (Gramatica Lisp)
    // ------------------------------------------------------------------------
    public enum TokenType {
        LPAREN,     // Delimitador '('
        RPAREN,     // Delimitador ')'
        BOOLEAN,    // Literal Booleano: T, NIL, #t, #f
        NUMBER,     // Literal Numerico: inteiros ou decimais com sinal opcional (+10, -2.5, 100)
        STRING,     // Literal String: texto entre aspas ("exemplo")
        SYMBOL,     // Simbolo / Identificador: funcoes, variaveis, operadores (+, -, verifica, if)
        ERROR,      // Caractere ou padrao lexico invalido (ex: @)
        EOF         // Fim de arquivo (End Of File)
    }

    // ------------------------------------------------------------------------
    // 2. Classe que representa um Token
    // ------------------------------------------------------------------------
    public static class Token {
        public final TokenType type;
        public final String lexeme;
        public final Object literal;
        public final int line;
        public final int column;
        public final String errorMessage;

        public Token(TokenType type, String lexeme, Object literal, int line, int column) {
            this(type, lexeme, literal, line, column, null);
        }

        public Token(TokenType type, String lexeme, Object literal, int line, int column, String errorMessage) {
            this.type = type;
            this.lexeme = lexeme;
            this.literal = literal;
            this.line = line;
            this.column = column;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            String valStr = (literal != null) ? " -> " + literal : "";
            if (type == TokenType.ERROR) {
                return String.format("[Linha %2d, Col %2d] %-8s : '%s' (%s)", line, column, type, lexeme, errorMessage);
            }
            return String.format("[Linha %2d, Col %2d] %-8s : '%s'%s", line, column, type, lexeme, valStr);
        }
    }

    // ------------------------------------------------------------------------
    // 3. Classe para Registro de Erros Lexicos
    // ------------------------------------------------------------------------
    public static class LexicalError {
        public final int line;
        public final int column;
        public final String offendingText;
        public final String message;

        public LexicalError(int line, int column, String offendingText, String message) {
            this.line = line;
            this.column = column;
            this.offendingText = offendingText;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("Erro Lexico na Linha %d, Coluna %d: %s [Trecho: '%s']",
                    line, column, message, offendingText);
        }
    }

    // ------------------------------------------------------------------------
    // 4. Atributos do Analisador Lexico
    // ------------------------------------------------------------------------
    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private final List<LexicalError> errors = new ArrayList<LexicalError>();

    private int start = 0;         // Posicao inicial do lexema atual no source
    private int current = 0;       // Posicao atual de leitura no source
    private int line = 1;          // Linha atual (1-indexada)
    private int column = 1;        // Coluna atual (1-indexada)
    private int tokenStartCol = 1; // Coluna em que o token atual iniciou

    // ------------------------------------------------------------------------
    // 5. Construtor
    // ------------------------------------------------------------------------
    public LispLexer(String source) {
        this.source = (source != null) ? source : "";
    }

    // ------------------------------------------------------------------------
    // 6. Metodo Principal: tokenize()
    // ------------------------------------------------------------------------
    public List<Token> tokenize() {
        tokens.clear();
        errors.clear();
        start = 0;
        current = 0;
        line = 1;
        column = 1;

        while (!isAtEnd()) {
            start = current;
            tokenStartCol = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    public List<LexicalError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    // ------------------------------------------------------------------------
    // 7. Logica de Varredura Caractere por Caractere
    // ------------------------------------------------------------------------
    private void scanToken() {
        char c = advance();

        switch (c) {
            // Delimitadores da linguagem
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;

            // Espacos em branco (ignorados com contagem correta de linha e coluna)
            case ' ':
            case '\t':
            case '\r':
                break;

            case '\n':
                line++;
                column = 1;
                break;

            // Comentarios da linguagem Lisp (; ate o fim da linha)
            case ';':
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
                break;

            // Strings delimitadas por aspas duplas ("...")
            case '"':
                scanString();
                break;

            // Booleanos prefixados por '#' (#t ou #f)
            case '#':
                scanHashBoolean();
                break;

            // Operadores que podem iniciar numero (+10, -2.5) ou simbolo (+, -)
            case '+':
            case '-':
                if (isDigit(peek())) {
                    scanNumber();
                } else {
                    scanSymbol();
                }
                break;

            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isInitialChar(c)) {
                    scanSymbol();
                } else {
                    // Caractere invalido (exemplo: @, $, etc.)
                    String invalidLexeme = String.valueOf(c);
                    String msg = "Caractere invalido ou inesperado '" + c + "'";
                    errors.add(new LexicalError(line, tokenStartCol, invalidLexeme, msg));
                    tokens.add(new Token(TokenType.ERROR, invalidLexeme, null, line, tokenStartCol, msg));
                }
                break;
        }
    }

    // ------------------------------------------------------------------------
    // 8. Reconhecimento de Strings (<string>)
    // ------------------------------------------------------------------------
    private void scanString() {
        StringBuilder value = new StringBuilder();

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            if (peek() == '\\' && peekNext() == '"') {
                advance(); // consome a barra
                value.append(advance()); // consome as aspas escapadas
            } else {
                value.append(advance());
            }
        }

        if (isAtEnd()) {
            String lexeme = source.substring(start, current);
            String msg = "String nao finalizada (esperada aspas duplas de fechamento)";
            errors.add(new LexicalError(line, tokenStartCol, lexeme, msg));
            tokens.add(new Token(TokenType.ERROR, lexeme, null, line, tokenStartCol, msg));
            return;
        }

        // Consome as aspas de fechamento '"'
        advance();

        String lexeme = source.substring(start, current);
        tokens.add(new Token(TokenType.STRING, lexeme, value.toString(), line, tokenStartCol));
    }

    // ------------------------------------------------------------------------
    // 9. Reconhecimento de Booleanos #t e #f
    // ------------------------------------------------------------------------
    private void scanHashBoolean() {
        char next = peek();
        if (next == 't' || next == 'f') {
            advance();
            if (isDelimiterOrWhitespace(peek())) {
                String lexeme = source.substring(start, current);
                boolean boolVal = (next == 't');
                tokens.add(new Token(TokenType.BOOLEAN, lexeme, Boolean.valueOf(boolVal), line, tokenStartCol));
                return;
            }
        }

        // Se nao for #t ou #f valido, registra erro lexico sem quebrar o analisador
        while (!isDelimiterOrWhitespace(peek()) && !isAtEnd()) {
            advance();
        }
        String lexeme = source.substring(start, current);
        String msg = "Constante ou simbolo iniciado por '#' invalido: '" + lexeme + "' (esperado #t ou #f)";
        errors.add(new LexicalError(line, tokenStartCol, lexeme, msg));
        tokens.add(new Token(TokenType.ERROR, lexeme, null, line, tokenStartCol, msg));
    }

    // ------------------------------------------------------------------------
    // 10. Reconhecimento de Numeros (<number>)
    // Gramatica: ["+" | "-"] <digits> ["." <digits>]
    // ------------------------------------------------------------------------
    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        // Ponto decimal opcional seguido de um ou mais digitos
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consome o '.'
            while (isDigit(peek())) {
                advance();
            }
        }

        String lexeme = source.substring(start, current);

        // Se houver letras ou caracteres colados ao numero (ex: 10abc)
        if (isInitialChar(peek())) {
            while (!isDelimiterOrWhitespace(peek()) && !isAtEnd()) {
                advance();
            }
            String badLexeme = source.substring(start, current);
            String msg = "Formato numerico invalido: '" + badLexeme + "'";
            errors.add(new LexicalError(line, tokenStartCol, badLexeme, msg));
            tokens.add(new Token(TokenType.ERROR, badLexeme, null, line, tokenStartCol, msg));
            return;
        }

        try {
            if (lexeme.contains(".")) {
                Double val = Double.valueOf(lexeme);
                tokens.add(new Token(TokenType.NUMBER, lexeme, val, line, tokenStartCol));
            } else {
                Long val = Long.valueOf(lexeme);
                tokens.add(new Token(TokenType.NUMBER, lexeme, val, line, tokenStartCol));
            }
        } catch (NumberFormatException e) {
            String msg = "Numero fora dos limites: '" + lexeme + "'";
            errors.add(new LexicalError(line, tokenStartCol, lexeme, msg));
            tokens.add(new Token(TokenType.ERROR, lexeme, null, line, tokenStartCol, msg));
        }
    }

    // ------------------------------------------------------------------------
    // 11. Reconhecimento de Simbolos (<symbol>) e Booleanos (T, NIL)
    // ------------------------------------------------------------------------
    private void scanSymbol() {
        while (isSymbolChar(peek())) {
            advance();
        }

        String lexeme = source.substring(start, current);

        // Conforme a gramatica oficial:
        // <boolean> ::= "T" | "NIL" | "#t" | "#f"
        if ("T".equals(lexeme)) {
            tokens.add(new Token(TokenType.BOOLEAN, lexeme, Boolean.TRUE, line, tokenStartCol));
            return;
        }
        if ("NIL".equals(lexeme)) {
            tokens.add(new Token(TokenType.BOOLEAN, lexeme, Boolean.FALSE, line, tokenStartCol));
            return;
        }

        tokens.add(new Token(TokenType.SYMBOL, lexeme, lexeme, line, tokenStartCol));
    }

    // ------------------------------------------------------------------------
    // 12. Metodos Auxiliares de Classificacao da Gramatica Lisp
    // ------------------------------------------------------------------------
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    // <initial-char> ::= <letter> | "+" | "-" | "*" | "/" | "=" | "<" | ">" | "!" | "?"
    private boolean isInitialChar(char c) {
        if (isLetter(c)) return true;
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '=':
            case '<':
            case '>':
            case '!':
            case '?':
                return true;
            default:
                return false;
        }
    }

    // <symbol-char> ::= <initial-char> | <digit>
    private boolean isSymbolChar(char c) {
        return isInitialChar(c) || isDigit(c);
    }

    private boolean isDelimiterOrWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '(' || c == ')' || c == '\0';
    }

    // ------------------------------------------------------------------------
    // 13. Controle do Cursor de Leitura
    // ------------------------------------------------------------------------
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char c = source.charAt(current++);
        column++;
        return c;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void addToken(TokenType type) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, null, line, tokenStartCol));
    }

    // ------------------------------------------------------------------------
    // 14. Relatorio Visual Formatado em Tabela
    // ------------------------------------------------------------------------
    public void printReport(String sourceName) {
        System.out.println("Arquivo: " + sourceName);
        System.out.printf("  %-6s %-7s %-10s %-20s %s%n", "LINHA", "COLUNA", "TIPO", "LEXEMA", "VALOR / DETALHE");

        for (Token t : tokens) {
            String valStr = "";
            if (t.type == TokenType.ERROR) {
                valStr = "[ERRO: " + t.errorMessage + "]";
            } else if (t.literal != null) {
                valStr = t.literal.toString();
            }

            String lexemePrint = t.lexeme.replace("\n", "\\n").replace("\r", "\\r");
            if (t.type == TokenType.EOF) {
                lexemePrint = "<EOF>";
            }

            System.out.printf("  %-6d %-7d %-10s %-20s %s%n",
                    t.line, t.column, t.type, lexemePrint, valStr);
        }

        if (hasErrors()) {
            System.out.println("  Status: " + errors.size() + " erro(s) lexico(s) encontrado(s):");
            for (LexicalError err : errors) {
                System.out.printf("   * Linha %d, Coluna %d: %s ['%s']%n",
                        err.line, err.column, err.message, err.offendingText);
            }
        } else {
            System.out.println("  Status: OK (nenhum erro lexico)");
        }
        System.out.println();
    }

    // ------------------------------------------------------------------------
    // 15. Leitura de Arquivo Auxiliar (UTF-8)
    // ------------------------------------------------------------------------
    public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // 16. Metodo main: Suporta arquivos especificos ou Bateria Completa de Testes
    // ------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("Compilador Lisp — Analisador Léxico (Mês 1 PJBL)\n");

        if (args.length > 0) {
            // Executa para cada arquivo passado na linha de comando
            for (String filename : args) {
                File file = new File(filename);
                if (!file.exists()) {
                    System.err.println("Arquivo nao encontrado: " + filename);
                    continue;
                }
                try {
                    String content = readFile(file);
                    LispLexer lexer = new LispLexer(content);
                    lexer.tokenize();
                    lexer.printReport(file.getName());
                } catch (IOException e) {
                    System.err.println("Erro ao ler arquivo " + filename + ": " + e.getMessage());
                }
            }
        } else {
            // Modo Bateria de Testes: executa os arquivos de teste do projeto
            String[] defaultTests = {
                "teste1_basico.lisp",
                "teste2_completo.lisp",
                "teste3_erro.lisp",
                "teste4_avancado.lisp"
            };

            boolean foundAny = false;
            for (String testName : defaultTests) {
                File f = new File(testName);
                if (f.exists()) {
                    foundAny = true;
                    try {
                        String content = readFile(f);
                        LispLexer lexer = new LispLexer(content);
                        lexer.tokenize();
                        lexer.printReport(f.getName());
                    } catch (IOException e) {
                        System.err.println("Erro ao processar " + testName + ": " + e.getMessage());
                    }
                }
            }

            if (!foundAny) {
                System.out.println("Nenhum arquivo .lisp encontrado. Executando testes demonstrativos em memoria...\n");

                runInlineTest("Teste 1 (Basico)", "(+ 15 -2.5 100)");
                runInlineTest("Teste 2 (Completo)", "(defun verifica (x)\n  (if (= x 10)\n    \"valido\"\n    NIL))");
                runInlineTest("Teste 3 (Erro)", "(soma 10 @)");
            }
        }
    }

    private static void runInlineTest(String title, String code) {
        System.out.println("Entrada: " + code.replace("\n", " "));
        LispLexer lexer = new LispLexer(code);
        lexer.tokenize();
        lexer.printReport(title);
    }
}
