package com.sgauto.app.controller.estoque;

import com.sgauto.app.model.Modelo;
import com.sgauto.app.service.ModeloService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ModeloFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final ModeloService modeloService;
    private Modelo modeloEmEdicao;
    private Runnable aoSalvar;

    public ModeloFormController(ModeloService modeloService) {
        this.modeloService = modeloService;
    }

    public void configurar(Modelo modeloExistente, Runnable aoSalvar) {
        this.modeloEmEdicao = modeloExistente;
        this.aoSalvar = aoSalvar;

        if (modeloExistente != null) {
            lblTituloModal.setText("Editar Modelo");
            txtNome.setText(modeloExistente.getNome());
            txtDescricao.setText(modeloExistente.getDescricao());
        }
    }

    @FXML
    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();

            if (modeloEmEdicao == null) {
                Modelo novo = new Modelo(nome, descricao, true);
                modeloService.cadastrar(novo);
            } else {
                modeloEmEdicao.setNome(nome);
                modeloEmEdicao.setDescricao(descricao);
                modeloService.atualizar(modeloEmEdicao);
            }

            aoSalvar.run();
            fecharModal();

        } catch (IllegalArgumentException e) {
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