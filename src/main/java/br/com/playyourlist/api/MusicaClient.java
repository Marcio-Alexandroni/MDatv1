package br.com.playyourlist.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "musicas", url = "${servicos.base-url}")
public interface MusicaClient {
    @GetMapping("/musicas/{id}")
    MusicaResumo buscar(@PathVariable("id") Long id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MusicaResumo(Long id, String titulo) { }
}
