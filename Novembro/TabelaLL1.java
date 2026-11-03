import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TabelaLL1 {

    public static class Regra {
        public final int numero;
        public final String naoTerminal;
        public final String[] direita;

        public Regra(int numero, String naoTerminal, String... direita) {
            this.numero = numero;
            this.naoTerminal = naoTerminal;
            this.direita = direita;
        }

        public String codigoCurto() {
            return "R" + numero;
        }

        @Override
        public String toString() {
            if (direita.length == 0) return "R" + numero + ": " + naoTerminal + " -> epsilon";
            StringBuilder sb = new StringBuilder("R" + numero + ": " + naoTerminal + " -> ");
            for (int i = 0; i < direita.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(direita[i]);
            }
            return sb.toString();
        }
    }

    public static final String SIMBOLO_INICIAL = "<program>";

    public static final String[] NAO_TERMINAIS = {
        "<program>", "<program-tail>", "<s-expr>", "<list>",
        "<list-tail>", "<elements>", "<elements-tail>", "<atom>"
    };

    public static final TokenType[] COLUNAS_TERMINAIS = {
        TokenType.LPAREN, TokenType.RPAREN, TokenType.NUMBER,
        TokenType.SYMBOL, TokenType.STRING, TokenType.BOOLEAN, TokenType.EOF
    };

    private final Map<String, Map<TokenType, Regra>> tabela = new LinkedHashMap<String, Map<TokenType, Regra>>();
    private final List<Regra> todasRegras = new ArrayList<Regra>();

    public TabelaLL1() {
        for (String nt : NAO_TERMINAIS) {
            tabela.put(nt, new EnumMap<TokenType, Regra>(TokenType.class));
        }
        montarTabela();
    }

    private void montarTabela() {
        Regra r1  = criarRegra(1,  "<program>",       "<s-expr>", "<program-tail>");
        Regra r2  = criarRegra(2,  "<program-tail>",  "<s-expr>", "<program-tail>");
        Regra r3  = criarRegra(3,  "<program-tail>");
        Regra r4  = criarRegra(4,  "<s-expr>",        "<atom>");
        Regra r5  = criarRegra(5,  "<s-expr>",        "<list>");
        Regra r6  = criarRegra(6,  "<list>",          "(", "<list-tail>");
        Regra r7  = criarRegra(7,  "<list-tail>",     ")");
        Regra r8  = criarRegra(8,  "<list-tail>",     "<elements>", ")");
        Regra r9  = criarRegra(9,  "<elements>",      "<s-expr>", "<elements-tail>");
        Regra r10 = criarRegra(10, "<elements-tail>", "<s-expr>", "<elements-tail>");
        Regra r11 = criarRegra(11, "<elements-tail>");
        Regra r12 = criarRegra(12, "<atom>",          "NUMBER");
        Regra r13 = criarRegra(13, "<atom>",          "SYMBOL");
        Regra r14 = criarRegra(14, "<atom>",          "STRING");
        Regra r15 = criarRegra(15, "<atom>",          "BOOLEAN");

        TokenType[] inicioExpressao = {
            TokenType.LPAREN, TokenType.NUMBER, TokenType.SYMBOL, TokenType.STRING, TokenType.BOOLEAN
        };
        TokenType[] inicioAtomo = {
            TokenType.NUMBER, TokenType.SYMBOL, TokenType.STRING, TokenType.BOOLEAN
        };

        preencher("<program>", inicioExpressao, r1);
        preencher("<program-tail>", inicioExpressao, r2);
        definir("<program-tail>", TokenType.EOF, r3);
        definir("<s-expr>", TokenType.LPAREN, r5);
        preencher("<s-expr>", inicioAtomo, r4);
        definir("<list>", TokenType.LPAREN, r6);
        definir("<list-tail>", TokenType.RPAREN, r7);
        preencher("<list-tail>", inicioExpressao, r8);
        preencher("<elements>", inicioExpressao, r9);
        preencher("<elements-tail>", inicioExpressao, r10);
        definir("<elements-tail>", TokenType.RPAREN, r11);
        definir("<atom>", TokenType.NUMBER,  r12);
        definir("<atom>", TokenType.SYMBOL,  r13);
        definir("<atom>", TokenType.STRING,  r14);
        definir("<atom>", TokenType.BOOLEAN, r15);
    }

    private Regra criarRegra(int numero, String naoTerminal, String... direita) {
        Regra r = new Regra(numero, naoTerminal, direita);
        todasRegras.add(r);
        return r;
    }

    private void preencher(String naoTerminal, TokenType[] terminais, Regra regra) {
        for (TokenType t : terminais) definir(naoTerminal, t, regra);
    }

    private void definir(String naoTerminal, TokenType terminal, Regra regra) {
        Map<TokenType, Regra> linha = tabela.get(naoTerminal);
        if (linha.containsKey(terminal)) {
            throw new IllegalStateException("Conflito LL(1) em M[" + naoTerminal + ", " + terminal + "]");
        }
        linha.put(terminal, regra);
    }

    public Regra buscar(String naoTerminal, TokenType terminal) {
        Map<TokenType, Regra> linha = tabela.get(naoTerminal);
        return (linha == null) ? null : linha.get(terminal);
    }

    public List<Regra> getTodasRegras() {
        return todasRegras;
    }

    public void imprimirResumoCompleto() {
        System.out.println("GRAMATICA LL(1):");
        for (Regra r : todasRegras) {
            System.out.println("  " + r);
        }
        System.out.println("\nTABELA LL(1) M[Nao-Terminal, Terminal]:");
        System.out.printf("  %-16s %-5s %-5s %-7s %-7s %-7s %-8s %s%n",
            "NAO-TERMINAL", "(", ")", "NUMBER", "SYMBOL", "STRING", "BOOLEAN", "$");
        for (String nt : NAO_TERMINAIS) {
            String[] c = new String[COLUNAS_TERMINAIS.length];
            for (int i = 0; i < COLUNAS_TERMINAIS.length; i++) {
                Regra r = buscar(nt, COLUNAS_TERMINAIS[i]);
                c[i] = (r == null) ? "." : r.codigoCurto();
            }
            System.out.printf("  %-16s %-5s %-5s %-7s %-7s %-7s %-8s %s%n",
                nt, c[0], c[1], c[2], c[3], c[4], c[5], c[6]);
        }
    }
}
