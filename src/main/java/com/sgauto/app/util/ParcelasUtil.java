package com.sgauto.app.util;

import com.sgauto.app.controller.financeiro.ModalPagamentoController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class ParcelasUtil {
    private final ApplicationContext springContext;

    public ParcelasUtil(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void abrirTelaPagamento(BigDecimal valorTotal, Long clienteId, Long osId, Long categoriaId, String descricao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/financeiro/modal_pagamento.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            ModalPagamentoController controller = loader.getController();
            controller.configurarPagamento(valorTotal, clienteId, osId, categoriaId, descricao);

            Stage stage = ModalUtil.abrir(root, "Condições de Pagamento");
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            String causa = e.getCause() != null ? e.getCause().toString() : e.getMessage();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro crítico ao carregar a tela de pagamento:\n" + causa);
            alert.showAndWait();
        }
    }
}
