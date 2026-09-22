# MDatv1 — Play Your List

API REST da atividade [MS-AV-01-Endpoints](https://github.com/esensato/ms-2026-02/blob/main/MS-AV-01-Endpoints.md).
Gerencia músicas, playlists e registros de reprodução. Todos os endpoints ficam em uma aplicação, conforme o enunciado.

## Tecnologias

- Java 21.
- Spring Boot 4.0.8 e Spring Web MVC: aplicação e endpoints HTTP.
- Spring Data JPA / Hibernate: persistência.
- H2: banco relacional em memória, inicializado por SQL.
- Jakarta Bean Validation: validação dos dados recebidos.
- Spring Cloud 2025.1.3 / OpenFeign: chamadas HTTP da camada de orquestração.
- Maven Wrapper 3.3.4 / Maven 3.9.16: compilação e dependências.
- JUnit, AssertJ e Spring Boot Test: testes de integração.

## Executar

É necessário um **JDK 21**, com Java disponível no terminal. A primeira execução precisa de internet para baixar o Maven e as dependências. Não é necessário instalar um banco nem o Maven separadamente.

Na pasta que contém o arquivo `pom.xml`:

**Windows / PowerShell**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**

```bash
./mvnw spring-boot:run
```

A API fica em **http://localhost:8080**. Para mudar a porta:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

O endereço usado pelo Feign acompanha `server.port`. Também é possível definir a variável de ambiente `PORT`.

### Testar e gerar o executável

```powershell
.\mvnw.cmd clean verify
java -jar target/mdatv1-0.0.1-SNAPSHOT.jar
```

No Linux/macOS, substitua `.\mvnw.cmd` por `./mvnw`.
Os relatórios ficam em `target/surefire-reports/`.
Os testes sobem um servidor em porta aleatória, usam um banco independente e fazem chamadas HTTP reais, inclusive pela orquestração com Feign.

## Como funciona

O fluxo usual é cadastrar músicas, criar uma playlist, associar músicas e registrar execuções.
“Executar” uma playlist registra uma ocorrência com data e hora; o projeto não transmite áudio.

```text
Cliente HTTP
    -> Controller -> Service -> Repository -> H2

Cliente HTTP -> /api
    -> ApiService -> OpenFeign -> /musicas, /playlists, /statistic
        -> Controller -> Service -> Repository -> H2
```

Pacotes em `src/main/java/br/com/playyourlist/`:

| Pacote | Responsabilidade |
| --- | --- |
| `musicas` | Cadastro e manutenção do catálogo |
| `playlists` | Cadastro das playlists e vínculos com músicas |
| `reproducoes` | Registro, consulta e contagem de execuções |
| `api` | Orquestração e interfaces Feign |
| `common` | Respostas de erro padronizadas |

As operações que alteram dados usam transações. Os vínculos têm chaves estrangeiras e uma restrição de unicidade para impedir músicas duplicadas na mesma playlist.

## Banco e dados iniciais

`schema.sql` cria as quatro tabelas: `musicas`, `playlists`, `playlist_musicas` e `reproducoes`.
`data.sql` carrega os dados do enunciado:

- 5 músicas e 5 playlists;
- 10 associações;
- 12 reproduções; totais das playlists 1 a 5: **5, 3, 2, 1 e 1**.

O banco fica em memória: **as alterações são perdidas ao encerrar a aplicação**. Uma nova execução restaura os dados iniciais.
As tabelas dependentes são removidas primeiro no SQL para respeitar as chaves estrangeiras.

Console de desenvolvimento: **http://localhost:8080/h2-console**.

- JDBC URL: `jdbc:h2:mem:playyourlist`
- Usuário: `sa`
- Senha: em branco

## Endpoints

### Músicas

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/musicas` | Cria música — 201 |
| GET | `/musicas` | Lista músicas — 200 |
| GET | `/musicas/{id}` | Busca por ID — 200 |
| PUT | `/musicas/{id}` | Atualiza os dados — 200 |
| DELETE | `/musicas/{id}` | Exclui música e seus vínculos — 204 |

Corpo do POST/PUT:

```json
{
  "titulo": "Hotel California",
  "artista": "Eagles",
  "album": "Hotel California",
  "duracao": 391,
  "genero": "Rock"
}
```

A duração é informada em segundos. O PUT substitui os campos editáveis; opcionais omitidos ficam nulos.

### Playlists

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/playlists` | Cria playlist — 201 |
| GET | `/playlists` | Lista playlists — 200 |
| GET | `/playlists/{playlistid}` | Busca por ID — 200 |
| PUT | `/playlists/{playlistid}` | Atualiza nome e descrição — 200 |
| DELETE | `/playlists/{playlistid}` | Exclui playlist, vínculos e reproduções — 204 |
| POST | `/playlists/{playlistid}/musicas/{musicaId}` | Associa música — 201 |
| DELETE | `/playlists/{playlistid}/musicas/{musicaId}` | Remove associação — 204 |
| GET | `/playlists/{playlistid}/musicas` | Lista IDs das músicas — 200 |

Corpo do POST/PUT:

```json
{"nome": "Estudo", "descricao": "Músicas para concentração"}
```

Excluir uma playlist ou remover uma associação **preserva as músicas do catálogo**.
Tentar associar novamente a mesma música retorna 409; remover uma associação inexistente retorna 404.

### Reproduções

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/reproducao` | Registra execução com data/hora automática — 201 |
| GET | `/reproducao/{playlistid}` | Lista registros da playlist — 200 |
| GET | `/reproducao/total/{playlistid}` | Retorna um número com o total — 200 |

Corpo do registro:

```json
{"playlistId": 1}
```

O campo `playlistid` também é aceito. A resposta contém `id`, `playlistId` e `dataHora`.
Uma playlist existente sem execuções retorna lista vazia e total zero; uma playlist inexistente retorna 404.

### Orquestração com OpenFeign

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/api/adicionar/{playlistId}/musicas/{musicaId}` | Consulta playlist e música via Feign, associa e retorna mensagem — 200 |
| PUT | `/api/executar/{playlistId}` | Valida playlist e registra uma execução via Feign — 200 |

Essas duas chamadas não exigem corpo. Por exemplo, em uma base recém-iniciada:

```text
POST /api/adicionar/3/musicas/1
Música Imagine adicionada com sucesso à playlist Pop Internacional.
```

**Divergência do enunciado:** a seção de reproduções usa `POST /reproducao`, mas a orquestração pede `POST /statistic`.
A aplicação aceita **as duas rotas para a mesma operação**. O cliente Feign chama `POST /statistic`, atendendo explicitamente à segunda referência.
Cada chamada de execução cria um registro; repetir o PUT incrementa novamente a contagem, conforme a atividade.

## Validações e erros

| Campo | Regra |
| --- | --- |
| Título e artista | Obrigatórios, não vazios nem só espaços; até 150 caracteres |
| Álbum | Opcional; até 150 caracteres |
| Duração | Obrigatória; inteiro maior que zero |
| Gênero | Opcional; até 50 caracteres |
| Nome da playlist | Obrigatório, não vazio nem só espaços; até 100 caracteres |
| Descrição | Opcional; até 255 caracteres |

Os limites de título, artista e nome também respeitam as colunas do SQL fornecido.
Dados inválidos retornam **400**, recursos inexistentes **404** e associação duplicada **409**.
Falhas de comunicação da orquestração retornam 502/503.

Os erros seguem o formato Problem Details, com `status`, `detail`, `instance` e, nas validações, `erros` por campo.
Nenhuma alteração é persistida quando a validação falha.

## Conferência da atividade

A suíte `ApiIntegrationTest` contém 103 casos e verifica:

- todos os 18 endpoints, mais o alias `/statistic`;
- dados iniciais e contagens;
- criação, leitura, atualização e exclusão;
- preservação do catálogo ao remover playlists e associações;
- validação em POST e PUT, campos opcionais e limites exatos;
- erros por IDs inexistentes, corpo ausente, JSON malformado e tipos inválidos;
- integração real por Feign, mensagem de sucesso e incremento da contagem;
- adições simultâneas sem duplicar vínculos e execuções simultâneas sem perder registros;
- todos os valores dos dados iniciais, acentos e nomes atualizados nas mensagens;
- limites numéricos, campos obrigatórios omitidos e rejeição de JSON nulo ou array;
- preservação das outras playlists e de seus históricos nas exclusões;
- ausência de alterações em operações rejeitadas.

A coleção `docs/MDatv1.postman_collection.json` permite executar um fluxo completo no Postman. Ela salva automaticamente os IDs criados e remove os registros de teste ao final.
O workflow do GitHub Actions está configurado para executar `clean verify` a cada push e pull request.

## Entrega

O enunciado solicita o **link do repositório Git**:
https://github.com/Marcio-Alexandroni/MDatv1

O código pode ser obtido com `git clone` ou em **Code → Download ZIP** no GitHub.
Após extrair o ZIP, execute os comandos da seção **Executar** na pasta que contém `pom.xml`.
O JAR é um executável de apoio; o repositório contém também o código-fonte, os testes, os scripts SQL e a coleção Postman necessários à avaliação.
