package com.sgauto.app.repository.OrdemServico;

import com.sgauto.app.dto.dashboard.ComissaoFuncionarioDTO;
import com.sgauto.app.dto.dashboard.ServicoMaisRealizadoDTO;
import com.sgauto.app.model.OrdemServico.OsServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OsServicoRepository extends JpaRepository<OsServico, Long> {
    List<OsServico> findByOrdemServicoId(Long osId);

    @Query("SELECT os FROM OsServico os JOIN FETCH os.servico WHERE os.ordemServico.id = :osId")
    List<OsServico> findByOrdemServicoIdComServico(@Param("osId") Long osId);

    @Query("""
            SELECT new com.sgauto.app.dto.dashboard.ComissaoFuncionarioDTO(
                f.id, f.nomeCompleto, SUM(os.valorTotal * f.comissaoPercentual / 100)
            )
            FROM OsServico os
            JOIN os.ordemServico o
            JOIN o.funcionario f
            WHERE o.status = com.sgauto.app.enums.StatusOS.FINALIZADA
            AND o.dataConclusao BETWEEN :inicio AND :fim
            GROUP BY f.id, f.nomeCompleto
            ORDER BY SUM(os.valorTotal * f.comissaoPercentual / 100) DESC
            """)
    List<ComissaoFuncionarioDTO> comissaoPorFuncionario(@Param("inicio") LocalDateTime inicio,
                                                        @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new com.sgauto.app.dto.dashboard.ServicoMaisRealizadoDTO(s.id, s.nome, SUM(os.quantidade))
        FROM OsServico os
        JOIN os.servico s
        JOIN os.ordemServico o
        WHERE o.status = com.sgauto.app.enums.StatusOS.FINALIZADA
        AND o.dataConclusao BETWEEN :inicio AND :fim
        GROUP BY s.id, s.nome
        ORDER BY SUM(os.quantidade) DESC
        """)
    List<ServicoMaisRealizadoDTO> servicosMaisRealizados(@Param("inicio") LocalDateTime inicio,
                                                         @Param("fim") LocalDateTime fim);
}
