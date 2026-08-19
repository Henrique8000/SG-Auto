package com.sgauto.app.controller.os;

import com.sgauto.app.controller.patio.SaidaPatioModalController;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.patio.EstadiaPatio;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoDetalheController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrdemServicoDetalheController.class);

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
    private final EstadiaPatioRepository estadiaPatioRepository;
    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;
    private Long osId;
    private Runnable aoFechar;
    private Map<String, StatusOS> mapaStatus = Map.of();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OrdemServicoDetalheController(OrdemServicoService ordemServicoService, EstadiaPatioRepository estadiaPatioRepository, ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.ordemServicoService = ordemServicoService;
        this.applicationContext = applicationContext;
        this.estadiaPatioRepository = estadiaPatioRepository;
        this.permissaoUtil = permissaoUtil;
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
        tabPecasController.atualizar();
        tabServicosController.atualizar();
        tabPagamentosController.atualizar();
        if (aoFechar != null) aoFechar.run();
    }

    private void carregarDados() {
        try {
            OrdemServico os = ordemServicoService.buscarComDetalhesCompletos(osId);

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
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao carregar O.S. #" + osId,
                    e.getMessage() != null ? e.getMessage() : "Erro inesperado ao carregar os dados.");
        }
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

        if (novoStatus == StatusOS.FINALIZADA && !permissaoUtil.verificar(PermissaoChave.OS_FINALIZAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        } else if (novoStatus == StatusOS.EM_EXECUCAO && !permissaoUtil.verificar(PermissaoChave.OS_APROVAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        } else if (novoStatus == StatusOS.CANCELADA && !permissaoUtil.verificar(PermissaoChave.OS_CANCELAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        } else if (novoStatus != StatusOS.FINALIZADA
                && novoStatus != StatusOS.EM_EXECUCAO
                && novoStatus != StatusOS.CANCELADA
                && !permissaoUtil.verificar(PermissaoChave.OS_EDITAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        try {
            ordemServicoService.alterarStatus(osId, novoStatus);
            if (novoStatus == StatusOS.CONCLUIDA || novoStatus == StatusOS.FINALIZADA || novoStatus == StatusOS.CANCELADA ) {

                Optional<EstadiaPatio> estadiaOpt = estadiaPatioRepository
                        .findByOrdemServicoIdAndStatus(osId, StatusEstadiaPatio.NO_PATIO);

                if (estadiaOpt.isPresent()) {
                    Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacao.setTitle("Veículo no Pátio");
                    confirmacao.setHeaderText("O.S. Encerrada com veículo no pátio");
                    confirmacao.setContentText("O veículo desta O.S. ainda consta no pátio da oficina.\n" +
                            "Deseja registrar a saída do veículo agora?");

                    Optional<ButtonType> resposta = confirmacao.showAndWait();

                    if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
                        abrirModalSaidaPatio(estadiaOpt.get());
                    }
                }
            }

            recarregar();

        } catch (IllegalStateException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível alterar o status", e.getMessage());
        }
    }

    @FXML
    private void cancelarOs() {
        if (!permissaoUtil.verificar(PermissaoChave.OS_CANCELAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Cancelar Ordem de Serviço");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente cancelar esta O.S.? As peças já lançadas retornarão ao estoque.");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                try{
                    Optional<EstadiaPatio> estadiaOpt = estadiaPatioRepository
                            .findByOrdemServicoIdAndStatus(osId, StatusEstadiaPatio.NO_PATIO);

                    if (estadiaOpt.isPresent()) {
                        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
                        c.setTitle("Veículo no Pátio");
                        c.setHeaderText("O.S. cancelada com veículo no pátio");
                        c.setContentText("O veículo desta O.S. ainda consta no pátio da oficina.\n" +
                                "Deseja registrar a saída do veículo agora?");

                        Optional<ButtonType> resposta = c.showAndWait();

                        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
                            abrirModalSaidaPatio(estadiaOpt.get());
                        }
                    }
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

    private void abrirModalSaidaPatio(EstadiaPatio estadia) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/saida-patio-modal.fxml"));

            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            SaidaPatioModalController controller = loader.getController();

            controller.configurar(estadia.getId(), () -> recarregar());

            Stage stage = ModalUtil.abrir(root, "Registrar Saída do Veículo", lblCliente.getScene().getWindow());
            stage.showAndWait();

        }
        catch (Exception e) {
            log.error("Erro ao abrir tela de saída do pátio a partir da O.S.", e);
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela do pátio.");
        }
    }
}