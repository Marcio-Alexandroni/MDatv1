package br.com.playyourlist.playlists;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {
    private final PlaylistService service;
    public PlaylistController(PlaylistService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Playlist> criar(@Valid @RequestBody PlaylistRequest request) {
        Playlist playlist = service.criar(request);
        return ResponseEntity.created(URI.create("/playlists/" + playlist.getId())).body(playlist);
    }

    @GetMapping
    public List<Playlist> listar() { return service.listar(); }

    @GetMapping("/{playlistid}")
    public Playlist buscar(@PathVariable Long playlistid) { return service.buscar(playlistid); }

    @PutMapping("/{playlistid}")
    public Playlist atualizar(@PathVariable Long playlistid, @Valid @RequestBody PlaylistRequest request) {
        return service.atualizar(playlistid, request);
    }

    @DeleteMapping("/{playlistid}")
    public ResponseEntity<Void> excluir(@PathVariable Long playlistid) {
        service.excluir(playlistid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<PlaylistMusica> adicionar(@PathVariable Long playlistid, @PathVariable Long musicaId) {
        PlaylistMusica vinculo = service.adicionar(playlistid, musicaId);
        return ResponseEntity.created(URI.create("/playlists/" + playlistid + "/musicas")).body(vinculo);
    }

    @DeleteMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<Void> remover(@PathVariable Long playlistid, @PathVariable Long musicaId) {
        service.remover(playlistid, musicaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playlistid}/musicas")
    public List<Long> listarMusicas(@PathVariable Long playlistid) { return service.listarMusicas(playlistid); }
}
