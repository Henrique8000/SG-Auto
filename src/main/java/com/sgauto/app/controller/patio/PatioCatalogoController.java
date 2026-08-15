package com.sgauto.app.controller.patio;

// Lembre-se de importar as suas classes de permissão
// import com.sgauto.app.util.PermissaoUtil;
// import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

@Component
public class PatioCatalogoController {

    @FXML private TabPane tabPanePatio;
    @FXML private Tab tabPatioAtual;
    @FXML private Tab tabHistorico;
    @FXML private Tab tabTarifas;
    @FXML private Tab tabMotivos;

    private final VerificaPermissaoUtil permissaoUtil;

    // Injeção de dependência
    public PatioCatalogoController(VerificaPermissaoUtil permissaoUtil) {
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        aplicarPermissoes();
    }

    private void aplicarPermissoes() {
        if (!permissaoUtil.verificar(PermissaoChave.PATIO_CONFIGURAR)) {
            tabPanePatio.getTabs().remove(tabTarifas);
            tabPanePatio.getTabs().remove(tabMotivos);
        }
    }
}