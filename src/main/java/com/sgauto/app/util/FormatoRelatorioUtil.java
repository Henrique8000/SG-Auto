package com.sgauto.app.util;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.os.StatusOS;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatação de valores do relatório diário, compartilhada pela tela e pelo PDF.
 * Valores nulos viram "—".
 */
public final class FormatoRelatorioUtil {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String VAZIO = "—";

    private FormatoRelatorioUtil() {}

    // NumberFormat não é thread-safe: cria um por chamada (a tela e o PDF rodam em threads diferentes)
    public static String moeda(BigDecimal valor) {
        return valor == null ? VAZIO : NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    public static String data(LocalDate data) {
        return data == null ? VAZIO : data.format(DATA);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? VAZIO : dataHora.format(DATA_HORA);
    }

    public static String diaHora(LocalDateTime dataHora) {
        return dataHora == null ? VAZIO : dataHora.format(DIA_HORA);
    }

    public static String hora(LocalDateTime dataHora) {
        return dataHora == null ? VAZIO : dataHora.format(HORA);
    }

    public static String status(StatusOS status) {
        if (status == null) return VAZIO;
        return switch (status) {
            case ABERTA -> "Aberta";
            case VERIFICANDO_ORCAMENTO -> "Verificando orçamento";
            case EM_EXECUCAO -> "Em execução";
            case AGUARDANDO -> "Aguardando";
            case CONCLUIDA -> "Concluída";
            case FINALIZADA -> "Finalizada";
            case CANCELADA -> "Cancelada";
        };
    }

    public static String origem(OrigemMovimentacao origem) {
        if (origem == null) return VAZIO;
        return switch (origem) {
            case OS_PAGAMENTO -> "O.S.";
            case PATIO -> "Pátio";
            case AVULSO -> "Avulso";
            case SUPRIMENTO -> "Suprimento";
            case SANGRIA -> "Sangria";
            case CONTA_RECEBER -> "Conta-Receber";
            case CONTA_PAGAR -> "Conta-Pagar";
        };
    }

    public static String forma(FormaPagamento forma) {
        if (forma == null) return VAZIO;
        return switch (forma) {
            case DINHEIRO -> "Dinheiro";
            case DEBITO -> "Débito";
            case CREDITO -> "Crédito";
            case PIX -> "Pix";
            case OUTROS -> "Outros";
            case ISENTO -> "Isento";
        };
    }

    public static String tipo(TipoMovimentacao tipo) {
        if (tipo == null) return VAZIO;
        return tipo == TipoMovimentacao.ENTRADA ? "Entrada" : "Saída";
    }

    public static String texto(String valor) {
        return valor == null || valor.isBlank() ? VAZIO : valor;
    }
}