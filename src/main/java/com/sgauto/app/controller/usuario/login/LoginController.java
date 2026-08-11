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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
            } else {
                iniciarSessao(usuarioAutenticado, event);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro interno ao tentar conectar. Contate o suporte.");
            e.printStackTrace();
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
        System.out.println("Login efetuado com sucesso! Usuário: " + usuario.getNomeExibicao());
        mudarTela(event, "/com/sgauto/app/view/principal.fxml", "SGAuto - Dashboard Principal");
    }

    private void abrirTelaTrocaDeSenhaObrigatoria(Usuario usuario, ActionEvent event) {
        System.out.println("Usuário necessita trocar a senha temporária.");
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
        } catch (IOException e) {
            mostrarErro("Erro ao carregar a tela de atualização de senha.");
            e.printStackTrace();
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

        } catch (IOException e) {
            mostrarErro("Erro ao carregar a próxima tela.");
            e.printStackTrace();
        }
    }
}