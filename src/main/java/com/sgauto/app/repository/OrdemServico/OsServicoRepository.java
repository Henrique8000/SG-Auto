package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.model.OrdemServico.OsServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsServicoRepository extends JpaRepository<OsServico, Long> {
    List<OsServico> findByOrdemServicoId(Long osId);
}
