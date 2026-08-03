package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.model.OrdemServico.OsPeca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsPecaRepository extends JpaRepository<OsPeca, Long> {
    List<OsPeca> findByOrdemServicoId(Long osId);

    @Query("SELECT op FROM OsPeca op JOIN FETCH op.peca WHERE op.ordemServico.id = :osId")
    List<OsPeca> findByOrdemServicoIdComPeca(@Param("osId") Long osId);
}
