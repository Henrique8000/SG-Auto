package com.sgauto.app.controller.patio;

import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.service.*;
import com.sgauto.app.util.AutoCompleteComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EntradaPatioModalController {

    private static final String OPCAO_SEM_OS = "Nenhuma (entrada avulsa)";
    private static final List<StatusOS> STATUS_ENCERRADOS = List.of(StatusOS.CANCELADA, StatusOS.FINALIZADA);

    @FXML private ComboBox<String> cmbCliente;
    @FXML private ComboBox<String> cmbVeiculo;
    @FXML private ComboBox<String> cmbOrdemServico;
    @FXML private ComboBox<String> cmbTarifa;
    @FXML private ComboBox<String> cmbMotivo;
    @FXML private TextField txtLocalizacao;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final PatioService patioService;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final OrdemServicoService ordemServicoService;
    private final TabelaPrecoPatioService tabelaPrecoPatioService;
    private final MotivoEstadiaService motivoEstadiaService;

    private Map<String, Long> mapaClientes = Map.of();
    private Map<String, Long> mapaVeiculos = Map.of();
    private Map<String, Long> mapaOrdensServico = Map.of();
    private Map<String, Long> mapaTarifas = Map.of();
    private Map<String, Long> mapaMotivos = Map.of();
    private Runnable aoSalvar;

    public EntradaPatioModalController(PatioService patioService, ClienteService clienteService,
                                       VeiculoService veiculoService, OrdemServicoService ordemServicoService,
                                       TabelaPrecoPatioService tabelaPrecoPatioService,
                                       MotivoEstadiaService motivoEstadiaService) {
        this.patioService = patioService;
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
        this.ordemServicoService = ordemServicoService;
        this.tabelaPrecoPatioService = tabelaPrecoPatioService;
        this.motivoEstadiaService = motivoEstadiaService;
    }

    @FXML
    public void initialize() {
        List<Cliente> clientes = clienteService.listarAtivos();
        mapaClientes = clientes.stream().collect(Collectors.toMap(Cliente::getNome, Cliente::getId, (a, b) -> a));
        new AutoCompleteComboBox(cmbCliente).definirItens(List.copyOf(mapaClientes.keySet()));

        List<TabelaPrecoPatio> tarifas = tabelaPrecoPatioService.listarAtivas();
        mapaTarifas = tarifas.stream().collect(Collectors.toMap(TabelaPrecoPatio::getDescricao, TabelaPrecoPatio::getId, (a, b) -> a));
        cmbTarifa.setItems(FXCollections.observableArrayList(mapaTarifas.keySet()));

        List<MotivoEstadia> motivos = motivoEstadiaService.listarAtivos();
        mapaMotivos = motivos.stream().collect(Collectors.toMap(MotivoEstadia::getNome, MotivoEstadia::getId, (a, b) -> a));
        cmbMotivo.setItems(FXCollections.observableArrayList(mapaMotivos.keySet()));

        cmbCliente.valueProperty().addListener((obs, antigo, novo) -> carregarVeiculosDoCliente(novo));
        cmbVeiculo.valueProperty().addListener((obs, antigo, novo) -> carregarOrdensDoVeiculo(novo));
    }

    private void carregarVeiculosDoCliente(String nomeCliente) {
        cmbVeiculo.setItems(FXCollections.observableArrayList());
        cmbOrdemServico.setItems(FXCollections.observableArrayList());
        mapaVeiculos = Map.of();

        Long clienteId = mapaClientes.get(nomeCliente);
        if (clienteId == null) return;

        List<Veiculo> veiculos = veiculoService.listarPorCliente(clienteId);
        mapaVeiculos = veiculos.stream().collect(Collectors.toMap(Veiculo::getPlaca, Veiculo::getId, (a, b) -> a));
        cmbVeiculo.setItems(FXCollections.observableArrayList(mapaVeiculos.keySet()));
    }

    private void carregarOrdensDoVeiculo(String placa) {
        mapaOrdensServico = Map.of();

        Long veiculoId = mapaVeiculos.get(placa);
        ObservableList<String> opcoes = FXCollections.observableArrayList(OPCAO_SEM_OS);

        if (veiculoId != null) {
            List<OrdemServico> ordens = ordemServicoService.listarHistoricoVeiculo(veiculoId).stream()
                    .filter(os -> !STATUS_ENCERRADOS.contains(os.getStatus()))
                    .toList();
            mapaOrdensServico = ordens.stream().collect(Collectors.toMap(
                    os -> "#" + os.getId() + " — " + os.getStatus(), OrdemServico::getId, (a, b) -> a));
            opcoes.addAll(mapaOrdensServico.keySet());
        }

        cmbOrdemServico.setItems(opcoes);
        cmbOrdemServico.getSelectionModel().selectFirst();
    }

    public void configurar(Runnable aoSalvar) {
        this.aoSalvar = aoSalvar;
    }

    @FXML
    private void salvar() {
        try {
            Long clienteId = mapaClientes.get(cmbCliente.getValue());
            Long veiculoId = mapaVeiculos.get(cmbVeiculo.getValue());
            Long ordemServicoId = mapaOrdensServico.get(cmbOrdemServico.getValue()); // null = avulsa
            Long tarifaId = mapaTarifas.get(cmbTarifa.getValue());
            Long motivoId = mapaMotivos.get(cmbMotivo.getValue());
            String localizacao = txtLocalizacao.getText() == null ? "" : txtLocalizacao.getText().trim();

            if (clienteId == null) { mostrarErro("Selecione o cliente."); return; }
            if (veiculoId == null) { mostrarErro("Selecione o veículo."); return; }
            if (tarifaId == null) { mostrarErro("Selecione a tarifa."); return; }
            if (motivoId == null) { mostrarErro("Selecione o motivo."); return; }
            if (localizacao.isEmpty()) { mostrarErro("Informe a localização no pátio."); return; }

            patioService.registrarEntradaManual(clienteId, veiculoId, ordemServicoId, tarifaId, motivoId, localizacao);

            aoSalvar.run();
            fecharModal();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        fecharModal();
    }

    private void mostrarErro(String mensagem) {
        lblErro.setText(mensagem);
        lblErro.setVisible(true);
        lblErro.setManaged(true);
    }

    private void fecharModal() {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }
}