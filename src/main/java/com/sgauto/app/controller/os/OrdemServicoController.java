package com.sgauto.app.controller.os;

import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.service.PatioService;
import com.sgauto.app.util.ModalUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class OrdemServicoController {

    @FXML private Label lblAbertas;
    @FXML private Label lblEmExecucao;
    @FXML private Label lblAtrasadas;
    @FXML private Label lblFaturamentoMes;
    @FXML private Label lblTicketMedio;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroStatus;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private CheckBox chkSomenteAtrasadas;
    @FXML private CheckBox chkSomenteNoPatio;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private TableView<OrdemServico> tabelaOs;
    @FXML private TableColumn<OrdemServico, String> colId;
    @FXML private TableColumn<OrdemServico, String> colCliente;
    @FXML private TableColumn<OrdemServico, String> colPlaca;
    @FXML private TableColumn<OrdemServico, String> colAbertura;
    @FXML private TableColumn<OrdemServico, String> colPrevisao;
    @FXML private TableColumn<OrdemServico, String> colValorTotal;
    @FXML private TableColumn<OrdemServico, Void> colStatus;
    @FXML private TableColumn<OrdemServico, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final OrdemServicoService ordemServicoService;
    private final PatioService patioService;
    private final ApplicationContext applicationContext;
    private final ObservableList<OrdemServico> osExibidas = FXCollections.observableArrayList();

    private List<OrdemServico> todasOs = List.of();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String ORD_RECENTE = "Mais recente";
    private static final String ORD_ANTIGA = "Mais antiga";
    private static final String ORD_MAIOR_VALOR = "Maior valor";
    private static final String ORD_PREVISAO = "Previsão mais próxima";

    public OrdemServicoController(OrdemServicoService ordemServicoService, PatioService patioService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.patioService = patioService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarFiltros() {
        List<String> statusOpcoes = new java.util.ArrayList<>();
        statusOpcoes.add("Todos os status");
        Arrays.stream(StatusOS.values()).forEach(s -> statusOpcoes.add(descreverStatus(s)));
        cmbFiltroStatus.setItems(FXCollections.observableArrayList(statusOpcoes));
        cmbFiltroStatus.getSelectionModel().selectFirst();

        cmbOrdenar.setItems(FXCollections.observableArrayList(ORD_RECENTE, ORD_ANTIGA, ORD_MAIOR_VALOR, ORD_PREVISAO));
        cmbOrdenar.getSelectionModel().select(ORD_RECENTE);

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        dpDataInicio.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        dpDataFim.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        chkSomenteAtrasadas.selectedProperty().addListener((obs, a, n) -> aplicarFiltros());
        chkSomenteNoPatio.selectedProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbOrdenar.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void configurarColunas() {
        colId.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        colCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCliente() != null ? data.getValue().getCliente().getNome() : "-"));
        colPlaca.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getVeiculo() != null ? data.getValue().getVeiculo().getPlaca() : "-"));
        colAbertura.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataAbertura() != null ? data.getValue().getDataAbertura().format(FMT) : "-"));
        colPrevisao.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataPrevisao() != null ? data.getValue().getDataPrevisao().format(FMT) : "-"));
        colValorTotal.setCellValueFactory(data -> new SimpleStringProperty(
                formatarMoeda(data.getValue().getValorTotalOs())));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                StatusOS status = getTableView().getItems().get(getIndex()).getStatus();
                badge.setText(descreverStatus(status));
                badge.getStyleClass().setAll("badge", classeParaStatus(status));
                setGraphic(badge);
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnAbrir = new Button("Abrir");
            {
                btnAbrir.getStyleClass().add("btn-table-action");
                btnAbrir.setOnAction(e -> abrirDetalhe(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btnAbrir);
            }
        });

        tabelaOs.setItems(osExibidas);
    }

    private String descreverStatus(StatusOS status) {
        if (status == null) return "-";
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
        if (status == null) return "badge-normal";
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

    private void carregarDados() {
        todasOs = ordemServicoService.listarTodas();
        atualizarCards();
        aplicarFiltros();
    }

    private void atualizarCards() {
        lblAbertas.setText(String.valueOf(ordemServicoService.contarOsPorStatus(StatusOS.ABERTA)));
        lblEmExecucao.setText(String.valueOf(ordemServicoService.contarOsPorStatus(StatusOS.EM_EXECUCAO)));
        lblAtrasadas.setText(String.valueOf(ordemServicoService.listarOsAtrasadas().size()));

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = LocalDateTime.now();
        BigDecimal faturamento = ordemServicoService.calcularFaturamento(inicioMes, fimMes);
        lblFaturamentoMes.setText(formatarMoeda(faturamento));

        BigDecimal ticketMedio = ordemServicoService.calcularTicketMedio(inicioMes, fimMes);
        lblTicketMedio.setText(formatarMoeda(ticketMedio));
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().trim().toLowerCase();
        String statusSelecionado = cmbFiltroStatus.getValue();
        LocalDate dataInicio = dpDataInicio.getValue();
        LocalDate dataFim = dpDataFim.getValue();
        boolean somenteAtrasadas = chkSomenteAtrasadas.isSelected();
        boolean somenteNoPatio = chkSomenteNoPatio.isSelected();

        boolean termoNumerico = termo.matches("\\d+");

        List<OrdemServico> filtradas = todasOs.stream()
                .filter(os -> termo.isBlank()
                        || (termoNumerico && String.valueOf(os.getId()).equals(termo))
                        || (!termoNumerico && os.getCliente() != null && os.getCliente().getNome().toLowerCase().contains(termo))
                        || (!termoNumerico && os.getVeiculo() != null && os.getVeiculo().getPlaca().toLowerCase().contains(termo)))
                .filter(os -> statusSelecionado == null || statusSelecionado.equals("Todos os status")
                        || statusSelecionado.equals(descreverStatus(os.getStatus())))
                .filter(os -> dataInicio == null || (os.getDataAbertura() != null && !os.getDataAbertura().toLocalDate().isBefore(dataInicio)))
                .filter(os -> dataFim == null || (os.getDataAbertura() != null && !os.getDataAbertura().toLocalDate().isAfter(dataFim)))
                .filter(os -> !somenteAtrasadas || estaAtrasada(os))
                .filter(os -> !somenteNoPatio || patioService.possuiEstadiaAberta(os.getId()))
                .sorted(obterComparador())
                .toList();

        osExibidas.setAll(filtradas);
        lblContagem.setText(filtradas.size() + " O.S.");
        atualizarEstadoVazio(filtradas.isEmpty());
    }

    private boolean estaAtrasada(OrdemServico os) {
        List<StatusOS> encerrados = List.of(StatusOS.CONCLUIDA, StatusOS.FINALIZADA, StatusOS.CANCELADA);
        return os.getDataPrevisao() != null
                && !encerrados.contains(os.getStatus())
                && os.getDataPrevisao().isBefore(LocalDateTime.now());
    }

    private Comparator<OrdemServico> obterComparador() {
        String ordem = cmbOrdenar.getValue();
        if (ORD_ANTIGA.equals(ordem)) return Comparator.comparing(OrdemServico::getDataAbertura);
        if (ORD_MAIOR_VALOR.equals(ordem)) return Comparator.comparing(OrdemServico::getValorTotalOs).reversed();
        if (ORD_PREVISAO.equals(ordem)) return Comparator.comparing(
                os -> os.getDataPrevisao() != null ? os.getDataPrevisao() : LocalDateTime.MAX);
        return Comparator.comparing((OrdemServico os) -> os.getDataAbertura() != null ? os.getDataAbertura() : LocalDateTime.MIN).reversed();
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaOs.setVisible(!vazio);
        tabelaOs.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovaOs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os/nova-os-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            NovaOsModalController controller = loader.getController();
            controller.configurar(osId -> {
                carregarDados();
                abrirDetalhePorId(osId);
            });

            Stage modal = ModalUtil.abrir(root, "Nova Ordem de Serviço", tabelaOs.getScene().getWindow());
            modal.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir nova O.S.", e);
        }
    }

    private void abrirDetalhe(OrdemServico os) {
        abrirDetalhePorId(os.getId());
    }

    private void abrirDetalhePorId(Long osId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/os/ordem-servico-detalhe-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            OrdemServicoDetalheController controller = loader.getController();
            controller.configurar(osId, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, "Ordem de Serviço #" + osId, tabelaOs.getScene().getWindow());
            modal.showAndWait();

            carregarDados();
        } catch (IOException e) {
            mostrarErro("Erro ao abrir tela", "Não foi possível carregar o formulário de detalhes da O.S.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir O.S. #" + osId, e.getMessage() != null ? e.getMessage() : "Erro inesperado.");
        }
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null ? String.format("R$ %,.2f", valor) : "R$ 0,00";
    }
}