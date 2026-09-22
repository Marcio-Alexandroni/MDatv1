package br.com.playyourlist.reproducoes;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
public class ReproducaoController {
    private final ReproducaoService service;
    public ReproducaoController(ReproducaoService service) { this.service = service; }

    // O enunciado usa /reproducao na seção de estatísticas e /statistic na orquestração.
    @PostMapping({"/reproducao", "/statistic"})
    public ResponseEntity<Reproducao> registrar(@Valid @RequestBody ReproducaoRequest request) {
        Reproducao reproducao = service.registrar(request);
        return ResponseEntity.created(URI.create("/reproducao/" + request.playlistId())).body(reproducao);
    }

    @GetMapping("/reproducao/{playlistid}")
    public List<Reproducao> listar(@PathVariable Long playlistid) { return service.listar(playlistid); }

    @GetMapping("/reproducao/total/{playlistid}")
    public long total(@PathVariable Long playlistid) { return service.total(playlistid); }
}
