package br.com.playyourlist.reproducoes;

import br.com.playyourlist.playlists.PlaylistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReproducaoService {
    private final ReproducaoRepository reproducoes;
    private final PlaylistRepository playlists;

    public ReproducaoService(ReproducaoRepository reproducoes, PlaylistRepository playlists) {
        this.reproducoes = reproducoes;
        this.playlists = playlists;
    }

    private void validarPlaylist(Long id) {
        if (!playlists.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist não encontrada.");
        }
    }

    @Transactional
    public Reproducao registrar(ReproducaoRequest request) {
        validarPlaylist(request.playlistId());
        return reproducoes.save(new Reproducao(request.playlistId()));
    }

    public List<Reproducao> listar(Long playlistId) {
        validarPlaylist(playlistId);
        return reproducoes.findByPlaylistIdOrderByDataHoraAscIdAsc(playlistId);
    }

    public long total(Long playlistId) {
        validarPlaylist(playlistId);
        return reproducoes.countByPlaylistId(playlistId);
    }
}
