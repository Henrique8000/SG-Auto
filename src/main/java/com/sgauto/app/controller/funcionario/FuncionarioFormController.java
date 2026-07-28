package com.sgauto.app.controller.funcionario;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.enums.TipoContratoFuncionario;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.service.FuncionarioService;
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
    @FXML private TextField txtCelular;
    @FXML private TextField txtTelefoneFixo;
    @FXML private TextField txtEmail;
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
        cmbStatus.getSelectionModel().select(StatusFuncionario.ATIVO.getDescricao());
        dpDataAdmissao.setValue(java.time.LocalDate.now());
        txtCargaHoraria.setText("44");
        txtComissaoPercentual.setText("0");
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
            txtCelular.setText(funcionarioExistente.getCelular());
            txtTelefoneFixo.setText(funcionarioExistente.getTelefoneFixo());
            txtEmail.setText(funcionarioExistente.getEmail());
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
            txtObservacoes.setText(funcionarioExistente.getObservacoes());
        }
    }

    @FXML
    private void salvar() {
        try {
            Funcionario f = funcionarioEmEdicao != null ? funcionarioEmEdicao : new Funcionario();

            f.setMatricula(txtMatricula.getText().trim());
            f.setCpf(txtCpf.getText().trim());
            f.setNomeCompleto(txtNomeCompleto.getText().trim());
            f.setNomeSocial(vazioParaNull(txtNomeSocial.getText()));
            f.setCelular(txtCelular.getText().trim());
            f.setTelefoneFixo(vazioParaNull(txtTelefoneFixo.getText()));
            f.setEmail(vazioParaNull(txtEmail.getText()));

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

            f.setObservacoes(vazioParaNull(txtObservacoes.getText()));

            if (funcionarioEmEdicao == null) {
                funcionarioService.cadastrar(f);
            } else {
                funcionarioService.atualizar(f.getId(), f);
            }

            aoSalvar.run();
            fecharModal();

        } catch (NumberFormatException e) {
            mostrarErro("Verifique se os campos numéricos foram preenchidos corretamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private CargoFuncionario converterCargo(String descricao) {
        if (descricao == null) { mostrarErro("Selecione o cargo."); throw new IllegalArgumentException("Cargo obrigatório."); }
        return java.util.Arrays.stream(CargoFuncionario.values())
                .filter(c -> c.getDescricao().equals(descricao)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cargo inválido."));
    }

    private TipoContratoFuncionario converterTipoContrato(String descricao) {
        if (descricao == null) { throw new IllegalArgumentException("Selecione o tipo de contrato."); }
        return java.util.Arrays.stream(TipoContratoFuncionario.values())
                .filter(t -> t.getDescricao().equals(descricao)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de contrato inválido."));
    }

    private StatusFuncionario converterStatus(String descricao) {
        if (descricao == null) { throw new IllegalArgumentException("Selecione o status."); }
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