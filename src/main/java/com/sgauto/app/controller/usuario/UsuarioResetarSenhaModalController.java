package com.sgauto.app.controller.usuario;

import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.service.usuario.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class UsuarioResetarSenhaModalController {

    @FXML private Label lblUsuario;
    @FXML private PasswordField txtNovaSenha;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final UsuarioService usuarioService;
    private Usuario usuario;
    private Runnable aoConfirmar;

    public UsuarioResetarSenhaModalController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void configurar(Usuario usuario, Runnable aoConfirmar) {
        this.usuario = usuario;
        this.aoConfirmar = aoConfirmar;
        lblUsuario.setText(usuario.getNomeExibicao() + " (" + usuario.getLogin() + ")");
    }

    @FXML
    private void confirmar() {
        try {
            usuarioService.redefinirSenha(usuario.getId(), txtNovaSenha.getText());
            aoConfirmar.run();
            fecharModal();
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        }
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
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}