package com.sgauto.app.repository.specifications.estoque;

import com.sgauto.app.controller.dto.estoque.FiltroCategoriaFornecedorDTO;
import com.sgauto.app.model.estoque.CategoriaFornecedor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategoriaFornecedorSpecification {

    public static Specification<CategoriaFornecedor> comFiltros(FiltroCategoriaFornecedorDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por Termo (Busca no Nome ou na Descrição)
            if (filtro.termo() != null && !filtro.termo().trim().isEmpty()) {
                String termoLike = "%" + filtro.termo().toLowerCase() + "%";

                Predicate nome = cb.like(cb.lower(root.get("nome")), termoLike);
                Predicate descricao = cb.like(cb.lower(root.get("descricao")), termoLike);

                predicates.add(cb.or(nome, descricao));
            }

            // Filtro por Status (Ativo / Inativo / Todos)
            if (filtro.ativo() != null) {
                predicates.add(cb.equal(root.get("ativo"), filtro.ativo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
