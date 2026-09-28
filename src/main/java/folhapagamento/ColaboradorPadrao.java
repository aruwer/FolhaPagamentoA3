package folhapagamento;

public final class ColaboradorPadrao extends Colaborador {
    public ColaboradorPadrao(String matricula, String nome, double salarioBase) {
        super(matricula, nome, salarioBase);
    }

    @Override
    public String getTipo() {
        return "Padrão";
    }

    @Override
    public String getCodigoPersistencia() {
        return "PADRAO";
    }

    @Override
    public double getAdicional() {
        return 0;
    }

    @Override
    public String getDetalhesRemuneracao() {
        return "Sem remuneração adicional";
    }

    @Override
    public String[] getDadosEspecificos() {
        return new String[0];
    }
}