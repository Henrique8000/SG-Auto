package com.sgauto.app.controller.servicos;

import com.sgauto.app.model.Servico;
import com.sgauto.app.service.CategoriaService;
import com.sgauto.app.service.ServicoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ServicoFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtCodigo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private TextField txtValor;
    @FXML private TextField txtComissao;
    @FXML private TextField txtTempoEstimado;
    @FXML private TextField txtGarantiaDias;
    @FXML private TextArea txtObservacoes;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final ServicoService servicoService;
    private final CategoriaService categoriaService;
    private Servico servicoEmEdicao;
    private Runnable aoSalvar;

    public ServicoFormController(ServicoService servicoService, CategoriaService categoriaService) {
        this.servicoService = servicoService;
        this.categoriaService = categoriaService;
    }

    @FXML
    public void initialize() {
        var nomesCategorias = categoriaService.listarAtivas().stream()
                .map(c -> c.getNome())
                .toList();
        cmbCategoria.setItems(FXCollections.observableArrayList(nomesCategorias));
    }

    public void configurar(Servico servicoExistente, Runnable aoSalvar) {
        this.servicoEmEdicao = servicoExistente;
        this.aoSalvar = aoSalvar;

        if (servicoExistente != null) {
            lblTituloModal.setText("Editar Serviço");
            txtCodigo.setText(servicoExistente.getCodigo());
            cmbCategoria.setValue(servicoExistente.getCategoria());
            txtNome.setText(servicoExistente.getNome());
            txtDescricao.setText(servicoExistente.getDescricao());
            txtValor.setText(servicoExistente.getValor().toString());
            txtComissao.setText(servicoExistente.getComissaoPorcentagem().toString());
            txtTempoEstimado.setText(servicoExistente.getTempoEstimadoMinutos().toString());
            txtGarantiaDias.setText(servicoExistente.getGarantiaDias().toString());
            txtObservacoes.setText(servicoExistente.getObservacoesTecnicas());
        }
    }

    @FXML
    private void salvar() {
        try {
            String codigo = txtCodigo.getText().trim();
            String categoria = cmbCategoria.getValue();
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();
            BigDecimal valor = new BigDecimal(txtValor.getText().trim());
            BigDecimal comissao = new BigDecimal(txtComissao.getText().trim());
            Integer tempoEstimado = Integer.parseInt(txtTempoEstimado.getText().trim());
            Integer garantiaDias = Integer.parseInt(txtGarantiaDias.getText().trim());
            String observacoes = txtObservacoes.getText().trim();

            if (categoria == null) {
                mostrarErro("Selecione uma categoria.");
                return;
            }

            if (servicoEmEdicao == null) {
                Servico novo = new Servico(codigo, nome, categoria, descricao, valor,
                        tempoEstimado, garantiaDias, comissao, observacoes, true);
                servicoService.cadastrar(novo);
            } else {
                servicoEmEdicao.setCodigo(codigo);
                servicoEmEdicao.setCategoria(categoria);
                servicoEmEdicao.setNome(nome);
                servicoEmEdicao.setDescricao(descricao);
                servicoEmEdicao.setValor(valor);
                servicoEmEdicao.setComissaoPorcentagem(comissao);
                servicoEmEdicao.setTempoEstimadoMinutos(tempoEstimado);
                servicoEmEdicao.setGarantiaDias(garantiaDias);
                servicoEmEdicao.setObservacoesTecnicas(observacoes);
                servicoService.atualizar(servicoEmEdicao);
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