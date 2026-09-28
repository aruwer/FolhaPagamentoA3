# Guia para compreensão do código

Este documento explica o projeto de folha de pagamento para pessoas que conhecem Python, mas ainda estão se familiarizando com Java, Spring Boot e Thymeleaf.

A ideia principal é mostrar **onde cada responsabilidade está** e **como os dados percorrem o sistema**.

## 1. O que o projeto faz

O sistema permite:

- Cadastrar colaboradores.
- Editar e excluir colaboradores.
- Calcular o salário final de cada colaborador.
- Exibir a folha individual.
- Exibir a folha completa.
- Mostrar o total da folha e os valores por categoria.

A aplicação é um site executado localmente. Os dados são armazenados no arquivo `dados/colaboradores.tsv`.

Não existe banco de dados neste projeto.

## 2. Comparação rápida com Python

Em um projeto Python, poderíamos imaginar uma estrutura parecida com esta:

```text
app.py                 # inicialização
routes.py              # rotas web
services.py            # regras de aplicação
models.py              # classes de domínio
repository.py          # acesso aos dados
templates/             # páginas HTML
```

Neste projeto Java, os papéis são equivalentes, mas a organização é feita principalmente por classes:

```text
FolhaPagamentoWebApplication.java  # inicialização do site
FolhaPagamentoController.java      # rotas web
FolhaPagamentoService.java         # serviços da aplicação
Colaborador*.java                  # modelo e regras de cálculo
RepositorioColaboradores.java      # leitura e gravação dos dados
templates/*.html                   # páginas Thymeleaf
```

A diferença mais importante é que Java exige mais declarações explícitas: tipos, modificadores de acesso, classes e métodos. Em compensação, o compilador ajuda a detectar muitos problemas antes da execução.

## 3. Como o site inicia

A classe principal é:

```text
src/main/java/folhapagamento/FolhaPagamentoWebApplication.java
```

Ela contém o método `main`:

```java
public static void main(String[] args) {
    SpringApplication.run(FolhaPagamentoWebApplication.class, args);
}
```

Em Python, isso seria semelhante a:

```python
if __name__ == "__main__":
    iniciar_aplicacao()
```

A diferença é que `SpringApplication.run(...)` não apenas chama uma função. O Spring também:

1. Inicializa o servidor web.
2. Procura as classes da aplicação.
3. Cria os objetos necessários.
4. Registra as rotas.
5. Prepara o Thymeleaf para renderizar as páginas.

A anotação `@SpringBootApplication` informa ao Spring que essa é a classe principal da aplicação.

## 4. As camadas do projeto

O fluxo principal pode ser visualizado assim:

```text
Navegador
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Arquivo TSV
```

Cada camada tem uma função específica.

### Controller

O controller recebe requisições HTTP e decide qual página ou ação deve ser executada.

Arquivo:

```text
src/main/java/folhapagamento/FolhaPagamentoController.java
```

Ele é parecido com um arquivo de rotas em Flask ou FastAPI.

Exemplo:

```java
@GetMapping("/folha")
public String folha(Model model) {
    model.addAttribute("colaboradores", service.listar());
    model.addAttribute("resumo", service.gerarResumo());
    return "folha";
}
```

A leitura desse método é:

1. Quando chegar uma requisição `GET /folha`...
2. Buscar os colaboradores pelo service.
3. Gerar o resumo da folha.
4. Enviar esses dados para o template.
5. Renderizar `templates/folha.html`.

Em Flask, seria conceitualmente parecido com:

```python
@app.get("/folha")
def folha():
    colaboradores = service.listar()
    resumo = service.gerar_resumo()
    return render_template(
        "folha.html",
        colaboradores=colaboradores,
        resumo=resumo,
    )
```

### Service

Arquivo:

```text
src/main/java/folhapagamento/FolhaPagamentoService.java
```

O service coordena as operações da aplicação. Ele não deveria saber detalhes de HTML, botões ou formulários.

Atualmente ele:

- Lista colaboradores.
- Busca um colaborador por matrícula.
- Cadastra, atualiza e exclui colaboradores.
- Calcula o resumo da folha.

O service chama o repository para acessar os dados.

### Repository

Arquivo:

```text
src/main/java/folhapagamento/RepositorioColaboradores.java
```

O repository é a camada que sabe como os dados são armazenados.

Neste projeto, ele mantém os colaboradores em um `Map` na memória e sincroniza esse conteúdo com o arquivo TSV.

Se futuramente o projeto usasse PostgreSQL, por exemplo, a ideia seria trocar ou adaptar o repository sem precisar reescrever as páginas e o controller.

## 5. Como uma requisição funciona

Considere o cadastro de um colaborador.

