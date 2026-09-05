package com.sgauto.app.service.usuario;

import com.sgauto.app.dto.usuario.FiltroPermissaoDTO;
import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.specifications.usuario.PermissaoSpecification;
import com.sgauto.app.repository.usuario.PermissaoRepository;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissaoService {

    private final PermissaoRepository permissaoRepository;

    public PermissaoService(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Permissao> listarTodas() {
        return permissaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, List<Permissao>> listarAgrupadasPorModulo() {
        return permissaoRepository.findAll().stream()
                .collect(Collectors.groupingBy(Permissao::getModulo));
    }

    @Transactional(readOnly = true)
    public Page<Permissao> buscar(FiltroPermissaoDTO filtro, Pageable pageable) {
        Specification<Permissao> spec = PermissaoSpecification.comFiltro(filtro.termo(), filtro.modulo());

        Page<Permissao> pagina = permissaoRepository.findAll(spec, pageable);

        pagina.getContent().forEach(perfilAcesso -> {
            Hibernate.initialize(perfilAcesso.getChave());
        });
        return permissaoRepository.findAll(spec, pageable);
    }
}
