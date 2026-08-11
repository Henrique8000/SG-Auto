package com.sgauto.app.repository.specifications.usuario;

import com.sgauto.app.model.usuario.PerfilAcesso;
import org.springframework.data.jpa.domain.Specification;

public class PerfilAcessoSpecification {

    private PerfilAcessoSpecification() {}

    public static Specification<PerfilAcesso> comFiltro(String termo, Boolean ativo) {
        Specification<PerfilAcesso> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (termo != null && !termo.trim().isEmpty()) {
            spec = spec.and(termoContem(termo));
        }

        if (ativo != null) {
            spec = spec.and(ativoIgual(ativo));
        }

        return spec;
    }

    private static Specification<PerfilAcesso> termoContem(String termo) {
        if (termo == null || termo.isBlank()) return null;
        String padrao = "%" + termo.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nome")), padrao),
                cb.like(cb.lower(cb.coalesce(root.get("descricao"), "")), padrao)
        );
    }

    private static Specification<PerfilAcesso> ativoIgual(Boolean ativo) {
        if (ativo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }
}
