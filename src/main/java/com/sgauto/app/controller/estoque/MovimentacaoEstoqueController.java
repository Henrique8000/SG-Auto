package com.sgauto.app.controller.estoque;

import com.sgauto.app.model.Peca;
import com.sgauto.app.service.EstoqueService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MovimentacaoEstoqueController {

    @FXML private Label lblPeca;
    @FXML private ToggleButton btnEntrada;
    @FXML private ToggleButton btnSaida;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final EstoqueService estoqueService;
    private Peca peca;
    private Runnable aoConfirmar;

    public MovimentacaoEstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    public void configurar(Peca peca, Runnable aoConfirmar) {
        this.peca = peca;
        this.aoConfirmar = aoConfirmar;
        lblPeca.setText(peca.getCodigo() + " — " + peca.getDescricao() + " (estoque atual: " + peca.getQuantidadeEstoque() + ")");
    }

    @FXML
    private void confirmar() {
        try {
            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());

            if (btnEntrada.isSelected()) {
                estoqueService.darEntradaEstoque(peca.getId(), quantidade);
            } else {
                estoqueService.darSaidaEstoque(peca.getId(), quantidade);
            }

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