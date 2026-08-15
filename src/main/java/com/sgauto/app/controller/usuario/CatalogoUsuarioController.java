package com.sgauto.app.controller.usuario;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

@Component
public class CatalogoUsuarioController {

    @FXML private TabPane tabPanePrincipal;
    @FXML private Tab tabUsuarios;
    @FXML private Tab tabPerfis;

    private final VerificaPermissaoUtil permissaoUtil;

    public CatalogoUsuarioController(VerificaPermissaoUtil permissaoUtil) {
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        configurarBloqueioDeAbas();
    }

    private void configurarBloqueioDeAbas() {
        tabPanePrincipal.getSelectionModel().selectedItemProperty().addListener((observable, abaAntiga, abaNova) -> {

            if (abaNova == tabPerfis) {

                if (!permissaoUtil.verificar(PermissaoChave.PERFIL_VISUALIZAR)) {
                    Platform.runLater(() -> {
                        tabPanePrincipal.getSelectionModel().select(abaAntiga);
                        ExibirMensagemBloqueioUtil.exibir();
                    });
                }
            }
        });
    }
}