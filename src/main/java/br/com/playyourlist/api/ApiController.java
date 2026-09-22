package br.com.playyourlist.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ApiService service;
    public ApiController(ApiService service) { this.service = service; }

    @PostMapping(value = "/adicionar/{playlistId}/musicas/{musicaId}", produces = "text/plain;charset=UTF-8")
    public String adicionar(@PathVariable Long playlistId, @PathVariable Long musicaId) {
        return service.adicionar(playlistId, musicaId);
    }

    @PutMapping("/executar/{playlistId}")
    public ReproducaoClient.ReproducaoResposta executar(@PathVariable Long playlistId) {
        return service.executar(playlistId);
    }
}
