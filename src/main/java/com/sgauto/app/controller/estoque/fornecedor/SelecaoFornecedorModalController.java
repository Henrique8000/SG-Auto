package com.sgauto.app.controller.estoque.fornecedor;

import com.sgauto.app.model.estoque.Fornecedor;
import com.sgauto.app.service.estoque.FornecedorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class SelecaoFornecedorModalController {

    @FXML private TextField txtBusca;
    @FXML private CheckBox chkApenasSelecionados; // Novo CheckBox de filtro
    @FXML private TableView<Fornecedor> tabelaFornecedores;
    @FXML private TableColumn<Fornecedor, Void> colSelecionar;
    @FXML private TableColumn<Fornecedor, String> colRazao;
    @FXML private TableColumn<Fornecedor, String> colCategoria;
    @FXML private Button btnConfirmar;
    @FXML private Label lblContagem;

    private final FornecedorService fornecedorService;
    private final ObservableList<Fornecedor> todosFornecedoresAtivos = FXCollections.observableArrayList();
    private FilteredList<Fornecedor> listaFiltrada; // Movido para o escopo da classe

    private final Set<Long> idsSelecionados = new HashSet<>();
    private Consumer<Set<Fornecedor>> onConfirmar;

    public SelecaoFornecedorModalController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        carregarDados();
        configurarFiltroDeBusca();
        atualizarContagem();
    }

    public void configurar(Set<Fornecedor> fornecedoresJaVinculados, Consumer<Set<Fornecedor>> onConfirmar) {
        this.onConfirmar = onConfirmar;

        if (fornecedoresJaVinculados != null) {
            fornecedoresJaVinculados.forEach(f -> idsSelecionados.add(f.getId()));
        }

        atualizarContagem();
        tabelaFornecedores.refresh();
    }

    private void configurarColunas() {
        colRazao.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNomeFantasia() != null && !d.getValue().getNomeFantasia().isEmpty()
                        ? d.getValue().getNomeFantasia()
                        : d.getValue().getRazaoSocial()
        ));

        colCategoria.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCategoria() != null ? d.getValue().getCategoria() : "-"
        ));

        colSelecionar.setCellFactory(coluna -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Fornecedor fornecedor = getTableRow().getItem();
                    if (fornecedor != null) {
                        if (checkBox.isSelected()) {
                            idsSelecionados.add(fornecedor.getId());
                        } else {
                            idsSelecionados.remove(fornecedor.getId());

                            // Se a caixa "Apenas Selecionados" estiver ligada,
                            // sumimos com o item da tela imediatamente ao desmarcar.
                            if (chkApenasSelecionados.isSelected()) {
                                aplicarFiltro();
                            }
                        }
                        atualizarContagem();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Fornecedor fornecedor = getTableRow().getItem();
                    checkBox.setSelected(idsSelecionados.contains(fornecedor.getId()));
                    setGraphic(checkBox);
                }
            }
        });
    }

    private void carregarDados() {
        List<Fornecedor> ativos = fornecedorService.listarAtivos();
        todosFornecedoresAtivos.setAll(ativos);
    }

    private void configurarFiltroDeBusca() {
        listaFiltrada = new FilteredList<>(todosFornecedoresAtivos, p -> true);
        tabelaFornecedores.setItems(listaFiltrada);

        // Os dois campos disparam a mesma verificação combinada
        txtBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
        chkApenasSelecionados.selectedProperty().addListener((obs, antigo, novo) -> aplicarFiltro());
    }

    // NOVO: Método que avalia os dois filtros ao mesmo tempo
    private void aplicarFiltro() {
        String busca = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        boolean apenasSelecionados = chkApenasSelecionados.isSelected();

        listaFiltrada.setPredicate(fornecedor -> {

            // 1ª Regra: Se o usuário quer ver só os selecionados e este NÃO está na lista, esconde ele.
            if (apenasSelecionados && !idsSelecionados.contains(fornecedor.getId())) {
                return false;
            }

            // 2ª Regra: Se o campo de busca estiver vazio, passa direto (pois já passou na regra 1)
            if (busca.trim().isEmpty()) {
                return true;
            }

            // 3ª Regra: Pesquisa por texto
            return (fornecedor.getRazaoSocial() != null && fornecedor.getRazaoSocial().toLowerCase().contains(busca))
                    || (fornecedor.getNomeFantasia() != null && fornecedor.getNomeFantasia().toLowerCase().contains(busca))
                    || (fornecedor.getCpfCnpj() != null && fornecedor.getCpfCnpj().contains(busca));
        });
    }

    private void atualizarContagem() {
        int qtd = idsSelecionados.size();
        lblContagem.setText(qtd == 1 ? "1 selecionado" : qtd + " selecionados");

        if (qtd > 0) {
            lblContagem.getStyleClass().setAll("chip");
        } else {
            lblContagem.getStyleClass().setAll("results-count");
        }
    }

    @FXML
    private void confirmar() {
        if (onConfirmar != null) {
            Set<Fornecedor> selecaoFinal = todosFornecedoresAtivos.stream()
                    .filter(f -> idsSelecionados.contains(f.getId()))
                    .collect(Collectors.toSet());

            onConfirmar.accept(selecaoFinal);
        }
        fecharModal();
    }

    @FXML
    private void fecharModal() {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }
}