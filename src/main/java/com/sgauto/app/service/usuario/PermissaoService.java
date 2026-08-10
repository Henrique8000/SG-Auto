package com.sgauto.app.service.usuario;

import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.repository.usuario.PermissaoRepository;
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
}
