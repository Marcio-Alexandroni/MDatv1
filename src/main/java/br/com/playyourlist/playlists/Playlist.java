package br.com.playyourlist.playlists;

import jakarta.persistence.*;

@Entity
@Table(name = "playlists")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(length = 255)
    private String descricao;

    protected Playlist() { }
    public Playlist(PlaylistRequest request) { atualizar(request); }
    public void atualizar(PlaylistRequest request) {
        nome = request.nome();
        descricao = request.descricao();
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}
