package br.com.playyourlist.musicas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MusicaRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 150, message = "O título deve ter até 150 caracteres.") String titulo,
        @NotBlank(message = "O artista é obrigatório.")
        @Size(max = 150, message = "O artista deve ter até 150 caracteres.") String artista,
        @Size(max = 150, message = "O álbum deve ter até 150 caracteres.") String album,
        @NotNull(message = "A duração é obrigatória.")
        @Positive(message = "A duração deve ser maior que zero.") Integer duracao,
        @Size(max = 50, message = "O gênero deve ter até 50 caracteres.") String genero) {
}
