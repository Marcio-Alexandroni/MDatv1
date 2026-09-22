package br.com.playyourlist.playlists;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter até 100 caracteres.") String nome,
        @Size(max = 255, message = "A descrição deve ter até 255 caracteres.") String descricao) {
}