### Etapa 1: o usuário abre o formulário

O navegador acessa:

```text
GET /colaboradores/novo
```

O controller cria um objeto `ColaboradorForm` e devolve:

```text
templates/colaboradores/form.html
```

### Etapa 2: o usuário envia o formulário

O formulário envia:

```text
POST /colaboradores
```

O controller recebe os campos no objeto `ColaboradorForm`.

```java
public String cadastrar(
        @Valid @ModelAttribute("form") ColaboradorForm form,
        BindingResult resultado,
        Model model,
        RedirectAttributes redirect)
```

Alguns conceitos importantes:

- `@ModelAttribute` liga os campos HTML ao objeto Java.
- `@Valid` pede para o Spring executar as validações.
- `BindingResult` contém os erros de validação.
- `Model` envia dados para o HTML.
- `RedirectAttributes` envia mensagens para a próxima página depois de um redirecionamento.

### Etapa 3: validação

O objeto `ColaboradorForm` usa anotações como:

```java
@NotBlank
@NotNull
@DecimalMin("0.0")
@Pattern(...)
```

Essas anotações são parecidas com validações feitas manualmente em Python:

```python
if not nome.strip():
    raise ValueError("Informe o nome")

if salario_base < 0:
    raise ValueError("O salário não pode ser negativo")
```

A diferença é que o Spring executa essas regras automaticamente antes de o método concluir.

### Etapa 4: criação do objeto de domínio

Depois da validação, o formulário chama:

```java
form.construirColaborador()
```

Esse método verifica o tipo escolhido e cria a classe correta:

```java
switch (tipo) {
    case "PADRAO" -> new ColaboradorPadrao(...);
    case "COMISSIONADO" -> new ColaboradorComissionado(...);
    case "PRODUCAO" -> new ColaboradorProducao(...);
}
```

Em Python, seria semelhante a:

```python
if tipo == "PADRAO":
    colaborador = ColaboradorPadrao(...)
elif tipo == "COMISSIONADO":
    colaborador = ColaboradorComissionado(...)
elif tipo == "PRODUCAO":
    colaborador = ColaboradorProducao(...)
```

### Etapa 5: gravação

O controller chama o service:

```java
service.cadastrar(colaborador);
```

O service chama o repository:

```java
repositorio.cadastrar(colaborador);
```

O repository:

1. Verifica se a matrícula já existe.
2. Adiciona o objeto ao mapa.
3. Grava novamente o arquivo TSV.
4. Retorna o controle ao controller.

### Etapa 6: redirecionamento

Se tudo der certo, o controller retorna:

```java
return "redirect:/";
```

Isso faz o navegador voltar para o painel principal.

## 6. O modelo de domínio

A classe central é:

```text
src/main/java/folhapagamento/Colaborador.java
```

Ela é uma classe abstrata. Isso significa que representa uma ideia geral de colaborador, mas não deve ser criada diretamente.

```java
public abstract class Colaborador
```

Em Python, uma ideia parecida seria:

```python
from abc import ABC, abstractmethod

class Colaborador(ABC):
    @abstractmethod
    def adicional(self):
        pass
```

A classe Java guarda os dados comuns:

```java
private final String matricula;
private final String nome;
private final double salarioBase;
```

O `private` significa que o campo só pode ser acessado diretamente pela própria classe.

O `final` significa que o valor não pode ser substituído depois de definido no construtor.

Os métodos `getMatricula()`, `getNome()` e `getSalarioBase()` funcionam como getters. Em Python, normalmente usaríamos atributos públicos ou propriedades.

## 7. Herança e polimorfismo

As classes abaixo herdam de `Colaborador`:

```text
ColaboradorPadrao.java
ColaboradorComissionado.java
ColaboradorProducao.java
```

Todas podem ser tratadas como `Colaborador`, mas cada uma calcula o adicional de maneira diferente.

```java
public final double getSalarioFinal() {
    return salarioBase + getAdicional();
}
```

O método `getSalarioFinal()` é único na classe base. Porém, `getAdicional()` é implementado por cada subclasse.

Esse comportamento é polimorfismo: o mesmo código chama `getAdicional()`, mas a implementação usada depende do tipo real do objeto.

### Colaborador padrão

```text
adicional = 0
salário final = salário base
```

### Colaborador comissionado

```text
adicional = valor das vendas * percentual de comissão / 100
salário final = salário base + adicional
```

### Colaborador por produção

```text
adicional = quantidade produzida * valor por unidade
salário final = salário base + adicional
```

Por isso o service pode trabalhar com uma lista única:

```java
List<Colaborador> colaboradores
```

