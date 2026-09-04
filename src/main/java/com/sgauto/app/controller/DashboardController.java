package com.sgauto.app.controller;

import com.sgauto.app.dto.dashboard.*;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.PeriodoDashboard;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.service.DashboardService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
    @FXML private PieChart chartFormaPagamento;
    @FXML private BarChart<String, Number> chartServicosMaisRealizados;

    // ---- Tabela de alerta ----
    @FXML private TableView<PecaEstoqueCriticoDTO> tabelaPecasCriticas;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, String> colPecaNome;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, Integer> colPecaQuantidade;
    @FXML private TableColumn<PecaEstoqueCriticoDTO, Integer> colPecaMinimo;

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
        carregarFormaPagamento();
        carregarServicosMaisRealizados();
    }

    private void configurarTabelas() {
        colPecaNome.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().nome()));
        colPecaQuantidade.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().quantidade()));
        colPecaMinimo.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().estoqueMinimo()));
    }

    private void configurarGraficos() {
        chartOsPorStatus.setAnimated(true);
        chartOsPorStatus.setLegendVisible(false);
        chartFaturamento.setAnimated(true);
        chartFaturamento.setCreateSymbols(true);
        chartFormaPagamento.setAnimated(true);
        chartFormaPagamento.setLabelsVisible(true);
        chartServicosMaisRealizados.setAnimated(true);
        chartServicosMaisRealizados.setLegendVisible(false);
    }

    private void carregarTudo() {
        carregarResumo();
        carregarOsPorStatus();
        carregarFaturamento();
        carregarFormaPagamento();
        carregarServicosMaisRealizados();
        carregarAlertas();
    }

    private void carregarResumo() {
        executarEmBackground(dashboardService::montarResumo, this::preencherResumo);
    }

    private void carregarOsPorStatus() {
        executarEmBackground(
                () -> dashboardService.osPorStatus(periodoSelecionado),
                this::preencherGraficoOsPorStatus
        );
    }

    private void carregarFaturamento() {
        executarEmBackground(
                () -> dashboardService.faturamentoNoPeriodo(periodoSelecionado),
                this::preencherGraficoFaturamento
        );
    }

    private void carregarFormaPagamento() {
        executarEmBackground(
                () -> dashboardService.faturamentoPorFormaPagamento(periodoSelecionado),
                this::preencherGraficoFormaPagamento
        );
    }

    private void carregarServicosMaisRealizados() {
        executarEmBackground(
                () -> dashboardService.servicosMaisRealizados(periodoSelecionado),
                this::preencherGraficoServicosMaisRealizados
        );
    }

    private void carregarAlertas() {
        executarEmBackground(dashboardService::pecasEstoqueCritico,
                lista -> tabelaPecasCriticas.getItems().setAll(lista));
    }

    private <T> void executarEmBackground(java.util.concurrent.Callable<T> consulta, Consumer<T> aoConcluir) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return consulta.call();
            }
        };
        task.setOnSucceeded(e -> aoConcluir.accept(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());

        Thread thread = new Thread(task, "dashboard-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void preencherResumo(DashboardResumoDTO resumo) {
        // Faturamento Dia
        String valFatDia = formatoMoeda.format(resumo.faturamentoCaixaDia());
        lblFaturamentoDia.setText(valFatDia);
        lblFaturamentoDia.setTooltip(new Tooltip("Faturamento hoje: " + valFatDia));

        // Faturamento Mês
        String valFatMes = formatoMoeda.format(resumo.faturamentoCaixaMes());
        lblFaturamentoMes.setText(valFatMes);
        lblFaturamentoMes.setTooltip(new Tooltip("Faturamento mês: " + valFatMes));

        // OS Finalizadas no Mês
        String valFatOsMes = formatoMoeda.format(resumo.faturamentoOsFinalizadasMes());
        lblFaturamentoOsMes.setText(valFatOsMes);
        lblFaturamentoOsMes.setTooltip(new Tooltip("OS finalizadas (Mês): " + valFatOsMes));

        // OS Abertas
        String valOsAbertas = String.valueOf(resumo.osAbertasTotal());
        lblOsAbertas.setText(valOsAbertas);
        lblOsAbertas.setTooltip(new Tooltip("OS Abertas: " + valOsAbertas));

        lblOsAguardando.setText(resumo.osAguardandoAprovacao() + " aguardando aprovação");

        // Ticket Médio
        String valTicket = formatoMoeda.format(resumo.ticketMedio());
        lblTicketMedio.setText(valTicket);
        lblTicketMedio.setTooltip(new Tooltip("Ticket médio: " + valTicket));

        // Estoque Crítico
        String valEstoque = String.valueOf(resumo.pecasEstoqueCritico());
        lblEstoqueCritico.setText(valEstoque);
        lblEstoqueCritico.setTooltip(new Tooltip("Estoque crítico: " + valEstoque));

        // Veículos no Pátio
        String valPatio = String.valueOf(resumo.veiculosNoPatio());
        lblVeiculosPatio.setText(valPatio);
        lblVeiculosPatio.setTooltip(new Tooltip("Veículos no pátio: " + valPatio));
    }

    private void preencherGraficoOsPorStatus(List<OsPorStatusDTO> dados) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (OsPorStatusDTO item : dados) {
            serie.getData().add(new XYChart.Data<>(formatarStatus(item.status()), item.quantidade()));
        }
        chartOsPorStatus.getData().setAll(serie);

        // ADICIONE ESTA LINHA AQUI
        ajustarLarguraBarras(chartOsPorStatus, dados.size());

        String[] cores = {"#c68a3c", "#5e977a", "#587b8d", "#c95d53", "#d8a45f"};

        Platform.runLater(() -> {
            int i = 0;
            for (XYChart.Data<String, Number> data : serie.getData()) {
                data.getNode().setStyle("-fx-bar-fill: " + cores[i % cores.length] + ";");
                Tooltip.install(data.getNode(), new Tooltip(data.getYValue() + " OS (" + data.getXValue() + ")"));
                i++;
            }
        });
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

    private void preencherGraficoFormaPagamento(List<FaturamentoPorFormaPagamentoDTO> dados) {
        List<PieChart.Data> fatias = dados.stream()
                .map(item -> new PieChart.Data(formatarFormaPagamento(item.formaPagamento()), item.valor().doubleValue()))
                .toList();
        chartFormaPagamento.getData().setAll(fatias);

        Platform.runLater(() -> chartFormaPagamento.getData().forEach(fatia -> {
            String texto = fatia.getName() + ": " + formatoMoeda.format(fatia.getPieValue());
            Tooltip.install(fatia.getNode(), new Tooltip(texto));
        }));
    }

    private void preencherGraficoServicosMaisRealizados(List<ServicoMaisRealizadoDTO> dados) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (ServicoMaisRealizadoDTO item : dados) {
            serie.getData().add(new XYChart.Data<>(item.servicoNome(), item.quantidade()));
        }
        chartServicosMaisRealizados.getData().setAll(serie);
        ajustarLarguraBarras(chartServicosMaisRealizados, dados.size());

        // Aplicando cores variadas também neste gráfico
        String[] cores = {"#587b8d", "#c68a3c", "#5e977a", "#d8a45f", "#c95d53"};

        Platform.runLater(() -> {
            int i = 0;
            for (XYChart.Data<String, Number> data : serie.getData()) {
                data.getNode().setStyle("-fx-bar-fill: " + cores[i % cores.length] + ";");
                String texto = data.getYValue() + "x realizado\n" + data.getXValue();
                Tooltip.install(data.getNode(), new Tooltip(texto));
                i++;
            }
        });
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

    private String formatarFormaPagamento(FormaPagamento formaPagamento) {
        String[] partes = formaPagamento.name().toLowerCase(Locale.ROOT).split("_");
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

    private void ajustarLarguraBarras(BarChart<String, Number> chart, int quantidadeCategorias) {
        double gap = switch (quantidadeCategorias) {
            case 0, 1 -> 250;
            case 2 -> 150;
            case 3 -> 80;
            case 4 -> 40;
            case 5 -> 20;
            default -> 10;
        };
        chart.setCategoryGap(gap);
    }
}