package br.com.playyourlist.playlists;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PlaylistMusicaRepository extends JpaRepository<PlaylistMusica, Long> {
    boolean existsByPlaylistIdAndMusicaId(Long playlistId, Long musicaId);
    List<PlaylistMusica> findByPlaylistIdOrderById(Long playlistId);

    @Modifying
    @Query("delete from PlaylistMusica pm where pm.playlistId = :playlistId")
    void deleteByPlaylistId(Long playlistId);

    @Modifying
    @Query("delete from PlaylistMusica pm where pm.musicaId = :musicaId")
    void deleteByMusicaId(Long musicaId);

    @Modifying
    @Query("delete from PlaylistMusica pm where pm.playlistId = :playlistId and pm.musicaId = :musicaId")
    int deleteByPlaylistIdAndMusicaId(Long playlistId, Long musicaId);
}
