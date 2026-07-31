package com.sgauto.app.controller.os;

import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.service.OrdemServicoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoDetalheController {

    @FXML private Label lblCliente;
    @FXML private Label lblVeiculo;
    @FXML private Label lblStatusAtual;
    @FXML private Label lblFuncionario;
    @FXML private Label lblDataAbertura;
    @FXML private Label lblDataPrevisao;
    @FXML private Label lblSintomas;
    @FXML private ComboBox<String> cmbNovoStatus;
    @FXML private Label lblSaldoDevedor;

    @FXML private OsItensPecaTabController tabPecasController;
    @FXML private OsItensServicoTabController tabServicosController;
    @FXML private OsPagamentosTabController tabPagamentosController;

    private final OrdemServicoService ordemServicoService;
    private final ApplicationContext applicationContext;
    private Long osId;
    private Runnable aoFechar;
    private Map<String, StatusOS> mapaStatus = Map.of();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OrdemServicoDetalheController(OrdemServicoService ordemServicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
    }

    public void configurar(Long osId, Runnable aoFechar) {
        this.osId = osId;
        this.aoFechar = aoFechar;

        tabPecasController.configurar(osId, this::recarregar);
        tabServicosController.configurar(osId, this::recarregar);
        tabPagamentosController.configurar(osId, this::recarregar);

        carregarDados();
    }

    private void recarregar() {
        carregarDados();
        if (aoFechar != null) aoFechar.run();
    }

    private void carregarDados() {
        OrdemServico os = ordemServicoService.buscarPorId(osId);

        lblCliente.setText(os.getCliente() != null ? os.getCliente().getNome() : "-");
        lblVeiculo.setText(os.getVeiculo() != null ? "Placa: " + os.getVeiculo().getPlaca() : "-");
        lblFuncionario.setText("Responsável: " + (os.getFuncionario() != null ? os.getFuncionario().getNomeExibicao() : "-"));
        lblDataAbertura.setText("Aberta em: " + os.getDataAbertura().format(FMT));
        lblDataPrevisao.setText(os.getDataPrevisao() != null ? "Previsão: " + os.getDataPrevisao().format(FMT) : "Sem previsão definida");
        lblSintomas.setText(os.getSintomasRelatados() != null ? "Sintomas: " + os.getSintomasRelatados() : "");

        lblStatusAtual.setText(descreverStatus(os.getStatus()));
        lblStatusAtual.getStyleClass().setAll("badge", classeParaStatus(os.getStatus()));

        BigDecimal saldo = ordemServicoService.calcularSaldoDevedor(osId);
        lblSaldoDevedor.setText("Saldo devedor: R$ " + String.format("%,.2f", saldo));

        configurarComboStatus(os.getStatus());
    }

    private void configurarComboStatus(StatusOS statusAtual) {
        List<StatusOS> encerrados = List.of(StatusOS.CANCELADA, StatusOS.FINALIZADA);
        if (encerrados.contains(statusAtual)) {
            cmbNovoStatus.setItems(FXCollections.observableArrayList());
            cmbNovoStatus.setDisable(true);
            return;
        }

        List<StatusOS> opcoes = Arrays.stream(StatusOS.values())
                .filter(s -> s != statusAtual)
                .filter(s -> !(statusAtual == StatusOS.ABERTA && s == StatusOS.FINALIZADA))
                .toList();

        mapaStatus = opcoes.stream().collect(Collectors.toMap(this::descreverStatus, s -> s, (a, b) -> a));
        cmbNovoStatus.setItems(FXCollections.observableArrayList(mapaStatus.keySet()));
        cmbNovoStatus.setDisable(false);
    }

    @FXML
    private void alterarStatus() {
        StatusOS novoStatus = mapaStatus.get(cmbNovoStatus.getValue());
        if (novoStatus == null) return;

        try {
            ordemServicoService.alterarStatus(osId, novoStatus);
            recarregar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível alterar o status", e.getMessage());
        }
    }

    @FXML
    private void cancelarOs() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Cancelar Ordem de Serviço");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente cancelar esta O.S.? As peças já lançadas retornarão ao estoque.");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try {
                    ordemServicoService.cancelarOS(osId);
                    recarregar();
                } catch (IllegalStateException e) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível cancelar", e.getMessage());
                }
            }
        });
    }

    private String descreverStatus(StatusOS status) {
        return switch (status) {
            case ABERTA -> "Aberta";
            case VERIFICANDO_ORCAMENTO -> "Verificando Orçamento";
            case EM_EXECUCAO -> "Em Execução";
            case AGUARDANDO -> "Aguardando";
            case CONCLUIDA -> "Concluída";
            case FINALIZADA -> "Finalizada";
            case CANCELADA -> "Cancelada";
        };
    }

    private String classeParaStatus(StatusOS status) {
        return switch (status) {
            case ABERTA -> "badge-os-aberta";
            case VERIFICANDO_ORCAMENTO -> "badge-os-orcamento";
            case EM_EXECUCAO -> "badge-os-execucao";
            case AGUARDANDO -> "badge-os-aguardando";
            case CONCLUIDA -> "badge-os-concluida";
            case FINALIZADA -> "badge-os-finalizada";
            case CANCELADA -> "badge-os-cancelada";
        };
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}