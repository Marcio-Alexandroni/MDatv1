package br.com.playyourlist.reproducoes;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reproducoes")
public class Reproducao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "playlistid", nullable = false)
    private Long playlistId;
    @Column(name = "datahora", nullable = false)
    private LocalDateTime dataHora;

    protected Reproducao() { }
    public Reproducao(Long playlistId) {
        this.playlistId = playlistId;
        this.dataHora = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public Long getPlaylistId() { return playlistId; }
    public LocalDateTime getDataHora() { return dataHora; }
}
