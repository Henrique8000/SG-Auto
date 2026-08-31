package com.sgauto.app.controller;

import com.sgauto.app.dto.dashboard.*;
import com.sgauto.app.enums.PeriodoDashboard;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.service.DashboardService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Component
public class DashboardController implements javafx.fxml.Initializable {

    // ---- KPIs (cards do topo) ----
    @FXML private Label lblFaturamentoDia;
    @FXML private Label lblFaturamentoMes;
    @FXML private Label lblFaturamentoOsMes;
    @FXML private Label lblOsAbertas;
    @FXML private Label lblOsAguardando;
    @FXML private Label lblTicketMedio;
    @FXML private Label lblEstoqueCritico;
    @FXML private Label lblVeiculosPatio;

    // ---- Filtro de período ----
    @FXML private ToggleButton btnPeriodo7d;
    @FXML private ToggleButton btnPeriodo30d;
    @FXML private ToggleButton btnPeriodo90d;
    @FXML private Button btnAtualizar;

    // ---- Gráficos ----
    @FXML private BarChart<String, Number> chartOsPorStatus;
    @FXML private LineChart<String, Number> chartFaturamento;
    @FXML private BarChart<String, Number> chartComissao;

    // ---- Tabelas de alerta ----
    @FXML private TableView<PecaEstoqueCriticoDTO> tabelaPecasCriticas;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, String> colPecaNome;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, Integer> colPecaQuantidade;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, Integer> colPecaMinimo;

    @FXML private TableView<VeiculoPatioDTO> tabelaPatio;
    @FXML private TableColumn<VeiculoPatioDTO, String> colPatioPlaca;
    @FXML private TableColumn<VeiculoPatioDTO, String> colPatioCliente;
    @FXML private TableColumn<VeiculoPatioDTO, Long> colPatioDias;

    private final DashboardService dashboardService;
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoDataCurta = DateTimeFormatter.ofPattern("dd/MM");

    private ToggleGroup grupoPeriodo;
    private PeriodoDashboard periodoSelecionado = PeriodoDashboard.ULTIMOS_30_DIAS;

    private Consumer<StatusOS> onStatusSelecionado = status -> { };

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public void setOnStatusSelecionado(Consumer<StatusOS> callback) {
        this.onStatusSelecionado = callback != null ? callback : (status -> { });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarFiltroPeriodo();
        configurarTabelas();
        configurarGraficos();
        carregarTudo();
    }

    private void configurarFiltroPeriodo() {
        grupoPeriodo = new ToggleGroup();
        btnPeriodo7d.setToggleGroup(grupoPeriodo);
        btnPeriodo30d.setToggleGroup(grupoPeriodo);
        btnPeriodo90d.setToggleGroup(grupoPeriodo);
        btnPeriodo30d.setSelected(true);

        btnPeriodo7d.setOnAction(e -> selecionarPeriodo(PeriodoDashboard.ULTIMOS_7_DIAS));
        btnPeriodo30d.setOnAction(e -> selecionarPeriodo(PeriodoDashboard.ULTIMOS_30_DIAS));
        btnPeriodo90d.setOnAction(e -> selecionarPeriodo(PeriodoDashboard.ULTIMOS_90_DIAS));

        btnAtualizar.setOnAction(e -> carregarTudo());
    }

    private void selecionarPeriodo(PeriodoDashboard periodo) {
        this.periodoSelecionado = periodo;
        carregarFaturamento();
        carregarComissao();
    }

