package com.sgauto.app.controller.estoque;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class EstoqueBaixoModalController {

    @FXML private Label lblSubtitulo;
    @FXML private TableView<Peca> tabelaEstoqueBaixo;
    @FXML private TableColumn<Peca, String> colCodigo;
    @FXML private TableColumn<Peca, String> colDescricao;
    @FXML private TableColumn<Peca, String> colModelo;
    @FXML private TableColumn<Peca, String> colAtual;
    @FXML private TableColumn<Peca, String> colMinimo;
    @FXML private TableColumn<Peca, Void> colSituacao;
    @FXML private TableColumn<Peca, Void> colAcoes;

    private final EstoqueService estoqueService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Peca> pecas = FXCollections.observableArrayList();
    private Runnable aoFechar;

    public EstoqueBaixoModalController(EstoqueService estoqueService, ApplicationContext applicationContext) {
        this.estoqueService = estoqueService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colAtual.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantidadeEstoque())));
        colMinimo.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getEstoqueMinimo())));

        colSituacao.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Peca p = getTableView().getItems().get(getIndex());
                boolean zerado = p.getQuantidadeEstoque() == 0;
                badge.setText(zerado ? "Zerado" : "Baixo");
                badge.getStyleClass().setAll("badge", zerado ? "badge-zerado" : "badge-baixo");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnRepor = new Button("Repor");
            {
                btnRepor.getStyleClass().add("btn-table-action");
                btnRepor.setOnAction(e -> abrirMovimentacao(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btnRepor);
            }
        });

        tabelaEstoqueBaixo.setItems(pecas);
    }

    public void configurar(Runnable aoFechar) {
        this.aoFechar = aoFechar;
        carregarDados();
    }

    private void carregarDados() {
        List<Peca> lista = estoqueService.listarComEstoqueBaixo();
        pecas.setAll(lista);
        lblSubtitulo.setText(lista.size() + " peça(s) no ponto ou abaixo do estoque mínimo");
    }

    private void abrirMovimentacao(Peca peca) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/movimentacao-estoque-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            MovimentacaoEstoqueController controller = loader.getController();
            controller.configurar(peca, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, "Movimentar Estoque");
            modal.showAndWait();


        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir movimentação de estoque", e);
        }
    }

    @FXML
    private void fechar() {
        if (aoFechar != null) aoFechar.run();
        ((Stage) tabelaEstoqueBaixo.getScene().getWindow()).close();
    }
}