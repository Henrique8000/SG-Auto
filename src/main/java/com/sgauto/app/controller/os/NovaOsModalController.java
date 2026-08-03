package com.sgauto.app.controller.os;

import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.service.ClienteService;
import com.sgauto.app.service.FuncionarioService;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.service.VeiculoService;
import com.sgauto.app.util.AutoCompleteComboBox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class NovaOsModalController {

    @FXML private ComboBox<String> cmbCliente;
    @FXML private ComboBox<String> cmbVeiculo;
    @FXML private ComboBox<String> cmbFuncionario;
    @FXML private TextArea txtSintomas;
    @FXML private DatePicker dpDataPrevisao;
    @FXML private CheckBox chkFicarNoPatio;
    @FXML private Label lblErro;
    @FXML private Button btnCriar;

    private final OrdemServicoService ordemServicoService;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final FuncionarioService funcionarioService;
    private final ApplicationContext applicationContext;

    private Map<String, Long> mapaClientes = Map.of();
    private Map<String, Long> mapaVeiculos = Map.of();
    private Map<String, Long> mapaFuncionarios = Map.of();
    private Consumer<Long> aoCriar;

    public NovaOsModalController(OrdemServicoService ordemServicoService, ClienteService clienteService,
                                 VeiculoService veiculoService, FuncionarioService funcionarioService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
        this.funcionarioService = funcionarioService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        List<Cliente> clientes = clienteService.listarTodos(); // ajuste o nome do método conforme seu ClienteService
        mapaClientes = clientes.stream().collect(Collectors.toMap(Cliente::getNome, Cliente::getId, (a, b) -> a));
        new AutoCompleteComboBox(cmbCliente).definirItens(List.copyOf(mapaClientes.keySet()));

        List<Funcionario> funcionarios = funcionarioService.listarAptosParaOrdemServico();
        mapaFuncionarios = funcionarios.stream().collect(Collectors.toMap(Funcionario::getNomeExibicao, Funcionario::getId, (a, b) -> a));
        new AutoCompleteComboBox(cmbFuncionario).definirItens(List.copyOf(mapaFuncionarios.keySet()));

        cmbCliente.valueProperty().addListener((obs, antigo, novo) -> carregarVeiculosDoCliente(novo));
    }

    private void carregarVeiculosDoCliente(String nomeCliente) {
        cmbVeiculo.setItems(FXCollections.observableArrayList());
        mapaVeiculos = Map.of();

        Long clienteId = mapaClientes.get(nomeCliente);
        if (clienteId == null) return;

        List<Veiculo> veiculos = veiculoService.listarPorCliente(clienteId); // ajuste o nome do método conforme seu VeiculoService
        mapaVeiculos = veiculos.stream().collect(Collectors.toMap(Veiculo::getPlaca, Veiculo::getId, (a, b) -> a));
        cmbVeiculo.setItems(FXCollections.observableArrayList(mapaVeiculos.keySet()));
    }

    public void configurar(Consumer<Long> aoCriar) {
        this.aoCriar = aoCriar;
    }

    @FXML
    private void criar() {
        try {
            Long clienteId = mapaClientes.get(cmbCliente.getValue());
            Long veiculoId = mapaVeiculos.get(cmbVeiculo.getValue());
            Long funcionarioId = mapaFuncionarios.get(cmbFuncionario.getValue());

            if (clienteId == null) { mostrarErro("Selecione o cliente."); return; }
            if (veiculoId == null) { mostrarErro("Selecione o veículo."); return; }
            if (funcionarioId == null) { mostrarErro("Selecione o funcionário responsável."); return; }

            LocalDateTime dataPrevisao = dpDataPrevisao.getValue() != null
                    ? dpDataPrevisao.getValue().atTime(18, 0) : null;

            OrdemServico nova = ordemServicoService.criarOS(clienteId, veiculoId, funcionarioId,
                    txtSintomas.getText().trim(), dataPrevisao, chkFicarNoPatio.isSelected());

            aoCriar.accept(nova.getId());
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
        ((Stage) btnCriar.getScene().getWindow()).close();
    }
}