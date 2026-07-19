package com.sgauto.app.controller.estoque;

import com.sgauto.app.controller.dto.ItemSelecionavelPeca;
import com.sgauto.app.enums.CampoPreco;
import com.sgauto.app.enums.TipoAjustePreco;
import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import com.sgauto.app.util.AutoCompleteComboBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AjustePrecoMassaEstoqueController {

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroModelo;
    @FXML private CheckBox chkSelecionarTodos;
    @FXML private TableView<ItemSelecionavelPeca> tabela;
    @FXML private TableColumn<ItemSelecionavelPeca, Boolean> colSelecionar;
    @FXML private TableColumn<ItemSelecionavelPeca, String> colCodigo;
    @FXML private TableColumn<ItemSelecionavelPeca, String> colDescricao;
    @FXML private TableColumn<ItemSelecionavelPeca, String> colModelo;
    @FXML private TableColumn<ItemSelecionavelPeca, String> colPrecoCusto;
    @FXML private TableColumn<ItemSelecionavelPeca, String> colPrecoVenda;

    @FXML private ToggleGroup grupoTipoAjuste;
    @FXML private ToggleButton btnValorFixo;
    @FXML private ToggleButton btnPercentual;
    @FXML private ToggleGroup grupoCampoAlvo;
    @FXML private ToggleButton btnCampoCusto;
    @FXML private ToggleButton btnCampoVenda;
    @FXML private ToggleButton btnCampoAmbos;
    @FXML private TextField txtValor;
    @FXML private Label lblSelecionados;
    @FXML private Label lblErro;

    private final EstoqueService estoqueService;
    private final ObservableList<ItemSelecionavelPeca> itens = FXCollections.observableArrayList();
    private AutoCompleteComboBox autoCompleteModelo;

    public AjustePrecoMassaEstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @FXML
    public void initialize() {
        autoCompleteModelo = new AutoCompleteComboBox(cmbFiltroModelo);
        configurarColunas();
        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltro());
        cmbFiltroModelo.valueProperty().addListener((obs, a, n) -> aplicarFiltro());
        carregarDados();
    }

    private void configurarColunas() {
        colSelecionar.setCellValueFactory(data -> data.getValue().selecionadoProperty());
        colSelecionar.setCellFactory(CheckBoxTableCell.forTableColumn(colSelecionar));
        colSelecionar.setEditable(true);
        tabela.setEditable(true);

        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeca().getCodigo()));
        colDescricao.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeca().getDescricao()));
        colModelo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeca().getModelo()));
        colPrecoCusto.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getPeca().getPrecoCusto())));
        colPrecoVenda.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getPeca().getPrecoVenda())));

        tabela.setItems(itens);
    }

    private void carregarDados() {
        List<Peca> todas = estoqueService.listarTodas();
        autoCompleteModelo.definirItens(
                java.util.stream.Stream.concat(
                        java.util.stream.Stream.of("Todos os modelos"),
                        estoqueService.listarModelosDistintos().stream()
                ).toList()
        );

        List<ItemSelecionavelPeca> wrapped = todas.stream().map(ItemSelecionavelPeca::new).toList();
        wrapped.forEach(item -> item.selecionadoProperty().addListener((obs, a, n) -> atualizarContador()));
        itens.setAll(wrapped);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String modelo = cmbFiltroModelo.getValue();

        List<ItemSelecionavelPeca> filtrados = itens.stream()
                .filter(i -> termo.isBlank()
                        || i.getPeca().getCodigo().toLowerCase().contains(termo)
                        || i.getPeca().getDescricao().toLowerCase().contains(termo))
                .filter(i -> modelo == null || modelo.isBlank() || modelo.equals("Todos os modelos")
                        || modelo.equals(i.getPeca().getModelo()))
                .toList();

        tabela.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void alternarSelecionarTodos() {
        boolean marcar = chkSelecionarTodos.isSelected();
        tabela.getItems().forEach(item -> item.setSelecionado(marcar));
    }

    private void atualizarContador() {
        long total = itens.stream().filter(ItemSelecionavelPeca::isSelecionado).count();
        lblSelecionados.setText(total + " peça(s) selecionada(s)");
    }

    @FXML
    private void aplicarAjuste() {
        ocultarErro();

        if (!btnValorFixo.isSelected() && !btnPercentual.isSelected()) {
            mostrarErro("Selecione o tipo de ajuste (Valor Fixo ou Percentual).");
            return;
        }

        if (grupoCampoAlvo.getSelectedToggle() == null) {
            mostrarErro("Selecione sobre qual preço o ajuste será aplicado (Custo, Venda ou Ambos).");
            return;
        }

        String textoValor = txtValor.getText().trim();
        if (textoValor.isEmpty()) {
            mostrarErro("Informe o valor numérico para o ajuste.");
            return;
        }

        try {
            List<Long> ids = itens.stream()
                    .filter(ItemSelecionavelPeca::isSelecionado)
                    .map(i -> i.getPeca().getId())
                    .toList();

            BigDecimal valor = new BigDecimal(txtValor.getText().trim());
            TipoAjustePreco tipo = btnValorFixo.isSelected() ? TipoAjustePreco.VALOR_FIXO : TipoAjustePreco.PERCENTUAL;
            CampoPreco campo = btnCampoCusto.isSelected() ? CampoPreco.CUSTO
                    : btnCampoAmbos.isSelected() ? CampoPreco.AMBOS : CampoPreco.VENDA;

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar ajuste em massa");
            confirmacao.setHeaderText(null);
            confirmacao.setContentText("Aplicar ajuste em " + ids.size() + " peça(s)? Esta ação não pode ser desfeita.");

            confirmacao.showAndWait().ifPresent(botao -> {
                if (botao == ButtonType.OK) {
                    int afetadas = estoqueService.ajustarPrecosEmMassa(ids, tipo, valor, campo);
                    ocultarErro();
                    carregarDados();
                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Ajuste aplicado");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText(afetadas + " peça(s) atualizada(s) com sucesso.");
                    sucesso.showAndWait();
                }
            });

        } catch (NumberFormatException e) {
            mostrarErro("Informe um valor numérico válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void ocultarErro() {
        lblErro.setVisible(false);
        lblErro.setManaged(false);
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }
}