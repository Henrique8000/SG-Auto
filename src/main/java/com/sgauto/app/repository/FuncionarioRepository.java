package com.sgauto.app.repository;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByCpf(String cpf);

    Optional<Funcionario> findByMatricula(String matricula);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    List<Funcionario> findByStatus(StatusFuncionario status);

    List<Funcionario> findByCargo(CargoFuncionario cargo);

    List<Funcionario> findByCargoAndStatus(CargoFuncionario cargo, StatusFuncionario status);

    List<Funcionario> findByIdIn(List<Long> ids);

    List<Funcionario> findByRemovidoEmIsNull();

    List<Funcionario> findByStatusAndRemovidoEmIsNull(StatusFuncionario status);

    List<Funcionario> findByExibeEmOsTrueAndStatusAndRemovidoEmIsNull(StatusFuncionario status);

    Optional<Funcionario> findTopByOrderByIdDesc();
}