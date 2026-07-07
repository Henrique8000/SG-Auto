package com.sgauto.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Ponto de entrada da aplicação SGAuto.
 * Inicializa o JavaFX e carrega a primeira View (FXML).
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("view/principal.fxml"))
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("css/estilo.css")).toExternalForm()
        );

        primaryStage.setTitle("SGAuto");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
