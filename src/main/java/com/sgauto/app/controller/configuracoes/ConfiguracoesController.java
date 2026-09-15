package com.sgauto.app.controller.configuracoes;

import com.sgauto.app.enums.ConfigChave;
import com.sgauto.app.enums.ModoConferencia;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.BackupHistorico;
import com.sgauto.app.model.caixa.ConfiguracaoCaixa;
import com.sgauto.app.service.backup.BackupHistoricoService;
import com.sgauto.app.service.backup.BackupService;
import com.sgauto.app.service.ConfigSistemaService;
import com.sgauto.app.service.ConfiguracaoCaixaService;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ConfiguracoesController {

    // --- Elementos do Caixa ---
    @FXML private ToggleGroup grupoModo;
    @FXML private RadioButton radioObrigatoria;
    @FXML private RadioButton radioOpcional;
    @FXML private RadioButton radioSemConferencia;

    // --- Elementos do Backup ---
    @FXML private Label lblUltimoBackup;
    @FXML private CheckBox chkBackupAutomatico;
    @FXML private CheckBox chkBackupCaixa;
    @FXML private Spinner<Integer> spinIntervaloDias;
    @FXML private TextField txtPastaLocal;
    @FXML private TextField txtPastaNuvem;
    @FXML private TextField txtPastaPgDump;

    @FXML private Label lblMensagem;

    private final ConfiguracaoCaixaService configuracaoCaixaService;
    private final ConfigSistemaService configSistemaService;
    private final BackupHistoricoService backupHistoricoService;
    private final BackupService backupService;
    private final VerificaPermissaoUtil permissaoUtil;

    private ModoConferencia modoOriginal;

    public ConfiguracoesController(ConfiguracaoCaixaService configuracaoCaixaService,
                                   ConfigSistemaService configSistemaService,
                                   BackupHistoricoService backupHistoricoService, BackupService backupService,
                                   VerificaPermissaoUtil permissaoUtil) {
        this.configuracaoCaixaService = configuracaoCaixaService;
        this.configSistemaService = configSistemaService;
        this.backupHistoricoService = backupHistoricoService;
        this.backupService = backupService;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        carregarConfiguracoesCaixa();
        carregarConfiguracoesBackup();
        carregarHistoricoBackup();
    }

    private void carregarConfiguracoesCaixa() {
        ConfiguracaoCaixa config = configuracaoCaixaService.buscarConfiguracao();
        modoOriginal = config.getModoConferencia();

        switch (modoOriginal) {
            case OBRIGATORIA -> radioObrigatoria.setSelected(true);
            case OPCIONAL -> radioOpcional.setSelected(true);
            case SEM_CONFERENCIA -> radioSemConferencia.setSelected(true);
        }
    }

    private void carregarConfiguracoesBackup() {
        int intervalo = configSistemaService.obterIntervaloDiasBackup();
        spinIntervaloDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, intervalo));

        chkBackupAutomatico.setSelected(configSistemaService.isBackupAutomaticoAtivo());
        chkBackupCaixa.setSelected(configSistemaService.isBackupAposFechamentoCaixaAtivo());

        configSistemaService.obterPastaBackupLocal().ifPresent(txtPastaLocal::setText);
        configSistemaService.obterPastaBackupNuvem().ifPresent(txtPastaNuvem::setText);

        String pastaPgDump = configSistemaService.obterValor(ConfigChave.BACKUP_DIRETORIO_PG_DUMP);
        if (pastaPgDump != null) txtPastaPgDump.setText(pastaPgDump);
    }

    private void carregarHistoricoBackup() {
        Optional<BackupHistorico> ultimo = backupHistoricoService.obterUltimoBackup();
        if (ultimo.isPresent()) {
            String dataFormatada = ultimo.get().getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
            lblUltimoBackup.setText("Último backup com sucesso: " + dataFormatada);
            if (backupHistoricoService.backupEstaAtrasado()) {
                lblUltimoBackup.setStyle("-fx-text-fill: #c95d53; -fx-font-weight: bold;");
                lblUltimoBackup.setText(lblUltimoBackup.getText() + " (Atrasado!)");
            }
        } else {
            lblUltimoBackup.setText("Nenhum backup realizado com sucesso até o momento.");
            lblUltimoBackup.setStyle("-fx-text-fill: #c95d53; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void escolherPastaLocal() {
        selecionarDiretorio(txtPastaLocal);
    }

    @FXML
    private void escolherPastaNuvem() {
        selecionarDiretorio(txtPastaNuvem);
    }

    @FXML
    private void escolherPastaPgDump() {
        selecionarDiretorio(txtPastaPgDump);
    }


    private void selecionarDiretorio(TextField targetField) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecionar Pasta");

        if (!targetField.getText().isEmpty()) {
            File pastaAtual = new File(targetField.getText());
            if (pastaAtual.exists() && pastaAtual.isDirectory()) {
                chooser.setInitialDirectory(pastaAtual);
            }
        }

        Window window = targetField.getScene().getWindow();
        File pastaSelecionada = chooser.showDialog(window);

        if (pastaSelecionada != null) {
            targetField.setText(pastaSelecionada.getAbsolutePath());
        }
    }

    @FXML
    private void salvar() {
        if(!permissaoUtil.verificar(PermissaoChave.CONFIGURACOES_EDITAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        salvarCaixa();
        salvarBackup();

        mostrarMensagem("Configurações salvas com sucesso!");
    }

    @FXML
    private void exportarParaPendrive() {
        if(!permissaoUtil.verificar(PermissaoChave.CONFIGURACOES_EDITAR)) {
            ExibirMensagemBloqueioUtil.exibir();
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecione a unidade do Pendrive");

        Window window = lblMensagem.getScene().getWindow();
        File pastaSelecionada = chooser.showDialog(window);

        if (pastaSelecionada != null) {
            String caminhoPendrive = pastaSelecionada.getAbsolutePath();

            // Dá feedback imediato na tela
            mostrarMensagem("Gerando backup e copiando para o pendrive, aguarde...");

            // Roda em background para não travar a interface do JavaFX
            java.util.concurrent.CompletableFuture.runAsync(() -> {

                // Chama o motor de backup
                backupService.executarBackupManual(caminhoPendrive);

                // Como estamos em outra thread, usamos o Platform.runLater para voltar a mexer na tela
                javafx.application.Platform.runLater(() -> {
                    mostrarMensagem("Backup exportado para " + caminhoPendrive + " com sucesso!");
                    carregarHistoricoBackup(); // Atualiza a label mostrando que acabou de ser feito
                });
            });
        }
    }

    private void salvarCaixa() {
        ModoConferencia modoSelecionado = obterModoSelecionado();
        if (modoSelecionado != modoOriginal) {
            configuracaoCaixaService.atualizarModoConferencia(modoSelecionado);
            modoOriginal = modoSelecionado;
        }
    }

    private void salvarBackup() {
        configSistemaService.salvarValor(ConfigChave.BACKUP_AUTOMATICO_ATIVO, String.valueOf(chkBackupAutomatico.isSelected()));
        configSistemaService.salvarValor(ConfigChave.BACKUP_APOS_FECHAMENTO_CAIXA, String.valueOf(chkBackupCaixa.isSelected()));
        configSistemaService.salvarValor(ConfigChave.BACKUP_INTERVALO_DIAS, spinIntervaloDias.getValue().toString());
        configSistemaService.salvarValor(ConfigChave.BACKUP_PASTA_LOCAL, txtPastaLocal.getText());
        configSistemaService.salvarValor(ConfigChave.BACKUP_PASTA_NUVEM, txtPastaNuvem.getText());
        configSistemaService.salvarValor(ConfigChave.BACKUP_DIRETORIO_PG_DUMP, txtPastaPgDump.getText());
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