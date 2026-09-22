package br.com.playyourlist.reproducoes;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReproducaoRequest(
        @JsonAlias("playlistid")
        @NotNull(message = "O ID da playlist é obrigatório.")
        @Positive(message = "O ID da playlist deve ser maior que zero.") Long playlistId) {
}
