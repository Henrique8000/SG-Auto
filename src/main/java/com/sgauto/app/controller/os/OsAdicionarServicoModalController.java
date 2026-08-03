package com.sgauto.app.controller.os;

import com.sgauto.app.model.Servico;
import com.sgauto.app.service.OrdemServicoService;
import com.sgauto.app.service.ServicoService;
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
public class OsAdicionarServicoModalController {

    @FXML private ComboBox<String> cmbServico;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblErro;
    @FXML private Button btnConfirmar;

    private final OrdemServicoService ordemServicoService;
    private final ServicoService servicoService;
    private final ApplicationContext applicationContext;

    private Map<String, Long> mapaServicos = Map.of();
    private Long osId;
    private Runnable aoConfirmar;

    public OsAdicionarServicoModalController(OrdemServicoService ordemServicoService, ServicoService servicoService, ApplicationContext applicationContext) {
        this.ordemServicoService = ordemServicoService;
        this.servicoService = servicoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Para serviços, não precisamos validar estoque, apenas se estão ativos.
        List<Servico> servicosAtivos = servicoService.listarAtivos();

        mapaServicos = servicosAtivos.stream().collect(Collectors.toMap(
                s -> s.getCodigo() + " — " + s.getNome(),
                Servico::getId, (a, b) -> a));

        new AutoCompleteComboBox(cmbServico).definirItens(List.copyOf(mapaServicos.keySet()));
    }

    public void configurar(Long osId, Runnable aoConfirmar) {
        this.osId = osId;
        this.aoConfirmar = aoConfirmar;
    }

    @FXML
    private void confirmar() {
        try {
            Long servicoId = mapaServicos.get(cmbServico.getValue());
            if (servicoId == null) { mostrarErro("Selecione um serviço."); return; }

            int quantidade = Integer.parseInt(txtQuantidade.getText().trim());

            ordemServicoService.adicionarServico(osId, servicoId, quantidade);

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