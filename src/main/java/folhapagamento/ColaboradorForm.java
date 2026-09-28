package folhapagamento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ColaboradorForm {
    @NotBlank(message = "Informe a matrícula.")
    private String matricula;

    @NotBlank(message = "Informe o nome.")
    private String nome;

    @NotNull(message = "Informe o salário base.")
    @DecimalMin(value = "0.0", message = "O salário base não pode ser negativo.")
    private Double salarioBase;

    @NotBlank(message = "Selecione o tipo de colaborador.")
    @Pattern(regexp = "PADRAO|COMISSIONADO|PRODUCAO", message = "Selecione um tipo válido.")
    private String tipo = "PADRAO";

    @DecimalMin(value = "0.0", message = "O valor de vendas não pode ser negativo.")
    private Double valorVendas;

    @DecimalMin(value = "0.0", message = "A comissão não pode ser negativa.")
    private Double percentualComissao;

    @DecimalMin(value = "0.0", message = "A quantidade produzida não pode ser negativa.")
    private Double quantidadeProduzida;

    @DecimalMin(value = "0.0", message = "O valor por unidade não pode ser negativo.")
    private Double valorPorUnidade;

    public Colaborador construirColaborador() {
        if (salarioBase == null) {
            throw new IllegalArgumentException("Informe o salário base.");
        }
        return switch (tipo) {
            case "PADRAO" -> new ColaboradorPadrao(matricula, nome, salarioBase);
            case "COMISSIONADO" -> new ColaboradorComissionado(matricula, nome, salarioBase,
                    exigir(valorVendas, "Informe o valor das vendas."),
                    exigir(percentualComissao, "Informe o percentual de comissão."));
            case "PRODUCAO" -> new ColaboradorProducao(matricula, nome, salarioBase,
                    exigir(quantidadeProduzida, "Informe a quantidade produzida."),
                    exigir(valorPorUnidade, "Informe o valor por unidade."));
            default -> throw new IllegalArgumentException("Selecione um tipo de colaborador válido.");
        };
    }

    public static ColaboradorForm de(Colaborador colaborador) {
        ColaboradorForm form = new ColaboradorForm();
        form.setMatricula(colaborador.getMatricula());
        form.setNome(colaborador.getNome());
        form.setSalarioBase(colaborador.getSalarioBase());
        if (colaborador instanceof ColaboradorComissionado comissionado) {
            form.setTipo("COMISSIONADO");
            form.setValorVendas(comissionado.getValorVendas());
            form.setPercentualComissao(comissionado.getPercentualComissao());
        } else if (colaborador instanceof ColaboradorProducao producao) {
            form.setTipo("PRODUCAO");
            form.setQuantidadeProduzida(producao.getQuantidadeProduzida());
            form.setValorPorUnidade(producao.getValorPorUnidade());
        } else {
            form.setTipo("PADRAO");
        }
        return form;
    }

    private double exigir(Double valor, String mensagem) {
        if (valor == null) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Double getSalarioBase() { return salarioBase; }
    public void setSalarioBase(Double salarioBase) { this.salarioBase = salarioBase; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getValorVendas() { return valorVendas; }
    public void setValorVendas(Double valorVendas) { this.valorVendas = valorVendas; }
    public Double getPercentualComissao() { return percentualComissao; }
    public void setPercentualComissao(Double percentualComissao) { this.percentualComissao = percentualComissao; }
    public Double getQuantidadeProduzida() { return quantidadeProduzida; }
    public void setQuantidadeProduzida(Double quantidadeProduzida) { this.quantidadeProduzida = quantidadeProduzida; }
    public Double getValorPorUnidade() { return valorPorUnidade; }
    public void setValorPorUnidade(Double valorPorUnidade) { this.valorPorUnidade = valorPorUnidade; }
}