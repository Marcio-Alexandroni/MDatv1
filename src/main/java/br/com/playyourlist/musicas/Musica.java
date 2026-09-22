package br.com.playyourlist.musicas;

import jakarta.persistence.*;

@Entity
@Table(name = "musicas")
public class Musica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(nullable = false, length = 150)
    private String artista;
    @Column(length = 150)
    private String album;
    @Column(nullable = false)
    private Integer duracao;
    @Column(length = 50)
    private String genero;

    protected Musica() { }

    public Musica(MusicaRequest request) { atualizar(request); }

    public void atualizar(MusicaRequest request) {
        titulo = request.titulo();
        artista = request.artista();
        album = request.album();
        duracao = request.duracao();
        genero = request.genero();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getAlbum() { return album; }
    public Integer getDuracao() { return duracao; }
    public String getGenero() { return genero; }
}
