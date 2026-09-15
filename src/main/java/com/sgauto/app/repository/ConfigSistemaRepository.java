package com.sgauto.app.repository;

import com.sgauto.app.enums.ConfigChave;
import com.sgauto.app.model.ConfigSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigSistemaRepository extends JpaRepository<ConfigSistema, Long> {

    Optional<ConfigSistema> findByChave(ConfigChave chave);

    boolean existsByChave(ConfigChave chave);
}
