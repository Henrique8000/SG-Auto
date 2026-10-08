package com.sgauto.app.service.backup;

import com.sgauto.app.enums.backup.ConfigChave;
import com.sgauto.app.enums.backup.TipoBackup;
import com.sgauto.app.service.ConfigSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    private final ConfigSistemaService configSistemaService;
    private final BackupHistoricoService backupHistoricoService;

    // Busca automaticamente o usuário e senha do banco no seu application.properties
    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Nome do banco de dados
    private final String dbName = "sgauto";

    public BackupService(ConfigSistemaService configSistemaService, BackupHistoricoService backupHistoricoService) {
        this.configSistemaService = configSistemaService;
        this.backupHistoricoService = backupHistoricoService;
    }

    // =========================================================================
    // MÉTODOS PÚBLICOS (Portas de Entrada)
    // =========================================================================

    public void executarBackupPadrao(TipoBackup tipo) {
        log.info("Iniciando rotina de backup padrão. Tipo: {}", tipo);

        try {
            File arquivoSql = gerarDumpPostgreSQL();
            File arquivoZip = compactarParaZip(arquivoSql);

            Optional<String> pastaLocal = configSistemaService.obterPastaBackupLocal();
            Optional<String> pastaNuvem = configSistemaService.obterPastaBackupNuvem();

            if (pastaLocal.isEmpty() && pastaNuvem.isEmpty()) {
                throw new IllegalStateException("Nenhuma pasta de backup (Local ou Nuvem) foi configurada pelo cliente.");
            }

            // O ifPresent verifica sozinho: se tiver pasta local, ele copia; se tiver nuvem, ele copia também.
            pastaLocal.ifPresent(destino -> copiarParaDestino(arquivoZip, destino));
            pastaNuvem.ifPresent(destino -> copiarParaDestino(arquivoZip, destino));

            backupHistoricoService.registrarSucesso(tipo, "Pastas configuradas", arquivoZip.length());
            limparArquivosTemporarios(arquivoSql, arquivoZip);

            log.info("Backup padrão concluído com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao executar backup padrão", e);
            backupHistoricoService.registrarFalha(tipo, "Múltiplos", e.getMessage());
        }
    }

    public void executarBackupManual(String diretorioDestino) {
        log.info("Iniciando backup manual sob demanda. Destino: {}", diretorioDestino);

        try {
            File arquivoSql = gerarDumpPostgreSQL();
            File arquivoZip = compactarParaZip(arquivoSql);

            copiarParaDestino(arquivoZip, diretorioDestino);

            backupHistoricoService.registrarSucesso(TipoBackup.MANUAL, diretorioDestino, arquivoZip.length());
            limparArquivosTemporarios(arquivoSql, arquivoZip);

            log.info("Backup manual exportado com sucesso para o pendrive!");

        } catch (Exception e) {
            log.error("Erro ao executar backup manual para o pendrive", e);
            backupHistoricoService.registrarFalha(TipoBackup.MANUAL, diretorioDestino, e.getMessage());
        }
    }

    // =========================================================================
    // MÉTODOS PRIVADOS (A Linha de Montagem)
    // =========================================================================

    private File gerarDumpPostgreSQL() throws Exception {
        String pastaPgDump = configSistemaService.obterValor(ConfigChave.BACKUP_DIRETORIO_PG_DUMP);
        if (pastaPgDump == null || pastaPgDump.trim().isEmpty()) {
            throw new IllegalStateException("O diretório do PostgreSQL não foi configurado nas Configurações.");
        }

        String pgDumpExe = pastaPgDump + File.separator + "pg_dump.exe";
        File executavel = new File(pgDumpExe);
        if (!executavel.exists()) {
            throw new IllegalStateException("O arquivo pg_dump.exe não existe no caminho: " + pgDumpExe);
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File tempSql = new File(tempDir, "sgauto_backup_" + timestamp + ".sql");

        ProcessBuilder pb = new ProcessBuilder(
                pgDumpExe,
                "-h", "localhost",
                "-p", "5432",
                "-U", dbUser,
                "-F", "p",
                "-f", tempSql.getAbsolutePath(),
                dbName
        );

        // Passa a senha em background de forma segura
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        Process processo = pb.start();
        int exitCode = processo.waitFor();

        if (exitCode != 0) {
            String erro = new String(processo.getInputStream().readAllBytes());
            throw new RuntimeException("Falha no pg_dump. Código: " + exitCode + " | Detalhe: " + erro);
        }

        return tempSql;
    }

    private File compactarParaZip(File arquivoSql) throws Exception {
        File arquivoZip = new File(arquivoSql.getAbsolutePath().replace(".sql", ".zip"));

        try (FileOutputStream fos = new FileOutputStream(arquivoZip);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(arquivoSql)) {

            ZipEntry zipEntry = new ZipEntry(arquivoSql.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
        }
        return arquivoZip;
    }

    private void copiarParaDestino(File arquivoOrigem, String caminhoDestino) {
        try {
            File diretorio = new File(caminhoDestino);
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }

            File arquivoFinal = new File(diretorio, arquivoOrigem.getName());
            Files.copy(arquivoOrigem.toPath(), arquivoFinal.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Cópia salva em: {}", arquivoFinal.getAbsolutePath());
        } catch (IOException e) {
            // Lançar RuntimeException aqui faz com que o método principal caia no "catch"
            // e registre a falha corretamente no banco de dados.
            throw new RuntimeException("Erro ao copiar arquivo para o destino: " + caminhoDestino, e);
        }
    }

    private void limparArquivosTemporarios(File... arquivos) {
        for (File f : arquivos) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }
}