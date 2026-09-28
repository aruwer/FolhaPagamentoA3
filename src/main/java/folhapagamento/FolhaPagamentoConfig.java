package folhapagamento;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FolhaPagamentoConfig {
    @Bean
    public RepositorioColaboradores repositorioColaboradores(
            @Value("${folha.arquivo-dados:dados/colaboradores.tsv}") String arquivo) throws IOException {
        return new RepositorioColaboradores(Path.of(arquivo));
    }
}