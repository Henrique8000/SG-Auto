package com.sgauto.app.controller.servicos;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.service.CategoriaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class CategoriaFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNome;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final CategoriaService categoriaService;
    private Categoria categoriaEmEdicao;
    private Runnable aoSalvar;

    public CategoriaFormController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(
                "MECÂNICA",
                "ESTÉTICA",
                "ELÉTRICA",
                "GEOMETRIA",
                "GERAL"
        ));
    }

    public void configurar(Categoria categoriaExistente, Runnable aoSalvar) {
        this.categoriaEmEdicao = categoriaExistente;
        this.aoSalvar = aoSalvar;

        if (categoriaExistente != null) {
            lblTituloModal.setText("Editar Categoria");
            txtNome.setText(categoriaExistente.getNome());
            cmbTipo.setValue(categoriaExistente.getTipo());
            txtDescricao.setText(categoriaExistente.getDescricao());
        }
    }

    @FXML
    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String tipo = cmbTipo.getValue();
            String descricao = txtDescricao.getText().trim();

            if (categoriaEmEdicao == null) {
                Categoria nova = new Categoria(nome, descricao, tipo, true);
                categoriaService.cadastrar(nova);
            } else {
                categoriaEmEdicao.setNome(nome);
                categoriaEmEdicao.setTipo(tipo);
                categoriaEmEdicao.setDescricao(descricao);
                categoriaService.atualizar(categoriaEmEdicao);
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