package folhapagamento;

public abstract class Colaborador {
    private final String matricula;
    private final String nome;
    private final double salarioBase;

    protected Colaborador(String matricula, String nome, double salarioBase) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("A matrícula é obrigatória.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        validarNaoNegativo(salarioBase, "O salário base");
        this.matricula = matricula.trim();
        this.nome = nome.trim();
        this.salarioBase = salarioBase;
    }

    protected static void validarNaoNegativo(double valor, String campo) {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException(campo + " não pode ser negativo.");
        }
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNome() {
        return nome;
    }

    public double getSalarioBase() {
        return salarioBase;
    }

    public abstract String getTipo();

    public abstract String getCodigoPersistencia();

    public abstract double getAdicional();

    public abstract String getDetalhesRemuneracao();

    public abstract String[] getDadosEspecificos();

    public final double getSalarioFinal() {
        return salarioBase + getAdicional();
    }
}