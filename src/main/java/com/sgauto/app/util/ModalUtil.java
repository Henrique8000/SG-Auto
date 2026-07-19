package com.sgauto.app.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ModalUtil {

    private static final String CSS_PATH = "/com/sgauto/app/css/estilo.css";

    public static Stage abrir(Parent root, String titulo) {
        return abrir(root, titulo, null);
    }

    public static Stage abrir(Parent root, String titulo, Window owner) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ModalUtil.class.getResource(CSS_PATH).toExternalForm());

        scene.setFill(javafx.scene.paint.Color.web("#1a1c20"));

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(titulo);
        modal.setScene(scene);

        if (owner != null) {
            modal.initOwner(owner);

            modal.setOnShown(event -> {
                double x = owner.getX() + (owner.getWidth() - modal.getWidth()) / 2;
                double y = owner.getY() + (owner.getHeight() - modal.getHeight()) / 2;
                modal.setX(x);
                modal.setY(y);
            });
        } else {
            modal.centerOnScreen();
        }

        return modal;
    }
}