package br.com.playyourlist.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "playlists", url = "${servicos.base-url}")
public interface PlaylistClient {
    @GetMapping("/playlists/{id}")
    PlaylistResumo buscar(@PathVariable("id") Long id);

    @PostMapping("/playlists/{playlistId}/musicas/{musicaId}")
    void adicionar(@PathVariable("playlistId") Long playlistId, @PathVariable("musicaId") Long musicaId);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlaylistResumo(Long id, String nome) { }
}