Ele não precisa fazer um `if` para cada tipo ao calcular o salário final. Cada objeto sabe calcular seu próprio adicional.

## 8. `List`, `Map` e equivalentes em Python

Algumas estruturas Java usadas no projeto correspondem a estruturas conhecidas em Python:

| Java | Python | Uso |
|---|---|---|
| `List<Colaborador>` | `list[Colaborador]` | Lista de colaboradores |
| `Map<String, Colaborador>` | `dict[str, Colaborador]` | Busca por matrícula |
| `Optional<Colaborador>` | `Colaborador | None` | Resultado que pode não existir |
| `List.copyOf(...)` | cópia imutável conceitual | Evita alteração externa da lista |
| `Map.merge(...)` | atualização acumulada de dicionário | Soma por categoria |

Exemplo em Java:

```java
Map<String, Double> totaisPorCategoria = new LinkedHashMap<>();

for (Colaborador colaborador : colaboradores) {
    totaisPorCategoria.merge(
        colaborador.getTipo(),
        colaborador.getSalarioFinal(),
        Double::sum
    );
}
```

A intenção é a mesma de:

```python
totais_por_categoria = {}

for colaborador in colaboradores:
    categoria = colaborador.tipo
    totais_por_categoria[categoria] = (
        totais_por_categoria.get(categoria, 0)
        + colaborador.salario_final
    )
```

## 9. Persistência no arquivo TSV

O arquivo possui uma linha por colaborador. O formato depende do tipo.

Colaborador padrão:

```text
PADRAO    matricula_codificada    nome_codificado    salario_base
```

Comissionado:

```text
COMISSIONADO    matricula_codificada    nome_codificado    salario_base    valor_vendas    percentual
```

Por produção:

```text
PRODUCAO    matricula_codificada    nome_codificado    salario_base    quantidade    valor_unidade
```

Os campos de texto são codificados em Base64 URL-safe. Isso evita que tabulações ou alguns caracteres especiais quebrem a estrutura do TSV.

Ao iniciar, o repository executa `carregar()` e transforma cada linha em um objeto Java.

Ao salvar, ele:

1. Monta todas as linhas novamente.
2. Escreve em um arquivo temporário.
3. Substitui o arquivo original.
4. Tenta fazer a substituição de forma atômica.

Em Python, isso seria semelhante a ler o arquivo com `csv` ou `pathlib`, converter cada registro em um objeto e depois regravar o arquivo.

## 10. Como funcionam os templates

Os HTMLs estão em:

```text
src/main/resources/templates/
```

Eles usam Thymeleaf. A marcação especial começa com `th:`.

Exemplo:

```html
<span th:text="${colaborador.nome}">Nome</span>
```

Isso significa: substitua o conteúdo do `span` pelo nome do colaborador.

Outro exemplo:

```html
<tr th:each="colaborador : ${colaboradores}">
```

Isso repete a linha da tabela para cada colaborador.

Em Jinja2, usado frequentemente com Flask, seria parecido com:

```html
<span>{{ colaborador.nome }}</span>

{% for colaborador in colaboradores %}
<tr>...</tr>
{% endfor %}
```

A diferença principal é a sintaxe, não a ideia.

## 11. JavaScript do formulário

O arquivo:

```text
src/main/resources/static/js/colaborador-form.js
```

controla os campos variáveis do formulário.

Quando o tipo é `COMISSIONADO`, aparecem os campos de vendas e comissão.

Quando o tipo é `PRODUCAO`, aparecem quantidade produzida e valor por unidade.

Quando o tipo é `PADRAO`, os campos variáveis ficam ocultos.

Esse JavaScript melhora a experiência do usuário, mas não substitui a validação do backend. O servidor ainda valida os dados em `ColaboradorForm` e nas classes de domínio.

## 12. Rotas principais

| Método | Caminho | Função |
|---|---|---|
| `GET` | `/` | Painel principal |
| `GET` | `/colaboradores/novo` | Formulário de cadastro |
| `POST` | `/colaboradores` | Cadastra colaborador |
| `GET` | `/colaboradores/{matricula}/editar` | Formulário de edição |
| `POST` | `/colaboradores/{matricula}` | Atualiza colaborador |
| `POST` | `/colaboradores/{matricula}/excluir` | Exclui colaborador |
| `GET` | `/folha` | Folha completa |
| `GET` | `/folha/{matricula}` | Folha individual |

`{matricula}` é uma variável da URL. Por exemplo:

```text
/folha/001
```

No controller, ela é recebida com:

```java
@PathVariable String matricula
```

Em Flask, a ideia seria:

```python
@app.get("/folha/<matricula>")
def folha_individual(matricula):
    ...
```

## 13. Anotações mais importantes do Spring

