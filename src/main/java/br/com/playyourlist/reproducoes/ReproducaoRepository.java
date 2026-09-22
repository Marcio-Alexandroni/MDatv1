package br.com.playyourlist.reproducoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReproducaoRepository extends JpaRepository<Reproducao, Long> {
    List<Reproducao> findByPlaylistIdOrderByDataHoraAscIdAsc(Long playlistId);
    long countByPlaylistId(Long playlistId);

    @Modifying
    @Query("delete from Reproducao r where r.playlistId = :playlistId")
    void deleteByPlaylistId(Long playlistId);
}
