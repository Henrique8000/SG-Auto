package com.sgauto.app.controller.servicos;

import com.sgauto.app.controller.dto.ItemSelecionavelServico;
import com.sgauto.app.enums.TipoAjustePreco;
import com.sgauto.app.model.Servico;
import com.sgauto.app.service.CategoriaService;
import com.sgauto.app.service.ServicoService;
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
public class AjustePrecoMassaServicoController {

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private CheckBox chkSelecionarTodos;
    @FXML private TableView<ItemSelecionavelServico> tabela;
    @FXML private TableColumn<ItemSelecionavelServico, Boolean> colSelecionar;
    @FXML private TableColumn<ItemSelecionavelServico, String> colCodigo;
    @FXML private TableColumn<ItemSelecionavelServico, String> colNome;
    @FXML private TableColumn<ItemSelecionavelServico, String> colCategoria;
    @FXML private TableColumn<ItemSelecionavelServico, String> colValor;

    @FXML private ToggleGroup grupoTipoAjuste;
    @FXML private ToggleButton btnValorFixo;
    @FXML private ToggleButton btnPercentual;
    @FXML private TextField txtValor;
    @FXML private Label lblSelecionados;
    @FXML private Label lblErro;

    private final ServicoService servicoService;
    private final CategoriaService categoriaService;
    private final ObservableList<ItemSelecionavelServico> itens = FXCollections.observableArrayList();
    private AutoCompleteComboBox autoCompleteCategoria;

    public AjustePrecoMassaServicoController(ServicoService servicoService, CategoriaService categoriaService) {
        this.servicoService = servicoService;
        this.categoriaService = categoriaService;
    }

    @FXML
    public void initialize() {
        autoCompleteCategoria = new AutoCompleteComboBox(cmbFiltroCategoria);
        configurarColunas();
        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltro());
        cmbFiltroCategoria.valueProperty().addListener((obs, a, n) -> aplicarFiltro());
        carregarDados();
    }

    private void configurarColunas() {
        colSelecionar.setCellValueFactory(data -> data.getValue().selecionadoProperty());
        colSelecionar.setCellFactory(CheckBoxTableCell.forTableColumn(colSelecionar));
        colSelecionar.setEditable(true);
        tabela.setEditable(true);

        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getServico().getCodigo()));
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getServico().getNome()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getServico().getCategoria()));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getServico().getValor())));

        tabela.setItems(itens);
    }

    private void carregarDados() {
        List<Servico> todos = servicoService.listarTodos();
        autoCompleteCategoria.definirItens(
                java.util.stream.Stream.concat(
                        java.util.stream.Stream.of("Todas as categorias"),
                        categoriaService.listarAtivas().stream().map(c -> c.getNome())
                ).toList()
        );

        List<ItemSelecionavelServico> wrapped = todos.stream().map(ItemSelecionavelServico::new).toList();
        wrapped.forEach(item -> item.selecionadoProperty().addListener((obs, a, n) -> atualizarContador()));
        itens.setAll(wrapped);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String categoria = cmbFiltroCategoria.getValue();

        List<ItemSelecionavelServico> filtrados = itens.stream()
                .filter(i -> termo.isBlank()
                        || i.getServico().getCodigo().toLowerCase().contains(termo)
                        || i.getServico().getNome().toLowerCase().contains(termo))
                .filter(i -> categoria == null || categoria.isBlank() || categoria.equals("Todas as categorias")
                        || categoria.equals(i.getServico().getCategoria()))
                .toList();

        tabela.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void alternarSelecionarTodos() {
        boolean marcar = chkSelecionarTodos.isSelected();
        tabela.getItems().forEach(item -> item.setSelecionado(marcar));
    }

    private void atualizarContador() {
        long total = itens.stream().filter(ItemSelecionavelServico::isSelecionado).count();
        lblSelecionados.setText(total + " serviço(s) selecionado(s)");
    }

    @FXML
    private void aplicarAjuste() {
        ocultarErro();

        if (!btnValorFixo.isSelected() && !btnPercentual.isSelected()) {
            mostrarErro("Selecione o tipo de ajuste (Valor Fixo ou Percentual).");
            return;
        }

        String textoValor = txtValor.getText().trim();
        if (textoValor.isEmpty()) {
            mostrarErro("Informe o valor numérico para o ajuste.");
            return;
        }

        try {
            List<Long> ids = itens.stream()
                    .filter(ItemSelecionavelServico::isSelecionado)
                    .map(i -> i.getServico().getId())
                    .toList();

            BigDecimal valor = new BigDecimal(txtValor.getText().trim());
            TipoAjustePreco tipo = btnValorFixo.isSelected() ? TipoAjustePreco.VALOR_FIXO : TipoAjustePreco.PERCENTUAL;

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar ajuste em massa");
            confirmacao.setHeaderText(null);
            confirmacao.setContentText("Aplicar ajuste em " + ids.size() + " serviço(s)? Esta ação não pode ser desfeita.");

            confirmacao.showAndWait().ifPresent(botao -> {
                if (botao == ButtonType.OK) {
                    int afetados = servicoService.ajustarValoresEmMassa(ids, tipo, valor);
                    ocultarErro();
                    carregarDados();
                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Ajuste aplicado");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText(afetados + " serviço(s) atualizado(s) com sucesso.");
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