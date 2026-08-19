package com.sgauto.app.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Ponto único de captura de exceções não tratadas.
 * Hoje: registra no log e avisa o usuário.
 * Futuro: é AQUI que o envio ao Sentry (ver de longe) entra — uma linha só,
 * sem tocar em mais nada do sistema.
 */
public final class TratadorErrosGlobal {

    private static final Logger log = LoggerFactory.getLogger(TratadorErrosGlobal.class);

    private static final String CAMINHO_LOG =
            System.getProperty("user.home")
                    + File.separator + ".sgauto"
                    + File.separator + "logs"
                    + File.separator + "sgauto.log";

    private TratadorErrosGlobal() {}

    /** Instala o handler para exceções não tratadas em qualquer thread. */
    public static void instalar() {
        Thread.setDefaultUncaughtExceptionHandler(TratadorErrosGlobal::capturar);
    }

    public static void capturar(Thread thread, Throwable erro) {
        // 1. Registra sempre (arquivo + console). Sentry entraria nesta linha no futuro.
        log.error("Exceção não tratada na thread '{}'", thread.getName(), erro);

        // 2. Avisa o usuário de forma amigável, sem sumir silenciosamente.
        if (Platform.isFxApplicationThread()) {
            mostrarAlerta(erro);
        } else {
            try {
                Platform.runLater(() -> mostrarAlerta(erro));
            } catch (IllegalStateException ignore) {
                // Toolkit JavaFX ainda não iniciado; o log acima já é suficiente.
            }
        }
    }

    private static void mostrarAlerta(Throwable erro) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Ocorreu um erro inesperado");
        alerta.setHeaderText("O sistema encontrou um problema.");
        alerta.setContentText(
                "Você pode continuar usando o sistema. Se o erro persistir, "
                        + "envie este arquivo ao suporte:\n\n" + CAMINHO_LOG
                        + "\n\nDetalhe técnico: " + erro.getClass().getSimpleName());
        alerta.showAndWait();
    }
}