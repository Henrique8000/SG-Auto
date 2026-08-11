package com.sgauto.app.service.usuario;

import com.sgauto.app.controller.dto.usuario.FiltroPerfilAcessoDTO;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.repository.specifications.usuario.PerfilAcessoSpecification;
import com.sgauto.app.repository.usuario.PerfilAcessoRepository;
import com.sgauto.app.repository.usuario.PermissaoRepository;
import com.sgauto.app.repository.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PerfilAcessoService {

    private final PerfilAcessoRepository perfilAcessoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilAcessoService(PerfilAcessoRepository perfilAcessoRepository,
                               PermissaoRepository permissaoRepository,
                               UsuarioRepository usuarioRepository) {
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PerfilAcesso> listarTodos() {
        return perfilAcessoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PerfilAcesso> listarAtivos() {
        return perfilAcessoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Page<PerfilAcesso> buscar(FiltroPerfilAcessoDTO filtro, Pageable pageable) {
        Specification<PerfilAcesso> spec = PerfilAcessoSpecification.comFiltro(filtro.termo(), filtro.ativo());
        return perfilAcessoRepository.findAll(spec, pageable);
    }

    @Transactional
    public PerfilAcesso cadastrar(String nome, String descricao, Set<Long> permissaoIds) {
        validarNome(nome, null);

        PerfilAcesso perfil = new PerfilAcesso(nome.trim(), descricao);
        perfil.setPermissoes(buscarPermissoes(permissaoIds));

        return perfilAcessoRepository.save(perfil);
    }

    @Transactional
    public PerfilAcesso atualizar(Long id, String nome, String descricao, Set<Long> permissaoIds) {
        PerfilAcesso perfil = buscarOuFalhar(id);

        if (Boolean.TRUE.equals(perfil.getProtegido())) {
            throw new IllegalStateException("O perfil \"" + perfil.getNome() + "\" é protegido pelo sistema e não pode ser editado.");
        }

        validarNome(nome, id);

        perfil.setNome(nome.trim());
        perfil.setDescricao(descricao);
        perfil.setPermissoes(buscarPermissoes(permissaoIds));

        return perfilAcessoRepository.save(perfil);
    }

    @Transactional
    public void ativar(Long id) {
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        PerfilAcesso perfil = buscarOuFalhar(id);

        if (Boolean.TRUE.equals(perfil.getProtegido())) {
            throw new IllegalStateException("O perfil \"" + perfil.getNome() + "\" é protegido pelo sistema e não pode ser desativado.");
        }

        if (usuarioRepository.countByPerfilIdAndAtivoTrue(id) > 0) {
            throw new IllegalStateException("Existem usuários ativos vinculados a este perfil. Transfira-os para outro perfil antes de desativar.");
        }

        perfil.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        PerfilAcesso perfil = buscarOuFalhar(id);

        if (Boolean.TRUE.equals(perfil.getProtegido())) {
            throw new IllegalStateException("O perfil \"" + perfil.getNome() + "\" é protegido pelo sistema e não pode ser excluído.");
        }

        if (!usuarioRepository.findByPerfilId(id).isEmpty()) {
            throw new IllegalStateException("Existem usuários vinculados a este perfil. Não é possível excluir.");
        }

        perfilAcessoRepository.delete(perfil);
    }

    private Set<Permissao> buscarPermissoes(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(permissaoRepository.findAllById(ids));
    }

    private void validarNome(String nome, Long idAtual) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do perfil é obrigatório.");
        }
        perfilAcessoRepository.findByNome(nome.trim()).ifPresent(existente -> {
            if (idAtual == null || !existente.getId().equals(idAtual)) {
                throw new IllegalArgumentException("Já existe um perfil com este nome.");
            }
        });
    }

    private PerfilAcesso buscarOuFalhar(Long id) {
        return perfilAcessoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado: " + id));
    }
}
