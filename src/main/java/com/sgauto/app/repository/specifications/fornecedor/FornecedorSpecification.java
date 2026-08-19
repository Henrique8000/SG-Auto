package com.sgauto.app.repository.specifications.fornecedor;

import com.sgauto.app.controller.dto.fornecedor.FiltroFornecedorDTO;
import com.sgauto.app.model.Fornecedor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class FornecedorSpecification {

    public static Specification<Fornecedor> comFiltros(FiltroFornecedorDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por Termo (Razão Social, Fantasia ou Documento)
            if (filtro.termo() != null && !filtro.termo().trim().isEmpty()) {
                String termoLike = "%" + filtro.termo().toLowerCase() + "%";
                Predicate razao = cb.like(cb.lower(root.get("razaoSocial")), termoLike);
                Predicate fantasia = cb.like(cb.lower(root.get("nomeFantasia")), termoLike);
                Predicate documento = cb.like(root.get("cpfCnpj"), termoLike);

                predicates.add(cb.or(razao, fantasia, documento));
            }

            // Filtro por Categoria Exata
            if (filtro.categoria() != null && !filtro.categoria().equalsIgnoreCase("Todas as categorias")) {
                predicates.add(cb.equal(root.get("categoria"), filtro.categoria()));
            }

            // Filtro por Status
            if (filtro.ativo() != null) {
                predicates.add(cb.equal(root.get("ativo"), filtro.ativo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
