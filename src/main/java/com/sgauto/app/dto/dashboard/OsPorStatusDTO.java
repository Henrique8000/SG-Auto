package com.sgauto.app.dto.dashboard;

import com.sgauto.app.enums.StatusOS;

public record OsPorStatusDTO(
        StatusOS status,
        long quantidade
) {
}
