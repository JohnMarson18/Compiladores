import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LispLexer {

    public static List<Token> tokenizarArquivo(File arquivo) throws IOException {
        String codigo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
        return tokenizar(codigo);
    }

    public static List<Token> tokenizar(String codigo) {
        List<Token> tokens = new ArrayList<Token>();
        int i = 0, n = codigo.length(), linha = 1, coluna = 1;

        while (i < n) {
            char c = codigo.charAt(i);

            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                if (c == '\n') { linha++; coluna = 1; } else { coluna++; }
                i++;
                continue;
            }

            if (c == ';') {
                while (i < n && codigo.charAt(i) != '\n') { i++; coluna++; }
                continue;
            }

            if (c == '(') { tokens.add(new Token(TokenType.LPAREN, "(", linha, coluna)); i++; coluna++; continue; }
            if (c == ')') { tokens.add(new Token(TokenType.RPAREN, ")", linha, coluna)); i++; coluna++; continue; }

            if (c == '"') {
                int linIni = linha, colIni = coluna, ini = i;
                i++; coluna++;
                boolean fechou = false;
                while (i < n) {
                    char ch = codigo.charAt(i);
                    if (ch == '"') { i++; coluna++; fechou = true; break; }
                    if (ch == '\n') { linha++; coluna = 1; } else { coluna++; }
                    i++;
                }
                tokens.add(new Token(fechou ? TokenType.STRING : TokenType.ERROR, codigo.substring(ini, i), linIni, colIni));
                continue;
            }

            if (c == '#') {
                if (i + 1 < n && (codigo.charAt(i + 1) == 't' || codigo.charAt(i + 1) == 'f')) {
                    if (!(i + 2 < n && ehCharSimbolo(codigo.charAt(i + 2)))) {
                        tokens.add(new Token(TokenType.BOOLEAN, codigo.substring(i, i + 2), linha, coluna));
                        i += 2; coluna += 2;
                        continue;
                    }
                }
                tokens.add(new Token(TokenType.ERROR, String.valueOf(c), linha, coluna));
                i++; coluna++;
                continue;
            }

            boolean digito = ehDigito(c);
            boolean sinalEDigito = (c == '+' || c == '-') && (i + 1 < n) && ehDigito(codigo.charAt(i + 1));
            if (digito || sinalEDigito) {
                int j = (c == '+' || c == '-') ? i + 1 : i;
                while (j < n && ehDigito(codigo.charAt(j))) j++;
                boolean pontoInvalido = false;
                if (j < n && codigo.charAt(j) == '.') {
                    if (j + 1 < n && ehDigito(codigo.charAt(j + 1))) {
                        j++;
                        while (j < n && ehDigito(codigo.charAt(j))) j++;
                    } else {
                        pontoInvalido = true;
                    }
                }
                if (!(sinalEDigito && !pontoInvalido && j < n && ehCharSimbolo(codigo.charAt(j)))) {
                    tokens.add(new Token(TokenType.NUMBER, codigo.substring(i, j), linha, coluna));
                    coluna += (j - i);
                    i = j;
                    continue;
                }
            }

            if (ehInicioSimbolo(c)) {
                int ini = i, colIni = coluna;
                while (i < n && ehCharSimbolo(codigo.charAt(i))) { i++; coluna++; }
                String lex = codigo.substring(ini, i);
                TokenType tipo = (lex.equals("T") || lex.equals("NIL")) ? TokenType.BOOLEAN : TokenType.SYMBOL;
                tokens.add(new Token(tipo, lex, linha, colIni));
                continue;
            }

            tokens.add(new Token(TokenType.ERROR, String.valueOf(c), linha, coluna));
            i++; coluna++;
        }

        tokens.add(new Token(TokenType.EOF, "$", linha, coluna));
        return tokens;
    }

    private static boolean ehDigito(char c) { return c >= '0' && c <= '9'; }
    private static boolean ehLetra(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private static boolean ehOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '<' || c == '>' || c == '!' || c == '?';
    }
    private static boolean ehInicioSimbolo(char c) { return ehLetra(c) || ehOperador(c); }
    private static boolean ehCharSimbolo(char c) { return ehInicioSimbolo(c) || ehDigito(c); }
}
