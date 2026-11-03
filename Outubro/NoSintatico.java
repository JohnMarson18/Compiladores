import java.util.ArrayList;
import java.util.List;

public class NoSintatico {
    public final String rotulo;
    public final List<NoSintatico> filhos = new ArrayList<NoSintatico>();

    public NoSintatico(String rotulo) {
        this.rotulo = rotulo;
    }

    public void adicionarFilho(NoSintatico filho) {
        if (filho != null) filhos.add(filho);
    }

    public void imprimir(String prefixo, boolean ehUltimo) {
        System.out.println(prefixo + (ehUltimo ? "`-- " : "|-- ") + rotulo);
        String novoPrefixo = prefixo + (ehUltimo ? "    " : "|   ");
        for (int i = 0; i < filhos.size(); i++) {
            filhos.get(i).imprimir(novoPrefixo, i == filhos.size() - 1);
        }
    }

    public void imprimirRaiz() {
        System.out.println("  " + rotulo);
        for (int i = 0; i < filhos.size(); i++) {
            filhos.get(i).imprimir("  ", i == filhos.size() - 1);
        }
    }
}
