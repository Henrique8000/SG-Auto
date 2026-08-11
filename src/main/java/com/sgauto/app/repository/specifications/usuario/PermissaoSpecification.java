package com.sgauto.app.repository.specifications.usuario;

import com.sgauto.app.model.usuario.Permissao;
import org.springframework.data.jpa.domain.Specification;

public class PermissaoSpecification {

    private PermissaoSpecification() {}

    public static Specification<Permissao> comFiltro(String termo, String modulo) {
        Specification<Permissao> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (termo != null && !termo.trim().isEmpty()) {
            spec = spec.and(termoContem(termo));
        }

        if (modulo != null && !modulo.trim().isEmpty()) {
            spec = spec.and(moduloIgual(modulo));
        }

        return spec;
    }

    private static Specification<Permissao> termoContem(String termo) {
        if (termo == null || termo.isBlank()) return null;
        String padrao = "%" + termo.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("chave")), padrao),
                cb.like(cb.lower(root.get("descricao")), padrao)
        );
    }

    private static Specification<Permissao> moduloIgual(String modulo) {
        if (modulo == null || modulo.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("modulo"), modulo);
    }
}