    private void configurarTabelas() {
        colPecaNome.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colPecaQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeEstoque"));
        colPecaMinimo.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));

        colPatioPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colPatioCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colPatioDias.setCellValueFactory(new PropertyValueFactory<>("diasNoPatio"));
    }

    private void configurarGraficos() {
        chartOsPorStatus.setAnimated(true);
        chartOsPorStatus.setLegendVisible(false);
        chartFaturamento.setAnimated(true);
        chartFaturamento.setCreateSymbols(true);
        chartComissao.setAnimated(true);
        chartComissao.setLegendVisible(false);
    }

    private void carregarTudo() {
        carregarResumo();
        carregarOsPorStatus();
        carregarFaturamento();
        carregarComissao();
        carregarAlertas();
    }

    private void carregarResumo() {
        executarEmBackground(dashboardService::montarResumo, this::preencherResumo);
    }

    private void carregarOsPorStatus() {
        executarEmBackground(dashboardService::osPorStatus, this::preencherGraficoOsPorStatus);
    }

    private void carregarFaturamento() {
        executarEmBackground(
                () -> dashboardService.faturamentoNoPeriodo(periodoSelecionado),
                this::preencherGraficoFaturamento
        );
    }

    private void carregarComissao() {
        executarEmBackground(
                () -> dashboardService.comissaoPorFuncionario(periodoSelecionado),
                this::preencherGraficoComissao
        );
    }

    private void carregarAlertas() {
        executarEmBackground(dashboardService::pecasEstoqueCritico,
                lista -> tabelaPecasCriticas.getItems().setAll(lista));
        executarEmBackground(dashboardService::veiculosNoPatio,
                lista -> tabelaPatio.getItems().setAll(lista));
    }

    private <T> void executarEmBackground(java.util.concurrent.Callable<T> consulta, Consumer<T> aoConcluir) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return consulta.call();
            }
        };
        task.setOnSucceeded(e -> aoConcluir.accept(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace()); // troque pelo seu TratadorErrosGlobal

        Thread thread = new Thread(task, "dashboard-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void preencherResumo(DashboardResumoDTO resumo) {
        lblFaturamentoDia.setText(formatoMoeda.format(resumo.faturamentoCaixaDia()));
        lblFaturamentoMes.setText(formatoMoeda.format(resumo.faturamentoCaixaMes()));
        lblFaturamentoOsMes.setText(formatoMoeda.format(resumo.faturamentoOsFinalizadasMes()));
        lblOsAbertas.setText(String.valueOf(resumo.osAbertasTotal()));
        lblOsAguardando.setText(resumo.osAguardandoAprovacao() + " aguardando aprovação");
        lblTicketMedio.setText(formatoMoeda.format(resumo.ticketMedio()));
        lblEstoqueCritico.setText(String.valueOf(resumo.pecasEstoqueCritico()));
        lblVeiculosPatio.setText(String.valueOf(resumo.veiculosNoPatio()));
    }

    private void preencherGraficoOsPorStatus(List<OsPorStatusDTO> dados) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (OsPorStatusDTO item : dados) {
            serie.getData().add(new XYChart.Data<>(formatarStatus(item.status()), item.quantidade()));
        }
        chartOsPorStatus.getData().setAll(serie);

        Platform.runLater(() -> serie.getData().forEach(ponto -> {
            String texto = ponto.getYValue() + " OS em \"" + ponto.getXValue() + "\"";
            Tooltip.install(ponto.getNode(), new Tooltip(texto));
            ponto.getNode().setCursor(Cursor.HAND);
            ponto.getNode().setOnMouseClicked(evt -> {
                StatusOS status = statusOriginal(dados, ponto.getXValue());
                if (status != null) onStatusSelecionado.accept(status);
            });
        }));
    }

    private void preencherGraficoFaturamento(List<FaturamentoDiarioDTO> dados) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Faturamento");
        for (FaturamentoDiarioDTO item : dados) {
            serie.getData().add(new XYChart.Data<>(item.data().format(formatoDataCurta), item.valor()));
        }
        chartFaturamento.getData().setAll(serie);

        Platform.runLater(() -> serie.getData().forEach(ponto -> {
            String texto = ponto.getXValue() + ": " + formatoMoeda.format(ponto.getYValue());
            Tooltip.install(ponto.getNode(), new Tooltip(texto));
        }));
    }

    private void preencherGraficoComissao(List<ComissaoFuncionarioDTO> dados) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (ComissaoFuncionarioDTO item : dados) {
            serie.getData().add(new XYChart.Data<>(item.funcionarioNome(), item.totalComissao()));
        }
        chartComissao.getData().setAll(serie);

        Platform.runLater(() -> serie.getData().forEach(ponto -> {
            String texto = ponto.getXValue() + ": " + formatoMoeda.format(ponto.getYValue());
            Tooltip.install(ponto.getNode(), new Tooltip(texto));
        }));
    }

    private String formatarStatus(StatusOS status) {
        String[] partes = status.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(Character.toUpperCase(partes[i].charAt(0))).append(partes[i].substring(1));
        }
        return sb.toString();
    }

    private StatusOS statusOriginal(List<OsPorStatusDTO> dados, String labelFormatado) {
        return dados.stream()
                .filter(d -> formatarStatus(d.status()).equals(labelFormatado))
                .map(OsPorStatusDTO::status)
                .findFirst()
                .orElse(null);
    }
}