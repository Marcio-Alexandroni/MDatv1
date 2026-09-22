package br.com.playyourlist.musicas;

import br.com.playyourlist.playlists.PlaylistMusicaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MusicaService {
    private final MusicaRepository musicas;
    private final PlaylistMusicaRepository vinculos;

    public MusicaService(MusicaRepository musicas, PlaylistMusicaRepository vinculos) {
        this.musicas = musicas;
        this.vinculos = vinculos;
    }

    public List<Musica> listar() { return musicas.findAll(Sort.by("id")); }

    public Musica buscar(Long id) {
        return musicas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Música não encontrada."));
    }

    @Transactional
    public Musica criar(MusicaRequest request) { return musicas.save(new Musica(request)); }

    @Transactional
    public Musica atualizar(Long id, MusicaRequest request) {
        Musica musica = buscar(id);
        musica.atualizar(request);
        return musica;
    }

    @Transactional
    public void excluir(Long id) {
        Musica musica = buscar(id);
        vinculos.deleteByMusicaId(id);
        musicas.delete(musica);
    }
}
