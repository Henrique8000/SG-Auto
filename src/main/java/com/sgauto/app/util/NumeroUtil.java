package com.sgauto.app.util;

import java.math.BigDecimal;

public class NumeroUtil {

    private NumeroUtil() {}

    public static BigDecimal parseValorMonetario(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new NumberFormatException("Valor não informado.");
        }

        String limpo = texto.trim();

        if (limpo.contains(",") && limpo.contains(".")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        } else if (limpo.contains(",")) {
            limpo = limpo.replace(",", ".");
        }

        return new BigDecimal(limpo);
    }
}