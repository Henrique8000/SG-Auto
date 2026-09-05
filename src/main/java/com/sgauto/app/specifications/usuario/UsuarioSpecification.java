package com.sgauto.app.specifications.usuario;

import com.sgauto.app.model.usuario.Usuario;
import org.springframework.data.jpa.domain.Specification;

public class UsuarioSpecification {

    private UsuarioSpecification() {}

    public static Specification<Usuario> comFiltro(String termo, Long perfilId, Boolean ativo) {
        Specification<Usuario> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (termo != null && !termo.trim().isEmpty()) {
            spec = spec.and(termoContem(termo));
        }

        if (perfilId != null) {
            spec = spec.and(perfilIgual(perfilId));
        }

        if (ativo != null) {
            spec = spec.and(ativoIgual(ativo));
        }

        return spec;
    }

    private static Specification<Usuario> termoContem(String termo) {
        if (termo == null || termo.isBlank()) return null;
        String padrao = "%" + termo.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("login")), padrao),
                cb.like(cb.lower(root.get("nomeExibicao")), padrao),
                cb.like(cb.lower(cb.coalesce(root.get("email"), "")), padrao)
        );
    }

    private static Specification<Usuario> perfilIgual(Long perfilId) {
        if (perfilId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("perfil").get("id"), perfilId);
    }

    private static Specification<Usuario> ativoIgual(Boolean ativo) {
        if (ativo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }
}