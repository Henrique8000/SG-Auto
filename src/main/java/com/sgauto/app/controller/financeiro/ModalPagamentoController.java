package com.sgauto.app.controller.financeiro;

import com.sgauto.app.dto.financeiro.RequisicaoContaReceberDTO;
import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.service.ClienteService;
import com.sgauto.app.service.financeiro.ContaReceberService;
import com.sgauto.app.util.SelecaoClienteUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class ModalPagamentoController {

    private final ContaReceberService contaReceberService;
    private final ClienteService clienteService;
    private final SelecaoClienteUtil selecaoClienteUtil;

    // Componentes FXML Novos (Cliente)
    @FXML private TextField txtNomeCliente;
    @FXML private Button btnBuscarCliente;
    @FXML private Button btnRemoverCliente;

    // Componentes FXML (Mantidos)
    @FXML private TextField txtValorTotal;
    @FXML private TextField txtValorEntrada;
    @FXML private ComboBox<FormaPagamento> cbFormaPagamentoEntrada;
    @FXML private Spinner<Integer> spnParcelas;
    @FXML private Spinner<Integer> spnIntervaloDias;
    @FXML private DatePicker dpVencimentoInicial;

    // Componentes FXML Novos (1ª Parcela)
    @FXML private CheckBox chkPrimeiraAVista;
    @FXML private VBox boxFormaPgtoPrimeira;
    @FXML private ComboBox<FormaPagamento> cbFormaPagamentoPrimeiraParcela;

    // Dados Ocultos
    private BigDecimal valorTotalOriginal;
    private Long clienteIdSelecionado;
    private Long ordemServicoId;
    private Long categoriaFinanceiraId;
    private String descricao;

    // Injeção de dependência dupla
    public ModalPagamentoController(ContaReceberService contaReceberService, ClienteService clienteService, SelecaoClienteUtil selecaoClienteUtil) {
        this.contaReceberService = contaReceberService;
        this.clienteService = clienteService;
        this.selecaoClienteUtil = selecaoClienteUtil;
    }

    @FXML
    public void initialize() {
        // Inicializa Spinners
        spnParcelas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 1));
        spnIntervaloDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));

        // Inicializa ComboBoxes
        cbFormaPagamentoEntrada.getItems().setAll(FormaPagamento.values());
        cbFormaPagamentoEntrada.setDisable(true);

        cbFormaPagamentoPrimeiraParcela.getItems().setAll(FormaPagamento.values());

        dpVencimentoInicial.setValue(LocalDate.now());

        // Listener reativo: Habilita Forma de Pagamento se Valor Entrada > 0
        txtValorEntrada.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    cbFormaPagamentoEntrada.setDisable(true);
                    return;
                }
                String formatado = newValue.replace(",", ".");
                BigDecimal entrada = new BigDecimal(formatado);
                cbFormaPagamentoEntrada.setDisable(entrada.compareTo(BigDecimal.ZERO) <= 0);
            } catch (NumberFormatException e) {
                cbFormaPagamentoEntrada.setDisable(true);
            }
        });

        // Listener reativo: Mostra/Oculta forma de pagamento da 1ª parcela
        chkPrimeiraAVista.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            boxFormaPgtoPrimeira.setVisible(isSelected);
            boxFormaPgtoPrimeira.setManaged(isSelected);
            if (!isSelected) {
                cbFormaPagamentoPrimeiraParcela.setValue(null); // Limpa ao ocultar
            }
        });
    }

    public void configurarPagamento(BigDecimal valorTotal, Long clienteId, Long osId, Long categoriaId, String descricao) {
        this.valorTotalOriginal = valorTotal;
        this.txtValorTotal.setText(valorTotal.toString());
        this.ordemServicoId = osId;
        this.categoriaFinanceiraId = categoriaId;
        this.descricao = descricao;

        if (osId != null && clienteId != null) {
            vincularCliente(clienteId);
            btnBuscarCliente.setVisible(false);
            btnBuscarCliente.setManaged(false);
            btnRemoverCliente.setVisible(false);
            btnRemoverCliente.setManaged(false);
        } else {
            if (clienteId != null) {
                vincularCliente(clienteId);
            } else {
                removerCliente();
            }
        }
    }

    @FXML
    public void abrirBuscaCliente() {
        Cliente clienteEscolhido = selecaoClienteUtil.abrirModalSelecao();
        if (clienteEscolhido != null) {
            vincularCliente(clienteEscolhido.getId());
        }
    }

    @FXML
    public void removerCliente() {
        this.clienteIdSelecionado = null;
        if (this.txtNomeCliente != null) {
            this.txtNomeCliente.clear();
        }
        if (this.btnRemoverCliente != null) {
            this.btnRemoverCliente.setVisible(false);
            this.btnRemoverCliente.setManaged(false);
        }
    }

    private void vincularCliente(Long id) {
        try {
            Cliente cliente = clienteService.buscarPorId(id);
            this.clienteIdSelecionado = cliente.getId();

            if (this.txtNomeCliente != null) {
                this.txtNomeCliente.setText(cliente.getNome());
            }

            if (this.ordemServicoId == null && this.btnRemoverCliente != null) {
                this.btnRemoverCliente.setVisible(true);
                this.btnRemoverCliente.setManaged(true);
            }
        } catch (Exception e) {
            mostrarErro("Erro ao carregar dados do cliente: " + e.getMessage());
        }
    }

    @FXML
    public void confirmar() {
        try {
            RequisicaoContaReceberDTO dto = new RequisicaoContaReceberDTO();

            dto.setValorTotal(valorTotalOriginal);
            dto.setClienteId(clienteIdSelecionado);
            dto.setOrdemServicoId(ordemServicoId);
            dto.setCategoriaFinanceiraId(categoriaFinanceiraId);
            dto.setDescricao(descricao);

            // Validação de Entrada
            String entradaTexto = txtValorEntrada.getText() != null ? txtValorEntrada.getText().replace(",", ".") : "0";
            BigDecimal entrada = entradaTexto.isEmpty() ? BigDecimal.ZERO : new BigDecimal(entradaTexto);

            if (entrada.compareTo(valorTotalOriginal) > 0) {
                throw new IllegalArgumentException("O valor de entrada não pode ser maior que o valor total.");
            }

            dto.setValorEntrada(entrada);

            if (entrada.compareTo(BigDecimal.ZERO) > 0) {
                if (cbFormaPagamentoEntrada.getValue() == null) {
                    throw new IllegalArgumentException("Selecione a forma de pagamento da entrada.");
                }
                dto.setFormaPagamentoEntrada(cbFormaPagamentoEntrada.getValue());
            }

            // Validação de Parcelas e Vencimento
            dto.setQuantidadeParcelas(spnParcelas.getValue());
            dto.setIntervaloDias(spnIntervaloDias.getValue());
            dto.setDataVencimentoInicial(dpVencimentoInicial.getValue());

            // Validação da 1ª Parcela à Vista
            boolean primeiraAVista = chkPrimeiraAVista.isSelected();
            dto.setPrimeiraParcelaAVista(primeiraAVista);

            if (primeiraAVista) {
                if (cbFormaPagamentoPrimeiraParcela.getValue() == null) {
                    throw new IllegalArgumentException("Selecione a forma de pagamento da 1ª parcela à vista.");
                }
                dto.setFormaPagamentoPrimeiraParcela(cbFormaPagamentoPrimeiraParcela.getValue());
            }

            contaReceberService.cadastrarDiretoPeloSistema(dto);

            mostrarSucesso("Pagamento processado com sucesso!");
            fecharModal();

        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro interno ao processar pagamento: " + e.getMessage());
        }
    }

    @FXML
    public void cancelar() {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) txtValorTotal.getScene().getWindow();
        stage.close();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validação");
        alert.setHeaderText("Atenção");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}