package com.sgauto.app.controller;

import com.sgauto.app.enums.usuario.PermissaoChave;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.SessaoUsuario;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class PrincipalController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrincipalController.class);

    @FXML private StackPane contentArea;
    @FXML private Label lblTituloPagina;
    @FXML private Label lblSubtituloPagina;

    @FXML private ToggleGroup menuLateral;

    // OPERAÇÃO
    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnOrdens;
    @FXML private ToggleButton btnClientes;
    @FXML private ToggleButton btnVeiculos;
    @FXML private ToggleButton btnPatioAtual;

    // FINANCEIRO (grupo expansível)
    @FXML private Button btnFinanceiroToggle;
    @FXML private Label lblFinanceiroChevron;
    @FXML private VBox submenuFinanceiro;
    @FXML private ToggleButton btnContasReceber;
    @FXML private ToggleButton btnContasPagar;
    @FXML private ToggleButton btnCategoriasFinanceiras;

    // GESTÃO
    @FXML private ToggleButton btnEstoque;
    @FXML private ToggleButton btnCatalogoServicos;
    @FXML private ToggleButton btnFuncionarios;
    @FXML private ToggleButton btnCaixa;

    // SISTEMA
    @FXML private ToggleButton btnUsuarios;
    @FXML private ToggleButton btnConfiguracoes;
    @FXML private ToggleButton btnLogoff;

    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;

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

        submenuFinanceiro.setVisible(false);
        submenuFinanceiro.setManaged(false);
        btnFinanceiroToggle.setOnAction(e -> alternarSubmenuFinanceiro());

        // Se o usuário navegar direto para uma conta (ex: veio de uma OS) o submenu já abre expandido
        btnContasReceber.selectedProperty().addListener((obs, antigo, novo) -> abrirSubmenuSeSelecionado(novo));
        btnContasPagar.selectedProperty().addListener((obs, antigo, novo) -> abrirSubmenuSeSelecionado(novo));
        btnCategoriasFinanceiras.selectedProperty().addListener((obs, antigo, novo) -> abrirSubmenuSeSelecionado(novo)); // <-- Adicionado

        irParaDashboard();
    }

    private void alternarSubmenuFinanceiro() {
        boolean novoEstado = !submenuFinanceiro.isVisible();
        submenuFinanceiro.setVisible(novoEstado);
        submenuFinanceiro.setManaged(novoEstado);
        lblFinanceiroChevron.setText(novoEstado ? "▾" : "▸");
    }

    private void abrirSubmenuSeSelecionado(boolean selecionado) {
        if (selecionado && !submenuFinanceiro.isVisible()) {
            submenuFinanceiro.setVisible(true);
            submenuFinanceiro.setManaged(true);
            lblFinanceiroChevron.setText("▾");
        }
    }

    @FXML
    private void irParaDashboard() {
        carregarTela(PermissaoChave.OS_VISUALIZAR, "/com/sgauto/app/view/dashboard/dashboard.fxml",
                "Dashboard", "Acompanhe os dados da sua oficina");
    }

    @FXML
    private void irParaOrdens() {
        carregarTela(PermissaoChave.OS_VISUALIZAR, "/com/sgauto/app/view/os/ordem-servico.fxml",
                "Ordem de Serviços", "Cadastro e histórico de ordens de serviço");
    }

    @FXML
    private void irParaClientes() {
        carregarTela(PermissaoChave.CLIENTE_VISUALIZAR, "/com/sgauto/app/view/clientes/clientes.fxml",
                "Clientes", "Cadastro e histórico de clientes");
    }

    @FXML
    private void irParaVeiculos() {
        carregarTela(PermissaoChave.VEICULO_VISUALIZAR, "/com/sgauto/app/view/veiculos/veiculos.fxml",
                "Veículos", "Cadastro e visualização de Veículos");
    }

    @FXML
    private void irParaPatioAtual() {
        carregarTela(PermissaoChave.PATIO_VISUALIZAR, "/com/sgauto/app/view/patio/catalogo-patio.fxml",
                "Pátio", "Veículos no pátio, tarifas e motivos de estadia");
    }

    @FXML
    private void irParaContasReceber() {
        carregarTela(PermissaoChave.CONTA_RECEBER_VISUALIZAR, "/com/sgauto/app/view/financeiro/contas-a-receber-listagem.fxml",
                "Contas a Receber", "Recebimentos de clientes e ordens de serviço");
    }

    @FXML
    private void irParaContasPagar() {
        carregarTela(PermissaoChave.CONTA_PAGAR_VISUALIZAR, "/com/sgauto/app/view/financeiro/contas-a-pagar-listagem.fxml",
                "Contas a Pagar", "Pagamentos a fornecedores e despesas da oficina");
    }

    @FXML
    private void irParaCategoriasFinanceiras() {
        carregarTela(PermissaoChave.CATEGORIA_CONTA_VISUALIZAR, "/com/sgauto/app/view/financeiro/categoria_financeira.fxml",
                "Categorias Financeiras", "Gerencie as classificações de despesas e receitas da oficina");
    }

    @FXML
    private void irParaEstoque() {
        carregarTela(PermissaoChave.PECA_VISUALIZAR, "/com/sgauto/app/view/estoque/catalogo-estoque.fxml",
                "Estoque", "Peças e controle de estoque");
    }

    @FXML
    private void irParaCatalogoServicos() {
        carregarTela(PermissaoChave.SERVICO_VISUALIZAR, "/com/sgauto/app/view/servicos/catalogo-servico.fxml",
                "Catálogo de Serviços", "Serviços e categorias disponíveis para uso em Ordens de Serviço");
    }

    @FXML
    private void irParaFuncionarios() {
        carregarTela(PermissaoChave.FUNCIONARIO_VISUALIZAR, "/com/sgauto/app/view/funcionario/funcionario.fxml",
                "Funcionários", "Cadastro e gestão de funcionários");
    }

    @FXML
    private void irParaCaixa() {
        carregarTela(PermissaoChave.CAIXA_VISUALIZAR, "/com/sgauto/app/view/caixa/caixa.fxml",
                "Caixa", "Movimentações e fechamento do caixa atual");
    }

    @FXML
    private void irParaUsuarios() {
        carregarTela(PermissaoChave.USUARIO_VISUALIZAR, "/com/sgauto/app/view/usuario/catalogo-usuario.fxml",
                "Usuários", "Criação e administração de perfis do sistema");
    }

    @FXML
    private void irParaConfiguracoes() {
        carregarTela(PermissaoChave.CONFIGURACOES_VISUALIZAR, "/com/sgauto/app/view/configuracoes/configuracoes.fxml",
                "Configurações", "Preferências do sistema");
    }

    private void carregarTela(PermissaoChave permissaoNecessaria, String caminhoFxml, String titulo, String subtitulo) {
        try {
            if (!permissaoUtil.verificar(permissaoNecessaria)) {
                ExibirMensagemBloqueioUtil.exibir();
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            loader.setControllerFactory(applicationContext::getBean);
            Parent tela = loader.load();
            mostrarTela(titulo, subtitulo, tela);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar tela: " + titulo, e);
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

                Stage loginStage = new Stage();
                loginStage.setScene(scene);
                loginStage.setTitle("SGAuto - Autenticação");
                loginStage.setResizable(false);
                loginStage.show();
                loginStage.centerOnScreen();

                Stage stageAtual = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stageAtual.close();
            } catch (IOException e) {
                log.error("Erro ao voltar para a tela de login", e);
            }
        }
    }

    private void mostrarTela(String titulo, String subtitulo, Node conteudo) {
        lblTituloPagina.setText(titulo);
        lblSubtituloPagina.setText(subtitulo);
        contentArea.getChildren().setAll(conteudo);
    }
}