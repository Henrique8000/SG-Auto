package com.sgauto.app.controller.estoque.fornecedor;

import com.sgauto.app.controller.estoque.PecaFormController;
import com.sgauto.app.model.estoque.CategoriaFornecedor;
import com.sgauto.app.model.estoque.Fornecedor;
import com.sgauto.app.service.estoque.CategoriaFornecedorService;
import com.sgauto.app.service.estoque.FornecedorService;
import com.sgauto.app.util.CepUtil;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class FornecedorFormController {

    // Dados Básicos
    @FXML private ComboBox<String> cmbTipoPessoa;
    @FXML private TextField txtCpfCnpj;
    @FXML private TextField txtRazaoSocial;
    @FXML private TextField txtNomeFantasia;
    @FXML private ComboBox<String> cmbCategoria;

    // Contato
    @FXML private TextField txtNomeContato;
    @FXML private TextField txtCelular;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSite;

    // Fiscais e Endereço
    @FXML private TextField txtInscricaoEstadual;
    @FXML private TextField txtInscricaoMunicipal;
    @FXML private TextField txtPrazoEntregaDias;
    @FXML private TextField txtCep;
    @FXML private TextField txtLogradouro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtUf;
    @FXML private TextArea txtObservacoes;

    // Ações
    @FXML private Button btnSalvar;

    private Fornecedor fornecedorEdicao;
    private Runnable acaoPosSalvar;
    private final FornecedorService fornecedorService;
    private final ApplicationContext applicationContext;
    private final CategoriaFornecedorService categoriaService;

    public FornecedorFormController(FornecedorService fornecedorService, ApplicationContext applicationContext, CategoriaFornecedorService categoriaService) {
        this.fornecedorService = fornecedorService;
        this.categoriaService = categoriaService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        cmbTipoPessoa.getItems().addAll("PJ", "PF");
        cmbTipoPessoa.getSelectionModel().selectFirst();

        aplicarMascaraEFormatacao(); // Normalização UX (Bloqueia erros de digitação)
        configurarBuscaDeCep();
        carregarCategorias();

        cmbCategoria.getEditor().focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
            if (!estaFocado) {
                String textoDigitado = cmbCategoria.getEditor().getText();

                if (textoDigitado != null && !textoDigitado.isEmpty() && !cmbCategoria.getItems().contains(textoDigitado)) {
                    cmbCategoria.setValue(null); // Reseta o valor
                    cmbCategoria.getEditor().setText(""); // Limpa a tela
                }
            }
        });
    }

    // =========================================================================
    // MELHORIA DE UX: Restrições de Input em Tempo Real
    // =========================================================================
    private void aplicarMascaraEFormatacao() {
        // Apenas números e tamanhos máximos
        restringirApenasNumeros(txtCpfCnpj, 14);
        restringirApenasNumeros(txtCep, 8);
        restringirApenasNumeros(txtCelular, 11);
        restringirApenasNumeros(txtTelefone, 11);
        restringirApenasNumeros(txtInscricaoEstadual, 20);
        restringirApenasNumeros(txtInscricaoMunicipal, 20);
        restringirApenasNumeros(txtPrazoEntregaDias, 4);

        // Força o campo UF a ficar sempre MAIÚSCULO e com no máximo 2 letras
        txtUf.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().toUpperCase()); // Converte o que digitou pra maiúsculo
            if (change.getControlNewText().length() > 2) {
                return null; // Rejeita digitação se passar de 2 caracteres
            }
            return change;
        }));
    }

    // Utilitário interno para bloquear letras em TextFields
    private void restringirApenasNumeros(TextField field, int maxLenght) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.getText().matches("\\d*")) {
                return null; // Rejeita se não for número
            }
            if (change.getControlNewText().length() > maxLenght) {
                return null; // Rejeita se estourar o limite
            }
            return change;
        }));
    }

    private void configurarBuscaDeCep() {
        txtCep.textProperty().addListener((obs, valorAntigo, valorNovo) -> {
            if (valorNovo != null && valorNovo.length() == 8) {
                CepUtil.buscarCepAsync(valorNovo, endereco -> {
                    txtLogradouro.setText(endereco.logradouro());
                    txtBairro.setText(endereco.bairro());
                    txtCidade.setText(endereco.cidade());
                    txtUf.setText(endereco.uf());

                    if(txtNumero != null) {
                        txtNumero.requestFocus(); // Joga o cursor do mouse para o número
                    }
                });
            }
        });
    }

    public void configurar(Fornecedor fornecedor, Runnable acaoPosSalvar) {
        this.acaoPosSalvar = acaoPosSalvar;
        this.fornecedorEdicao = fornecedor;

        if (fornecedor != null) {
            preencherCampos();
        }
    }

    private void preencherCampos() {
        cmbTipoPessoa.setValue(fornecedorEdicao.getTipoPessoa());
        txtCpfCnpj.setText(fornecedorEdicao.getCpfCnpj());
        txtRazaoSocial.setText(fornecedorEdicao.getRazaoSocial());
        txtNomeFantasia.setText(fornecedorEdicao.getNomeFantasia());
        cmbCategoria.setValue(fornecedorEdicao.getCategoria());

        txtNomeContato.setText(fornecedorEdicao.getNomeContato());
        txtCelular.setText(fornecedorEdicao.getCelular());
        txtTelefone.setText(fornecedorEdicao.getTelefone());
        txtEmail.setText(fornecedorEdicao.getEmail());
        txtSite.setText(fornecedorEdicao.getSite());

        txtInscricaoEstadual.setText(fornecedorEdicao.getInscricaoEstadual());
        txtInscricaoMunicipal.setText(fornecedorEdicao.getInscricaoMunicipal());

        if (fornecedorEdicao.getPrazoEntregaDias() != null) {
            txtPrazoEntregaDias.setText(String.valueOf(fornecedorEdicao.getPrazoEntregaDias()));
        }

        txtCep.setText(fornecedorEdicao.getCep());
        txtLogradouro.setText(fornecedorEdicao.getLogradouro());
        txtNumero.setText(fornecedorEdicao.getNumero());
        txtComplemento.setText(fornecedorEdicao.getComplemento());
        txtBairro.setText(fornecedorEdicao.getBairro());
        txtCidade.setText(fornecedorEdicao.getCidade());
        txtUf.setText(fornecedorEdicao.getUf());

        txtObservacoes.setText(fornecedorEdicao.getObservacoes());
    }

    @FXML
    private void salvar() {
        try {
            Fornecedor f = (fornecedorEdicao == null) ? new Fornecedor() : fornecedorEdicao;

            f.setTipoPessoa(cmbTipoPessoa.getValue());
            f.setCpfCnpj(txtCpfCnpj.getText());
            f.setRazaoSocial(txtRazaoSocial.getText());
            f.setNomeFantasia(txtNomeFantasia.getText());
            String catSelecionada = cmbCategoria.getValue();
            if (catSelecionada == null || catSelecionada.trim().isEmpty()) {
                throw new IllegalArgumentException("A seleção da categoria é obrigatória.");
            }
            if (!cmbCategoria.getItems().contains(catSelecionada)) {
                throw new IllegalArgumentException("Por favor, selecione uma categoria válida na lista suspensa.");
            }

            f.setCategoria(catSelecionada);

            f.setNomeContato(txtNomeContato.getText());
            f.setCelular(txtCelular.getText());
            f.setTelefone(txtTelefone.getText());
            f.setEmail(txtEmail.getText());
            f.setSite(txtSite.getText());

            f.setInscricaoEstadual(txtInscricaoEstadual.getText());
            f.setInscricaoMunicipal(txtInscricaoMunicipal.getText());

            String prazoTexto = txtPrazoEntregaDias.getText();
            if (prazoTexto != null && !prazoTexto.trim().isEmpty()) {
                f.setPrazoEntregaDias(Integer.parseInt(prazoTexto));
            } else {
                f.setPrazoEntregaDias(null);
            }

            f.setCep(txtCep.getText());
            f.setLogradouro(txtLogradouro.getText());
            f.setNumero(txtNumero.getText());
            f.setComplemento(txtComplemento.getText());
            f.setBairro(txtBairro.getText());
            f.setCidade(txtCidade.getText());
            f.setUf(txtUf.getText());

            f.setObservacoes(txtObservacoes.getText());

            if (fornecedorEdicao == null) {
                fornecedorService.cadastrar(f);
            } else {
                fornecedorService.atualizar(f.getId(), f);
            }

            if (acaoPosSalvar != null) {
                acaoPosSalvar.run();
            }

            fecharModal();
        } catch (IllegalArgumentException | IllegalStateException e) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada(e.getMessage());
        }
    }

    @FXML
    private void fecharModal() {
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void abrirModalCategorias() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/estoque/categoria-fornecedor-modal.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CategoriaFornecedorModalController controller = loader.getController();

            controller.configurar(categoriaSelecionada -> {
                if (categoriaSelecionada != null) {
                    cmbCategoria.setValue(categoriaSelecionada.getNome());
                }
            });

            Stage modal = ModalUtil.abrir(
                    root,
                    "Categorias de Fornecedor",
                    cmbCategoria.getScene().getWindow()
            );

            modal.showAndWait();

        } catch (IOException e) {
            ExibirMensagemBloqueioUtil.exibirMensagemPersonalizada("Erro ao abrir a janela de categorias.");
            e.printStackTrace();
        }
    }

    private void carregarCategorias() {
        List<String> nomesCategorias = categoriaService.listarAtivas()
                .stream()
                .map(CategoriaFornecedor::getNome)
                .toList();

        cmbCategoria.getItems().addAll(nomesCategorias);
    }
}