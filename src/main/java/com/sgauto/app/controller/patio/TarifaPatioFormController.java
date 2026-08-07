package com.sgauto.app.controller.patio;

import com.sgauto.app.enums.CategoriaVeiculoPatio;
import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.service.TabelaPrecoPatioService;
import com.sgauto.app.util.NumeroUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TarifaPatioFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtDescricao;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtValorDiaria;
    @FXML private TextField txtDiasCarencia;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final TabelaPrecoPatioService tabelaPrecoPatioService;
    private TabelaPrecoPatio tarifaEmEdicao;
    private Runnable aoSalvar;

    private static final Map<String, CategoriaVeiculoPatio> CATEGORIAS = new LinkedHashMap<>();
    static {
        CATEGORIAS.put("Moto", CategoriaVeiculoPatio.MOTO);
        CATEGORIAS.put("Passeio", CategoriaVeiculoPatio.PASSEIO);
        CATEGORIAS.put("SUV/Caminhonete", CategoriaVeiculoPatio.SUV_CAMINHONETE);
        CATEGORIAS.put("Pesado", CategoriaVeiculoPatio.PESADO);
        CATEGORIAS.put("Outros", CategoriaVeiculoPatio.OUTROS);
    }

    public TarifaPatioFormController(TabelaPrecoPatioService tabelaPrecoPatioService) {
        this.tabelaPrecoPatioService = tabelaPrecoPatioService;
    }

    @FXML
    public void initialize() {
        cmbCategoria.setItems(FXCollections.observableArrayList(CATEGORIAS.keySet()));
    }

    public void configurar(TabelaPrecoPatio tarifaExistente, Runnable aoSalvar) {
        this.tarifaEmEdicao = tarifaExistente;
        this.aoSalvar = aoSalvar;

        if (tarifaExistente != null) {
            lblTituloModal.setText("Editar Tarifa");
            txtDescricao.setText(tarifaExistente.getDescricao());
            txtValorDiaria.setText(tarifaExistente.getValorDiaria().toString());
            txtDiasCarencia.setText(String.valueOf(tarifaExistente.getDiasCarencia()));
            CATEGORIAS.entrySet().stream()
                    .filter(e -> e.getValue() == tarifaExistente.getCategoria())
                    .findFirst()
                    .ifPresent(e -> cmbCategoria.setValue(e.getKey()));
        }
    }

    @FXML
    private void salvar() {
        try {
            String descricao = txtDescricao.getText().trim();
            CategoriaVeiculoPatio categoria = CATEGORIAS.get(cmbCategoria.getValue());
            BigDecimal valorDiaria = NumeroUtil.parseValorMonetario(txtValorDiaria.getText());
            int diasCarencia = Integer.parseInt(txtDiasCarencia.getText().trim());

            if (tarifaEmEdicao == null) {
                TabelaPrecoPatio nova = new TabelaPrecoPatio(descricao, categoria, valorDiaria, diasCarencia, true);
                tabelaPrecoPatioService.cadastrar(nova);
            } else {
                tarifaEmEdicao.setDescricao(descricao);
                tarifaEmEdicao.setCategoria(categoria);
                tarifaEmEdicao.setValorDiaria(valorDiaria);
                tarifaEmEdicao.setDiasCarencia(diasCarencia);
                tabelaPrecoPatioService.atualizar(tarifaEmEdicao);
            }

            aoSalvar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Valor da diária e dias de carência precisam ser números válidos.");
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