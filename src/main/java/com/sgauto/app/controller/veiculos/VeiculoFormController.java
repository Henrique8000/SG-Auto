package com.sgauto.app.controller.veiculos;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class VeiculoFormController {

    @FXML private Label lblTituloModal;
    @FXML private ComboBox<String> cmbCliente;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtAno;
    @FXML private TextField txtMarca;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private TextField txtKm;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private Object veiculoEmEdicao;
    private Runnable aoSalvar;

    // TODO(backend): injetar VeiculoService, ClienteService e ModeloService via construtor.

    @FXML
    public void initialize() {
        // TODO(backend): popular cmbCliente com clienteService.listarAtivos()
        //  e cmbModelo com modeloService.listarAtivas().
    }

    public void configurar(Object veiculoExistente, Runnable aoSalvar) {
        this.veiculoEmEdicao = veiculoExistente;
        this.aoSalvar = aoSalvar;

        if (veiculoExistente != null) {
            lblTituloModal.setText("Editar Veículo");
            // TODO(backend): preencher os campos a partir do Veiculo recebido.
        }
    }

    @FXML
    private void salvar() {
        esconderErro();

        String placa = txtPlaca.getText() == null ? "" : txtPlaca.getText().trim().toUpperCase();
        String marca = txtMarca.getText() == null ? "" : txtMarca.getText().trim();
        String modelo = cmbModelo.getValue();
        String dono = cmbCliente.getValue();

        // Validações de front (as regras de negócio ficarão no VeiculoService)
        if (dono == null || dono.isBlank()) {
            mostrarErro("Selecione o dono do veículo.");
            return;
        }
        if (placa.isEmpty()) {
            mostrarErro("A placa é obrigatória.");
            return;
        }
        if (marca.isEmpty()) {
            mostrarErro("A marca é obrigatória.");
            return;
        }
        if (modelo == null || modelo.isBlank()) {
            mostrarErro("Selecione o modelo.");
            return;
        }

        String anoTexto = txtAno.getText() == null ? "" : txtAno.getText().trim();
        if (!anoTexto.isEmpty() && !anoTexto.matches("\\d{4}")) {
            mostrarErro("Ano deve ter 4 dígitos.");
            return;
        }

        String kmTexto = txtKm.getText() == null ? "" : txtKm.getText().replaceAll("\\D", "");

        // TODO(backend): montar o Veiculo e chamar veiculoService.cadastrar / atualizar,
        //  capturando IllegalArgumentException/IllegalStateException para o lblErro.

        if (aoSalvar != null) {
            aoSalvar.run();
        }
        fecharModal();
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