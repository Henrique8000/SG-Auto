package com.sgauto.app.enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum PeriodoDashboard {

    ULTIMOS_7_DIAS("7 dias", 7),
    ULTIMOS_30_DIAS("30 dias", 30),
    ULTIMOS_90_DIAS("90 dias", 90);

    private final String label;
    private final int dias;

    PeriodoDashboard(String label, int dias) {
        this.label = label;
        this.dias = dias;
    }

    public String getLabel() { return label; }
    public int getDias() { return dias; }

    public LocalDateTime getInicio() {
        return LocalDate.now().minusDays(dias - 1L).atStartOfDay();
    }

    public LocalDateTime getFim() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }
}
