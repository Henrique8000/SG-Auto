package com.sgauto.app.service.estoque;

import com.sgauto.app.controller.dto.fornecedor.FiltroFornecedorDTO;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.Fornecedor;
import com.sgauto.app.repository.FornecedorRepository;
import com.sgauto.app.repository.specifications.fornecedor.FornecedorSpecification;
import com.sgauto.app.util.VerificaPermissaoUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final VerificaPermissaoUtil permissaoUtil;

    public FornecedorService(FornecedorRepository fornecedorRepository, VerificaPermissaoUtil permissaoUtil) {
        this.fornecedorRepository = fornecedorRepository;
        this.permissaoUtil = permissaoUtil;
    }


    @Transactional
    public Fornecedor cadastrar(Fornecedor fornecedor) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_CRIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para cadastrar fornecedores.");
        }

        if (fornecedor == null) {
            throw new IllegalArgumentException("Cadastro abortado: os dados do fornecedor enviados estão nulos.");
        }

        validarDadosFornecedor(fornecedor, null);

        fornecedor.setAtivo(true);

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Long id, Fornecedor dadosAtualizados) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para editar fornecedores.");
        }

        Fornecedor fornecedorExistente = buscarPorId(id);

        validarDadosFornecedor(dadosAtualizados, id);

        fornecedorExistente.setTipoPessoa(dadosAtualizados.getTipoPessoa());
        fornecedorExistente.setCpfCnpj(dadosAtualizados.getCpfCnpj());
        fornecedorExistente.setRazaoSocial(dadosAtualizados.getRazaoSocial().trim());
        fornecedorExistente.setNomeFantasia(dadosAtualizados.getNomeFantasia());
        fornecedorExistente.setInscricaoEstadual(dadosAtualizados.getInscricaoEstadual());
        fornecedorExistente.setInscricaoMunicipal(dadosAtualizados.getInscricaoMunicipal());

        fornecedorExistente.setNomeContato(dadosAtualizados.getNomeContato());
        fornecedorExistente.setTelefone(dadosAtualizados.getTelefone());
        fornecedorExistente.setCelular(dadosAtualizados.getCelular());
        fornecedorExistente.setEmail(dadosAtualizados.getEmail());
        fornecedorExistente.setSite(dadosAtualizados.getSite());

        fornecedorExistente.setCep(dadosAtualizados.getCep());
        fornecedorExistente.setLogradouro(dadosAtualizados.getLogradouro());
        fornecedorExistente.setNumero(dadosAtualizados.getNumero());
        fornecedorExistente.setComplemento(dadosAtualizados.getComplemento());
        fornecedorExistente.setBairro(dadosAtualizados.getBairro());
        fornecedorExistente.setCidade(dadosAtualizados.getCidade());
        fornecedorExistente.setUf(dadosAtualizados.getUf());

        fornecedorExistente.setCategoria(dadosAtualizados.getCategoria());
        fornecedorExistente.setPrazoEntregaDias(dadosAtualizados.getPrazoEntregaDias());
        fornecedorExistente.setObservacoes(dadosAtualizados.getObservacoes());

        return fornecedorRepository.save(fornecedorExistente);
    }

    @Transactional
    public void alternarStatus(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_EXCLUIR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para ativar ou desativar fornecedores.");
        }

        Fornecedor fornecedor = buscarPorId(id);

        fornecedor.setAtivo(!fornecedor.getAtivo());

        fornecedorRepository.save(fornecedor);
    }


    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_VISUALIZAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para visualizar fornecedores.");
        }
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fornecedor não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Fornecedor> pesquisar(FiltroFornecedorDTO filtro, Pageable pageable) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_VISUALIZAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para visualizar fornecedores.");
        }
        return fornecedorRepository.findAll(FornecedorSpecification.comFiltros(filtro), pageable);
    }

    @Transactional(readOnly = true)
    public List<Fornecedor> listarAtivosParaSelecao(String categoria) {
        if (categoria != null && !categoria.trim().isEmpty()) {
            return fornecedorRepository.findByCategoriaAndAtivoTrueOrderByRazaoSocialAsc(categoria);
        }
        return fornecedorRepository.findByAtivoTrueOrderByRazaoSocialAsc();
    }

    @Transactional(readOnly = true)
    public List<String> listarCategoriasDisponiveis() {
        return fornecedorRepository.findCategoriasAtivas();
    }


    private void validarDadosFornecedor(Fornecedor fornecedor, Long idAtual) {
        if (fornecedor.getRazaoSocial() == null || fornecedor.getRazaoSocial().trim().isEmpty()) {
            throw new IllegalArgumentException("A Razão Social do fornecedor é obrigatória.");
        }

        if (fornecedor.getCpfCnpj() == null || fornecedor.getCpfCnpj().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF ou CNPJ do fornecedor é obrigatório.");
        }

        String documentoLimpo = fornecedor.getCpfCnpj().replaceAll("[^0-9]", "");
        fornecedor.setCpfCnpj(documentoLimpo);

        boolean documentoJaExiste;
        if (idAtual == null) {
            documentoJaExiste = fornecedorRepository.existsByCpfCnpj(documentoLimpo);
        } else {
            documentoJaExiste = fornecedorRepository.existsByCpfCnpjAndIdNot(documentoLimpo, idAtual);
        }

        if (documentoJaExiste) {
            throw new IllegalStateException("Já existe um fornecedor cadastrado com este CPF/CNPJ: " + documentoLimpo);
        }

        if (fornecedor.getCelular() != null) {
            fornecedor.setCelular(fornecedor.getCelular().replaceAll("[^0-9]", ""));
        }
        if (fornecedor.getCep() != null) {
            fornecedor.setCep(fornecedor.getCep().replaceAll("[^0-9]", ""));
        }
    }

    @Transactional(readOnly = true)
    public long contarTotal() {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_VISUALIZAR)) return 0;
        return fornecedorRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarPorStatus(boolean ativo) {
        if (!permissaoUtil.verificar(PermissaoChave.FORNECEDOR_VISUALIZAR)) return 0;
        return fornecedorRepository.countByAtivo(ativo);
    }
}
