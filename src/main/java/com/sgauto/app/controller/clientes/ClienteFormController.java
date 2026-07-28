package com.sgauto.app.controller.clientes;

import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.ClientePF;
import com.sgauto.app.model.ClientePJ;
import com.sgauto.app.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ClienteFormController {

    @FXML private Label lblTituloModal;
    @FXML private ToggleGroup grupoTipoPessoa;
    @FXML private ToggleButton btnTipoPF;
    @FXML private ToggleButton btnTipoPJ;
    @FXML private Label lblNome;
    @FXML private TextField txtNome;
    @FXML private VBox boxNomeFantasia;
    @FXML private TextField txtNomeFantasia;
    @FXML private Label lblDocumento;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtCelular;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final ClienteService clienteService;

    private Cliente clienteEmEdicao;
    private Runnable aoSalvar;

    public ClienteFormController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        grupoTipoPessoa.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) {
                antigo.setSelected(true);
            } else {
                ajustarCamposPorTipo();
            }
        });

        ajustarCamposPorTipo();
    }

    public void configurar(Cliente clienteExistente, Runnable aoSalvar) {
        this.clienteEmEdicao = clienteExistente;
        this.aoSalvar = aoSalvar;

        if (clienteExistente != null) {
            lblTituloModal.setText("Editar Cliente");

            boolean ehPJ = clienteExistente instanceof ClientePJ;
            btnTipoPJ.setSelected(ehPJ);
            btnTipoPF.setSelected(!ehPJ);

            // O tipo (discriminador) não pode mudar depois de criado
            btnTipoPF.setDisable(true);
            btnTipoPJ.setDisable(true);

            txtNome.setText(clienteExistente.getNome());
            txtDocumento.setText(clienteExistente.getDocumento() == null
                    ? "" : clienteExistente.getDocumentoFormatado());
            txtCelular.setText(clienteExistente.getCelular());
            txtTelefone.setText(clienteExistente.getTelefone());
            txtEmail.setText(clienteExistente.getEmail());

            if (clienteExistente instanceof ClientePJ pj) {
                txtNomeFantasia.setText(pj.getNomeFantasia());
            }

            ajustarCamposPorTipo();
        }
    }

    private void ajustarCamposPorTipo() {
        boolean ehPJ = btnTipoPJ.isSelected();

        lblNome.setText(ehPJ ? "Razão social" : "Nome completo");
        lblDocumento.setText(ehPJ ? "CNPJ" : "CPF");
        txtDocumento.setPromptText(ehPJ ? "14 caracteres, sem pontuação" : "Somente números (11 dígitos)");

        boxNomeFantasia.setVisible(ehPJ);
        boxNomeFantasia.setManaged(ehPJ);
    }

    @FXML
    private void salvar() {
        esconderErro();

        String celular = somenteDigitos(txtCelular.getText());
        String telefone = somenteDigitos(txtTelefone.getText());

        if (celular != null && (celular.length() < 10 || celular.length() > 11)) {
            mostrarErro("Celular deve ter 10 ou 11 dígitos (DDD + número).");
            return;
        }
        if (telefone != null && telefone.length() > 10) {
            mostrarErro("Telefone fixo deve ter no máximo 10 dígitos (DDD + número).");
            return;
        }

        boolean ehPJ = btnTipoPJ.isSelected();

        try {
            if (clienteEmEdicao == null) {
                Cliente novo = ehPJ
                        ? new ClientePJ(texto(txtNome), texto(txtDocumento), celular, telefone,
                        texto(txtEmail), null, true, texto(txtNomeFantasia), null)
                        : new ClientePF(texto(txtNome), texto(txtDocumento), celular, telefone,
                        texto(txtEmail), null, true, null, null);
                clienteService.cadastrar(novo);
            } else {
                clienteEmEdicao.setNome(texto(txtNome));
                clienteEmEdicao.setDocumento(texto(txtDocumento));
                clienteEmEdicao.setCelular(celular);
                clienteEmEdicao.setTelefone(telefone);
                clienteEmEdicao.setEmail(texto(txtEmail));
                if (clienteEmEdicao instanceof ClientePJ pj) {
                    pj.setNomeFantasia(texto(txtNomeFantasia));
                }
                clienteService.atualizar(clienteEmEdicao);
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

    private String texto(TextField campo) {
        String valor = campo.getText();
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private String somenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        String digitos = valor.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
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