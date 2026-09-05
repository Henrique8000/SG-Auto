package com.sgauto.app.controller.patio;

import com.sgauto.app.dto.patio.PatioFiltroDTO;
import com.sgauto.app.dto.patio.PatioItemDashboardDTO;
import com.sgauto.app.dto.patio.PatioResumoDashboardDTO;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.service.MotivoEstadiaService;
import com.sgauto.app.service.PatioService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PatioAtualTabController {

    private static final int TAMANHO_PAGINA = 15;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String ORD_ENTRADA_RECENTE = "Entrada mais recente";
    private static final String ORD_ENTRADA_ANTIGA = "Entrada mais antiga";

    @FXML private Label lblQuantidadeNoPatio;
    @FXML private Label lblValorEstimado;
    @FXML private Label lblTempoMedio;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroMotivo;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private Label lblContagem;
    @FXML private TableView<PatioItemDashboardDTO> tabelaPatio;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colPlaca;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colCliente;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colOs;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colMotivo;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colEntrada;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colTempo;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colLocalizacao;
    @FXML private TableColumn<PatioItemDashboardDTO, String> colValorEstimado;
    @FXML private TableColumn<PatioItemDashboardDTO, Void> colAcoes;
    @FXML private VBox painelVazio;
    @FXML private Button btnPaginaAnterior;
    @FXML private Label lblPaginaAtual;
    @FXML private Button btnProximaPagina;

    private final PatioService patioService;
    private final MotivoEstadiaService motivoEstadiaService;
    private final ApplicationContext applicationContext;
    private final VerificaPermissaoUtil permissaoUtil;
    private final ObservableList<PatioItemDashboardDTO> itens = FXCollections.observableArrayList();

    private Map<String, Long> mapaMotivos = Map.of();
    private int paginaAtual = 0;
    private int totalPaginas = 1;

    public PatioAtualTabController(PatioService patioService, MotivoEstadiaService motivoEstadiaService,
                                   ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.patioService = patioService;
        this.motivoEstadiaService = motivoEstadiaService;
        this.applicationContext = applicationContext;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        configurarColunas();
        cmbOrdenar.setItems(FXCollections.observableArrayList(ORD_ENTRADA_RECENTE, ORD_ENTRADA_ANTIGA));
        cmbOrdenar.getSelectionModel().select(ORD_ENTRADA_RECENTE);
        carregarMotivos();

        txtBusca.textProperty().addListener((obs, a, n) -> carregarPagina(0));
        cmbFiltroMotivo.valueProperty().addListener((obs, a, n) -> carregarPagina(0));
        cmbOrdenar.valueProperty().addListener((obs, a, n) -> carregarPagina(0));

        carregarPagina(0);
        atualizarResumo();
    }

    private void carregarMotivos() {
        List<MotivoEstadia> motivos = motivoEstadiaService.listarAtivos();
        mapaMotivos = motivos.stream().collect(Collectors.toMap(MotivoEstadia::getNome, MotivoEstadia::getId, (a, b) -> a));

        String selecionadoAntes = cmbFiltroMotivo.getValue();
        ObservableList<String> opcoes = FXCollections.observableArrayList();
        opcoes.add("Todos os motivos");
        opcoes.addAll(mapaMotivos.keySet());
        cmbFiltroMotivo.setItems(opcoes);
        cmbFiltroMotivo.setValue(selecionadoAntes != null && opcoes.contains(selecionadoAntes) ? selecionadoAntes : "Todos os motivos");
    }

    private void configurarColunas() {
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoNome"));
        colOs.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOrdemServicoId() != null ? "#" + data.getValue().getOrdemServicoId() : "—"));
        colEntrada.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDataEntrada().format(FORMATO_DATA)));
        colTempo.setCellValueFactory(data -> new SimpleStringProperty(formatarDuracao(data.getValue().getDataEntrada())));
        colLocalizacao.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLocalizacao() != null ? data.getValue().getLocalizacao() : "Não definida"));
        colValorEstimado.setCellValueFactory(data -> new SimpleStringProperty(formatarMoeda(data.getValue().getValorEstimadoOuFinal())));

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnLocalizacao = new Button("Localização");
            private final Button btnSaida = new Button("Dar Saída");
            private final HBox container = new HBox(6, btnLocalizacao, btnSaida);
            {
                btnLocalizacao.getStyleClass().add("btn-table-action");
                btnSaida.getStyleClass().add("btn-table-action");
                btnLocalizacao.setOnAction(e -> editarLocalizacao(getTableView().getItems().get(getIndex())));
                btnSaida.setOnAction(e -> abrirModalSaida(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : container);
            }
        });

        tabelaPatio.setItems(itens);
    }

    private String formatarDuracao(LocalDateTime dataEntrada) {
        long horas = Duration.between(dataEntrada, LocalDateTime.now()).toHours();
        long dias = horas / 24;
        long horasRestantes = horas % 24;
        return dias > 0 ? dias + "d " + horasRestantes + "h" : horasRestantes + "h";
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %,.2f", valor);
    }

    private void carregarPagina(int indice) {
        PatioFiltroDTO filtro = montarFiltro();
        Sort ordenacao = ORD_ENTRADA_ANTIGA.equals(cmbOrdenar.getValue())
                ? Sort.by("dataEntrada").ascending()
                : Sort.by("dataEntrada").descending();

        Pageable pageable = PageRequest.of(indice, TAMANHO_PAGINA, ordenacao);
        Page<PatioItemDashboardDTO> pagina = patioService.listarPatioAtualPaginado(filtro, pageable);

        itens.setAll(pagina.getContent());
        paginaAtual = indice;
        totalPaginas = Math.max(1, pagina.getTotalPages());
        lblPaginaAtual.setText("Página " + (paginaAtual + 1) + " de " + totalPaginas);
        btnPaginaAnterior.setDisable(paginaAtual == 0);
        btnProximaPagina.setDisable(paginaAtual >= totalPaginas - 1);
        lblContagem.setText(pagina.getTotalElements() + " resultado(s)");
        atualizarEstadoVazio(pagina.isEmpty());
    }

    private PatioFiltroDTO montarFiltro() {
        PatioFiltroDTO filtro = new PatioFiltroDTO();
        filtro.setBusca(txtBusca.getText());
        String motivoSelecionado = cmbFiltroMotivo.getValue();
        if (motivoSelecionado != null && !motivoSelecionado.equals("Todos os motivos")) {
            filtro.setMotivoId(mapaMotivos.get(motivoSelecionado));
        }
        return filtro;
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaPatio.setVisible(!vazio);
        tabelaPatio.setManaged(!vazio);
    }

    private void atualizarResumo() {
        PatioResumoDashboardDTO resumo = patioService.obterResumoDashboard();
        lblQuantidadeNoPatio.setText(String.valueOf(resumo.getQuantidadeVeiculosNoPatio()));
        lblValorEstimado.setText(formatarMoeda(resumo.getValorTotalEstimadoEmAberto()));
        lblTempoMedio.setText(String.format("%.0fh", resumo.getTempoMedioPermanenciaHoras()));
    }

    @FXML
    private void irPaginaAnterior() {
        if (paginaAtual > 0) carregarPagina(paginaAtual - 1);
    }

    @FXML
    private void irProximaPagina() {
        if (paginaAtual < totalPaginas - 1) carregarPagina(paginaAtual + 1);
    }

    @FXML
    private void abrirModalEntradaManual() {
        if (permissaoUtil.verificar(PermissaoChave.PATIO_ENTRADA)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/entrada-patio-modal.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                EntradaPatioModalController controller = loader.getController();
                controller.configurar(this::recarregarTudo);

                Stage modal = ModalUtil.abrir(root, "Nova Entrada no Pátio", tabelaPatio.getScene().getWindow());
                modal.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao abrir entrada manual de pátio", e);
            }
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    private void abrirModalSaida(PatioItemDashboardDTO item) {
        if(permissaoUtil.verificar(PermissaoChave.PATIO_SAIDA)){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/patio/saida-patio-modal.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                SaidaPatioModalController controller = loader.getController();
                controller.configurar(item.getEstadiaId(), this::recarregarTudo);

                Stage modal = ModalUtil.abrir(root, "Dar Saída — Placa " + item.getPlaca(), tabelaPatio.getScene().getWindow());
                modal.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao abrir saída de pátio", e);
            }
        }else {
            ExibirMensagemBloqueioUtil.exibir();
        }

    }

    private void editarLocalizacao(PatioItemDashboardDTO item) {
        TextInputDialog dialog = new TextInputDialog(item.getLocalizacao() != null ? item.getLocalizacao() : "");
        dialog.setTitle("Localização no Pátio");
        dialog.setHeaderText(null);
        dialog.setContentText("Vaga/setor do veículo placa " + item.getPlaca() + ":");

        dialog.showAndWait().ifPresent(novaLocalizacao -> {
            patioService.atualizarLocalizacao(item.getEstadiaId(), novaLocalizacao);
            recarregarTudo();
        });
    }

    private void recarregarTudo() {
        carregarMotivos();
        carregarPagina(paginaAtual);
        atualizarResumo();
    }
}