package com.sgauto.app.controller.caixa;

import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.model.Caixa;
import com.sgauto.app.service.CaixaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HistoricoCaixaController {

    @FXML private TextField txtBuscaId;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private ComboBox<String> cmbModoConferencia;
    @FXML private Label lblContagem;
    @FXML private TableView<Caixa> tabelaHistorico;
    @FXML private TableColumn<Caixa, String> colId;
    @FXML private TableColumn<Caixa, String> colAbertura;
    @FXML private TableColumn<Caixa, String> colFechamento;
    @FXML private TableColumn<Caixa, String> colUsuarioFechamento;
    @FXML private TableColumn<Caixa, String> colValorEsperado;
    @FXML private TableColumn<Caixa, String> colValorContado;
    @FXML private TableColumn<Caixa, String> colDiferenca;
    @FXML private TableColumn<Caixa, Void> colModo;
    @FXML private TableColumn<Caixa, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final CaixaService caixaService;
    private final ObservableList<Caixa> historico = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HistoricoCaixaController(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    @FXML
    public void initialize() {
        configurarColunas();

        cmbModoConferencia.setItems(FXCollections.observableArrayList(
                "Todos os modos", "Obrigatória", "Opcional", "Sem conferência"));
        cmbModoConferencia.getSelectionModel().selectFirst();

        txtBuscaId.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        dpDataInicio.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        dpDataFim.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbModoConferencia.valueProperty().addListener((obs, a, n) -> aplicarFiltros());

        carregarDados();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colAbertura.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataAbertura().format(FORMATADOR)));
        colFechamento.setCellValueFactory(data -> {
            var dataFechamento = data.getValue().getDataFechamento();
            return new SimpleStringProperty(dataFechamento != null ? dataFechamento.format(FORMATADOR) : "-");
        });
        colUsuarioFechamento.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUsuarioFechamento() != null ? data.getValue().getUsuarioFechamento() : "-"));
        colValorEsperado.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoedaOuTraco(data.getValue().getValorEsperado())));
        colValorContado.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoedaOuTraco(data.getValue().getValorContado())));
        colDiferenca.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoedaOuTraco(data.getValue().getDiferenca())));

        colModo.setCellFactory(coluna -> new TableCell<>() {
            private final Label chip = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Caixa c = getTableView().getItems().get(getIndex());
                ModoConferencia modo = c.getModoConferenciaUsado();
                chip.setText(descreverModo(modo));
                chip.getStyleClass().setAll(modo == ModoConferencia.SEM_CONFERENCIA ? "chip-neutral" : "chip");
                setGraphic(chip);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnDetalhes = new Button("Ver detalhes");
            {
                btnDetalhes.getStyleClass().add("btn-table-action");
                btnDetalhes.setOnAction(e -> exibirDetalhes(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btnDetalhes);
            }
        });

        tabelaHistorico.setItems(historico);
    }

    private void carregarDados() {
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String textoId = txtBuscaId.getText() == null ? "" : txtBuscaId.getText().trim();
        LocalDate dataInicio = dpDataInicio.getValue();
        LocalDate dataFim = dpDataFim.getValue();
        String modoSelecionado = cmbModoConferencia.getValue();

        List<Caixa> filtrados = caixaService.listarHistorico().stream()
                .filter(c -> textoId.isBlank() || String.valueOf(c.getId()).contains(textoId))
                .filter(c -> dataInicio == null || !c.getDataFechamento().toLocalDate().isBefore(dataInicio))
                .filter(c -> dataFim == null || !c.getDataFechamento().toLocalDate().isAfter(dataFim))
                .filter(c -> filtrarPorModo(c, modoSelecionado))
                .sorted((a, b) -> b.getDataFechamento().compareTo(a.getDataFechamento()))
                .toList();

        historico.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " fechamento(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private boolean filtrarPorModo(Caixa caixa, String modoSelecionado) {
        if (modoSelecionado == null || modoSelecionado.equals("Todos os modos")) {
            return true;
        }
        return descreverModo(caixa.getModoConferenciaUsado()).equals(modoSelecionado);
    }

    private String descreverModo(ModoConferencia modo) {
        if (modo == null) return "-";
        return switch (modo) {
            case OBRIGATORIA -> "Obrigatória";
            case OPCIONAL -> "Opcional";
            case SEM_CONFERENCIA -> "Sem conferência";
        };
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaHistorico.setVisible(!vazio);
        tabelaHistorico.setManaged(!vazio);
    }

    @FXML
    private void limparFiltros() {
        txtBuscaId.clear();
        dpDataInicio.setValue(null);
        dpDataFim.setValue(null);
        cmbModoConferencia.getSelectionModel().selectFirst();
    }

    private void exibirDetalhes(Caixa caixa) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhes do Caixa #" + caixa.getId());
        alert.setHeaderText(null);
        alert.setContentText(
                "Abertura: " + caixa.getDataAbertura().format(FORMATADOR) + "\n" +
                        "Fechamento: " + (caixa.getDataFechamento() != null ? caixa.getDataFechamento().format(FORMATADOR) : "-") + "\n\n" +
                        "Total de entradas: " + formatarMoedaOuTraco(caixa.getTotalEntradas()) + "\n" +
                        "Total de saídas: " + formatarMoedaOuTraco(caixa.getTotalSaidas()) + "\n" +
                        "Vendas de peças: " + formatarMoedaOuTraco(caixa.getTotalVendasPecas()) + "\n" +
                        "Serviços: " + formatarMoedaOuTraco(caixa.getTotalServicos()) + "\n" +
                        "Avulso: " + formatarMoedaOuTraco(caixa.getTotalAvulso()) + "\n" +
                        "Sangria: " + formatarMoedaOuTraco(caixa.getTotalSangria()) + "\n" +
                        "Suprimento: " + formatarMoedaOuTraco(caixa.getTotalSuprimento()) + "\n\n" +
                        "Dinheiro: " + formatarMoedaOuTraco(caixa.getTotalDinheiro()) + "\n" +
                        "Débito: " + formatarMoedaOuTraco(caixa.getTotalDebito()) + "\n" +
                        "Crédito: " + formatarMoedaOuTraco(caixa.getTotalCredito()) + "\n" +
                        "Pix: " + formatarMoedaOuTraco(caixa.getTotalPix()) + "\n\n" +
                        "Valor esperado: " + formatarMoedaOuTraco(caixa.getValorEsperado()) + "\n" +
                        "Valor contado: " + formatarMoedaOuTraco(caixa.getValorContado()) + "\n" +
                        "Diferença: " + formatarMoedaOuTraco(caixa.getDiferenca()) +
                        (caixa.getJustificativaDiferenca() != null ? "\nJustificativa: " + caixa.getJustificativaDiferenca() : "")
        );
        alert.showAndWait();
    }

    private String formatarMoedaOuTraco(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "-";
    }
}