package com.sgauto.app.controller.os;

import com.sgauto.app.model.OrdemServico.OsPeca;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class OsItensPecaTabController {

    @FXML private TableView<OsPeca> tabela;
    @FXML private TableColumn<OsPeca, String> colDescricao;
    @FXML private TableColumn<OsPeca, String> colQuantidade;
    @FXML private TableColumn<OsPeca, String> colValorUnitario;
    @FXML private TableColumn<OsPeca, String> colValorTotal;
    @FXML private TableColumn<OsPeca, Void> colAcoes;
    @FXML private Label lblTotalPecas;

    private final OrdemServicoService ordemServicoService;
    private final ObservableList<OsPeca> itens = FXCollections.observableArrayList();
    private Long osId;
    private Runnable aoAlterar;
    private final ApplicationContext applicationContext;

    public OsItensPecaTabController(OrdemServicoService ordemServicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        colDescricao.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPeca().getDescricao()));
        colQuantidade.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getQuantidade())));
        colValorUnitario.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoeda(data.getValue().getValorUnitario())));
        colValorTotal.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoeda(data.getValue().getValorTotal())));

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnRemover = new Button("Remover");
            {
                btnRemover.getStyleClass().add("btn-table-action");
                btnRemover.setOnAction(e -> remover(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btnRemover);
            }
        });

        tabela.setItems(itens);
    }

    public void configurar(Long osId, Runnable aoAlterar) {
        this.osId = osId;
        this.aoAlterar = aoAlterar;
        carregarDados();
    }

    private void carregarDados() {
        var os = ordemServicoService.buscarPorId(osId);
        itens.setAll(os.getPecas());

        BigDecimal total = itens.stream().map(OsPeca::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPecas.setText(formatarMoeda(total));
    }

    @FXML
    private void abrirModalAdicionar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os-adicionar-peca-modal.fxml"));
            loader.setControllerFactory(param -> {
                try {
                    return org.springframework.beans.factory.config.AutowireCapableBeanFactory.class
                            .cast(null); // placeholder, substituído abaixo
                } catch (Exception e) { return null; }
            });

            Parent root = loader.load();

            OsAdicionarPecaModalController controller = loader.getController();
            controller.configurar(osId, () -> {
                carregarDados();
                if (aoAlterar != null) aoAlterar.run();
            });

            Stage modal = ModalUtil.abrir(root, "Adicionar Peça", tabela.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir adicionar peça", e);
        }
    }

    private void remover(OsPeca osPeca) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover peça");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Remover \"" + osPeca.getPeca().getDescricao() + "\" da O.S.? A quantidade retorna ao estoque.");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                ordemServicoService.removerPeca(osId, osPeca.getId());
                carregarDados();
                if (aoAlterar != null) aoAlterar.run();
            }
        });
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "R$ 0,00";
    }
}