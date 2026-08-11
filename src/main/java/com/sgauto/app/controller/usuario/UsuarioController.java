package com.sgauto.app.controller.usuario;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.controller.dto.usuario.FiltroUsuarioDTO;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.service.usuario.PerfilAcessoService;
import com.sgauto.app.service.usuario.UsuarioService;
import com.sgauto.app.util.AutoCompleteComboBox;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UsuarioController {

    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoStatus;
    @FXML private ToggleButton btnStatusTodos;
    @FXML private ToggleButton btnStatusAtivos;
    @FXML private ToggleButton btnStatusInativos;
    @FXML private ComboBox<String> cmbFiltroPerfil;
    @FXML private TableView<Usuario> tabelaUsuarios;
    @FXML private TableColumn<Usuario, String> colLogin;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colPerfil;
    @FXML private TableColumn<Usuario, String> colUltimoLogin;
    @FXML private TableColumn<Usuario, Void> colStatus;
    @FXML private TableColumn<Usuario, Void> colAcoes;
    @FXML private PaginacaoController paginacaoController;

    private final UsuarioService usuarioService;
    private final PerfilAcessoService perfilAcessoService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Usuario> usuariosExibidos = FXCollections.observableArrayList();

    private Map<String, Long> mapaPerfisFiltro = Map.of();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UsuarioController(UsuarioService usuarioService, PerfilAcessoService perfilAcessoService,
                             ApplicationContext applicationContext) {
        this.usuarioService = usuarioService;
        this.perfilAcessoService = perfilAcessoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltroPerfil();

        paginacaoController.configurar(
                pagina -> carregarPagina(pagina),
                tamanho -> carregarPagina(0)
        );

        txtBusca.textProperty().addListener((obs, a, n) -> reiniciarBusca());
        cmbFiltroPerfil.valueProperty().addListener((obs, a, n) -> reiniciarBusca());
        grupoStatus.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else reiniciarBusca();
        });

        carregarPagina(0);
    }

    private void configurarFiltroPerfil() {
        List<PerfilAcesso> perfis = perfilAcessoService.listarAtivos();
        mapaPerfisFiltro = perfis.stream().collect(Collectors.toMap(PerfilAcesso::getNome, PerfilAcesso::getId, (a, b) -> a));

        AutoCompleteComboBox autoComplete = new AutoCompleteComboBox(cmbFiltroPerfil);
        List<String> opcoes = new java.util.ArrayList<>();
        opcoes.add("Todos os perfis");
        opcoes.addAll(mapaPerfisFiltro.keySet());
        autoComplete.definirItens(opcoes);
        cmbFiltroPerfil.getSelectionModel().selectFirst();
    }

    private void configurarColunas() {
        colLogin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLogin()));
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomeExibicao()));
        colPerfil.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPerfil() != null ? data.getValue().getPerfil().getNome() : "-"));
        colUltimoLogin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getUltimoLogin() != null ? data.getValue().getUltimoLogin().format(FMT) : "Nunca"));

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
            private final Button btnResetarSenha = new Button("Redefinir Senha");
            private final Button btnToggle = new Button();
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(6);
            {
                btnEditar.getStyleClass().add("btn-table-action");
                btnResetarSenha.getStyleClass().add("btn-table-action");
                btnEditar.setOnAction(e -> abrirModalEdicao(getTableView().getItems().get(getIndex())));
                btnResetarSenha.setOnAction(e -> abrirModalResetSenha(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> alternarStatus(getTableView().getItems().get(getIndex())));
                container.getChildren().addAll(btnEditar, btnResetarSenha, btnToggle);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                boolean ativo = Boolean.TRUE.equals(getTableView().getItems().get(getIndex()).getAtivo());
                btnToggle.setText(ativo ? "Desativar" : "Ativar");
                btnToggle.getStyleClass().setAll(ativo ? "btn-table-toggle-on" : "btn-table-toggle-off");
                setGraphic(container);
            }
        });

        tabelaUsuarios.setItems(usuariosExibidos);
    }

    private void reiniciarBusca() {
        paginacaoController.resetarPagina();
        carregarPagina(0);
    }

    private void carregarPagina(int pagina) {
        String termo = txtBusca.getText();
        String perfilSelecionado = cmbFiltroPerfil.getValue();
        Long perfilId = (perfilSelecionado == null || perfilSelecionado.equals("Todos os perfis"))
                ? null : mapaPerfisFiltro.get(perfilSelecionado);

        Boolean ativo = null;
        if (grupoStatus.getSelectedToggle() == btnStatusAtivos) ativo = true;
        else if (grupoStatus.getSelectedToggle() == btnStatusInativos) ativo = false;

        FiltroUsuarioDTO filtro = new FiltroUsuarioDTO(termo, perfilId, ativo);
        PageRequest pageRequest = PageRequest.of(pagina, paginacaoController.getTamanhoPagina(),
                Sort.by(Sort.Direction.ASC, "nomeExibicao"));

        Page<Usuario> resultado = usuarioService.buscar(filtro, pageRequest);

        usuariosExibidos.setAll(resultado.getContent());
        paginacaoController.atualizar(resultado);
    }

    @FXML
    private void abrirModalNovoUsuario() {
        abrirModal(null);
    }

    private void abrirModalEdicao(Usuario usuario) {
        abrirModal(usuario);
    }

    private void abrirModal(Usuario usuarioExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/usuario-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            UsuarioFormController controller = loader.getController();
            controller.configurar(usuarioExistente, () -> carregarPagina(0));

            Stage modal = ModalUtil.abrir(root, usuarioExistente == null ? "Novo Usuário" : "Editar Usuário",
                    tabelaUsuarios.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de usuário", e);
        }
    }

    private void abrirModalResetSenha(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/usuario-resetar-senha-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            UsuarioResetarSenhaModalController controller = loader.getController();
            controller.configurar(usuario, () -> carregarPagina(0));

            Stage modal = ModalUtil.abrir(root, "Redefinir Senha", tabelaUsuarios.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir redefinição de senha", e);
        }
    }

    private void alternarStatus(Usuario usuario) {
        try {
            if (Boolean.TRUE.equals(usuario.getAtivo())) {
                usuarioService.desativar(usuario.getId());
            } else {
                usuarioService.ativar(usuario.getId());
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