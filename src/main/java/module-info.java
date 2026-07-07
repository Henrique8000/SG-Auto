module com.sgauto.app {
    requires javafx.controls;
    requires javafx.fxml;

    // Permite ao FXMLLoader acessar os controllers via reflexão
    opens com.sgauto.app.controller to javafx.fxml;
    // Permite que propriedades do model sejam usadas em bindings da UI
    //opens com.sgauto.app.model to javafx.base;

    exports com.sgauto.app;
}
