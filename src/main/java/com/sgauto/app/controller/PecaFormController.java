package com.sgauto.app.controller;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PecaFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtPrecoCusto;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private TextField txtModelo;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;


    private final EstoqueService estoqueService;
    private Peca pecaEmEdicao;
    private Runnable aoSalvar;

    public PecaFormController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    public void configurar(Peca pecaExistente, Runnable aoSalvar) {
        this.pecaEmEdicao = pecaExistente;
        this.aoSalvar = aoSalvar;

        if (pecaExistente != null) {
            lblTituloModal.setText("Editar Peça");
            txtCodigo.setText(pecaExistente.getCodigo());
            txtDescricao.setText(pecaExistente.getDescricao());
            txtPrecoCusto.setText(pecaExistente.getPrecoCusto().toString());
            txtPrecoVenda.setText(pecaExistente.getPrecoVenda().toString());
            txtQuantidade.setText(pecaExistente.getQuantidadeEstoque().toString());
            txtEstoqueMinimo.setText(pecaExistente.getEstoqueMinimo().toString());
            txtModelo.setText(pecaExistente.getModelo());
        }
    }

    @FXML
    private void salvar() {
        try {
            String codigo = txtCodigo.getText().trim();
            String descricao = txtDescricao.getText().trim();
            String modelo = txtModelo.getText().trim();
            BigDecimal precoCusto = new BigDecimal(txtPrecoCusto.getText().trim());
            BigDecimal precoVenda = new BigDecimal(txtPrecoVenda.getText().trim());
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());
            int estoqueMinimo = Integer.parseInt(txtEstoqueMinimo.getText().trim());

            if (codigo.isEmpty() || descricao.isEmpty()) {
                mostrarErro("Código e descrição são obrigatórios.");
                return;
            }

            if (pecaEmEdicao == null) {
                Peca novaPeca = new Peca(codigo, descricao, modelo, precoCusto, precoVenda, quantidade, estoqueMinimo);
                estoqueService.cadastrarPeca(novaPeca);
            } else {
                pecaEmEdicao.setCodigo(codigo);
                pecaEmEdicao.setDescricao(descricao);
                pecaEmEdicao.setPrecoCusto(precoCusto);
                pecaEmEdicao.setPrecoVenda(precoVenda);
                pecaEmEdicao.setQuantidadeEstoque(quantidade);
                pecaEmEdicao.setEstoqueMinimo(estoqueMinimo);
                pecaEmEdicao.setModelo(modelo);
                estoqueService.atualizar(pecaEmEdicao);
            }

            aoSalvar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Verifique se os campos numéricos foram preenchidos corretamente.");
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