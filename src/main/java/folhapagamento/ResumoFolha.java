package folhapagamento;

import java.util.Map;

public record ResumoFolha(int quantidadeColaboradores, double totalFolha,
        Map<String, Double> totaisPorCategoria) {
}