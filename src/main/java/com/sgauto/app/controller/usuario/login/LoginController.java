package com.sgauto.app.controller.usuario.login;

import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.SessaoUsuario;
import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.service.usuario.UsuarioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMensagemErro;
    @FXML private Button btnEntrar;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginController.class);
    private final UsuarioService usuarioService;
    private final ApplicationContext applicationContext;

    public LoginController(UsuarioService usuarioService, ApplicationContext applicationContext) {
        this.usuarioService = usuarioService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void autenticar(ActionEvent event) {
        esconderErro();

        String login = txtLogin.getText();
        String senha = txtSenha.getText();

        if (login == null || login.trim().isEmpty() || senha == null || senha.isEmpty()) {
            mostrarErro("Por favor, preencha o usuário e a senha.");
            return;
        }

        try {
            Usuario usuarioAutenticado = usuarioService.autenticar(login, senha);

            if (Boolean.TRUE.equals(usuarioAutenticado.getDeveTrocarSenha())) {
                abrirTelaTrocaDeSenhaObrigatoria(usuarioAutenticado, event);
            }
            else {
                iniciarSessao(usuarioAutenticado, event);
            }

        }
        catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
        catch (Exception e) {
            mostrarErro("Erro interno ao tentar conectar. Contate o suporte.");
            log.error("Falha inesperada na autenticação", e);
        }
    }

    private void mostrarErro(String mensagem) {
        lblMensagemErro.setText(mensagem);
        lblMensagemErro.setVisible(true);
        lblMensagemErro.setManaged(true);
    }

    private void esconderErro() {
        lblMensagemErro.setText("");
        lblMensagemErro.setVisible(false);
        lblMensagemErro.setManaged(false);
    }

    private void iniciarSessao(Usuario usuario, ActionEvent event) {
        SessaoUsuario.getInstancia().setUsuarioLogado(usuario);
        log.info("Login efetuado com sucesso. Usuário: {}", usuario.getNomeExibicao());
        mudarTela(event, "/com/sgauto/app/view/principal.fxml", "SGAuto - Dashboard Principal");
    }

    private void abrirTelaTrocaDeSenhaObrigatoria(Usuario usuario, ActionEvent event) {
        log.info("Usuário necessita trocar a senha temporária.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/login/troca-senha-obrigatoria.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            TrocaSenhaObrigatoriaController controller = loader.getController();
            controller.setUsuarioLogado(usuario);

            Stage janelaDeLogin = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Stage telaTroca = ModalUtil.abrir(root, "SGAuto - Atualize sua senha", null, false);
            telaTroca.show();

            janelaDeLogin.close();
        }
        catch (IOException e) {
            mostrarErro("Erro ao carregar a tela de atualização de senha.");
            log.error("Erro ao carregar tela de troca de senha obrigatória", e)
        }
    }

    private void mudarTela(ActionEvent event, String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage janelaAtual = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage novaJanela = ModalUtil.abrir(root, titulo, null, true);
            novaJanela.show();
            janelaAtual.close();

        }
        catch (IOException e) {
            mostrarErro("Erro ao carregar a próxima tela.");
            log.error("Erro ao carregar tela: {}", fxmlPath, e);
        }
    }

    @FXML
    private void mostrarInfoBloqueio() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Política de Segurança");
        alerta.setHeaderText(null);
        alerta.setContentText("Para garantir a segurança dos seus dados, o sistema permite um máximo de 5 tentativas incorretas de login.\n\nCaso você erre a senha em todas as tentativas, o seu usuário será bloqueado temporariamente por 15 minutos.");

        alerta.showAndWait();
    }
}