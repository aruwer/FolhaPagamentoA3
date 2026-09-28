package folhapagamento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RepositorioColaboradores {
    private final Path arquivo;
    private final Map<String, Colaborador> colaboradores = new LinkedHashMap<>();

    public RepositorioColaboradores(Path arquivo) throws IOException {
        this.arquivo = arquivo;
        carregar();
    }

    public synchronized List<Colaborador> listar() {
        return List.copyOf(colaboradores.values());
    }

    public synchronized Optional<Colaborador> buscar(String matricula) {
        return Optional.ofNullable(colaboradores.get(matricula));
    }

    public synchronized void cadastrar(Colaborador colaborador) throws IOException {
        if (colaboradores.containsKey(colaborador.getMatricula())) {
            throw new IllegalArgumentException("Já existe colaborador com essa matrícula.");
        }
        colaboradores.put(colaborador.getMatricula(), colaborador);
        salvar();
    }

    public synchronized void atualizar(Colaborador colaborador) throws IOException {
        if (!colaboradores.containsKey(colaborador.getMatricula())) {
            throw new IllegalArgumentException("Matrícula não encontrada.");
        }
        colaboradores.put(colaborador.getMatricula(), colaborador);
        salvar();
    }

    public synchronized void excluir(String matricula) throws IOException {
        if (colaboradores.remove(matricula) == null) {
            throw new IllegalArgumentException("Matrícula não encontrada.");
        }
        salvar();
    }

    private void carregar() throws IOException {
        if (!Files.exists(arquivo)) {
            return;
        }
        int numeroLinha = 0;
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            numeroLinha++;
            if (linha.isBlank()) {
                continue;
            }
            try {
                Colaborador colaborador = decodificar(linha.split("\\t", -1));
                if (colaboradores.putIfAbsent(colaborador.getMatricula(), colaborador) != null) {
                    throw new IllegalArgumentException("Matrícula duplicada");
                }
            } catch (RuntimeException erro) {
                throw new IOException("Registro inválido na linha " + numeroLinha + " do arquivo de dados.", erro);
            }
        }
    }

    private Colaborador decodificar(String[] campos) {
        if (campos.length < 4) {
            throw new IllegalArgumentException("Campos insuficientes");
        }
        String tipo = campos[0];
        String matricula = decodificarTexto(campos[1]);
        String nome = decodificarTexto(campos[2]);
        double salarioBase = Double.parseDouble(campos[3]);
        return switch (tipo) {
            case "PADRAO" -> {
                exigirCampos(campos, 4);
                yield new ColaboradorPadrao(matricula, nome, salarioBase);
            }
            case "COMISSIONADO" -> {
                exigirCampos(campos, 6);
                yield new ColaboradorComissionado(matricula, nome, salarioBase,
                        Double.parseDouble(campos[4]), Double.parseDouble(campos[5]));
            }
            case "PRODUCAO" -> {
                exigirCampos(campos, 6);
                yield new ColaboradorProducao(matricula, nome, salarioBase,
                        Double.parseDouble(campos[4]), Double.parseDouble(campos[5]));
            }
            default -> throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
        };
    }

    private void exigirCampos(String[] campos, int quantidade) {
        if (campos.length != quantidade) {
            throw new IllegalArgumentException("Quantidade de campos inválida");
        }
    }

    private String decodificarTexto(String texto) {
        return new String(Base64.getUrlDecoder().decode(texto), StandardCharsets.UTF_8);
    }

    private String codificarTexto(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private void salvar() throws IOException {
        Files.createDirectories(arquivo.getParent());
        List<String> linhas = new ArrayList<>();
        for (Colaborador colaborador : colaboradores.values()) {
            StringBuilder linha = new StringBuilder()
                    .append(colaborador.getCodigoPersistencia()).append('\t')
                    .append(codificarTexto(colaborador.getMatricula())).append('\t')
                    .append(codificarTexto(colaborador.getNome())).append('\t')
                    .append(colaborador.getSalarioBase());
            for (String dado : colaborador.getDadosEspecificos()) {
                linha.append('\t').append(dado);
            }
            linhas.add(linha.toString());
        }

        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        Files.write(temporario, linhas, StandardCharsets.UTF_8);
        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException erro) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}