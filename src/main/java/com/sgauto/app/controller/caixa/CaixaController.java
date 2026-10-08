package com.sgauto.app.controller.caixa;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;
import com.sgauto.app.enums.usuario.PermissaoChave;
import com.sgauto.app.model.caixa.Caixa;
import com.sgauto.app.model.caixa.CaixaMovimentacao;
import com.sgauto.app.service.financeiro.CaixaService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CaixaController {

    @FXML private Label lblCaixaAbertoDesde;
    @FXML private Label lblIdCaixaAtual;
    @FXML private Label lblValorAbertura;
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSaidas;
    @FXML private Label lblValorEsperado;
    @FXML private Label lblContagem;

    // Componentes de Filtro (Nomes exatos do FXML)
    @FXML private TextField txtBusca;
    @FXML private ComboBox<OrigemMovimentacao> cmbFiltroOrigem;
    @FXML private ComboBox<FormaPagamento> cmbFiltroFormaPagamento;

    // Botões e Grupos
    @FXML private ToggleGroup grupoTipo;
    @FXML private ToggleButton btnFiltroTodos;
    @FXML private ToggleButton btnFiltroEntrada;
    @FXML private ToggleButton btnFiltroSaida;

    // Tabela e Painel Vazio
    @FXML private TableView<CaixaMovimentacao> tabelaMovimentacoes;
    @FXML private VBox painelVazio;

    @FXML private TableColumn<CaixaMovimentacao, String> colData;
    @FXML private TableColumn<CaixaMovimentacao, String> colTipo;
    @FXML private TableColumn<CaixaMovimentacao, String> colOrigem;
    @FXML private TableColumn<CaixaMovimentacao, String> colFormaPagamento;
    @FXML private TableColumn<CaixaMovimentacao, String> colValor;
    @FXML private TableColumn<CaixaMovimentacao, String> colDescricao;

    private final CaixaService caixaService;
    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;

    private final ObservableList<CaixaMovimentacao> movimentacoes = FXCollections.observableArrayList();
    private FilteredList<CaixaMovimentacao> filteredMovimentacoes;
    private TipoMovimentacao tipoFiltroAtual = null;

    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CaixaController(CaixaService caixaService, ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.caixaService = caixaService;
        this.applicationContext = applicationContext;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    // ==========================================
    // LÓGICA DE FILTROS REATIVA
    // ==========================================
    private void configurarFiltros() {
        cmbFiltroOrigem.getItems().setAll(OrigemMovimentacao.values());
        cmbFiltroFormaPagamento.getItems().setAll(FormaPagamento.values());

        filteredMovimentacoes = new FilteredList<>(movimentacoes, b -> true);

        SortedList<CaixaMovimentacao> sortedData = new SortedList<>(filteredMovimentacoes);
        sortedData.comparatorProperty().bind(tabelaMovimentacoes.comparatorProperty());
        tabelaMovimentacoes.setItems(sortedData);

        // Listener de Texto e ComboBoxes
        txtBusca.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroOrigem.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroFormaPagamento.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // Listener do Grupo de Botões (Todos, Entrada, Saída)
        grupoTipo.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // Impede que o utilizador desmarque todos os botões clicando no que já está ativo
                btnFiltroTodos.setSelected(true);
                tipoFiltroAtual = null;
            } else if (newToggle == btnFiltroEntrada) {
                tipoFiltroAtual = TipoMovimentacao.ENTRADA;
            } else if (newToggle == btnFiltroSaida) {
                tipoFiltroAtual = TipoMovimentacao.SAIDA;
            } else {
                tipoFiltroAtual = null; // btnFiltroTodos
            }
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        filteredMovimentacoes.setPredicate(mov -> {
            // Filtro 1: Entrada/Saída
            if (tipoFiltroAtual != null && mov.getTipo() != tipoFiltroAtual) {
                return false;
            }

            // Filtro 2: Origem
            OrigemMovimentacao origemSelecionada = cmbFiltroOrigem.getValue();
            if (origemSelecionada != null && mov.getOrigem() != origemSelecionada) {
                return false;
            }

            // Filtro 3: Forma de Pagamento
            FormaPagamento formaSelecionada = cmbFiltroFormaPagamento.getValue();
            if (formaSelecionada != null && mov.getFormaPagamento() != formaSelecionada) {
                return false;
            }

            // Filtro 4: Busca por Texto (Descrição ou Placa)
            String busca = txtBusca.getText();
            if (busca != null && !busca.isEmpty()) {
                String buscaLower = busca.toLowerCase();
                String descricao = mov.getDescricao() != null ? mov.getDescricao().toLowerCase() : "";
                String placa = mov.getPlaca() != null ? mov.getPlaca().toLowerCase() : "";

                if (!descricao.contains(buscaLower) && !placa.contains(buscaLower)) {
                    return false;
                }
            }

            return true;
        });

        int resultados = filteredMovimentacoes.size();
        lblContagem.setText(resultados + " movimentação(ões)");

        // Exibe o Empty State visual se não houver resultados
        boolean vazio = resultados == 0;
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaMovimentacoes.setVisible(!vazio);
        tabelaMovimentacoes.setManaged(!vazio);
    }
    // ==========================================

    private void configurarColunas() {
        colData.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getData().format(FORMATADOR_DATA)));
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipo().toString()));
        colOrigem.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOrigem().toString()));
        colFormaPagamento.setCellValueFactory(data -> {
            FormaPagamento forma = data.getValue().getFormaPagamento();
            return new SimpleStringProperty(forma != null ? forma.toString() : "-");
        });
        colValor.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoeda(data.getValue().getValor())));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
    }

    private void carregarDados() {
        Caixa caixaAberto = caixaService.buscarCaixaAberto();

        lblCaixaAbertoDesde.setText("Caixa aberto desde: " + caixaAberto.getDataAbertura().format(FORMATADOR_DATA));
        lblValorAbertura.setText(formatarMoeda(caixaAberto.getValorAbertura()));
        lblIdCaixaAtual.setText("ID do caixa: " + caixaAberto.getId());

        List<CaixaMovimentacao> lista = caixaService.listarMovimentacoes(caixaAberto.getId());
        movimentacoes.setAll(lista);

        aplicarFiltros(); // Força a reavaliação da UI

        BigDecimal totalEntradas = lista.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.ENTRADA)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSaidas = lista.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.SAIDA)
                .map(CaixaMovimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalEntradas.setText(formatarMoeda(totalEntradas));
        lblTotalSaidas.setText(formatarMoeda(totalSaidas));

        BigDecimal valorEsperado = caixaService.calcularValorEsperado(caixaAberto.getId());
        lblValorEsperado.setText(formatarMoeda(valorEsperado));
    }

    @FXML
    private void abrirModalMovimentacao() {
        if (permissaoUtil.verificar(PermissaoChave.CAIXA_MOVIMENTAR)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/caixa/movimentacao-caixa-modal.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                MovimentacaoCaixaModalController controller = loader.getController();
                controller.configurar(this::carregarDados);

                Stage modal = ModalUtil.abrir(root, "Nova Movimentação");
                modal.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao abrir movimentação de caixa", e);
            }
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    @FXML
    private void abrirModalFechamento() {
        if (permissaoUtil.verificar(PermissaoChave.CAIXA_FECHAR)) {
            try {
                Caixa caixaAberto = caixaService.buscarCaixaAberto();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/caixa/fechamento-caixa-modal.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                FechamentoCaixaModalController controller = loader.getController();
                controller.configurar(caixaAberto, this::carregarDados);

                Stage modal = ModalUtil.abrir(root, "Fechamento");
                modal.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao abrir fechamento de caixa", e);
            }
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    @FXML
    private void abrirModalHistorico() {
        if (permissaoUtil.verificar(PermissaoChave.CAIXA_FINANCEIRO_RELATORIOS)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/caixa/historico-caixa.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                Stage modal = com.sgauto.app.util.ModalUtil.abrir(root, "Histórico de Fechamentos",
                        tabelaMovimentacoes.getScene().getWindow());
                modal.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao abrir histórico de caixa", e);
            }
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    @FXML
    public void limparFiltrosCompletos() {
        txtBusca.clear();

        cmbFiltroOrigem.setValue(null);
        cmbFiltroFormaPagamento.setValue(null);

        btnFiltroTodos.setSelected(true);
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }
}