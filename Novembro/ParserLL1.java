import java.util.List;
import java.util.Stack;

public class ParserLL1 {

    private final TabelaLL1 tabela;
    private final boolean mostrarPassos;
    private String ultimoErro;
    private int totalPassos;

    public ParserLL1() {
        this(true);
    }

    public ParserLL1(boolean mostrarPassos) {
        this.tabela = new TabelaLL1();
        this.mostrarPassos = mostrarPassos;
    }

    public TabelaLL1 getTabela() { return tabela; }
    public String getUltimoErro() { return ultimoErro; }
    public int getTotalPassos() { return totalPassos; }

    public boolean analisarCodigo(String codigo) {
        return analisar(LispLexer.tokenizar(codigo));
    }

    public boolean analisar(List<Token> tokens) {
        this.ultimoErro = null;
        this.totalPassos = 0;

        if (tokens == null || tokens.isEmpty()) {
            this.ultimoErro = "Erro sintatico: lista de tokens vazia.";
            return false;
        }

        Stack<String> pilha = new Stack<String>();
        pilha.push("$");
        pilha.push(TabelaLL1.SIMBOLO_INICIAL);
        int pos = 0;

        if (mostrarPassos) {
            System.out.printf("  %-5s %-38s %-24s %s%n", "PASSO", "PILHA", "ENTRADA", "ACAO");
        }

        while (!pilha.isEmpty()) {
            totalPassos++;
            String topo = pilha.peek();
            Token atual = tokens.get(pos);

            if (atual.tipo == TokenType.ERROR) {
                ultimoErro = "Erro lexico na linha " + atual.linha + ", coluna " + atual.coluna
                    + ": token invalido '" + atual.lexema + "'.";
                imprimirPasso(totalPassos, pilha, tokens, pos, "ERRO LEXICO " + atual.lexema);
                return false;
            }

            if (topo.equals("$")) {
                if (atual.tipo == TokenType.EOF) {
                    imprimirPasso(totalPassos, pilha, tokens, pos, "ACEITO");
                    pilha.pop();
                    return true;
                }
                ultimoErro = "Erro sintatico na linha " + atual.linha + ", coluna " + atual.coluna
                    + ": token inesperado '" + atual.lexema + "' apos o fim do programa.";
                imprimirPasso(totalPassos, pilha, tokens, pos, "ERRO: sobrou entrada");
                return false;
            }

            if (!topo.startsWith("<")) {
                if (terminalCombina(topo, atual.tipo)) {
                    imprimirPasso(totalPassos, pilha, tokens, pos, "Casa " + topo + " com '" + atual.lexema + "'");
                    pilha.pop();
                    pos++;
                    continue;
                }
                ultimoErro = "Erro sintatico na linha " + atual.linha + ", coluna " + atual.coluna
                    + ": esperado '" + topo + "', encontrado '" + atual.lexema + "'.";
                imprimirPasso(totalPassos, pilha, tokens, pos, "ERRO: esperava " + topo);
                return false;
            }

            TabelaLL1.Regra regra = tabela.buscar(topo, atual.tipo);
            if (regra == null) {
                ultimoErro = montarErroTabela(topo, atual);
                imprimirPasso(totalPassos, pilha, tokens, pos, "ERRO: M[" + topo + ", " + atual.tipo + "] vazio");
                return false;
            }

            imprimirPasso(totalPassos, pilha, tokens, pos, regra.toString());
            pilha.pop();
            for (int i = regra.direita.length - 1; i >= 0; i--) {
                pilha.push(regra.direita[i]);
            }
        }
        return false;
    }

    private boolean terminalCombina(String terminal, TokenType tipo) {
        if (terminal.equals("("))       return tipo == TokenType.LPAREN;
        if (terminal.equals(")"))       return tipo == TokenType.RPAREN;
        if (terminal.equals("NUMBER"))  return tipo == TokenType.NUMBER;
        if (terminal.equals("SYMBOL"))  return tipo == TokenType.SYMBOL;
        if (terminal.equals("STRING"))  return tipo == TokenType.STRING;
        if (terminal.equals("BOOLEAN")) return tipo == TokenType.BOOLEAN;
        if (terminal.equals("$"))       return tipo == TokenType.EOF;
        return false;
    }

    private String montarErroTabela(String naoTerminal, Token atual) {
        if ((naoTerminal.equals("<list-tail>") || naoTerminal.equals("<elements-tail>")) && atual.tipo == TokenType.EOF) {
            return "Erro sintatico na linha " + atual.linha + ", coluna " + atual.coluna
                + ": lista aberta com '(' nao foi fechada com ')' (M[" + naoTerminal + ", EOF] vazio).";
        }
        if (atual.tipo == TokenType.RPAREN) {
            return "Erro sintatico na linha " + atual.linha + ", coluna " + atual.coluna
                + ": parentese ')' inesperado sem '(' correspondente (M[" + naoTerminal + ", RPAREN] vazio).";
        }
        return "Erro sintatico na linha " + atual.linha + ", coluna " + atual.coluna
            + ": token inesperado '" + atual.lexema + "' (M[" + naoTerminal + ", " + atual.tipo + "] vazio).";
    }

    private void imprimirPasso(int passo, Stack<String> pilha, List<Token> tokens, int pos, String acao) {
        if (!mostrarPassos) return;
        StringBuilder sp = new StringBuilder();
        for (int i = 0; i < pilha.size(); i++) {
            if (i > 0) sp.append(" ");
            sp.append(pilha.get(i));
        }
        String txtPilha = sp.toString();
        if (txtPilha.length() > 38) txtPilha = "$ ... " + txtPilha.substring(txtPilha.length() - 32);

        StringBuilder se = new StringBuilder();
        for (int i = pos; i < tokens.size(); i++) {
            if (i > pos) se.append(" ");
            se.append(tokens.get(i).lexema);
        }
        String txtEntrada = se.toString();
        if (txtEntrada.length() > 24) txtEntrada = txtEntrada.substring(0, 20) + " ...";

        System.out.printf("  %-5d %-38s %-24s %s%n", passo, txtPilha, txtEntrada, acao);
    }
}
