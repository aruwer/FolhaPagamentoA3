# Sistema de Folha de Pagamento

Aplicação web Java para cadastrar colaboradores, calcular remunerações e emitir relatórios individuais e consolidados.

## Requisitos

- JDK 17 ou superior
- PowerShell no Windows para usar o Maven Wrapper incluído

## Executar

Na pasta raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Na primeira execução, o Maven Wrapper baixa o Maven necessário. Acesse [http://localhost:8080](http://localhost:8080) no navegador.

Para gerar um JAR executável:

```powershell
.\mvnw.cmd clean package
java -jar target\folha-pagamento-1.0.0.jar
```

Os dados ficam em `dados/colaboradores.tsv`, criado automaticamente. O local pode ser alterado pela propriedade `folha.arquivo-dados`.

## Funcionalidades

- Cadastro, consulta, alteração e exclusão de colaboradores.
- Remuneração padrão, comissionada e por produção, calculada por tipo.
- Folha individual e consolidada, com opção de impressão pelo navegador.
- Resumo com quantidade de colaboradores, custo total e valores por categoria.
- Validação de dados e matrícula única.
- Páginas Thymeleaf e backend Spring Boot no mesmo repositório.
