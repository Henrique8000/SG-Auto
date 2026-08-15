package com.sgauto.app.controller.configuracoes;

import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.ConfiguracaoCaixa;
import com.sgauto.app.service.ConfiguracaoCaixaService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracoesController {

    @FXML private ToggleGroup grupoModo;
    @FXML private RadioButton radioObrigatoria;
    @FXML private RadioButton radioOpcional;
    @FXML private RadioButton radioSemConferencia;
    @FXML private Label lblMensagem;

    private final ConfiguracaoCaixaService configuracaoCaixaService;
    private final VerificaPermissaoUtil permissaoUtil;
    private ModoConferencia modoOriginal;

    public ConfiguracoesController(ConfiguracaoCaixaService configuracaoCaixaService, VerificaPermissaoUtil permissaoUtil) {
        this.configuracaoCaixaService = configuracaoCaixaService;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        ConfiguracaoCaixa config = configuracaoCaixaService.buscarConfiguracao();
        modoOriginal = config.getModoConferencia();

        switch (modoOriginal) {
            case OBRIGATORIA -> radioObrigatoria.setSelected(true);
            case OPCIONAL -> radioOpcional.setSelected(true);
            case SEM_CONFERENCIA -> radioSemConferencia.setSelected(true);
        }
    }

    @FXML
    private void salvar() {
        if(!permissaoUtil.verificar(PermissaoChave.CONFIGURACOES_EDITAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        ModoConferencia modoSelecionado = obterModoSelecionado();

        if (modoSelecionado == modoOriginal) {
            mostrarMensagem("Nenhuma alteração foi feita.");
            return;
        }

        configuracaoCaixaService.atualizarModoConferencia(modoSelecionado);
        modoOriginal = modoSelecionado;
        mostrarMensagem("Configuração salva com sucesso.");
    }

    private ModoConferencia obterModoSelecionado() {
        if (radioObrigatoria.isSelected()) return ModoConferencia.OBRIGATORIA;
        if (radioOpcional.isSelected()) return ModoConferencia.OPCIONAL;
        return ModoConferencia.SEM_CONFERENCIA;
    }

    private void mostrarMensagem(String texto) {
        lblMensagem.setText(texto);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }
}