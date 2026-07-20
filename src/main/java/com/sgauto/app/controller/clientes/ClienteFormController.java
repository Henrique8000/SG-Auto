package com.sgauto.app.controller.clientes;

import com.sgauto.app.controller.clientes.ClientesController.ClienteRow;
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

    private ClienteRow clienteEmEdicao;
    private Runnable aoSalvar;

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

    public void configurar(ClienteRow clienteExistente, Runnable aoSalvar) {
        this.clienteEmEdicao = clienteExistente;
        this.aoSalvar = aoSalvar;

        if (clienteExistente != null) {
            lblTituloModal.setText("Editar Cliente");

            boolean ehPJ = "PJ".equals(clienteExistente.tipo());
            btnTipoPJ.setSelected(ehPJ);
            btnTipoPF.setSelected(!ehPJ);

            txtNome.setText(clienteExistente.nome());
            txtDocumento.setText(clienteExistente.documento());
            txtCelular.setText(clienteExistente.celular());
            txtEmail.setText(clienteExistente.email());
        }
    }

    private void ajustarCamposPorTipo() {
        boolean ehPJ = btnTipoPJ.isSelected();

        lblNome.setText(ehPJ ? "Razão social" : "Nome completo");
        lblDocumento.setText(ehPJ ? "CNPJ" : "CPF");
        txtDocumento.setPromptText(ehPJ ? "Somente números (14 dígitos)" : "Somente números (11 dígitos)");

        boxNomeFantasia.setVisible(ehPJ);
        boxNomeFantasia.setManaged(ehPJ);
    }

    @FXML
    private void salvar() {
        String nome = txtNome.getText() == null ? "" : txtNome.getText().trim();
        String documento = txtDocumento.getText() == null ? "" : txtDocumento.getText().replaceAll("\\D", "");
        boolean ehPJ = btnTipoPJ.isSelected();

        if (nome.isEmpty()) {
            mostrarErro(ehPJ ? "Razão social é obrigatória." : "Nome é obrigatório.");
            return;
        }

        if (!documento.isEmpty()) {
            int tamanhoEsperado = ehPJ ? 14 : 11;
            if (documento.length() != tamanhoEsperado) {
                mostrarErro((ehPJ ? "CNPJ" : "CPF") + " deve ter " + tamanhoEsperado + " dígitos.");
                return;
            }
        }

        // TODO: montar ClientePF/ClientePJ e chamar ClienteService.cadastrar / atualizar
        //  Por enquanto o formulário só valida e fecha o front-end

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

    private void fecharModal() {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }
}