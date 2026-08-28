package com.sgauto.app.specifications.patio;

import com.sgauto.app.dto.patio.PatioFiltroDTO;
import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.model.patio.EstadiaPatio;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EstadiaPatioSpecifications {

    private EstadiaPatioSpecifications() {}

    public static Specification<EstadiaPatio> comFiltro(PatioFiltroDTO filtro) {
        return Specification
                .where(comStatus(filtro.getStatus()))
                .and(comBusca(filtro.getBusca()))
                .and(comMotivo(filtro.getMotivoId()))
                .and(comDataEntradaEntre(filtro.getDataEntradaInicio(), filtro.getDataEntradaFim()));
    }

    private static Specification<EstadiaPatio> comStatus(StatusEstadiaPatio status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<EstadiaPatio> comBusca(String busca) {
        return (root, query, cb) -> {
            if (busca == null || busca.isBlank()) return null;
            String termo = "%" + busca.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("placa")), termo),
                    cb.like(cb.lower(root.join("cliente").get("nome")), termo)
            );
        };
    }

    private static Specification<EstadiaPatio> comMotivo(Long motivoId) {
        return (root, query, cb) -> motivoId == null ? null : cb.equal(root.join("motivo").get("id"), motivoId);
    }

    private static Specification<EstadiaPatio> comDataEntradaEntre(LocalDate inicio, LocalDate fim) {
        return (root, query, cb) -> {
            if (inicio == null && fim == null) return null;
            LocalDateTime de = inicio != null ? inicio.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime ate = fim != null ? fim.atTime(23, 59, 59) : LocalDateTime.now();
            return cb.between(root.get("dataEntrada"), de, ate);
        };
    }
}