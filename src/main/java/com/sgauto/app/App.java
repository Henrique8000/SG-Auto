package com.sgauto.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class App extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(SgAutoApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/login/login.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 600, 500);

        java.net.URL cssUrl = getClass().getResource("/com/sgauto/app/css/estilo.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Aviso: Arquivo CSS não encontrado. Verifique o caminho!");
        }

        scene.setFill(javafx.scene.paint.Color.web("#181818"));

        stage.setScene(scene);
        stage.setTitle("SGAuto - Autenticação");

        stage.setResizable(false);

        stage.show();

        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}