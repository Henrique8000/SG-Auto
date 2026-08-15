package com.sgauto.app.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;


// usar apenas nos controllers
public class ExibirMensagemBloqueioUtil {

    private ExibirMensagemBloqueioUtil() {
    }

    public static void exibir() {
        exibirMensagemPersonalizada("Seu usuário não possui permissão para acessar este recurso.");
    }

    // metodo para exibir mensagem personalizada vinda do backend - vou usar apenas a padrao por enquanto
    public static void exibirMensagemPersonalizada(String mensagem) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Acesso Negado");
            alerta.setHeaderText(null);
            alerta.setContentText(mensagem);

            alerta.showAndWait();
        });
    }
}