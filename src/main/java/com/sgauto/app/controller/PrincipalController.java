package com.sgauto.app.controller;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.repository.usuario.PerfilAcessoRepository;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.SessaoUsuario;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;

@Component
public class PrincipalController {

    @FXML private StackPane contentArea;
    @FXML private Label lblTituloPagina;
    @FXML private Label lblSubtituloPagina;

    @FXML private ToggleGroup menuLateral;

    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;

    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnOrdens;
    @FXML private ToggleButton btnClientes;
    @FXML private ToggleButton btnServicos;
    @FXML private ToggleButton btnPatioAtual;
    @FXML private ToggleButton btnEstoque;
    @FXML private ToggleButton btnCaixa;
    @FXML private ToggleButton btnConfiguracoes;

    public PrincipalController(ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.applicationContext = applicationContext;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {

        // Impedir nenhuma seleção no menu lateral
        menuLateral.selectedToggleProperty().addListener((obs, toggleAntigo, toggleNovo) -> {
            if (toggleNovo == null) {
                toggleAntigo.setSelected(true);
            }
        });

        irParaDashboard();
    }

    @FXML
    private void irParaDashboard() {
        mostrarTela("Dashboard", "Visão geral da oficina", montarPlaceholder("Tela de Dashboard em construção"));
    }

    @FXML
    private void irParaOrdens() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.OS_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os/ordem-servico.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Ordem de Serviços", "Cadastro e histórico de ordens de serviço", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de OS", e);
        }
    }

    @FXML
    private void irParaClientes() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.CLIENTE_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/clientes/clientes.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Clientes", "Cadastro e histórico de clientes", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Clientes", e);
        }
    }

    @FXML
    private void irParaVeiculos() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.VEICULO_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/veiculos/veiculos.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Veículos", "Cadastro e visualização de Veículos", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Veículos", e);
        }
    }

    @FXML
    private void irParaCatalogoServicos() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.SERVICO_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/servicos/catalogo-servico.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Catálogo de Serviços", "Serviços e categorias disponíveis para uso em Ordens de Serviço", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Catálogo de Serviços", e);
        }
    }

    @FXML
    private void irParaFuncionarios() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/funcionario/funcionario.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Funcionários", "Cadastro e gestão de funcionários", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Funcionários", e);
        }
    }

    @FXML
    private void irParaPatioAtual() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.PATIO_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/catalogo-patio.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Pátio", "Veículos no pátio, tarifas e motivos de estadia", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Pátio", e);
        }
    }

    @FXML
    private void irParaEstoque() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.PECA_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/catalogo-estoque.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Estoque", "Peças e controle de estoque", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Estoque", e);
        }
    }

    @FXML
    private void irParaCaixa() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.CAIXA_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/caixa/caixa.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Caixa", "Movimentações e fechamento do caixa atual", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Caixa", e);
        }
    }

    @FXML
    private void irParaConfiguracoes() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.CONFIGURACOES_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/configuracoes/configuracoes.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Configurações", "Preferências do sistema", tela);
            }
            else{
                ExibirMensagemBloqueioUtil.exibir();
            }

        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Configurações", e);
        }
    }

    @FXML
    private void irParaUsuarios() {
        try {
            if(permissaoUtil.verificar(PermissaoChave.USUARIO_VISUALIZAR)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/catalogo-usuario.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent tela = loader.load();
                mostrarTela("Usuários", "Criação e administração de perfis do sistema", tela);
            }else{
                ExibirMensagemBloqueioUtil.exibir();
            }

        }
        catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela de Usuários", e);
        }
    }

    @FXML
    public void fazerLogoff(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja sair do sistema?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            SessaoUsuario.getInstancia().limparSessao();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/usuario/login/login.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                Scene scene = new Scene(root, 600, 500);

                String css = getClass().getResource("/com/sgauto/app/css/estilo.css").toExternalForm();
                scene.getStylesheets().add(css);
                scene.setFill(javafx.scene.paint.Color.web("#181818"));

                // Não utiliza o ModalUtil pra ficar identico ao App.java
                Stage loginStage = new Stage();
                loginStage.setScene(scene);
                loginStage.setTitle("SGAuto - Autenticação");
                loginStage.setResizable(false);

                loginStage.show();
                loginStage.centerOnScreen();

                Stage stageAtual = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stageAtual.close();

            } catch (IOException e) {
                System.err.println("Erro ao tentar voltar para a tela de login.");
                e.printStackTrace();
            }
        }
    }

    private void mostrarTela(String titulo, String subtitulo, javafx.scene.Node conteudo) {
        lblTituloPagina.setText(titulo);
        lblSubtituloPagina.setText(subtitulo);
        contentArea.getChildren().setAll(conteudo);
    }

    private VBox montarPlaceholder(String texto) {
        VBox box = new VBox();
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label label = new Label(texto);
        label.getStyleClass().add("placeholder-text");
        box.getChildren().add(label);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }
}