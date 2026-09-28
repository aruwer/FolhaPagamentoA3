package folhapagamento;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import org.springframework.stereotype.Service;

@Service
public class FolhaPagamentoService {
    private final RepositorioColaboradores repositorio;

    public FolhaPagamentoService(RepositorioColaboradores repositorio) {
        this.repositorio = repositorio;
    }

    public List<Colaborador> listar() {
        return repositorio.listar();
    }

    public Optional<Colaborador> buscar(String matricula) {
        return repositorio.buscar(matricula);
    }

    public void cadastrar(Colaborador colaborador) throws IOException {
        repositorio.cadastrar(colaborador);
    }

    public void atualizar(Colaborador colaborador) throws IOException {
        repositorio.atualizar(colaborador);
    }

    public void excluir(String matricula) throws IOException {
        repositorio.excluir(matricula);
    }

    public ResumoFolha gerarResumo() {
        List<Colaborador> colaboradores = listar();
        Map<String, Double> totaisPorCategoria = new LinkedHashMap<>();
        for (Colaborador colaborador : colaboradores) {
            totaisPorCategoria.merge(colaborador.getTipo(), colaborador.getSalarioFinal(), Double::sum);
        }
        double total = colaboradores.stream().mapToDouble(Colaborador::getSalarioFinal).sum();
        return new ResumoFolha(colaboradores.size(), total, Collections.unmodifiableMap(totaisPorCategoria));
    }
}