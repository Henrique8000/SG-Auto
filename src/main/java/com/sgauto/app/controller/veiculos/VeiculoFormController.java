package com.sgauto.app.controller.veiculos;

import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.service.ClienteService;
import com.sgauto.app.service.estoque.ModeloService;
import com.sgauto.app.service.VeiculoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VeiculoFormController {

    @FXML private Label lblTituloModal;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtAno;
    @FXML private TextField txtMarca;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private TextField txtKm;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final VeiculoService veiculoService;
    private final ClienteService clienteService;
    private final ModeloService modeloService;

    private Veiculo veiculoEmEdicao;
    private Runnable aoSalvar;

    public VeiculoFormController(VeiculoService veiculoService,
                                 ClienteService clienteService,
                                 ModeloService modeloService) {
        this.veiculoService = veiculoService;
        this.clienteService = clienteService;
        this.modeloService = modeloService;
    }

    @FXML
    public void initialize() {
        // Combo de clientes: mostra o nome, mas guarda o objeto Cliente
        cmbCliente.setItems(FXCollections.observableArrayList(clienteService.listarAtivos()));
        cmbCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? "" : cliente.getNome() + " — " + cliente.getTipo();
            }
            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });

        // Combo de modelos: nomes do catálogo ativo
        List<String> modelos = modeloService.listarAtivas().stream().map(m -> m.getNome()).toList();
        cmbModelo.setItems(FXCollections.observableArrayList(modelos));
    }

    public void configurar(Veiculo veiculoExistente, Runnable aoSalvar) {
        this.veiculoEmEdicao = veiculoExistente;
        this.aoSalvar = aoSalvar;

        if (veiculoExistente != null) {
            lblTituloModal.setText("Editar Veículo");

            selecionarClienteAtual(veiculoExistente.getCliente());
            txtPlaca.setText(veiculoExistente.getPlaca());
            txtMarca.setText(veiculoExistente.getMarca());
            cmbModelo.setValue(veiculoExistente.getModelo());
            txtAno.setText(veiculoExistente.getAno() == null ? "" : String.valueOf(veiculoExistente.getAno()));
            txtKm.setText(veiculoExistente.getKm() == null ? "" : String.valueOf(veiculoExistente.getKm()));
        }
    }

    /**
     * Seleciona no combo o cliente dono, comparando por id — a instância vinda
     * da lista de veículos pode não ser a mesma da lista de clientes ativos.
     */
    private void selecionarClienteAtual(Cliente dono) {
        if (dono == null) {
            return;
        }
        cmbCliente.getItems().stream()
                .filter(c -> c.getId().equals(dono.getId()))
                .findFirst()
                .ifPresentOrElse(
                        cmbCliente::setValue,
                        () -> {
                            // Dono inativo não veio na lista de ativos: adiciona só para exibir
                            cmbCliente.getItems().add(dono);
                            cmbCliente.setValue(dono);
                        });
    }

    @FXML
    private void salvar() {
        esconderErro();

        Cliente dono = cmbCliente.getValue();
        String placa = txtPlaca.getText();
        String marca = txtMarca.getText();
        String modelo = cmbModelo.getValue();

        Integer ano = null;
        String anoTexto = txtAno.getText() == null ? "" : txtAno.getText().trim();
        if (!anoTexto.isEmpty()) {
            if (!anoTexto.matches("\\d{4}")) {
                mostrarErro("Ano deve ter 4 dígitos.");
                return;
            }
            ano = Integer.parseInt(anoTexto);
        }

        Integer km = null;
        String kmTexto = txtKm.getText() == null ? "" : txtKm.getText().replaceAll("\\D", "");
        if (!kmTexto.isEmpty()) {
            km = Integer.parseInt(kmTexto);
        }

        try {
            if (veiculoEmEdicao == null) {
                Veiculo novo = new Veiculo(dono, placa, marca, modelo, ano, km, true);
                veiculoService.cadastrar(novo);
            } else {
                veiculoEmEdicao.setCliente(dono);
                veiculoEmEdicao.setPlaca(placa);
                veiculoEmEdicao.setMarca(marca);
                veiculoEmEdicao.setModelo(modelo);
                veiculoEmEdicao.setAno(ano);
                veiculoEmEdicao.setKm(km);
                veiculoService.atualizar(veiculoEmEdicao);
            }

            if (aoSalvar != null) {
                aoSalvar.run();
            }
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

    private void esconderErro() {
        lblErro.setVisible(false);
        lblErro.setManaged(false);
    }

    private void fecharModal() {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }
}