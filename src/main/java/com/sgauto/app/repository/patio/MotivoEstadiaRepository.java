package com.sgauto.app.repository.patio;

import com.sgauto.app.model.patio.MotivoEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotivoEstadiaRepository extends JpaRepository<MotivoEstadia, Long> {
    List<MotivoEstadia> findByAtivoTrue();
    Optional<MotivoEstadia> findByNome(String nome);
}
