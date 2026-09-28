package folhapagamento;

public final class ColaboradorProducao extends Colaborador {
    private final double quantidadeProduzida;
    private final double valorPorUnidade;

    public ColaboradorProducao(String matricula, String nome, double salarioBase,
            double quantidadeProduzida, double valorPorUnidade) {
        super(matricula, nome, salarioBase);
        validarNaoNegativo(quantidadeProduzida, "A quantidade produzida");
        validarNaoNegativo(valorPorUnidade, "O valor por unidade");
        this.quantidadeProduzida = quantidadeProduzida;
        this.valorPorUnidade = valorPorUnidade;
    }

    @Override
    public String getTipo() {
        return "Por produção";
    }

    @Override
    public String getCodigoPersistencia() {
        return "PRODUCAO";
    }

    @Override
    public double getAdicional() {
        return quantidadeProduzida * valorPorUnidade;
    }

    public double getQuantidadeProduzida() {
        return quantidadeProduzida;
    }

    public double getValorPorUnidade() {
        return valorPorUnidade;
    }

    @Override
    public String getDetalhesRemuneracao() {
        return "Produção: " + quantidadeProduzida + " unidades | Valor/unidade: " + valorPorUnidade;
    }

    @Override
    public String[] getDadosEspecificos() {
        return new String[] { Double.toString(quantidadeProduzida), Double.toString(valorPorUnidade) };
    }
}