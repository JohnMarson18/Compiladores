public class Token {
    public TokenType tipo;
    public String lexema;
    public int linha;
    public int coluna;

    public Token(TokenType tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return "[" + linha + ":" + coluna + "] " + tipo + " (" + lexema + ")";
    }
}
