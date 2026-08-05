package com.sgauto.app.controller.patio;

import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.service.MotivoEstadiaService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MotivoEstadiaFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final MotivoEstadiaService motivoEstadiaService;
    private MotivoEstadia motivoEmEdicao;
    private Runnable aoSalvar;

    public MotivoEstadiaFormController(MotivoEstadiaService motivoEstadiaService) {
        this.motivoEstadiaService = motivoEstadiaService;
    }

    public void configurar(MotivoEstadia motivoExistente, Runnable aoSalvar) {
        this.motivoEmEdicao = motivoExistente;
        this.aoSalvar = aoSalvar;

        if (motivoExistente != null) {
            lblTituloModal.setText("Editar Motivo");
            txtNome.setText(motivoExistente.getNome());
            txtDescricao.setText(motivoExistente.getDescricao());
        }
    }

    @FXML
    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();

            if (motivoEmEdicao == null) {
                MotivoEstadia novo = new MotivoEstadia(nome, descricao, true, false);
                motivoEstadiaService.cadastrar(novo);
            } else {
                motivoEmEdicao.setNome(nome);
                motivoEmEdicao.setDescricao(descricao);
                motivoEstadiaService.atualizar(motivoEmEdicao);
            }

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