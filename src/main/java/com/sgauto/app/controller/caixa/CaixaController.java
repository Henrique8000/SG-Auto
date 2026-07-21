package com.sgauto.app.controller.caixa;

import com.sgauto.app.controller.FechamentoCaixaModalController;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.Caixa;
import com.sgauto.app.model.CaixaMovimentacao;
import com.sgauto.app.service.CaixaService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
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
    @FXML private TableView<CaixaMovimentacao> tabelaMovimentacoes;
    @FXML private TableColumn<CaixaMovimentacao, String> colData;
    @FXML private TableColumn<CaixaMovimentacao, String> colTipo;
    @FXML private TableColumn<CaixaMovimentacao, String> colOrigem;
    @FXML private TableColumn<CaixaMovimentacao, String> colFormaPagamento;
    @FXML private TableColumn<CaixaMovimentacao, String> colValor;
    @FXML private TableColumn<CaixaMovimentacao, String> colDescricao;

    private final CaixaService caixaService;
    private final ApplicationContext applicationContext;
    private final ObservableList<CaixaMovimentacao> movimentacoes = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CaixaController(CaixaService caixaService, ApplicationContext applicationContext) {
        this.caixaService = caixaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        carregarDados();
    }

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

        tabelaMovimentacoes.setItems(movimentacoes);
    }

    private void carregarDados() {
        Caixa caixaAberto = caixaService.buscarCaixaAberto();

        lblCaixaAbertoDesde.setText("Caixa aberto desde: " + caixaAberto.getDataAbertura().format(FORMATADOR_DATA));
        lblValorAbertura.setText(formatarMoeda(caixaAberto.getValorAbertura()));

        lblIdCaixaAtual.setText("ID do caixa: " + caixaAberto.getId());

        List<CaixaMovimentacao> lista = caixaService.listarMovimentacoes(caixaAberto.getId());
        movimentacoes.setAll(lista);
        lblContagem.setText(lista.size() + " movimentação(ões)");

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
    }

    @FXML
    private void abrirModalFechamento() {
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
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format("R$ %,.2f", valor);
    }
}