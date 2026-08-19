package com.sgauto.app.controller.usuario.login;

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
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TrocaSenhaObrigatoriaController {

    @FXML private PasswordField txtSenhaAtual;
    @FXML private PasswordField txtNovaSenha;
    @FXML private PasswordField txtConfirmarSenha;
    @FXML private Label lblMensagemErro;
    @FXML private Button btnSalvar;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrocaSenhaObrigatoriaController.class);
    private final UsuarioService usuarioService;
    private final ApplicationContext applicationContext;

    private Usuario usuarioLogado;

    public TrocaSenhaObrigatoriaController(UsuarioService usuarioService, ApplicationContext applicationContext) {
        this.usuarioService = usuarioService;
        this.applicationContext = applicationContext;
    }

    public void setUsuarioLogado(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    @FXML
    public void salvarNovaSenha(ActionEvent event) {
        esconderErro();

        String senhaAtual = txtSenhaAtual.getText();
        String novaSenha = txtNovaSenha.getText();
        String confirmarSenha = txtConfirmarSenha.getText();

        if (senhaAtual.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
            mostrarErro("Todos os campos são obrigatórios.");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            mostrarErro("A nova senha e a confirmação não coincidem.");
            return;
        }

        try {
            usuarioService.trocarSenha(usuarioLogado.getId(), senhaAtual, novaSenha);

            System.out.println("Senha atualizada com sucesso. Liberando acesso ao sistema...");

            SessaoUsuario.getInstancia().setUsuarioLogado(usuarioLogado);

            mudarTela(event, "/com/sgauto/app/view/principal.fxml", "SGAuto - Dashboard Principal");

        }
        catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        }
        catch (Exception e) {
            mostrarErro("Erro interno ao tentar alterar a senha.");
            log.error("Falha inesperada ao alterar senha obrigatória", e);
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

    private void mudarTela(ActionEvent event, String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.setMaximized(true);
        }
        catch (IOException e) {
            mostrarErro("Erro ao carregar o sistema principal.");
            log.error("Erro ao carregar tela principal após troca de senha", e);
        }
    }
}