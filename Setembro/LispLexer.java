import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LispLexer {
    private final String fonte;
    private int pos = 0;
    private int linha = 1;
    private int coluna = 1;
    private boolean eofEmitido = false;

    public LispLexer(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        int ch;
        while ((ch = br.read()) != -1) {
            sb.append((char) ch);
        }
        this.fonte = sb.toString();
    }

    public Token yylex() {
        pularEspacosEComentarios();
        if (pos >= fonte.length()) {
            if (eofEmitido) return null;
            eofEmitido = true;
            return new Token(TokenType.EOF, "EOF", linha, coluna);
        }

        char c = fonte.charAt(pos);
        int tl = linha;
        int tc = coluna;

        if (c == '(') { avancar(); return new Token(TokenType.LPAREN, "(", tl, tc); }
        if (c == ')') { avancar(); return new Token(TokenType.RPAREN, ")", tl, tc); }

        if (c == '"') {
            StringBuilder sb = new StringBuilder("\"");
            avancar();
            while (pos < fonte.length()) {
                char ch = fonte.charAt(pos);
                if (ch == '"') { sb.append('"'); avancar(); return new Token(TokenType.STRING, sb.toString(), tl, tc); }
                if (ch == '\n') { linha++; coluna = 1; pos++; } else avancar();
                sb.append(ch);
            }
            return new Token(TokenType.ERROR, sb.toString(), tl, tc);
        }

        if (c == '#' && pos + 1 < fonte.length() && (fonte.charAt(pos + 1) == 't' || fonte.charAt(pos + 1) == 'f')) {
            String lex = fonte.substring(pos, pos + 2);
            avancar(); avancar();
            return new Token(TokenType.BOOLEAN, lex, tl, tc);
        }

        if (Character.isDigit(c) || ((c == '+' || c == '-') && pos + 1 < fonte.length() && Character.isDigit(fonte.charAt(pos + 1)))) {
            StringBuilder sb = new StringBuilder();
            if (c == '+' || c == '-') { sb.append(c); avancar(); }
            while (pos < fonte.length() && Character.isDigit(fonte.charAt(pos))) { sb.append(fonte.charAt(pos)); avancar(); }
            if (pos + 1 < fonte.length() && fonte.charAt(pos) == '.' && Character.isDigit(fonte.charAt(pos + 1))) {
                sb.append('.'); avancar();
                while (pos < fonte.length() && Character.isDigit(fonte.charAt(pos))) { sb.append(fonte.charAt(pos)); avancar(); }
            }
            return new Token(TokenType.NUMBER, sb.toString(), tl, tc);
        }

        if (isSimbolo(c)) {
            StringBuilder sb = new StringBuilder();
            while (pos < fonte.length() && (isSimbolo(fonte.charAt(pos)) || Character.isDigit(fonte.charAt(pos)))) {
                sb.append(fonte.charAt(pos)); avancar();
            }
            String lex = sb.toString();
            TokenType tipo = (lex.equals("T") || lex.equals("NIL")) ? TokenType.BOOLEAN : TokenType.SYMBOL;
            return new Token(tipo, lex, tl, tc);
        }

        avancar();
        return new Token(TokenType.ERROR, String.valueOf(c), tl, tc);
    }

    private boolean isSimbolo(char c) {
        return Character.isLetter(c) || "+-*/=<>!?".indexOf(c) >= 0;
    }

    private void pularEspacosEComentarios() {
        while (pos < fonte.length()) {
            char c = fonte.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') avancar();
            else if (c == '\n') { linha++; coluna = 1; pos++; }
            else if (c == ';') { while (pos < fonte.length() && fonte.charAt(pos) != '\n') pos++; }
            else break;
        }
    }

    private void avancar() {
        pos++;
        coluna++;
    }
}
