package br.com.playyourlist.api;

import br.com.playyourlist.reproducoes.ReproducaoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@FeignClient(name = "reproducoes", url = "${servicos.base-url}")
public interface ReproducaoClient {
    @PostMapping("/statistic")
    ReproducaoResposta registrar(@RequestBody ReproducaoRequest request);

    record ReproducaoResposta(Long id, Long playlistId, LocalDateTime dataHora) { }
}
