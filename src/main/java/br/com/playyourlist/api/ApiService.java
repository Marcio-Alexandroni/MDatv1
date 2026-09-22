package br.com.playyourlist.api;

import br.com.playyourlist.reproducoes.ReproducaoRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ApiService {
    private final MusicaClient musicas;
    private final PlaylistClient playlists;
    private final ReproducaoClient reproducoes;

    public ApiService(@Lazy MusicaClient musicas, @Lazy PlaylistClient playlists,
                      @Lazy ReproducaoClient reproducoes) {
        this.musicas = musicas;
        this.playlists = playlists;
        this.reproducoes = reproducoes;
    }

    // A orquestração passa pelos endpoints HTTP via Feign, inclusive neste projeto único.
    public String adicionar(Long playlistId, Long musicaId) {
        var playlist = playlists.buscar(playlistId);
        var musica = musicas.buscar(musicaId);
        playlists.adicionar(playlistId, musicaId);
        return "Música " + musica.titulo() + " adicionada com sucesso à playlist " + playlist.nome() + ".";
    }

    public ReproducaoClient.ReproducaoResposta executar(Long playlistId) {
        playlists.buscar(playlistId);
        return reproducoes.registrar(new ReproducaoRequest(playlistId));
    }
}
