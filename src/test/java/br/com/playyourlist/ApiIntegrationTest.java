package br.com.playyourlist;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "servicos.base-url=http://127.0.0.1:${local.server.port}",
        "spring.cloud.openfeign.lazy-attributes-resolution=true"
})
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ApiIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final JsonMapper json = JsonMapper.builder().findAndAddModules().build();

    static Map<String, Object> musica() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("titulo", "Hotel California");
        body.put("artista", "Eagles");
        body.put("album", "Hotel California");
        body.put("duracao", 391);
        body.put("genero", "Rock");
        return body;
    }

    static Map<String, Object> playlist() {
        return new LinkedHashMap<>(Map.of("nome", "Estudo", "descricao", "Concentração"));
    }

    HttpResponse<String> request(String method, String path, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(15));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    JsonNode expect(String method, String path, Object body, int status) throws Exception {
        var response = request(method, path, body);
        assertThat(response.statusCode()).as(method + " " + path + ": " + response.body()).isEqualTo(status);
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    @Test
    void carregaDadosExatosDoEnunciado() throws Exception {
        assertThat(expect("GET", "/musicas", null, 200).size()).isEqualTo(5);
        assertThat(expect("GET", "/musicas/1", null, 200).get("titulo").asText()).isEqualTo("Imagine");
        assertThat(expect("GET", "/playlists", null, 200).size()).isEqualTo(5);
        assertThat(expect("GET", "/playlists/1", null, 200).get("nome").asText()).isEqualTo("Clássicos do Rock");
        assertThat(expect("GET", "/playlists/1/musicas", null, 200).toString()).isEqualTo("[1,3,5]");
        int[] totals = {5, 3, 2, 1, 1};
        for (int id = 1; id <= 5; id++) {
            assertThat(expect("GET", "/reproducao/total/" + id, null, 200).asInt()).isEqualTo(totals[id - 1]);
            assertThat(expect("GET", "/reproducao/" + id, null, 200).size()).isEqualTo(totals[id - 1]);
        }
    }

    @Test
    void executaCrudDeMusicas() throws Exception {
        var response = request("POST", "/musicas", musica());
        assertThat(response.statusCode()).isEqualTo(201);
        long id = json.readTree(response.body()).get("id").asLong();
        assertThat(id).isEqualTo(6L);
        assertThat(response.headers().firstValue("Location")).contains("/musicas/" + id);
        assertThat(expect("GET", "/musicas/" + id, null, 200).get("artista").asText()).isEqualTo("Eagles");
        var edit = musica();
        edit.put("titulo", "Nova versão");
        edit.put("album", null);
        edit.put("genero", null);
        var updated = expect("PUT", "/musicas/" + id, edit, 200);
        assertThat(updated.get("titulo").asText()).isEqualTo("Nova versão");
        assertThat(updated.get("album").isNull()).isTrue();
        assertThat(expect("GET", "/musicas/" + id, null, 200).get("titulo").asText()).isEqualTo("Nova versão");
        assertThat(expect("GET", "/musicas", null, 200).size()).isEqualTo(6);
        expect("DELETE", "/musicas/" + id, null, 204);
        expect("GET", "/musicas/" + id, null, 404);
        assertThat(expect("GET", "/musicas", null, 200).size()).isEqualTo(5);
    }

    @Test
    void excluirMusicaRemoveVinculosSemApagarPlaylists() throws Exception {
        expect("DELETE", "/musicas/1", null, 204);
        expect("GET", "/musicas/1", null, 404);
        assertThat(expect("GET", "/playlists/1/musicas", null, 200).toString()).isEqualTo("[3,5]");
        assertThat(expect("GET", "/playlists/4/musicas", null, 200).toString()).isEqualTo("[4]");
        assertThat(expect("GET", "/playlists", null, 200).size()).isEqualTo(5);
        assertThat(jdbc.queryForObject("select count(*) from playlist_musicas where musicaid = 1", Long.class)).isZero();
    }

    @Test
    void executaCrudDePlaylistsELimpaDependencias() throws Exception {
        var response = request("POST", "/playlists", playlist());
        assertThat(response.statusCode()).isEqualTo(201);
        long id = json.readTree(response.body()).get("id").asLong();
        assertThat(response.headers().firstValue("Location")).contains("/playlists/" + id);
        assertThat(expect("GET", "/playlists/" + id + "/musicas", null, 200).size()).isZero();
        assertThat(expect("GET", "/reproducao/total/" + id, null, 200).asLong()).isZero();
        assertThat(expect("GET", "/reproducao/" + id, null, 200).size()).isZero();
        var edit = playlist();
        edit.put("nome", "Foco");
        edit.put("descricao", null);
        assertThat(expect("PUT", "/playlists/" + id, edit, 200).get("descricao").isNull()).isTrue();
        assertThat(expect("GET", "/playlists/" + id, null, 200).get("nome").asText()).isEqualTo("Foco");
        expect("POST", "/playlists/" + id + "/musicas/1", null, 201);
        expect("POST", "/reproducao", Map.of("playlistId", id), 201);
        expect("DELETE", "/playlists/" + id, null, 204);
        expect("GET", "/playlists/" + id, null, 404);
        expect("GET", "/musicas/1", null, 200);
        assertThat(jdbc.queryForObject("select count(*) from playlist_musicas where playlistid = ?", Long.class, id)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from reproducoes where playlistid = ?", Long.class, id)).isZero();
    }

    @Test
    void adicionaListaERemoveAssociacaoSemDuplicar() throws Exception {
        expect("POST", "/playlists/3/musicas/1", null, 201);
        assertThat(expect("GET", "/playlists/3/musicas", null, 200).toString()).isEqualTo("[2,1]");
        expect("POST", "/playlists/3/musicas/1", null, 409);
        assertThat(expect("GET", "/playlists/3/musicas", null, 200).size()).isEqualTo(2);
        expect("DELETE", "/playlists/3/musicas/1", null, 204);
        expect("DELETE", "/playlists/3/musicas/1", null, 404);
        expect("GET", "/musicas/1", null, 200);
        assertThat(expect("GET", "/playlists/3/musicas", null, 200).toString()).isEqualTo("[2]");
    }

    @ParameterizedTest
    @CsvSource({"/reproducao,playlistId", "/reproducao,playlistid", "/statistic,playlistId", "/statistic,playlistid"})
    void registraReproducaoComDataAutomatica(String route, String field) throws Exception {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        var created = expect("POST", route, Map.of(field, 2), 201);
        assertThat(created.get("playlistId").asLong()).isEqualTo(2);
        assertThat(LocalDateTime.parse(created.get("dataHora").asText()))
                .isBetween(before, LocalDateTime.now().plusSeconds(1));
        assertThat(expect("GET", "/reproducao/total/2", null, 200).asLong()).isEqualTo(4);
        assertThat(expect("GET", "/reproducao/2", null, 200).size()).isEqualTo(4);
        assertThat(expect("GET", "/reproducao/total/1", null, 200).asLong()).isEqualTo(5);
    }

    @Test
    void orquestraAdicaoPorHttpComFeignEDevolveMensagemCompleta() throws Exception {
        var response = request("POST", "/api/adicionar/3/musicas/1", null);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Música Imagine adicionada com sucesso à playlist Pop Internacional.");
        assertThat(expect("GET", "/playlists/3/musicas", null, 200).toString()).isEqualTo("[2,1]");
        expect("POST", "/api/adicionar/3/musicas/1", null, 409);
        assertThat(expect("GET", "/playlists/3/musicas", null, 200).size()).isEqualTo(2);
    }

    @Test
    void cadaExecucaoViaFeignGeraExatamenteUmRegistro() throws Exception {
        var first = expect("PUT", "/api/executar/1", null, 200);
        var second = expect("PUT", "/api/executar/1", null, 200);
        assertThat(first.get("playlistId").asLong()).isEqualTo(1);
        assertThat(first.get("id").asLong()).isNotEqualTo(second.get("id").asLong());
        assertThat(first.get("dataHora").asText()).isNotBlank();
        assertThat(expect("GET", "/reproducao/total/1", null, 200).asLong()).isEqualTo(7);
    }

    static Stream<Arguments> musicasInvalidas() {
        List<Object[]> values = Arrays.asList(
                new Object[]{"titulo", null}, new Object[]{"titulo", ""}, new Object[]{"titulo", "   "},
                new Object[]{"titulo", "a".repeat(151)},
                new Object[]{"artista", null}, new Object[]{"artista", ""}, new Object[]{"artista", "   "},
                new Object[]{"artista", "a".repeat(151)}, new Object[]{"album", "a".repeat(151)},
                new Object[]{"duracao", null}, new Object[]{"duracao", 0}, new Object[]{"duracao", -1},
                new Object[]{"genero", "a".repeat(51)});
        return Stream.of("POST", "PUT").flatMap(method ->
                values.stream().map(v -> Arguments.of(method, v[0], v[1])));
    }

    @ParameterizedTest(name = "{0} música inválida: {1} = {2}")
    @MethodSource("musicasInvalidas")
    void validaMusicasAoCriarEAtualizar(String method, String field, Object value) throws Exception {
        var body = musica();
        body.put(field, value);
        var problem = expect(method, method.equals("POST") ? "/musicas" : "/musicas/1", body, 400);
        assertThat(problem.get("erros").has(field)).isTrue();
        assertThat(expect("GET", "/musicas", null, 200).size()).isEqualTo(5);
        assertThat(expect("GET", "/musicas/1", null, 200).get("titulo").asText()).isEqualTo("Imagine");
    }

    static Stream<Arguments> playlistsInvalidas() {
        List<Object[]> values = Arrays.asList(new Object[]{"nome", null}, new Object[]{"nome", ""},
                new Object[]{"nome", "   "}, new Object[]{"nome", "a".repeat(101)},
                new Object[]{"descricao", "a".repeat(256)});
        return Stream.of("POST", "PUT").flatMap(method ->
                values.stream().map(v -> Arguments.of(method, v[0], v[1])));
    }

    @ParameterizedTest
    @MethodSource("playlistsInvalidas")
    void validaPlaylistsAoCriarEAtualizar(String method, String field, Object value) throws Exception {
        var body = playlist();
        body.put(field, value);
        var problem = expect(method, method.equals("POST") ? "/playlists" : "/playlists/1", body, 400);
        assertThat(problem.get("erros").has(field)).isTrue();
        assertThat(expect("GET", "/playlists", null, 200).size()).isEqualTo(5);
        assertThat(expect("GET", "/playlists/1", null, 200).get("nome").asText()).isEqualTo("Clássicos do Rock");
    }

    @Test
    void aceitaOpcionaisAusentesENulosELimitesExatos() throws Exception {
        var body = musica();
        body.remove("album");
        body.remove("genero");
        expect("POST", "/musicas", body, 201);
        body.put("titulo", "a".repeat(150));
        body.put("artista", "a".repeat(150));
        body.put("album", "a".repeat(150));
        body.put("genero", "a".repeat(50));
        body.put("duracao", 1);
        expect("POST", "/musicas", body, 201);
        expect("PUT", "/musicas/1", body, 200);
        body.put("album", null);
        body.put("genero", null);
        expect("POST", "/musicas", body, 201);
        expect("POST", "/playlists", Map.of("nome", "Sem descrição"), 201);
        var list = playlist();
        list.put("descricao", null);
        expect("POST", "/playlists", list, 201);
        list.put("nome", "a".repeat(100));
        list.put("descricao", "a".repeat(255));
        expect("POST", "/playlists", list, 201);
        expect("PUT", "/playlists/1", list, 200);
    }

    static Stream<Arguments> recursosInexistentes() {
        return Stream.of(
                Arguments.of("GET", "/musicas/999999", null),
                Arguments.of("PUT", "/musicas/999999", musica()),
                Arguments.of("DELETE", "/musicas/999999", null),
                Arguments.of("GET", "/playlists/999999", null),
                Arguments.of("PUT", "/playlists/999999", playlist()),
                Arguments.of("DELETE", "/playlists/999999", null),
                Arguments.of("GET", "/playlists/999999/musicas", null),
                Arguments.of("POST", "/playlists/999999/musicas/1", null),
                Arguments.of("POST", "/playlists/1/musicas/999999", null),
                Arguments.of("DELETE", "/playlists/999999/musicas/1", null),
                Arguments.of("DELETE", "/playlists/1/musicas/999999", null),
                Arguments.of("POST", "/reproducao", Map.of("playlistId", 999999)),
                Arguments.of("POST", "/statistic", Map.of("playlistId", 999999)),
                Arguments.of("GET", "/reproducao/999999", null),
                Arguments.of("GET", "/reproducao/total/999999", null),
                Arguments.of("POST", "/api/adicionar/999999/musicas/1", null),
                Arguments.of("POST", "/api/adicionar/1/musicas/999999", null),
                Arguments.of("PUT", "/api/executar/999999", null));
    }

    @ParameterizedTest
    @MethodSource("recursosInexistentes")
    void retorna404SemAlterarDados(String method, String path, Object body) throws Exception {
        var problem = expect(method, path, body, 404);
        assertThat(problem.get("status").asInt()).isEqualTo(404);
        assertThat(problem.get("detail").asText()).isNotBlank();
        assertThat(jdbc.queryForObject("select count(*) from musicas", Long.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("select count(*) from playlists", Long.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("select count(*) from playlist_musicas", Long.class)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select count(*) from reproducoes", Long.class)).isEqualTo(12);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/reproducao", "/statistic"})
    void rejeitaReproducaoSemPlaylistValida(String path) throws Exception {
        for (var body : List.of(Map.of(), Map.of("playlistId", 0), Map.of("playlistId", -1))) {
            assertThat(expect("POST", path, body, 400).get("erros").has("playlistId")).isTrue();
        }
        assertThat(jdbc.queryForObject("select count(*) from reproducoes", Long.class)).isEqualTo(12);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void rejeitaDuracaoFracionariaSemTruncar(String method) throws Exception {
        var body = musica();
        body.put("duracao", 1.5);
        expect(method, method.equals("POST") ? "/musicas" : "/musicas/1", body, 400);
        assertThat(expect("GET", "/musicas", null, 200).size()).isEqualTo(5);
        assertThat(expect("GET", "/musicas/1", null, 200).get("duracao").asInt()).isEqualTo(183);
    }

    @Test
    void rejeitaJsonMalformadoETiposInvalidos() throws Exception {
        var badJson = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/musicas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{invalido")).build();
        assertThat(http.send(badJson, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(400);
        expect("GET", "/musicas/abc", null, 400);
        expect("GET", "/playlists/abc", null, 400);
        var body = musica();
        body.put("duracao", "texto");
        expect("POST", "/musicas", body, 400);
        expect("POST", "/musicas", null, 400);
    }
}
