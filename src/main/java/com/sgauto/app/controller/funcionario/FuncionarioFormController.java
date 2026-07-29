package com.sgauto.app.controller.funcionario;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.enums.TipoContratoFuncionario;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.service.FuncionarioService;
import com.sgauto.app.util.CepUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FuncionarioFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtCpf;
    @FXML private TextField txtNomeCompleto;
    @FXML private TextField txtNomeSocial;
    @FXML private TextField txtRg;
    @FXML private DatePicker dpDataNascimento;
    @FXML private TextField txtGenero;
    @FXML private TextField txtCelular;
    @FXML private TextField txtTelefoneFixo;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCep;
    @FXML private TextField txtEstado;
    @FXML private TextField txtLogradouro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private ComboBox<String> cmbTipoContrato;
    @FXML private TextField txtEspecialidade;
    @FXML private DatePicker dpDataAdmissao;
    @FXML private DatePicker dpDataDemissao;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextField txtCargaHoraria;
    @FXML private CheckBox chkExibeEmOs;
    @FXML private TextField txtSalarioBase;
    @FXML private TextField txtCustoHora;
    @FXML private TextField txtComissaoPercentual;
    @FXML private TextField txtNumeroCnh;
    @FXML private TextField txtCategoriaCnh;
    @FXML private DatePicker dpValidadeCnh;
    @FXML private TextField txtFotoUrl;
    @FXML private TextArea txtObservacoes;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final FuncionarioService funcionarioService;
    private Funcionario funcionarioEmEdicao;
    private Runnable aoSalvar;

    public FuncionarioFormController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @FXML
    public void initialize() {
        cmbCargo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(CargoFuncionario.values()).map(CargoFuncionario::getDescricao).toList()));
        cmbTipoContrato.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(TipoContratoFuncionario.values()).map(TipoContratoFuncionario::getDescricao).toList()));
        cmbStatus.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(StatusFuncionario.values()).map(StatusFuncionario::getDescricao).toList()));

        chkExibeEmOs.setSelected(true);
        cmbTipoContrato.getSelectionModel().select(TipoContratoFuncionario.CLT.getDescricao());
        cmbStatus.getSelectionModel().select(StatusFuncionario.ATIVO.getDescricao());
        dpDataAdmissao.setValue(java.time.LocalDate.now());
        txtCargaHoraria.setText("44");
        txtComissaoPercentual.setText("0");

        txtCep.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.replaceAll("[^0-9]", "").length() == 8) {
                CepUtil.buscarCepAsync(newVal, endereco -> {
                    if (txtLogradouro.getText().isEmpty()) txtLogradouro.setText(endereco.logradouro());
                    if (txtBairro.getText().isEmpty()) txtBairro.setText(endereco.bairro());
                    if (txtCidade.getText().isEmpty()) txtCidade.setText(endereco.cidade());
                    if (txtEstado.getText().isEmpty()) txtEstado.setText(endereco.uf().toUpperCase());
                });
            }
        });
    }

    public void configurar(Funcionario funcionarioExistente, Runnable aoSalvar) {
        this.funcionarioEmEdicao = funcionarioExistente;
        this.aoSalvar = aoSalvar;

        if (funcionarioExistente != null) {
            lblTituloModal.setText("Editar Funcionário");
            txtMatricula.setText(funcionarioExistente.getMatricula());
            txtCpf.setText(funcionarioExistente.getCpf());
            txtNomeCompleto.setText(funcionarioExistente.getNomeCompleto());
            txtNomeSocial.setText(funcionarioExistente.getNomeSocial());
            txtRg.setText(funcionarioExistente.getRg());
            dpDataNascimento.setValue(funcionarioExistente.getDataNascimento());
            txtGenero.setText(funcionarioExistente.getGenero());
            txtCelular.setText(funcionarioExistente.getCelular());
            txtTelefoneFixo.setText(funcionarioExistente.getTelefoneFixo());
            txtEmail.setText(funcionarioExistente.getEmail());
            txtCep.setText(funcionarioExistente.getCep());
            txtEstado.setText(funcionarioExistente.getEstado());
            txtLogradouro.setText(funcionarioExistente.getLogradouro());
            txtNumero.setText(funcionarioExistente.getNumero());
            txtComplemento.setText(funcionarioExistente.getComplemento());
            txtBairro.setText(funcionarioExistente.getBairro());
            txtCidade.setText(funcionarioExistente.getCidade());
            if (funcionarioExistente.getCargo() != null) cmbCargo.setValue(funcionarioExistente.getCargo().getDescricao());
            if (funcionarioExistente.getTipoContrato() != null) cmbTipoContrato.setValue(funcionarioExistente.getTipoContrato().getDescricao());
            txtEspecialidade.setText(funcionarioExistente.getEspecialidade());
            dpDataAdmissao.setValue(funcionarioExistente.getDataAdmissao());
            dpDataDemissao.setValue(funcionarioExistente.getDataDemissao());
            if (funcionarioExistente.getStatus() != null) cmbStatus.setValue(funcionarioExistente.getStatus().getDescricao());
            if (funcionarioExistente.getCargaHorariaSemanal() != null) txtCargaHoraria.setText(String.valueOf(funcionarioExistente.getCargaHorariaSemanal()));
            chkExibeEmOs.setSelected(Boolean.TRUE.equals(funcionarioExistente.getExibeEmOs()));
            if (funcionarioExistente.getSalarioBase() != null) txtSalarioBase.setText(funcionarioExistente.getSalarioBase().toString());
            if (funcionarioExistente.getCustoHora() != null) txtCustoHora.setText(funcionarioExistente.getCustoHora().toString());
            if (funcionarioExistente.getComissaoPercentual() != null) txtComissaoPercentual.setText(funcionarioExistente.getComissaoPercentual().toString());
            txtNumeroCnh.setText(funcionarioExistente.getNumeroCnh());
            txtCategoriaCnh.setText(funcionarioExistente.getCategoriaCnh());
            dpValidadeCnh.setValue(funcionarioExistente.getValidadeCnh());
            txtFotoUrl.setText(funcionarioExistente.getFotoUrl());
            txtObservacoes.setText(funcionarioExistente.getObservacoes());
        }
    }

    @FXML
    private void salvar() {
        try {
            validarCampos();

            Funcionario f = funcionarioEmEdicao != null ? funcionarioEmEdicao : new Funcionario();

            f.setMatricula(txtMatricula.getText().trim());
            f.setCpf(txtCpf.getText().replaceAll("[^0-9]", "")); // Salva apenas números
            f.setNomeCompleto(txtNomeCompleto.getText().trim());
            f.setNomeSocial(vazioParaNull(txtNomeSocial.getText()));
            f.setRg(vazioParaNull(txtRg.getText()));
            f.setDataNascimento(dpDataNascimento.getValue());
            f.setGenero(vazioParaNull(txtGenero.getText()));
            f.setCelular(txtCelular.getText().trim());
            f.setTelefoneFixo(vazioParaNull(txtTelefoneFixo.getText()));
            f.setEmail(vazioParaNull(txtEmail.getText()));

            f.setCep(vazioParaNull(txtCep.getText()));
            f.setEstado(vazioParaNullMaiusculo(txtEstado.getText()));
            f.setLogradouro(vazioParaNull(txtLogradouro.getText()));
            f.setNumero(vazioParaNull(txtNumero.getText()));
            f.setComplemento(vazioParaNull(txtComplemento.getText()));
            f.setBairro(vazioParaNull(txtBairro.getText()));
            f.setCidade(vazioParaNull(txtCidade.getText()));

            f.setCargo(converterCargo(cmbCargo.getValue()));
            f.setTipoContrato(converterTipoContrato(cmbTipoContrato.getValue()));
            f.setEspecialidade(vazioParaNull(txtEspecialidade.getText()));

            f.setDataAdmissao(dpDataAdmissao.getValue());
            f.setDataDemissao(dpDataDemissao.getValue());

            f.setStatus(converterStatus(cmbStatus.getValue()));
            f.setCargaHorariaSemanal(Integer.parseInt(txtCargaHoraria.getText().trim()));
            f.setExibeEmOs(chkExibeEmOs.isSelected());

            f.setSalarioBase(parseBigDecimalOuNull(txtSalarioBase.getText()));
            f.setCustoHora(parseBigDecimalOuNull(txtCustoHora.getText()));
            f.setComissaoPercentual(new BigDecimal(txtComissaoPercentual.getText().trim()));

            f.setNumeroCnh(vazioParaNull(txtNumeroCnh.getText()));
            f.setCategoriaCnh(vazioParaNull(txtCategoriaCnh.getText()));
            f.setValidadeCnh(dpValidadeCnh.getValue());

            f.setFotoUrl(vazioParaNull(txtFotoUrl.getText()));
            f.setObservacoes(vazioParaNull(txtObservacoes.getText()));

            if (funcionarioEmEdicao == null) {
                funcionarioService.cadastrar(f);
            } else {
                funcionarioService.atualizar(f.getId(), f);
            }

            aoSalvar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Verifique os campos numéricos. Salário, Custo/Hora, Carga Horária e Comissão devem conter números válidos (utilize ponto para decimais).");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }


    private void validarCampos() {
        if (estaVazio(txtMatricula)) {
            throw new IllegalArgumentException("A Matrícula do funcionário é obrigatória.");
        }
        if (estaVazio(txtCpf)) {
            throw new IllegalArgumentException("O CPF do funcionário é obrigatório.");
        }
        if (!isCpfValido(txtCpf.getText())) {
            throw new IllegalArgumentException("O CPF informado é inválido. Verifique os números digitados.");
        }
        if (estaVazio(txtNomeCompleto)) {
            throw new IllegalArgumentException("O Nome Completo é obrigatório.");
        }

        if (estaVazio(txtCelular)) {
            throw new IllegalArgumentException("O número de Celular é obrigatório.");
        }

        if (!estaVazio(txtEstado) && txtEstado.getText().trim().length() != 2) {
            throw new IllegalArgumentException("O Estado (UF) deve conter exatamente 2 letras (Ex: SP, RJ).");
        }

        if (cmbCargo.getValue() == null || cmbCargo.getValue().isBlank()) {
            throw new IllegalArgumentException("Selecione o Cargo do funcionário.");
        }
        if (cmbTipoContrato.getValue() == null || cmbTipoContrato.getValue().isBlank()) {
            throw new IllegalArgumentException("Selecione o Tipo de Contrato do funcionário.");
        }
        if (dpDataAdmissao.getValue() == null) {
            throw new IllegalArgumentException("A Data de Admissão é obrigatória.");
        }
        if (cmbStatus.getValue() == null || cmbStatus.getValue().isBlank()) {
            throw new IllegalArgumentException("Selecione o Status do funcionário.");
        }
        if (estaVazio(txtCargaHoraria)) {
            throw new IllegalArgumentException("A Carga Horária Semanal é obrigatória.");
        }
        if (estaVazio(txtComissaoPercentual)) {
            throw new IllegalArgumentException("O percentual de Comissão é obrigatório (informe 0 se não houver).");
        }

        // Validações Lógicas de Datas
        if (dpDataNascimento.getValue() != null && dpDataNascimento.getValue().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("A Data de Nascimento não pode ser no futuro.");
        }
        if (dpDataDemissao.getValue() != null && dpDataDemissao.getValue().isBefore(dpDataAdmissao.getValue())) {
            throw new IllegalArgumentException("A Data de Demissão não pode ser anterior à Data de Admissão.");
        }
    }

    private boolean estaVazio(TextField campo) {
        return campo == null || campo.getText() == null || campo.getText().trim().isEmpty();
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null) return false;
        String limpo = cpf.replaceAll("[^0-9]", "");

        // Deve ter 11 dígitos e não ser uma sequência repetida (ex: 111.111.111-11)
        if (limpo.length() != 11 || limpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (limpo.charAt(i) - '0') * (10 - i);
            }
            int resto = 11 - (soma % 11);
            int digito1 = (resto > 9) ? 0 : resto;
            if ((limpo.charAt(9) - '0') != digito1) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (limpo.charAt(i) - '0') * (11 - i);
            }
            resto = 11 - (soma % 11);
            int digito2 = (resto > 9) ? 0 : resto;
            return (limpo.charAt(10) - '0') == digito2;

        } catch (Exception e) {
            return false;
        }
    }

    private CargoFuncionario converterCargo(String descricao) {
        if (descricao == null) throw new IllegalArgumentException("Selecione o cargo.");
        return java.util.Arrays.stream(CargoFuncionario.values())
                .filter(c -> c.getDescricao().equals(descricao)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cargo inválido."));
    }

    private TipoContratoFuncionario converterTipoContrato(String descricao) {
        if (descricao == null) throw new IllegalArgumentException("Selecione o tipo de contrato.");
        return java.util.Arrays.stream(TipoContratoFuncionario.values())
                .filter(t -> t.getDescricao().equals(descricao)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de contrato inválido."));
    }

    private StatusFuncionario converterStatus(String descricao) {
        if (descricao == null) throw new IllegalArgumentException("Selecione o status.");
        return java.util.Arrays.stream(StatusFuncionario.values())
                .filter(s -> s.getDescricao().equals(descricao)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status inválido."));
    }

    private BigDecimal parseBigDecimalOuNull(String texto) {
        return (texto == null || texto.isBlank()) ? null : new BigDecimal(texto.trim());
    }

    private String vazioParaNull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private String vazioParaNullMaiusculo(String texto) {
        String valor = vazioParaNull(texto);
        return valor != null ? valor.toUpperCase() : null;
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