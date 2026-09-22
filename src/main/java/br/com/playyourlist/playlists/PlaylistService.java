package br.com.playyourlist.playlists;

import br.com.playyourlist.musicas.MusicaRepository;
import br.com.playyourlist.reproducoes.ReproducaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlaylistService {
    private final PlaylistRepository playlists;
    private final MusicaRepository musicas;
    private final PlaylistMusicaRepository vinculos;
    private final ReproducaoRepository reproducoes;

    public PlaylistService(PlaylistRepository playlists, MusicaRepository musicas,
                           PlaylistMusicaRepository vinculos, ReproducaoRepository reproducoes) {
        this.playlists = playlists;
        this.musicas = musicas;
        this.vinculos = vinculos;
        this.reproducoes = reproducoes;
    }

    public List<Playlist> listar() { return playlists.findAll(Sort.by("id")); }

    public Playlist buscar(Long id) {
        return playlists.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist não encontrada."));
    }

    @Transactional
    public Playlist criar(PlaylistRequest request) { return playlists.save(new Playlist(request)); }

    @Transactional
    public Playlist atualizar(Long id, PlaylistRequest request) {
        Playlist playlist = buscar(id);
        playlist.atualizar(request);
        return playlist;
    }

    @Transactional
    public void excluir(Long id) {
        Playlist playlist = buscar(id);
        vinculos.deleteByPlaylistId(id);
        reproducoes.deleteByPlaylistId(id);
        playlists.delete(playlist);
    }

    @Transactional
    public PlaylistMusica adicionar(Long playlistId, Long musicaId) {
        buscar(playlistId);
        if (!musicas.existsById(musicaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Música não encontrada.");
        }
        if (vinculos.existsByPlaylistIdAndMusicaId(playlistId, musicaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A música já pertence à playlist.");
        }
        return vinculos.saveAndFlush(new PlaylistMusica(playlistId, musicaId));
    }

    @Transactional
    public void remover(Long playlistId, Long musicaId) {
        buscar(playlistId);
        if (vinculos.deleteByPlaylistIdAndMusicaId(playlistId, musicaId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Música não associada à playlist.");
        }
    }

    public List<Long> listarMusicas(Long playlistId) {
        buscar(playlistId);
        return vinculos.findByPlaylistIdOrderById(playlistId).stream().map(PlaylistMusica::getMusicaId).toList();
    }
}
