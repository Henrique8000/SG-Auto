package com.sgauto.app.controller.relatorio;

import com.sgauto.app.dto.dashboard.PecaEstoqueCriticoDTO;
import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO;
import com.sgauto.app.dto.relatorio.RelatorioDiarioDTO.*;
import com.sgauto.app.service.RelatorioDiarioPdfService;
import com.sgauto.app.service.RelatorioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import static com.sgauto.app.util.FormatoRelatorioUtil.*;

@Component
public class RelatorioDiarioController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RelatorioDiarioController.class);

    // ---- Filtro ----
    @FXML private Button btnDiaAnterior;
    @FXML private DatePicker dpData;
    @FXML private Button btnDiaSeguinte;
    @FXML private Button btnHoje;
    @FXML private Label lblGeradoEm;
    @FXML private Button btnAtualizar;
    @FXML private Button btnExportarPdf;

    // ---- KPIs ----
    @FXML private Label lblRecebido;
    @FXML private Label lblResultado;
    @FXML private Label lblProduzido;
    @FXML private Label lblProduzidoDetalhe;
    @FXML private Label lblMargem;
    @FXML private Label lblMargemDetalhe;
    @FXML private Label lblAReceber;
    @FXML private Label lblAReceberDetalhe;
    @FXML private Label lblAtrasadas;
    @FXML private Label lblEmAndamento;
    @FXML private Label lblPosicao;

    // ---- Seções ----
    @FXML private GridPane gridFinanceiro;
    @FXML private TableView<Fechamento> tabelaFechamentos;
    @FXML private TableView<LinhaOs> tabelaOsDia;
    @FXML private TableView<LinhaPendencia> tabelaPendencias;
    @FXML private TableView<Mecanico> tabelaMecanicos;
    @FXML private TableView<LinhaPatio> tabelaPatio;
    @FXML private TableView<Movimentacao> tabelaMovimentacoes;
    @FXML private TableView<PecaEstoqueCriticoDTO> tabelaEstoque;

    // Linhas das tabelas que juntam várias listas do DTO
    private record LinhaOs(String evento, OsResumo os) {}
    private record LinhaPendencia(String situacao, OsPendente os) {}
    private record LinhaPatio(String movimento, EstadiaResumo estadia) {}

    private final RelatorioService relatorioService;
    private final RelatorioDiarioPdfService relatorioDiarioPdfService;

    // Último relatório exibido: é ele que vai para o PDF
    private RelatorioDiarioDTO relatorioAtual;

    // Lembra a pasta do último PDF salvo enquanto o sistema estiver aberto
    private File ultimoDiretorio;

    public RelatorioDiarioController(RelatorioService relatorioService,
                                     RelatorioDiarioPdfService relatorioDiarioPdfService) {
        this.relatorioService = relatorioService;
        this.relatorioDiarioPdfService = relatorioDiarioPdfService;
    }

    @FXML
    public void initialize() {
        configurarFiltro();
        configurarTabelas();

        relatorioAtual = null;
        btnExportarPdf.setDisable(true);
        dpData.setValue(LocalDate.now()); // dispara o carregamento pelo listener
    }

    // ===================== FILTRO =====================

    private void configurarFiltro() {
        // Não permite escolher datas futuras
        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });

        dpData.valueProperty().addListener((obs, antiga, nova) -> {
            if (nova == null || nova.isAfter(LocalDate.now())) {
                dpData.setValue(antiga != null ? antiga : LocalDate.now());
                return;
            }
            btnDiaSeguinte.setDisable(!nova.isBefore(LocalDate.now()));
            carregar();
        });

        btnDiaAnterior.setOnAction(e -> dpData.setValue(dpData.getValue().minusDays(1)));
        btnDiaSeguinte.setOnAction(e -> dpData.setValue(dpData.getValue().plusDays(1)));
        btnHoje.setOnAction(e -> dpData.setValue(LocalDate.now()));
        btnAtualizar.setOnAction(e -> carregar());
        btnExportarPdf.setOnAction(e -> exportarPdf());
    }

    // ===================== CARREGAMENTO =====================

    private void carregar() {
        LocalDate data = dpData.getValue();
        if (data == null) return;

        btnAtualizar.setDisable(true);
        lblGeradoEm.setText("Gerando relatório...");

        Task<RelatorioDiarioDTO> task = new Task<>() {
            @Override
            protected RelatorioDiarioDTO call() {
                return relatorioService.montarRelatorioDiario(data);
            }
        };

        task.setOnSucceeded(e -> {
            btnAtualizar.setDisable(false);
            RelatorioDiarioDTO relatorio = task.getValue();
            // Se o usuário trocou a data enquanto carregava, descarta o resultado antigo
            if (relatorio.data().equals(dpData.getValue())) {
                preencher(relatorio);
            }
        });

        task.setOnFailed(e -> {
            btnAtualizar.setDisable(false);
            lblGeradoEm.setText("");
            Throwable erro = task.getException();
            if (erro instanceof IllegalArgumentException || erro instanceof IllegalStateException) {
                mostrarAlerta(Alert.AlertType.WARNING, erro.getMessage());
            } else {
                log.error("Erro ao gerar relatório diário de {}", data, erro);
                mostrarAlerta(Alert.AlertType.ERROR, "Não foi possível gerar o relatório. Os detalhes foram registrados no log.");
            }
        });

        Thread thread = new Thread(task, "relatorio-diario-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void preencher(RelatorioDiarioDTO r) {
        btnExportarPdf.setDisable(false);
        relatorioAtual = r;
        lblGeradoEm.setText("Gerado às " + hora(r.geradoEm()));

        preencherKpis(r);
        preencherFinanceiro(r.financeiro());

        tabelaFechamentos.getItems().setAll(r.fechamentos());
        tabelaOsDia.getItems().setAll(linhasOs(r.producao()));
        tabelaPendencias.getItems().setAll(linhasPendencias(r.pendencias()));
        tabelaMecanicos.getItems().setAll(r.mecanicos());
        tabelaPatio.getItems().setAll(linhasPatio(r.patio()));
        tabelaMovimentacoes.getItems().setAll(r.movimentacoes());
        tabelaEstoque.getItems().setAll(r.estoqueCritico());
    }

    private void preencherKpis(RelatorioDiarioDTO r) {
        Financeiro f = r.financeiro();
        Producao p = r.producao();
        Pendencias pe = r.pendencias();

        lblRecebido.setText(moeda(f.totalRecebido()));
        lblResultado.setText("Após despesas: " + moeda(f.resultado()));

        lblProduzido.setText(moeda(p.valorProduzido()));
        lblProduzidoDetalhe.setText(p.concluidas().size() + " O.S. concluídas · ticket " + moeda(p.ticketMedio()));

        lblMargem.setText(moeda(p.margemBruta()));
        lblMargemDetalhe.setText("Custo das peças: " + moeda(p.custoPecas()));

        lblAReceber.setText(moeda(pe.totalAReceber()));
        lblAReceberDetalhe.setText(pe.aReceber().size() + " O.S. com saldo");

        lblAtrasadas.setText(String.valueOf(pe.atrasadas().size()));
        lblEmAndamento.setText(pe.emAndamento().size() + " O.S. em andamento");

        boolean ehHoje = r.data().equals(LocalDate.now());
        lblPosicao.setText(ehHoje
                ? "Pendências e pátio: posição agora (" + hora(r.referencia()) + ")."
                : "Pendências e pátio: posição ao fim do dia " + data(r.data()) + ".");
    }

    private void preencherFinanceiro(Financeiro f) {
        gridFinanceiro.getChildren().clear();
        int linha = 0;

        linha = linhaFinanceiro(linha, "Ordens de serviço", f.recebidoOs(), false);
        linha = linhaFinanceiro(linha, "Pátio", f.recebidoPatio(), false);
        linha = linhaFinanceiro(linha, "Vendas avulsas", f.recebidoAvulso(), false);
        linha = linhaFinanceiro(linha, "Contas a receber", f.recebidoContaReceber(), false);
        linha = linhaFinanceiro(linha, "Total recebido", f.totalRecebido(), true);
        linha = separador(linha);
        linha = linhaFinanceiro(linha, "Dinheiro", f.dinheiro(), false);
        linha = linhaFinanceiro(linha, "Débito", f.debito(), false);
        linha = linhaFinanceiro(linha, "Crédito", f.credito(), false);
        linha = linhaFinanceiro(linha, "Pix", f.pix(), false);
        if (f.outros().signum() != 0) {
            linha = linhaFinanceiro(linha, "Outros", f.outros(), false);
        }
        linha = separador(linha);
        linha = linhaFinanceiro(linha, "Despesas", f.despesas(), false);
        linha = linhaFinanceiro(linha, "Contas pagas", f.contasPagas(), false);
        linha = linhaFinanceiro(linha, "Resultado", f.resultado(), true);
        linha = separador(linha);
        linha = linhaFinanceiro(linha, "Suprimentos (troco)", f.suprimentos(), false);
        linhaFinanceiro(linha, "Sangrias", f.sangrias(), false);
    }

    private int linhaFinanceiro(int linha, String nome, BigDecimal valor, boolean destaque) {
        Label lblNome = new Label(nome);
        Label lblValor = new Label(moeda(valor));
        lblNome.getStyleClass().add(destaque ? "card-title" : "form-label");
        lblValor.getStyleClass().add(destaque ? "card-title" : "form-label");
        if (valor != null && valor.signum() < 0) {
            lblValor.setStyle("-fx-text-fill: -danger;");
        }
        gridFinanceiro.addRow(linha, lblNome, lblValor);
        return linha + 1;
    }

    private int separador(int linha) {
        gridFinanceiro.add(new Separator(), 0, linha, 2, 1);
        return linha + 1;
    }

    // ===================== LINHAS COMBINADAS =====================

    private List<LinhaOs> linhasOs(Producao p) {
        List<LinhaOs> linhas = new ArrayList<>();
        p.abertas().forEach(os -> linhas.add(new LinhaOs("Aberta", os)));
        p.concluidas().forEach(os -> linhas.add(new LinhaOs("Concluída", os)));
        p.finalizadas().forEach(os -> linhas.add(new LinhaOs("Finalizada", os)));
        p.canceladas().forEach(os -> linhas.add(new LinhaOs("Cancelada", os)));
        return linhas;
    }

    private List<LinhaPendencia> linhasPendencias(Pendencias pe) {
        List<LinhaPendencia> linhas = new ArrayList<>();
        pe.emAndamento().forEach(os ->
                linhas.add(new LinhaPendencia(pe.atrasadas().contains(os) ? "Atrasada" : "Em andamento", os)));
        pe.aReceber().forEach(os -> linhas.add(new LinhaPendencia("A receber", os)));
        return linhas;
    }

    private List<LinhaPatio> linhasPatio(Patio pa) {
        List<LinhaPatio> linhas = new ArrayList<>();
        pa.entradas().forEach(e -> linhas.add(new LinhaPatio("Entrada", e)));
        pa.saidas().forEach(e -> linhas.add(new LinhaPatio("Saída", e)));
        pa.noPatio().forEach(e -> linhas.add(new LinhaPatio("No pátio", e)));
        return linhas;
    }

    // ===================== TABELAS =====================

    private void configurarTabelas() {
        coluna(tabelaFechamentos, "Caixa", 60, f -> "#" + f.caixaId());
        coluna(tabelaFechamentos, "Abertura", 110, f -> diaHora(f.abertura()));
        coluna(tabelaFechamentos, "Fechamento", 110, f -> diaHora(f.fechamento()));
        coluna(tabelaFechamentos, "Fechado por", 100, f -> texto(f.usuario()));
        coluna(tabelaFechamentos, "Esperado", 100, f -> moeda(f.esperado()));
        coluna(tabelaFechamentos, "Contado", 100, f -> moeda(f.contado()));
        colunaValor(tabelaFechamentos, "Diferença", 100, Fechamento::diferenca);
        coluna(tabelaFechamentos, "Justificativa", 200, f -> texto(f.justificativa()));
        vazio(tabelaFechamentos, "Nenhum caixa fechado neste dia.");

        coluna(tabelaOsDia, "Evento", 90, l -> l.evento());
        coluna(tabelaOsDia, "O.S.", 60, l -> "#" + l.os().id());
        coluna(tabelaOsDia, "Cliente", 200, l -> l.os().cliente());
        coluna(tabelaOsDia, "Placa", 90, l -> l.os().placa());
        coluna(tabelaOsDia, "Mecânico", 180, l -> l.os().mecanico());
        coluna(tabelaOsDia, "Status atual", 130, l -> status(l.os().status()));
        coluna(tabelaOsDia, "Valor", 110, l -> moeda(l.os().valorTotal()));
        vazio(tabelaOsDia, "Nenhuma O.S. aberta, concluída, finalizada ou cancelada neste dia.");

        coluna(tabelaPendencias, "Situação", 110, l -> l.situacao());
        coluna(tabelaPendencias, "O.S.", 60, l -> "#" + l.os().id());
        coluna(tabelaPendencias, "Cliente", 200, l -> l.os().cliente());
        coluna(tabelaPendencias, "Placa", 90, l -> l.os().placa());
        coluna(tabelaPendencias, "Mecânico", 180, l -> l.os().mecanico());
        coluna(tabelaPendencias, "Status atual", 130, l -> status(l.os().status()));
        coluna(tabelaPendencias, "Previsão", 110, l -> diaHora(l.os().previsao()));
        coluna(tabelaPendencias, "Valor", 110, l -> moeda(l.os().valorTotal()));
        coluna(tabelaPendencias, "Saldo", 110, l -> moeda(l.os().saldo()));
        vazio(tabelaPendencias, "Nenhuma pendência.");

        coluna(tabelaMecanicos, "Mecânico", 180, Mecanico::nome);
        coluna(tabelaMecanicos, "O.S.", 50, m -> String.valueOf(m.quantidadeOs()));
        coluna(tabelaMecanicos, "Serviços", 100, m -> moeda(m.valorServicos()));
        coluna(tabelaMecanicos, "Peças", 100, m -> moeda(m.valorPecas()));
        coluna(tabelaMecanicos, "Total", 100, m -> moeda(m.valorTotal()));
        vazio(tabelaMecanicos, "Nenhuma O.S. concluída neste dia.");

        coluna(tabelaPatio, "Movimento", 90, l -> l.movimento());
        coluna(tabelaPatio, "Placa", 90, l -> l.estadia().placa());
        coluna(tabelaPatio, "Cliente", 170, l -> l.estadia().cliente());
        coluna(tabelaPatio, "Motivo", 140, l -> l.estadia().motivo());
        coluna(tabelaPatio, "Entrada", 100, l -> diaHora(l.estadia().entrada()));
        coluna(tabelaPatio, "Saída", 100, l -> diaHora(l.estadia().saida()));
        coluna(tabelaPatio, "Dias", 50, l -> String.valueOf(l.estadia().dias()));
        coluna(tabelaPatio, "Valor", 90, l -> moeda(l.estadia().valor()));
        vazio(tabelaPatio, "Nenhum movimento no pátio.");

        coluna(tabelaMovimentacoes, "Hora", 60, m -> hora(m.data()));
        coluna(tabelaMovimentacoes, "Tipo", 70, m -> tipo(m.tipo()));
        coluna(tabelaMovimentacoes, "Origem", 90, m -> origem(m.origem()));
        coluna(tabelaMovimentacoes, "Forma", 80, m -> forma(m.formaPagamento()));
        coluna(tabelaMovimentacoes, "Valor", 100, m -> moeda(m.valor()));
        coluna(tabelaMovimentacoes, "Descrição", 260, m -> texto(m.descricao()));
        vazio(tabelaMovimentacoes, "Nenhuma movimentação no caixa neste dia.");

        coluna(tabelaEstoque, "Peça", 160, PecaEstoqueCriticoDTO::nome);
        coluna(tabelaEstoque, "Qtd.", 50, p -> String.valueOf(p.quantidade()));
        coluna(tabelaEstoque, "Mín.", 50, p -> String.valueOf(p.estoqueMinimo()));
        vazio(tabelaEstoque, "Nenhuma peça abaixo do mínimo.");
    }

    private <T> void coluna(TableView<T> tabela, String titulo, double largura, Function<T, String> valor) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
        col.setPrefWidth(largura);
        col.setSortable(false);
        tabela.getColumns().add(col);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // Coluna de valor que fica vermelha quando negativa (ex.: quebra de caixa)
    private <T> void colunaValor(TableView<T> tabela, String titulo, double largura, Function<T, BigDecimal> valor) {
        TableColumn<T, BigDecimal> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(valor.apply(c.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : moeda(item));
                setStyle(!empty && item != null && item.signum() < 0 ? "-fx-text-fill: -danger;" : "");
            }
        });
        col.setPrefWidth(largura);
        col.setSortable(false);
        tabela.getColumns().add(col);
    }

    private void vazio(TableView<?> tabela, String texto) {
        Label placeholder = new Label(texto);
        placeholder.getStyleClass().add("placeholder-text");
        tabela.setPlaceholder(placeholder);
    }

    // ===================== EXPORTAÇÃO EM PDF =====================

    private void exportarPdf() {
        // O PDF é gerado a partir do relatório que está na tela: o que o usuário vê é o que vai para o arquivo
        RelatorioDiarioDTO relatorio = relatorioAtual;
        if (relatorio == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar relatório diário");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        chooser.setInitialFileName("relatorio-diario-" + relatorio.data() + ".pdf");
        if (ultimoDiretorio != null && ultimoDiretorio.isDirectory()) {
            chooser.setInitialDirectory(ultimoDiretorio);
        }

        File destino = chooser.showSaveDialog(btnExportarPdf.getScene().getWindow());
        if (destino == null) return;
        ultimoDiretorio = destino.getParentFile();

        btnExportarPdf.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (OutputStream saida = new FileOutputStream(destino)) {
                    relatorioDiarioPdfService.gerar(relatorio, saida);
                } catch (Exception e) {
                    destino.delete(); // não deixa um PDF pela metade no disco
                    throw e;
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            btnExportarPdf.setDisable(false);
            log.info("Relatório diário de {} exportado em PDF", relatorio.data());
            oferecerAbrir(destino);
        });

        task.setOnFailed(e -> {
            btnExportarPdf.setDisable(false);
            log.error("Erro ao exportar relatório diário de {} em PDF", relatorio.data(), task.getException());
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Não foi possível salvar o PDF. Verifique se o arquivo não está aberto em outro programa.");
        });

        Thread thread = new Thread(task, "relatorio-diario-pdf");
        thread.setDaemon(true);
        thread.start();
    }

    private void oferecerAbrir(File arquivo) {
        ButtonType abrir = new ButtonType("Abrir", ButtonBar.ButtonData.OK_DONE);
        ButtonType fechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION, "PDF salvo em:\n" + arquivo.getAbsolutePath(), abrir, fechar);
        alerta.setTitle("Relatório diário");
        alerta.setHeaderText(null);
        alerta.showAndWait()
                .filter(botao -> botao == abrir)
                .ifPresent(botao -> abrirNoSistema(arquivo));
    }

    // Desktop (AWT) roda fora da thread do JavaFX para não travar a tela
    private void abrirNoSistema(File arquivo) {
        Thread thread = new Thread(() -> {
            try {
                if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    throw new IOException("Abertura de arquivos não suportada neste sistema");
                }
                Desktop.getDesktop().open(arquivo);
            } catch (IOException e) {
                log.warn("Não foi possível abrir o PDF {}", arquivo, e);
                Platform.runLater(() -> mostrarAlerta(Alert.AlertType.WARNING,
                        "O PDF foi salvo, mas não foi possível abri-lo automaticamente.\nAbra pelo caminho:\n" + arquivo.getAbsolutePath()));
            }
        }, "relatorio-diario-abrir-pdf");
        thread.setDaemon(true);
        thread.start();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Relatório diário");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}