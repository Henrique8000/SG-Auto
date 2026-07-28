package com.sgauto.app.service;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.repository.FuncionarioRepository;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }


    public List<Funcionario> listarTodos() {
         return funcionarioRepository.findAll();
    }

    public List<Funcionario> listarNaoRemovidos() {
        return funcionarioRepository.findByRemovidoEmIsNull();
    }

    public List<Funcionario> listarPorStatus(StatusFuncionario status) {
        if (status == null)
            throw new IllegalArgumentException("Nenhum STATUS foi informado");

        return funcionarioRepository.findByStatus(status);
    }

    public List<Funcionario> listarPorCargo(CargoFuncionario cargo) {
        if (cargo == null)
            throw new IllegalArgumentException("Nenhum CARGO foi informado");

        return funcionarioRepository.findByCargo(cargo);
    }

    public List<Funcionario> listarAptosParaOrdemServico() {
        return funcionarioRepository.findByExibeEmOsTrueAndStatusAndRemovidoEmIsNull(StatusFuncionario.ATIVO);
    }

    public Optional<Funcionario> buscarPorId(Long id) {
        if(id == null)
            throw new IllegalArgumentException("O id de busca de funcionário esta vazio");

        return funcionarioRepository.findById(id);
    }

    public Optional<Funcionario> buscarPorCpf(String cpf) {
        if(!StringUtils.hasText(cpf))
            throw new IllegalArgumentException("Nenhum CPF foi localizado");

        return funcionarioRepository.findByCpf(cpf);
    }

    public String ultimaMatricula() {
        return funcionarioRepository.findTopByOrderByIdDesc()
                .map(Funcionario::getMatricula)
                .orElse("0");
    }



    @Transactional
    public Funcionario cadastrar(Funcionario novoFuncionario) {
        if (novoFuncionario == null) {
            throw new IllegalArgumentException("Os dados do funcionário não podem ser nulos.");
        }

        novoFuncionario.setId(null);
        novoFuncionario.setRemovidoEm(null);

        // 2. Validação de Duplicidade (Passando null como ID para checar a tabela toda)
        validarDuplicidade(novoFuncionario.getCpf(), novoFuncionario.getMatricula(), null);

        // 3. Validação de Regras de Negócio (ex: datas coerentes)
        validarRegrasDeNegocio(novoFuncionario);

        return funcionarioRepository.save(novoFuncionario);
    }

    @Transactional
    public Funcionario atualizar(Long id, Funcionario funcionarioAtualizado) {
        if (id == null || funcionarioAtualizado == null) {
            throw new IllegalArgumentException("ID e dados do funcionário são obrigatórios para atualização.");
        }

        Funcionario funcionarioExistente = buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Erro ao buscar funcionário de ID: " + id));

        if (funcionarioExistente.getRemovidoEm() != null) {
            throw new IllegalStateException("Não é possível editar um funcionário que já foi excluído/removido do sistema.");
        }

        validarDuplicidade(funcionarioAtualizado.getCpf(), funcionarioAtualizado.getMatricula(), id);

        validarRegrasDeNegocio(funcionarioAtualizado);

        atualizarCamposPermitidos(funcionarioExistente, funcionarioAtualizado);

        return funcionarioRepository.save(funcionarioExistente);
    }

    @Transactional
    public Funcionario alterarStatus(Long id, String novoStatus) {
        if (id == null || novoStatus == null || novoStatus.isBlank()) {
            throw new IllegalArgumentException("ID e novo status são obrigatórios para atualização.");
        }

        Funcionario func = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Erro ao buscar funcionário de ID: " + id));

        if (func.getRemovidoEm() != null) {
            throw new IllegalStateException("Não é possível alterar o status de um funcionário que já foi excluído do sistema.");
        }

        StatusFuncionario statusConvertido;
        try {
            statusConvertido = StatusFuncionario.valueOf(novoStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O status '" + novoStatus + "' é inválido. "
                    + "Valores aceitos: " + java.util.Arrays.toString(StatusFuncionario.values()));
        }

        func.setStatus(statusConvertido);

        return funcionarioRepository.save(func);
    }

    @Transactional
    public void remover(Long id) {
        if (id == null)
            throw new IllegalArgumentException("O ID é obrigatório para a exclusão.");

        Funcionario func = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Erro ao buscar funcionário de ID: " + id));

        if (func.getRemovidoEm() != null)
            throw new IllegalStateException("Não é possível excluir um funcionário já excluído");

        func.setRemovidoEm(OffsetDateTime.now());

        funcionarioRepository.save(func);
    }

    @Transactional
    public void cancelarRemocao(Long id) {
        if (id == null)
            throw new IllegalArgumentException("ID é obrigatórios para cancelar a exclusão");

        Funcionario func = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Erro ao buscar funcionário de ID: " + id));

        if (func.getRemovidoEm() == null)
            throw new IllegalStateException("Não é possível cancelar exclusão de um funcionário não deletado");

        func.setRemovidoEm(null);

        funcionarioRepository.save(func);
    }


    private void validarDuplicidade(String cpf, String matricula, Long idAtual) {
        // Validação de CPF
        boolean cpfDuplicado = (idAtual == null)
                ? funcionarioRepository.existsByCpf(cpf)
                : funcionarioRepository.existsByCpfAndIdNot(cpf, idAtual);

        if (cpfDuplicado) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com o CPF: " + cpf);
        }

        // Validação de Matrícula
        boolean matriculaDuplicada = (idAtual == null)
                ? funcionarioRepository.existsByMatricula(matricula)
                : funcionarioRepository.existsByMatriculaAndIdNot(matricula, idAtual);

        if (matriculaDuplicada) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com a Matrícula: " + matricula);
        }
    }

    private void validarRegrasDeNegocio(Funcionario f) {
        if (f.getDataAdmissao() != null && f.getDataDemissao() != null) {
            if (f.getDataDemissao().isBefore(f.getDataAdmissao())) {
                throw new IllegalArgumentException("A data de demissão não pode ser anterior à data de admissão.");
            }
        }

        if (f.getStatus() == StatusFuncionario.DEMITIDO && f.getDataDemissao() == null) {
            throw new IllegalArgumentException("Para alterar o status para DEMITIDO, a data de demissão deve ser informada.");
        }
    }

    private void atualizarCamposPermitidos(Funcionario existente, Funcionario novo) {
        // Dados pessoais e de contato
        existente.setMatricula(novo.getMatricula());
        existente.setNomeCompleto(novo.getNomeCompleto());
        existente.setNomeSocial(novo.getNomeSocial());
        existente.setCpf(novo.getCpf());
        existente.setRg(novo.getRg());
        existente.setDataNascimento(novo.getDataNascimento());
        existente.setGenero(novo.getGenero());
        existente.setTelefoneFixo(novo.getTelefoneFixo());
        existente.setCelular(novo.getCelular());
        existente.setEmail(novo.getEmail());

        // Endereço
        existente.setCep(novo.getCep());
        existente.setLogradouro(novo.getLogradouro());
        existente.setNumero(novo.getNumero());
        existente.setComplemento(novo.getComplemento());
        existente.setBairro(novo.getBairro());
        existente.setCidade(novo.getCidade());
        existente.setEstado(novo.getEstado());

        // Dados profissionais e operacionais
        existente.setCargo(novo.getCargo());
        existente.setEspecialidade(novo.getEspecialidade());
        existente.setTipoContrato(novo.getTipoContrato());
        existente.setDataAdmissao(novo.getDataAdmissao());
        existente.setDataDemissao(novo.getDataDemissao());
        existente.setCargaHorariaSemanal(novo.getCargaHorariaSemanal());
        existente.setExibeEmOs(novo.getExibeEmOs());
        existente.setCustoHora(novo.getCustoHora());
        existente.setSalarioBase(novo.getSalarioBase());
        existente.setComissaoPercentual(novo.getComissaoPercentual());

        // CNH e Status
        existente.setNumeroCnh(novo.getNumeroCnh());
        existente.setCategoriaCnh(novo.getCategoriaCnh());
        existente.setValidadeCnh(novo.getValidadeCnh());
        existente.setStatus(novo.getStatus());
        existente.setFotoUrl(novo.getFotoUrl());
        existente.setObservacoes(novo.getObservacoes());

        // NOTA: NUNCA atualizamos o 'id', 'dataCriacao' ou 'removidoEm' aqui!
    }
}