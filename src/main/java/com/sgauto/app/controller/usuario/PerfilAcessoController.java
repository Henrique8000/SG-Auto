package com.sgauto.app.controller.usuario;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.controller.dto.usuario.FiltroPerfilAcessoDTO;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.service.usuario.PerfilAcessoService;
import com.sgauto.app.util.ModalUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PerfilAcessoController {

    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private TableView<PerfilAcesso> tabelaPerfis;
    @FXML private TableColumn<PerfilAcesso, String> colNome;
    @FXML private TableColumn<PerfilAcesso, String> colDescricao;
    @FXML private TableColumn<PerfilAcesso, String> colQtdPermissoes;
    @FXML private TableColumn<PerfilAcesso, Void> colStatus;
    @FXML private TableColumn<PerfilAcesso, Void> colAcoes;
    @FXML private PaginacaoController paginacaoController;

    private final PerfilAcessoService perfilAcessoService;
    private final ApplicationContext applicationContext;
    private final ObservableList<PerfilAcesso> perfisExibidos = FXCollections.observableArrayList();

    public PerfilAcessoController(PerfilAcessoService perfilAcessoService, ApplicationContext applicationContext) {
        this.perfilAcessoService = perfilAcessoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();

        paginacaoController.configurar(this::carregarPagina, tamanho -> carregarPagina(0));

        txtBusca.textProperty().addListener((obs, a, n) -> reiniciarBusca());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else reiniciarBusca();
        });

        carregarPagina(0);
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));
        colDescricao.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDescricao() != null ? data.getValue().getDescricao() : "-"));
        colQtdPermissoes.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getPermissoes().size())));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                boolean ativo = Boolean.TRUE.equals(getTableView().getItems().get(getIndex()).getAtivo());
                badge.setText(ativo ? "Ativo" : "Inativo");
                badge.getStyleClass().setAll("badge", ativo ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(6, btnEditar, btnToggle);
            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                PerfilAcesso p = getTableView().getItems().get(getIndex());
                boolean ativo = Boolean.TRUE.equals(p.getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                btnEditar.setDisable(Boolean.TRUE.equals(p.getProtegido()));
                btnToggle.setDisable(Boolean.TRUE.equals(p.getProtegido()));
                setGraphic(container);
            }
        });

        tabelaPerfis.setItems(perfisExibidos);
    }

    private void reiniciarBusca() {
        paginacaoController.resetarPagina();
        carregarPagina(0);
    }

    private void carregarPagina(int pagina) {
        Boolean ativo = null;
        if (grupoStatus.getSelectedToggle() == btnStatusAtivos) ativo = true;
        else if (grupoStatus.getSelectedToggle() == btnStatusInativos) ativo = false;

        FiltroPerfilAcessoDTO filtro = new FiltroPerfilAcessoDTO(txtBusca.getText(), ativo);
        PageRequest pageRequest = PageRequest.of(pagina, paginacaoController.getTamanhoPagina(),
                Sort.by(Sort.Direction.ASC, "nome"));

        Page<PerfilAcesso> resultado = perfilAcessoService.buscar(filtro, pageRequest);

        perfisExibidos.setAll(resultado.getContent());
        paginacaoController.atualizar(resultado);
    }

    @FXML
    private void abrirModalNovoPerfil() {
        abrirModal(null);
    }

    private void abrirModalEdicao(PerfilAcesso perfil) {
        abrirModal(perfil);
    }

    private void abrirModal(PerfilAcesso perfilExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/perfil-acesso-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            PerfilAcessoFormController controller = loader.getController();
            controller.configurar(perfilExistente, () -> carregarPagina(0));

            Stage modal = ModalUtil.abrir(root, perfilExistente == null ? "Novo Perfil" : "Editar Perfil",
                    tabelaPerfis.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de perfil", e);
        }
    }

    private void alternarStatus(PerfilAcesso perfil) {
        try {
            if (Boolean.TRUE.equals(perfil.getAtivo())) {
                perfilAcessoService.desativar(perfil.getId());
            } else {
                perfilAcessoService.ativar(perfil.getId());
            }
            carregarPagina(0);
        } catch (IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Não é possível alterar o status");
            alerta.setHeaderText(null);
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }
}