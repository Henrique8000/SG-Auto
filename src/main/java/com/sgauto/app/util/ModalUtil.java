package com.sgauto.app.util;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.net.URL;

public class ModalUtil {

    private static final String CSS_PATH = "/com/sgauto/app/css/estilo.css";

    public static Stage abrir(Parent root, String titulo) {
        return abrir(root, titulo, null, false);
    }

    public static Stage abrir(Parent root, String titulo, Window owner) {
        return abrir(root, titulo, owner, false);
    }

    public static Stage abrir(Parent root, String titulo, Window owner, boolean fs) {
        Scene scene = new Scene(root);

        URL cssUrl = ModalUtil.class.getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Aviso: Arquivo CSS não encontrado no caminho: " + CSS_PATH);
        }

        scene.setFill(javafx.scene.paint.Color.web("#1a1c20"));

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(titulo);
        modal.setScene(scene);

        if (owner != null) {
            modal.initOwner(owner);
        }

        if (fs) {
            modal.setMaximized(true);
        }

        registrarEscutadoresDeLayout(scene, modal, owner);

        modal.setOnShown(event -> {
            if (!fs) {
                modal.sizeToScene();
                recentralizar(modal, owner);
            }
        });

        return modal;
    }

    private static void registrarEscutadoresDeLayout(Scene scene, Stage modal, Window owner) {
        adicionarEscutadorNoRoot(scene.getRoot(), scene, modal, owner);

        scene.rootProperty().addListener((obs, oldRoot, newRoot) -> {
            if (newRoot != null) {
                adicionarEscutadorNoRoot(newRoot, scene, modal, owner);
                ajustarTamanho(modal, owner, scene, newRoot);
            }
        });
    }

    private static void adicionarEscutadorNoRoot(Parent root, Scene scene, Stage modal, Window owner) {
        root.needsLayoutProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                ajustarTamanho(modal, owner, scene, root);
            }
        });
    }

    private static void ajustarTamanho(Stage modal, Window owner, Scene scene, Parent root) {
        Platform.runLater(() -> {
            if (!modal.isShowing()) {
                return;
            }

            if (modal.isFullScreen() || modal.isMaximized()) {
                return;
            }

            double prefWidth = root.prefWidth(-1);
            double prefHeight = root.prefHeight(prefWidth);

            if (Math.abs(scene.getWidth() - prefWidth) > 1 || Math.abs(scene.getHeight() - prefHeight) > 1) {
                modal.sizeToScene();
                recentralizar(modal, owner);
            }
        });
    }

    private static void recentralizar(Stage modal, Window owner) {
        if (owner == null || !owner.isShowing()) {
            modal.centerOnScreen();
            return;
        }
        double x = owner.getX() + (owner.getWidth() - modal.getWidth()) / 2;
        double y = owner.getY() + (owner.getHeight() - modal.getHeight()) / 2;
        modal.setX(x);
        modal.setY(y);
    }
}