package com.sgauto.app.controller.os;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.model.OrdemServico.OsPagamento;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class OsPagamentosTabController {

    @FXML private TableView<OsPagamento> tabela;
    @FXML private TableColumn<OsPagamento, String> colData;
    @FXML private TableColumn<OsPagamento, String> colForma;
    @FXML private TableColumn<OsPagamento, String> colValor;
    @FXML private Label lblTotalPago;
    @FXML private Label lblSaldo;

    private final OrdemServicoService ordemServicoService;
    private final ApplicationContext applicationContext;
    private final ObservableList<OsPagamento> pagamentos = FXCollections.observableArrayList();

    private Long osId;
    private Runnable aoAlterar;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OsPagamentosTabController(OrdemServicoService ordemServicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataPagamento().format(FMT)));
        colForma.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormaPagamento().toString()));
        colValor.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getValorPago())));

        tabela.setItems(pagamentos);
    }

    public void configurar(Long osId, Runnable aoAlterar) {
        this.osId = osId;
        this.aoAlterar = aoAlterar;
        carregarDados();
    }

    private void carregarDados() {
        var os = ordemServicoService.buscarPorId(osId);
        pagamentos.setAll(os.getPagamentos());

        BigDecimal totalPago = pagamentos.stream().map(OsPagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPago.setText(formatarMoeda(totalPago));

        BigDecimal saldo = ordemServicoService.calcularSaldoDevedor(osId);
        lblSaldo.setText(formatarMoeda(saldo));
    }

    @FXML
    private void abrirModalPagamento() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os-registrar-pagamento-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            OsRegistrarPagamentoModalController controller = loader.getController();
            controller.configurar(osId, () -> {
                carregarDados();
                if (aoAlterar != null) aoAlterar.run();
            });

            Stage modal = ModalUtil.abrir(root, "Registrar Pagamento", tabela.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir pagamento", e);
        }
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "R$ 0,00";
    }
}