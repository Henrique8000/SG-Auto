package com.sgauto.app.controller.usuario;

import com.sgauto.app.model.Funcionario;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.service.FuncionarioService;
import com.sgauto.app.service.usuario.PerfilAcessoService;
import com.sgauto.app.service.usuario.UsuarioService;
import com.sgauto.app.util.AutoCompleteComboBox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UsuarioFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtLogin;
    @FXML private VBox boxSenha;
    @FXML private PasswordField txtSenha;
    @FXML private TextField txtNomeExibicao;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbFuncionario;
    @FXML private ComboBox<String> cmbPerfil;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final UsuarioService usuarioService;
    private final PerfilAcessoService perfilAcessoService;
    private final FuncionarioService funcionarioService;

    private Map<String, Long> mapaFuncionarios = Map.of();
    private Map<String, Long> mapaPerfis = Map.of();
    private Usuario usuarioEmEdicao;
    private Runnable aoSalvar;

    public UsuarioFormController(UsuarioService usuarioService, PerfilAcessoService perfilAcessoService,
                                 FuncionarioService funcionarioService) {
        this.usuarioService = usuarioService;
        this.perfilAcessoService = perfilAcessoService;
        this.funcionarioService = funcionarioService;
    }

    @FXML
    public void initialize() {
        List<Funcionario> funcionarios = funcionarioService.listarAptosParaOrdemServico();
        mapaFuncionarios = funcionarios.stream().collect(Collectors.toMap(Funcionario::getNomeExibicao, Funcionario::getId, (a, b) -> a));
        new AutoCompleteComboBox(cmbFuncionario).definirItens(List.copyOf(mapaFuncionarios.keySet()));

        List<PerfilAcesso> perfis = perfilAcessoService.listarAtivos();
        mapaPerfis = perfis.stream().collect(Collectors.toMap(PerfilAcesso::getNome, PerfilAcesso::getId, (a, b) -> a));
        cmbPerfil.setItems(FXCollections.observableArrayList(mapaPerfis.keySet()));
    }

    public void configurar(Usuario usuarioExistente, Runnable aoSalvar) {
        this.usuarioEmEdicao = usuarioExistente;
        this.aoSalvar = aoSalvar;

        if (usuarioExistente != null) {
            lblTituloModal.setText("Editar Usuário");
            txtLogin.setText(usuarioExistente.getLogin());
            txtLogin.setDisable(true); // login não muda após criado
            boxSenha.setVisible(false);
            boxSenha.setManaged(false);
            txtNomeExibicao.setText(usuarioExistente.getNomeExibicao());
            txtEmail.setText(usuarioExistente.getEmail());
            if (usuarioExistente.getFuncionario() != null) cmbFuncionario.setValue(usuarioExistente.getFuncionario().getNomeExibicao());
            if (usuarioExistente.getPerfil() != null) cmbPerfil.setValue(usuarioExistente.getPerfil().getNome());
        }
    }

    @FXML
    private void salvar() {
        try {
            String nomeExibicao = txtNomeExibicao.getText().trim();
            String email = vazioParaNull(txtEmail.getText());
            Long funcionarioId = mapaFuncionarios.get(cmbFuncionario.getValue());
            Long perfilId = mapaPerfis.get(cmbPerfil.getValue());

            if (perfilId == null) { mostrarErro("Selecione o perfil de acesso."); return; }

            if (usuarioEmEdicao == null) {
                String login = txtLogin.getText().trim();
                String senha = txtSenha.getText();
                usuarioService.cadastrar(login, senha, nomeExibicao, email, funcionarioId, perfilId);
            } else {
                usuarioService.atualizar(usuarioEmEdicao.getId(), nomeExibicao, email, funcionarioId, perfilId);
            }

            aoSalvar.run();
            fecharModal();

        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private String vazioParaNull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    @FXML
    private void cancelar() {
        fecharModal();
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void fecharModal() {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }
}