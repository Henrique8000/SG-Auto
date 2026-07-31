package com.sgauto.app.controller.os;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.util.AutoCompleteComboBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OsAdicionarPecaModalController {

    @FXML private ComboBox<String> cmbPeca;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final OrdemServicoService ordemServicoService;
    private final EstoqueService estoqueService;
    private final ApplicationContext applicationContext;

    private Map<String, Long> mapaPecas = Map.of();
    private Long osId;
    private Runnable aoConfirmar;

    public OsAdicionarPecaModalController(OrdemServicoService ordemServicoService, EstoqueService estoqueService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.estoqueService = estoqueService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        List<Peca> pecasComEstoque = estoqueService.listarTodas().stream()
                .filter(p -> p.getQuantidadeEstoque() > 0)
                .toList();

        mapaPecas = pecasComEstoque.stream().collect(Collectors.toMap(
                p -> p.getCodigo() + " — " + p.getDescricao() + " (estoque: " + p.getQuantidadeEstoque() + ")",
                Peca::getId, (a, b) -> a));

        new AutoCompleteComboBox(cmbPeca).definirItens(List.copyOf(mapaPecas.keySet()));
    }

    public void configurar(Long osId, Runnable aoConfirmar) {
        this.osId = osId;
        this.aoConfirmar = aoConfirmar;
    }

    @FXML
    private void confirmar() {
        try {
            Long pecaId = mapaPecas.get(cmbPeca.getValue());
            if (pecaId == null) { mostrarErro("Selecione uma peça."); return; }

            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());

            ordemServicoService.adicionarPeca(osId, pecaId, quantidade);

            aoConfirmar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Informe uma quantidade numérica válida.");
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
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}