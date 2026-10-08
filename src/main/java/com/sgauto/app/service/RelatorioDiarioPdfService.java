package com.sgauto.app.service;

import com.sgauto.app.dto.dashboard.PecaEstoqueCriticoDTO;
import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO;
import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO.*;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.sgauto.app.util.FormatoRelatorioUtil.*;

/**
 * Gera o PDF do relatório diário a partir do DTO já montado pelo RelatorioService.
 * Não consulta o banco: o PDF é sempre igual ao que está na tela.
 */
@Service
public class RelatorioDiarioPdfService {

    // Paleta sóbria para impressão (fundo branco, tons de cinza e um destaque)
    private static final Color COR_TEXTO = new Color(0x22, 0x22, 0x22);
    private static final Color COR_SECUNDARIA = new Color(0x6B, 0x6B, 0x6B);
    private static final Color COR_BORDA = new Color(0xD0, 0xD0, 0xD0);
    private static final Color COR_CABECALHO = new Color(0xEE, 0xEE, 0xEE);
    private static final Color COR_DESTAQUE = new Color(0xB8, 0x6B, 0x1E);
    private static final Color COR_PERIGO = new Color(0xB0, 0x3A, 0x2E);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, COR_TEXTO);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.NORMAL, COR_SECUNDARIA);
    private static final Font SECAO = new Font(Font.HELVETICA, 11, Font.BOLD, COR_TEXTO);
    private static final Font KPI_TITULO = new Font(Font.HELVETICA, 8, Font.BOLD, COR_SECUNDARIA);
    private static final Font KPI_DETALHE = new Font(Font.HELVETICA, 7, Font.NORMAL, COR_SECUNDARIA);
    private static final Font TABELA_CABECALHO = new Font(Font.HELVETICA, 8, Font.BOLD, COR_TEXTO);
    private static final Font TABELA = new Font(Font.HELVETICA, 8, Font.NORMAL, COR_TEXTO);
    private static final Font TABELA_NEGRITO = new Font(Font.HELVETICA, 8, Font.BOLD, COR_TEXTO);
    private static final Font TABELA_PERIGO = new Font(Font.HELVETICA, 8, Font.NORMAL, COR_PERIGO);
    private static final Font VAZIO = new Font(Font.HELVETICA, 8, Font.ITALIC, COR_SECUNDARIA);
    private static final Font RODAPE = new Font(Font.HELVETICA, 7, Font.NORMAL, COR_SECUNDARIA);

    public void gerar(RelatorioDiarioDTO r, OutputStream saida) {
        Document documento = new Document(PageSize.A4, 36, 36, 36, 48);
        PdfWriter writer = PdfWriter.getInstance(documento, saida);
        writer.setPageEvent(new Rodape(r.data()));

        documento.addTitle("Relatório diário - " + data(r.data()));
        documento.addCreator("SG Auto");
        documento.open();

        cabecalho(documento, r);
        kpis(documento, r);
        financeiro(documento, r.financeiro());
        fechamentos(documento, r.fechamentos());
        osDoDia(documento, r.producao());
        pendencias(documento, r.pendencias());
        mecanicos(documento, r.mecanicos());
        patio(documento, r.patio());
        movimentacoes(documento, r.movimentacoes());
        estoque(documento, r.estoqueCritico());

        documento.close();
    }

    // ===================== CABEÇALHO E KPIs =====================

    private void cabecalho(Document doc, RelatorioDiarioDTO r) {
        doc.add(new Paragraph("Relatório Diário - " + data(r.data()), TITULO));

        boolean ehHoje = r.data().equals(LocalDate.now());
        String posicao = ehHoje
                ? "Pendências e pátio: posição às " + hora(r.referencia()) + " de hoje"
                : "Pendências e pátio: posição ao fim do dia";
        Paragraph sub = new Paragraph("Gerado em " + dataHora(r.geradoEm()) + "  ·  " + posicao, SUBTITULO);
        sub.setSpacingAfter(10);
        doc.add(sub);
    }

    private void kpis(Document doc, RelatorioDiarioDTO r) {
        Financeiro f = r.financeiro();
        Producao p = r.producao();
        Pendencias pe = r.pendencias();

        PdfPTable tabela = new PdfPTable(5);
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(4);

        tabela.addCell(kpi("RECEBIDO NO DIA", moeda(f.totalRecebido()), "Após despesas: " + moeda(f.resultado()), COR_DESTAQUE));
        tabela.addCell(kpi("PRODUZIDO NO DIA", moeda(p.valorProduzido()),
                p.concluidas().size() + " O.S. · ticket " + moeda(p.ticketMedio()), COR_TEXTO));
        tabela.addCell(kpi("MARGEM BRUTA", moeda(p.margemBruta()), "Custo das peças: " + moeda(p.custoPecas()), COR_TEXTO));
        tabela.addCell(kpi("A RECEBER", moeda(pe.totalAReceber()), pe.aReceber().size() + " O.S. com saldo", COR_TEXTO));
        tabela.addCell(kpi("O.S. ATRASADAS", String.valueOf(pe.atrasadas().size()),
                pe.emAndamento().size() + " O.S. em andamento",
                pe.atrasadas().isEmpty() ? COR_TEXTO : COR_PERIGO));

        doc.add(tabela);

        Paragraph nota = new Paragraph(
                "Recebido = entradas no caixa no dia (sem suprimento). Produzido = O.S. concluídas no dia, pagas ou não.",
                KPI_DETALHE);
        nota.setSpacingAfter(6);
        doc.add(nota);
    }

    private PdfPCell kpi(String titulo, String valor, String detalhe, Color corValor) {
        PdfPCell celula = new PdfPCell();
        celula.setBorderColor(COR_BORDA);
        celula.setPadding(6);
        celula.addElement(new Paragraph(titulo, KPI_TITULO));
        celula.addElement(new Paragraph(valor, new Font(Font.HELVETICA, 13, Font.BOLD, corValor)));
        celula.addElement(new Paragraph(detalhe, KPI_DETALHE));
        return celula;
    }

    // ===================== SEÇÕES =====================

    private void financeiro(Document doc, Financeiro f) {
        secao(doc, "Recebimentos e saídas");

        // Três blocos lado a lado: por origem | por forma | saídas e gaveta
        PdfPTable tabela = new PdfPTable(new float[]{3, 2, 0.4f, 3, 2, 0.4f, 3, 2});
        tabela.setWidthPercentage(100);

        String[][] origem = {
                {"Ordens de serviço", moeda(f.recebidoOs())},
                {"Pátio", moeda(f.recebidoPatio())},
                {"Vendas avulsas", moeda(f.recebidoAvulso())},
                {"Contas a receber", moeda(f.recebidoContaReceber())},
                {"Total recebido", moeda(f.totalRecebido())}
        };
        String[][] forma = {
                {"Dinheiro", moeda(f.dinheiro())},
                {"Débito", moeda(f.debito())},
                {"Crédito", moeda(f.credito())},
                {"Pix", moeda(f.pix())},
                {"Outros", moeda(f.outros())}
        };
        String[][] saidas = {
                {"Despesas", moeda(f.despesas())},
                {"Contas pagas", moeda(f.contasPagas())},
                {"Resultado", moeda(f.resultado())},
                {"Suprimentos (troco)", moeda(f.suprimentos())},
                {"Sangrias", moeda(f.sangrias())}
        };

        for (int i = 0; i < 5; i++) {
            parValor(tabela, origem[i], i == 4);
            tabela.addCell(semBorda(""));
            parValor(tabela, forma[i], false);
            tabela.addCell(semBorda(""));
            parValor(tabela, saidas[i], i == 2);
        }

        doc.add(tabela);
    }

    private void parValor(PdfPTable tabela, String[] par, boolean destaque) {
        Font fonte = destaque ? TABELA_NEGRITO : TABELA;
        PdfPCell nome = semBorda(par[0], fonte);
        PdfPCell valor = semBorda(par[1], fonte);
        valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (destaque) {
            nome.setBorder(Rectangle.TOP);
            valor.setBorder(Rectangle.TOP);
            nome.setBorderColor(COR_BORDA);
            valor.setBorderColor(COR_BORDA);
        }
        tabela.addCell(nome);
        tabela.addCell(valor);
    }

    private void fechamentos(Document doc, List<Fechamento> lista) {
        secao(doc, "Fechamentos de caixa no dia");
        tabela(doc, lista, "Nenhum caixa fechado neste dia.",
                new float[]{0.7f, 1.3f, 1.3f, 1.5f, 1.3f, 1.3f, 1.3f, 2.8f},
                List.of(
                        col("Caixa", f -> "#" + f.caixaId()),
                        col("Abertura", f -> diaHora(f.abertura())),
                        col("Fechamento", f -> diaHora(f.fechamento())),
                        col("Fechado por", f -> texto(f.usuario())),
                        colMoeda("Esperado", Fechamento::esperado),
                        colMoeda("Contado", Fechamento::contado),
                        colMoeda("Diferença", Fechamento::diferenca),
                        col("Justificativa", f -> texto(f.justificativa()))));
    }

    private void osDoDia(Document doc, Producao p) {
        secao(doc, "Ordens de serviço do dia");

        record Linha(String evento, OsResumo os) {}
        List<Linha> linhas = new ArrayList<>();
        p.abertas().forEach(os -> linhas.add(new Linha("Aberta", os)));
        p.concluidas().forEach(os -> linhas.add(new Linha("Concluída", os)));
        p.finalizadas().forEach(os -> linhas.add(new Linha("Finalizada", os)));
        p.canceladas().forEach(os -> linhas.add(new Linha("Cancelada", os)));

        tabela(doc, linhas, "Nenhuma O.S. aberta, concluída, finalizada ou cancelada neste dia.",
                new float[]{1.2f, 0.7f, 3, 1.2f, 2.6f, 1.6f, 1.4f},
                List.of(
                        col("Evento", Linha::evento),
                        col("O.S.", l -> "#" + l.os().id()),
                        col("Cliente", l -> l.os().cliente()),
                        col("Placa", l -> l.os().placa()),
                        col("Mecânico", l -> l.os().mecanico()),
                        col("Status atual", l -> status(l.os().status())),
                        colMoeda("Valor", l -> l.os().valorTotal())));
    }

    private void pendencias(Document doc, Pendencias pe) {
        secao(doc, "Pendências");

        record Linha(String situacao, OsPendente os) {}
        List<Linha> linhas = new ArrayList<>();
        pe.emAndamento().forEach(os ->
                linhas.add(new Linha(pe.atrasadas().contains(os) ? "Atrasada" : "Em andamento", os)));
        pe.aReceber().forEach(os -> linhas.add(new Linha("A receber", os)));

        tabela(doc, linhas, "Nenhuma pendência.",
                new float[]{1.7f, 0.7f, 2.2f, 1.3f, 2.4f, 1.3f, 1.6f, 1.3f, 1.3f},
                List.of(
                        col("Situação", Linha::situacao),
                        col("O.S.", l -> "#" + l.os().id()),
                        col("Cliente", l -> l.os().cliente()),
                        col("Placa", l -> l.os().placa()),
                        col("Mecânico", l -> l.os().mecanico()),
                        col("Status atual", l -> status(l.os().status())),
                        col("Previsão", l -> diaHora(l.os().previsao())),
                        colMoeda("Valor", l -> l.os().valorTotal()),
                        colMoeda("Saldo", l -> l.os().saldo())));
    }

    private void mecanicos(Document doc, List<Mecanico> lista) {
        secao(doc, "Produção por mecânico (O.S. concluídas no dia)");
        tabela(doc, lista, "Nenhuma O.S. concluída neste dia.",
                new float[]{3, 0.8f, 1.5f, 1.5f, 1.5f},
                List.of(
                        col("Mecânico", Mecanico::nome),
                        col("O.S.", m -> String.valueOf(m.quantidadeOs())),
                        colMoeda("Serviços", Mecanico::valorServicos),
                        colMoeda("Peças", Mecanico::valorPecas),
                        colMoeda("Total", Mecanico::valorTotal)));
    }

    private void patio(Document doc, Patio pa) {
        secao(doc, "Pátio");

        record Linha(String movimento, EstadiaResumo e) {}
        List<Linha> linhas = new ArrayList<>();
        pa.entradas().forEach(e -> linhas.add(new Linha("Entrada", e)));
        pa.saidas().forEach(e -> linhas.add(new Linha("Saída", e)));
        pa.noPatio().forEach(e -> linhas.add(new Linha("No pátio", e)));

        tabela(doc, linhas, "Nenhum movimento no pátio.",
                new float[]{1.2f, 1.1f, 2.6f, 2, 1.3f, 1.3f, 0.6f, 1.2f},
                List.of(
                        col("Movimento", Linha::movimento),
                        col("Placa", l -> l.e().placa()),
                        col("Cliente", l -> l.e().cliente()),
                        col("Motivo", l -> l.e().motivo()),
                        col("Entrada", l -> diaHora(l.e().entrada())),
                        col("Saída", l -> diaHora(l.e().saida())),
                        col("Dias", l -> String.valueOf(l.e().dias())),
                        colMoeda("Valor", l -> l.e().valor())));
    }

    private void movimentacoes(Document doc, List<Movimentacao> lista) {
        secao(doc, "Movimentações do caixa");
        tabela(doc, lista, "Nenhuma movimentação no caixa neste dia.",
                new float[]{0.8f, 1, 1.2f, 1.2f, 1.4f, 4.5f},
                List.of(
                        col("Hora", m -> hora(m.data())),
                        col("Tipo", m -> tipo(m.tipo())),
                        col("Origem", m -> origem(m.origem())),
                        col("Forma", m -> forma(m.formaPagamento())),
                        colMoeda("Valor", Movimentacao::valor),
                        col("Descrição", m -> texto(m.descricao()))));
    }

    private void estoque(Document doc, List<PecaEstoqueCriticoDTO> lista) {
        secao(doc, "Estoque crítico (posição atual)");
        tabela(doc, lista, "Nenhuma peça abaixo do mínimo.",
                new float[]{5, 1, 1},
                List.of(
                        col("Peça", PecaEstoqueCriticoDTO::nome),
                        col("Qtd.", p -> String.valueOf(p.quantidade())),
                        col("Mín.", p -> String.valueOf(p.estoqueMinimo()))));
    }

    // ===================== TABELAS GENÉRICAS =====================

    // Coluna: título, como extrair o texto, se é valor monetário (alinha à direita e pinta negativo)
    private record Coluna<T>(String titulo, Function<T, String> texto, Function<T, BigDecimal> valor) {}

    private <T> Coluna<T> col(String titulo, Function<T, String> texto) {
        return new Coluna<>(titulo, texto, null);
    }

    private <T> Coluna<T> colMoeda(String titulo, Function<T, BigDecimal> valor) {
        return new Coluna<>(titulo, t -> moeda(valor.apply(t)), valor);
    }

    private <T> void tabela(Document doc, List<T> linhas, String textoVazio, float[] larguras, List<Coluna<T>> colunas) {
        if (linhas.isEmpty()) {
            doc.add(new Paragraph(textoVazio, VAZIO));
            return;
        }

        PdfPTable tabela = new PdfPTable(larguras);
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1); // repete o cabeçalho se a tabela quebrar de página

        for (Coluna<T> c : colunas) {
            PdfPCell cab = new PdfPCell(new Phrase(c.titulo(), TABELA_CABECALHO));
            cab.setBackgroundColor(COR_CABECALHO);
            cab.setBorderColor(COR_BORDA);
            cab.setPadding(4);
            if (c.valor() != null) cab.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabela.addCell(cab);
        }

        for (T linha : linhas) {
            for (Coluna<T> c : colunas) {
                BigDecimal valor = c.valor() != null ? c.valor().apply(linha) : null;
                Font fonte = valor != null && valor.signum() < 0 ? TABELA_PERIGO : TABELA;
                PdfPCell cel = new PdfPCell(new Phrase(c.texto().apply(linha), fonte));
                cel.setBorderColor(COR_BORDA);
                cel.setPadding(4);
                if (c.valor() != null) cel.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(cel);
            }
        }

        doc.add(tabela);
    }

    private void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, SECAO);
        p.setSpacingBefore(12);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    private PdfPCell semBorda(String texto) {
        return semBorda(texto, TABELA);
    }

    private PdfPCell semBorda(String texto, Font fonte) {
        PdfPCell cel = new PdfPCell(new Phrase(texto, fonte));
        cel.setBorder(Rectangle.NO_BORDER);
        cel.setPadding(3);
        return cel;
    }

    // ===================== RODAPÉ =====================

    private static class Rodape extends PdfPageEventHelper {
        private final String texto;

        Rodape(LocalDate data) {
            this.texto = "SG Auto  ·  Relatório diário de " + data(data);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 20;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(texto, RODAPE), document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), RODAPE), document.right(), y, 0);
        }
    }
}