import java.util.ArrayList;
import java.util.List;

public class ParserRecursivo {
    private final List<Token> tokens;
    private int pos = 0;
    private final List<String> passosDerivacao = new ArrayList<String>();
    private final List<String> erros = new ArrayList<String>();

    public ParserRecursivo(List<Token> tokens) {
        this.tokens = tokens;
    }

    public NoSintatico parsePrograma() {
        NoSintatico raiz = new NoSintatico("<programa>");
        if (atual().tipo == TokenType.EOF) {
            registrarErro(atual(), "Arquivo vazio: esperada ao menos uma <s-expr>.");
            return raiz;
        }
        while (atual().tipo != TokenType.EOF) {
            raiz.adicionarFilho(parseSExpr());
        }
        return raiz;
    }

    public NoSintatico parseSExpr() {
        NoSintatico noSExpr = new NoSintatico("<s-expr>");
        Token t = atual();

        if (t.tipo == TokenType.LPAREN) {
            registrarPasso("<s-expr> -> <list>");
            noSExpr.adicionarFilho(parseList());
            return noSExpr;
        }
        if (ehAtomo(t.tipo)) {
            registrarPasso("<s-expr> -> <atom>");
            noSExpr.adicionarFilho(parseAtom());
            return noSExpr;
        }

        if (t.tipo == TokenType.RPAREN) {
            registrarErro(t, "Parentese ')' inesperado.");
            avancar();
        } else if (t.tipo == TokenType.ERROR) {
            registrarErro(t, "Token invalido '" + t.lexema + "'.");
            avancar();
        } else {
            registrarErro(t, "Esperado <atom> ou '(', encontrado '" + t.lexema + "'.");
            if (t.tipo != TokenType.EOF) avancar();
        }
        noSExpr.adicionarFilho(new NoSintatico("<erro: " + t.lexema + ">"));
        return noSExpr;
    }

    public NoSintatico parseList() {
        NoSintatico noLista = new NoSintatico("<list>");
        Token abre = consumir(TokenType.LPAREN, "Esperado '('.");
        noLista.adicionarFilho(new NoSintatico("("));

        if (atual().tipo == TokenType.RPAREN) {
            registrarPasso("<list> -> \"(\" \")\"");
            avancar();
            noLista.adicionarFilho(new NoSintatico(")"));
            return noLista;
        }
        if (atual().tipo == TokenType.EOF) {
            registrarErro(atual(), "Esperado ')' para '(' da linha " + abre.linha + ":" + abre.coluna + ".");
            return noLista;
        }

        registrarPasso("<list> -> \"(\" <elements> \")\"");
        noLista.adicionarFilho(parseElements());

        if (atual().tipo == TokenType.RPAREN) {
            avancar();
            noLista.adicionarFilho(new NoSintatico(")"));
        } else {
            registrarErro(atual(), "Esperado ')' para '(' da linha " + abre.linha + ":" + abre.coluna + ".");
        }
        return noLista;
    }

    public NoSintatico parseElements() {
        NoSintatico noElementos = new NoSintatico("<elements>");

        while (atual().tipo == TokenType.ERROR) {
            Token inv = avancar();
            registrarErro(inv, "Token invalido '" + inv.lexema + "' na lista.");
            noElementos.adicionarFilho(new NoSintatico("<erro: " + inv.lexema + ">"));
        }
        if (atual().tipo == TokenType.RPAREN || atual().tipo == TokenType.EOF) {
            return noElementos;
        }

        int idx = reservarPasso();
        noElementos.adicionarFilho(parseSExpr());

        while (atual().tipo == TokenType.ERROR) {
            Token inv = avancar();
            registrarErro(inv, "Token invalido '" + inv.lexema + "' na lista.");
            noElementos.adicionarFilho(new NoSintatico("<erro: " + inv.lexema + ">"));
        }

        if (ehInicioDeSExpr(atual().tipo)) {
            definirPasso(idx, "<elements> -> <s-expr> <elements>");
            noElementos.adicionarFilho(parseElements());
        } else {
            definirPasso(idx, "<elements> -> <s-expr>");
        }
        return noElementos;
    }

    public NoSintatico parseAtom() {
        Token t = atual();
        if (t.tipo == TokenType.NUMBER) {
            avancar();
            registrarPasso("<atom> -> <number> (" + t.lexema + ")");
            return new NoSintatico("NUMBER " + t.lexema);
        }
        if (t.tipo == TokenType.SYMBOL) {
            avancar();
            registrarPasso("<atom> -> <symbol> (" + t.lexema + ")");
            return new NoSintatico("SYMBOL " + t.lexema);
        }
        if (t.tipo == TokenType.STRING) {
            avancar();
            registrarPasso("<atom> -> <string> (" + t.lexema + ")");
            return new NoSintatico("STRING " + t.lexema);
        }
        if (t.tipo == TokenType.BOOLEAN) {
            avancar();
            registrarPasso("<atom> -> <boolean> (" + t.lexema + ")");
            return new NoSintatico("BOOLEAN " + t.lexema);
        }
        registrarErro(t, "Esperado <atom>, encontrado '" + t.lexema + "'.");
        if (t.tipo != TokenType.EOF && t.tipo != TokenType.RPAREN) avancar();
        return new NoSintatico("<erro: " + t.lexema + ">");
    }

    private boolean ehAtomo(TokenType t) {
        return t == TokenType.NUMBER || t == TokenType.SYMBOL || t == TokenType.STRING || t == TokenType.BOOLEAN;
    }

    private boolean ehInicioDeSExpr(TokenType t) {
        return t == TokenType.LPAREN || ehAtomo(t);
    }

    private Token atual() {
        return tokens.get(pos >= tokens.size() ? tokens.size() - 1 : pos);
    }

    private Token avancar() {
        Token t = atual();
        if (pos < tokens.size() - 1) pos++;
        return t;
    }

    private Token consumir(TokenType esperado, String msg) {
        Token t = atual();
        if (t.tipo == esperado) return avancar();
        registrarErro(t, msg + " Encontrado: '" + t.lexema + "'.");
        return t;
    }

    private void registrarPasso(String r) { passosDerivacao.add(r); }
    private int reservarPasso() { passosDerivacao.add(""); return passosDerivacao.size() - 1; }
    private void definirPasso(int i, String r) { passosDerivacao.set(i, r); }
    private void registrarErro(Token t, String d) { erros.add("Linha " + t.linha + ":" + t.coluna + " - " + d); }

    public List<String> getPassosDerivacao() { return passosDerivacao; }
    public List<String> getErros() { return erros; }
    public boolean temErros() { return !erros.isEmpty(); }
}
