package br.com.playyourlist.playlists;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_musicas", uniqueConstraints = @UniqueConstraint(columnNames = {"playlistid", "musicaid"}))
public class PlaylistMusica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "playlistid", nullable = false)
    private Long playlistId;
    @Column(name = "musicaid", nullable = false)
    private Long musicaId;

    protected PlaylistMusica() { }
    public PlaylistMusica(Long playlistId, Long musicaId) {
        this.playlistId = playlistId;
        this.musicaId = musicaId;
    }
    public Long getId() { return id; }
    public Long getPlaylistId() { return playlistId; }
    public Long getMusicaId() { return musicaId; }
}
