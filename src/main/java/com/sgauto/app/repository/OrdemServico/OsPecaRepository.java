package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.model.OrdemServico.OsPeca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsPecaRepository extends JpaRepository<OsPeca, Long> {
    List<OsPeca> findByOrdemServicoId(Long osId);
}
