package com.sgauto.app.util;

import com.sgauto.app.controller.clientes.ModalSelecaoClienteController;
import com.sgauto.app.model.Cliente;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SelecaoClienteUtil {

    private final ApplicationContext springContext;

    public SelecaoClienteUtil(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public Cliente abrirModalSelecao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/clientes/modal_selecao_cliente.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();
            ModalSelecaoClienteController controller = loader.getController();

            // Usa o seu ModalUtil para abrir com os estilos globais
            Stage stage = ModalUtil.abrir(root, "Pesquisar Cliente");
            stage.showAndWait(); // A thread espera aqui até o utilizador fechar a janela

            return controller.getClienteSelecionado();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao abrir o ecrã de seleção de clientes:\n" + e.getMessage());
            alert.showAndWait();
            return null;
        }
    }
}