package com.sgauto.app.controller.patio;

import com.sgauto.app.controller.dto.patio.PatioFiltroDTO;
import com.sgauto.app.controller.dto.patio.PatioItemDashboardDTO;
import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.service.PatioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class PatioHistoricoTabController {

    private static final int TAMANHO_PAGINA = 20;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusNoPatio;
    @FXML private ToggleButton btnStatusFinalizado;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private Label lblContagem;
    @FXML private TableView<PatioItemDashboardDTO> tabelaHistorico;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colPlaca;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colCliente;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colOs;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colMotivo;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colEntrada;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colSaida;
    @FXML private TableColumn<PatioItemDashboardDTO, Void> colStatus;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colValor;
    @FXML private VBox painelVazio;
    @FXML private Button btnPaginaAnterior;
    @FXML private Label lblPaginaAtual;
    @FXML private Button btnProximaPagina;

    private final PatioService patioService;
    private final ObservableList<PatioItemDashboardDTO> itens = FXCollections.observableArrayList();

    private int paginaAtual = 0;
    private int totalPaginas = 1;

    public PatioHistoricoTabController(PatioService patioService) {
        this.patioService = patioService;
    }

    @FXML
    public void initialize() {
        configurarColunas();

        txtBusca.textProperty().addListener((obs, a, n) -> carregarPagina(0));
        dpDataInicio.valueProperty().addListener((obs, a, n) -> carregarPagina(0));
        dpDataFim.valueProperty().addListener((obs, a, n) -> carregarPagina(0));
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else carregarPagina(0);
        });

        carregarPagina(0);
    }

    private void configurarColunas() {
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoNome"));
        colOs.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOrdemServicoId() != null ? "#" + data.getValue().getOrdemServicoId() : "—"));
        colEntrada.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataEntrada().format(FORMATO_DATA)));
        colSaida.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataSaida() != null ? data.getValue().getDataSaida().format(FORMATO_DATA) : "—"));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getValorEstimadoOuFinal())));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                PatioItemDashboardDTO dto = getTableView().getItems().get(getIndex());
                boolean noPatio = dto.getStatus() == StatusEstadiaPatio.NO_PATIO;
                badge.setText(noPatio ? "No Pátio" : "Finalizado");
                badge.getStyleClass().setAll("badge", noPatio ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        tabelaHistorico.setItems(itens);
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %,.2f", valor);
    }

    private void carregarPagina(int indice) {
        PatioFiltroDTO filtro = montarFiltro();
        Pageable pageable = PageRequest.of(indice, TAMANHO_PAGINA, Sort.by("dataEntrada").descending());
        Page<PatioItemDashboardDTO> pagina = patioService.listarHistoricoPaginado(filtro, pageable);

        itens.setAll(pagina.getContent());
        paginaAtual = indice;
        totalPaginas = Math.max(1, pagina.getTotalPages());
        lblPaginaAtual.setText("Página " + (paginaAtual + 1) + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 0);
        btnProximaPagina.setDisable(paginaAtual >= totalPaginas - 1);
        lblContagem.setText(pagina.getTotalElements() + " resultado(s)");
        atualizarEstadoVazio(pagina.isEmpty());
    }

    private PatioFiltroDTO montarFiltro() {
        PatioFiltroDTO filtro = new PatioFiltroDTO();
        filtro.setBusca(txtBusca.getText());
        filtro.setDataEntradaInicio(dpDataInicio.getValue());
        filtro.setDataEntradaFim(dpDataFim.getValue());

        Toggle selecionado = grupoStatus.getSelectedToggle();
        if (selecionado == btnStatusNoPatio) filtro.setStatus(StatusEstadiaPatio.NO_PATIO);
        else if (selecionado == btnStatusFinalizado) filtro.setStatus(StatusEstadiaPatio.FINALIZADO);

        return filtro;
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaHistorico.setVisible(!vazio);
        tabelaHistorico.setManaged(!vazio);
    }

    @FXML
    private void limparFiltros() {
        txtBusca.clear();
        dpDataInicio.setValue(null);
        dpDataFim.setValue(null);
        btnStatusTodos.setSelected(true);
    }

    @FXML
    private void irPaginaAnterior() {
        if (paginaAtual > 0) carregarPagina(paginaAtual - 1);
    }

    @FXML
    private void irProximaPagina() {
        if (paginaAtual < totalPaginas - 1) carregarPagina(paginaAtual + 1);
    }
}