| Anotação | Significado |
|---|---|
| `@SpringBootApplication` | Classe principal da aplicação Spring Boot |
| `@Controller` | Classe que atende requisições web e retorna páginas |
| `@Service` | Classe que representa uma camada de serviço |
| `@Configuration` | Classe que define configurações e objetos do Spring |
| `@Bean` | Objeto que será criado e gerenciado pelo Spring |
| `@GetMapping` | Rota HTTP GET |
| `@PostMapping` | Rota HTTP POST |
| `@RequestMapping` | Prefixo ou configuração de rotas |
| `@Valid` | Executa validações do objeto recebido |
| `@ModelAttribute` | Liga dados do formulário a um objeto |
| `@PathVariable` | Obtém uma variável presente na URL |
| `@Value` | Lê um valor de configuração |

## 14. Injeção de dependência

O controller recebe o service pelo construtor:

```java
public FolhaPagamentoController(FolhaPagamentoService service) {
    this.service = service;
}
```

O service recebe o repository da mesma forma.

O Spring cria esses objetos e os conecta automaticamente. Isso é chamado de **injeção de dependência**.

Em Python, seria parecido com passar as dependências explicitamente:

```python
service = FolhaPagamentoService(repository)
controller = FolhaPagamentoController(service)
```

A diferença é que o Spring faz essa montagem automaticamente com base nas anotações.

## 15. Onde procurar quando algo der errado

### A página não abre

Verifique:

1. Se `FolhaPagamentoWebApplication` foi iniciado.
2. Se a porta `8080` está disponível.
3. Se o template correspondente existe.
4. Se o controller retornou o nome correto do template.

### O formulário não salva

Verifique:

1. Os erros exibidos no formulário.
2. As regras de `ColaboradorForm`.
3. O método `construirColaborador()`.
4. As validações do construtor da subclasse.
5. O caminho e a permissão de escrita do arquivo TSV.

### O salário está errado

Verifique:

1. `getAdicional()` da subclasse correspondente.
2. `getSalarioFinal()` em `Colaborador`.
3. Os valores carregados pelo `RepositorioColaboradores`.
4. O formato da linha no arquivo TSV.

### Um colaborador não é encontrado

Verifique:

1. Se a matrícula usada na URL está correta.
2. Se a matrícula foi carregada do arquivo.
3. Se o arquivo contém uma linha válida.
4. Se não existe diferença de espaços ou maiúsculas/minúsculas.

## 16. Ordem recomendada para estudar o projeto

Para entender o código com menos esforço, leia nesta ordem:

1. `README.md`
2. `FolhaPagamentoWebApplication.java`
3. `Colaborador.java`
4. `ColaboradorPadrao.java`
5. `ColaboradorComissionado.java`
6. `ColaboradorProducao.java`
7. `RepositorioColaboradores.java`
8. `FolhaPagamentoService.java`
9. `ColaboradorForm.java`
10. `FolhaPagamentoController.java`
11. Os templates HTML
12. `application.properties`

Começar pelo modelo de domínio ajuda a entender os cálculos antes de entrar nos detalhes do Spring.

## 17. Pontos importantes para futuras melhorias

O projeto funciona, mas alguns pontos poderiam evoluir:

- Usar `BigDecimal` no lugar de `double` para valores monetários.
- Criar testes automatizados para os três tipos de colaborador.
- Criar testes para cadastro, edição e exclusão.
- Substituir o arquivo TSV por um banco de dados quando houver necessidade de maior escala.
- Separar melhor mensagens e regras de validação, caso o sistema cresça.
- Preencher corretamente o indicador da página ativa no menu.
- Adicionar controle de concorrência mais robusto se vários usuários acessarem o arquivo simultaneamente.

## Resumo final

O sistema pode ser entendido como uma aplicação web em camadas:

```text
HTML/Thymeleaf
    -> Controller
    -> Service
    -> Repository
    -> Arquivo TSV
```

As classes `ColaboradorPadrao`, `ColaboradorComissionado` e `ColaboradorProducao` representam diferentes regras de remuneração. A classe base define o contrato comum, e cada subclasse calcula seu próprio adicional.

Para quem vem de Python, a melhor forma de estudar este projeto é fazer estas equivalências:

- Controller = rotas Flask/FastAPI.
- Service = funções ou classes de regra de negócio.
- Repository = camada de acesso a dados.
- Thymeleaf = Jinja2 integrado ao Spring.
- Anotações Spring = configuração e registro automático de componentes.
- Classes Java = classes Python com tipos e modificadores mais explícitos.

Com esse mapa, o restante do código passa a ser principalmente a implementação detalhada dessas responsabilidades.
