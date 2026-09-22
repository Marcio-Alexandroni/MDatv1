package br.com.playyourlist.musicas;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/musicas")
public class MusicaController {
    private final MusicaService service;
    public MusicaController(MusicaService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Musica> criar(@Valid @RequestBody MusicaRequest request) {
        Musica musica = service.criar(request);
        return ResponseEntity.created(URI.create("/musicas/" + musica.getId())).body(musica);
    }

    @GetMapping
    public List<Musica> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Musica buscar(@PathVariable Long id) { return service.buscar(id); }

    @PutMapping("/{id}")
    public Musica atualizar(@PathVariable Long id, @Valid @RequestBody MusicaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
