package com.sgauto.app.controller.os;

import com.sgauto.app.model.OrdemServico.OsServico;
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
public class OsItensServicoTabController {

    @FXML private TableView<OsServico> tabela;
    @FXML private TableColumn<OsServico, String> colNome;
    @FXML private TableColumn<OsServico, String> colQuantidade;
    @FXML private TableColumn<OsServico, String> colValorUnitario;
    @FXML private TableColumn<OsServico, String> colValorTotal;
    @FXML private TableColumn<OsServico, Void> colAcoes;
    @FXML private Label lblTotalServicos;

    private final OrdemServicoService ordemServicoService;
    private final ObservableList<OsServico> itens = FXCollections.observableArrayList();
    private Long osId;
    private Runnable aoAlterar;
    private final ApplicationContext applicationContext;

    public OsItensServicoTabController(OrdemServicoService ordemServicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getServico().getNome()));
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
        itens.setAll(os.getServicos());

        BigDecimal total = itens.stream().map(OsServico::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalServicos.setText(formatarMoeda(total));
    }

    @FXML
    private void abrirModalAdicionar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os-adicionar-servico-modal.fxml"));
            loader.setControllerFactory(param -> {
                try {
                    return org.springframework.beans.factory.config.AutowireCapableBeanFactory.class
                            .cast(null); // placeholder, será gerenciado pelo Spring no contexto real
                } catch (Exception e) { return null; }
            });

            Parent root = loader.load();

            OsAdicionarServicoModalController controller = loader.getController();
            controller.configurar(osId, () -> {
                carregarDados();
                if (aoAlterar != null) aoAlterar.run();
            });

            Stage modal = ModalUtil.abrir(root, "Adicionar Serviço", tabela.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir adicionar serviço", e);
        }
    }

    private void remover(OsServico osServico) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover serviço");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Remover \"" + osServico.getServico().getNome() + "\" da O.S.?");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                ordemServicoService.removerServico(osId, osServico.getId());
                carregarDados();
                if (aoAlterar != null) aoAlterar.run();
            }
        });
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "R$ 0,00";
    }
}