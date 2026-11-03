import java.util.ArrayList;
import java.util.List;

public class ScannerSimples {
    private final String codigo;
    private int pos = 0;
    private int linha = 1;
    private int coluna = 1;

    public ScannerSimples(String codigo) {
        this.codigo = (codigo == null) ? "" : codigo;
    }

    public List<Token> escanear() {
        List<Token> tokens = new ArrayList<Token>();
        while (!fim()) {
            pularEspacosEComentarios();
            if (fim()) break;

            int lin = linha;
            int col = coluna;
            char c = espiar();

            if (c == '(') {
                avancar();
                tokens.add(new Token(TokenType.LPAREN, "(", lin, col));
            } else if (c == ')') {
                avancar();
                tokens.add(new Token(TokenType.RPAREN, ")", lin, col));
            } else if (c == '"') {
                tokens.add(lerString(lin, col));
            } else {
                tokens.add(lerPalavra(lin, col));
            }
        }
        tokens.add(new Token(TokenType.EOF, "<EOF>", linha, coluna));
        return tokens;
    }

    private void pularEspacosEComentarios() {
        while (!fim()) {
            char c = espiar();
            if (Character.isWhitespace(c)) {
                avancar();
            } else if (c == ';') {
                while (!fim() && espiar() != '\n') avancar();
            } else {
                break;
            }
        }
    }

    private Token lerString(int lin, int col) {
        StringBuilder sb = new StringBuilder();
        sb.append(avancar());
        while (!fim() && espiar() != '"' && espiar() != '\n') {
            sb.append(avancar());
        }
        if (!fim() && espiar() == '"') {
            sb.append(avancar());
            return new Token(TokenType.STRING, sb.toString(), lin, col);
        }
        return new Token(TokenType.ERROR, sb.toString(), lin, col);
    }

    private Token lerPalavra(int lin, int col) {
        StringBuilder sb = new StringBuilder();
        while (!fim() && !ehDelimitador(espiar())) {
            sb.append(avancar());
        }
        String p = sb.toString();
        if (ehBooleano(p)) return new Token(TokenType.BOOLEAN, p, lin, col);
        if (ehNumero(p)) return new Token(TokenType.NUMBER, p, lin, col);
        if (ehSimbolo(p)) return new Token(TokenType.SYMBOL, p, lin, col);
        return new Token(TokenType.ERROR, p, lin, col);
    }

    private boolean ehBooleano(String s) {
        return "T".equals(s) || "NIL".equals(s) || "#t".equals(s) || "#f".equals(s);
    }

    private boolean ehNumero(String s) {
        if (s.isEmpty()) return false;
        int i = (s.charAt(0) == '+' || s.charAt(0) == '-') ? 1 : 0;
        if (i >= s.length() || !ehDigito(s.charAt(i))) return false;
        while (i < s.length() && ehDigito(s.charAt(i))) i++;
        if (i < s.length() && s.charAt(i) == '.') {
            i++;
            if (i >= s.length() || !ehDigito(s.charAt(i))) return false;
            while (i < s.length() && ehDigito(s.charAt(i))) i++;
        }
        return i == s.length();
    }

    private boolean ehSimbolo(String s) {
        if (s.isEmpty() || !ehInitialChar(s.charAt(0))) return false;
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!ehInitialChar(c) && !ehDigito(c)) return false;
        }
        return true;
    }

    private boolean ehInitialChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || "+-*/=<>!?".indexOf(c) >= 0;
    }

    private boolean ehDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean ehDelimitador(char c) {
        return c == '(' || c == ')' || c == '"' || c == ';' || Character.isWhitespace(c);
    }

    private boolean fim() {
        return pos >= codigo.length();
    }

    private char espiar() {
        return fim() ? '\0' : codigo.charAt(pos);
    }

    private char avancar() {
        char c = codigo.charAt(pos++);
        if (c == '\n') {
            linha++;
            coluna = 1;
        } else if (c != '\r') {
            coluna++;
        }
        return c;
    }
}
