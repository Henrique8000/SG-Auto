package com.sgauto.app.controller.usuario;

import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.service.usuario.PerfilAcessoService;
import com.sgauto.app.service.usuario.PermissaoService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PerfilAcessoFormController {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private VBox boxPermissoes;
    @FXML private Label lblErro;
    @FXML private Button btnSalvar;

    private final PerfilAcessoService perfilAcessoService;
    private final PermissaoService permissaoService;

    private final Map<Long, CheckBox> checkBoxesPorPermissao = new LinkedHashMap<>();
    private PerfilAcesso perfilEmEdicao;
    private Runnable aoSalvar;

    public PerfilAcessoFormController(PerfilAcessoService perfilAcessoService, PermissaoService permissaoService) {
        this.perfilAcessoService = perfilAcessoService;
        this.permissaoService = permissaoService;
    }

    @FXML
    public void initialize() {
        Map<String, List<Permissao>> agrupadas = permissaoService.listarAgrupadasPorModulo();

        agrupadas.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Label tituloModulo = new Label(entry.getKey());
                    tituloModulo.getStyleClass().add("form-label");

                    VBox boxModulo = new VBox(6);
                    boxModulo.getChildren().add(tituloModulo);

                    entry.getValue().forEach(permissao -> {
                        CheckBox checkBox = new CheckBox(permissao.getDescricao());
                        checkBoxesPorPermissao.put(permissao.getId(), checkBox);
                        boxModulo.getChildren().add(checkBox);
                    });

                    boxPermissoes.getChildren().add(boxModulo);
                });
    }

    public void configurar(PerfilAcesso perfilExistente, Runnable aoSalvar) {
        this.perfilEmEdicao = perfilExistente;
        this.aoSalvar = aoSalvar;

        if (perfilExistente != null) {
            lblTituloModal.setText("Editar Perfil");
            txtNome.setText(perfilExistente.getNome());
            txtDescricao.setText(perfilExistente.getDescricao());

            Set<Long> idsMarcados = perfilExistente.getPermissoes().stream()
                    .map(Permissao::getId).collect(Collectors.toSet());
            checkBoxesPorPermissao.forEach((id, checkBox) -> checkBox.setSelected(idsMarcados.contains(id)));

            if (Boolean.TRUE.equals(perfilExistente.getProtegido())) {
                txtNome.setDisable(true);
                checkBoxesPorPermissao.values().forEach(cb -> cb.setDisable(true));
            }
        }
    }

    @FXML
    private void salvar() {
        try {
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();

            Set<Long> idsSelecionados = checkBoxesPorPermissao.entrySet().stream()
                    .filter(e -> e.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            if (perfilEmEdicao == null) {
                perfilAcessoService.cadastrar(nome, descricao, idsSelecionados);
            } else {
                perfilAcessoService.atualizar(perfilEmEdicao.getId(), nome, descricao, idsSelecionados);
            }

            aoSalvar.run();
            fecharModal();

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
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }
}