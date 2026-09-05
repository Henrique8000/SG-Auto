package com.sgauto.app.controller.usuario;

import com.sgauto.app.controller.PaginacaoController;
import com.sgauto.app.dto.usuario.FiltroPermissaoDTO;
import com.sgauto.app.model.usuario.Permissao;
import com.sgauto.app.service.usuario.PermissaoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissaoController {

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroModulo;
    @FXML private TableView<Permissao> tabelaPermissoes;
    @FXML private TableColumn<Permissao, String> colModulo;
    @FXML private TableColumn<Permissao, String> colChave;
    @FXML private TableColumn<Permissao, String> colDescricao;
    @FXML private PaginacaoController paginacaoController;

    private final PermissaoService permissaoService;
    private final ObservableList<Permissao> permissoesExibidas = FXCollections.observableArrayList();

    public PermissaoController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @FXML
    public void initialize() {
        colModulo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getModulo()));
        colChave.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getChave()));
        colDescricao.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescricao()));
        tabelaPermissoes.setItems(permissoesExibidas);

        List<String> modulos = permissaoService.listarAgrupadasPorModulo().keySet().stream().sorted().toList();
        List<String> opcoes = new java.util.ArrayList<>();
        opcoes.add("Todos os módulos");
        opcoes.addAll(modulos);
        cmbFiltroModulo.setItems(FXCollections.observableArrayList(opcoes));
        cmbFiltroModulo.getSelectionModel().selectFirst();

        paginacaoController.configurar(this::carregarPagina, tamanho -> carregarPagina(0));

        txtBusca.textProperty().addListener((obs, a, n) -> reiniciarBusca());
        cmbFiltroModulo.valueProperty().addListener((obs, a, n) -> reiniciarBusca());

        carregarPagina(0);
    }

    private void reiniciarBusca() {
        paginacaoController.resetarPagina();
        carregarPagina(0);
    }

    private void carregarPagina(int pagina) {
        String moduloSelecionado = cmbFiltroModulo.getValue();
        String modulo = (moduloSelecionado == null || moduloSelecionado.equals("Todos os módulos")) ? null : moduloSelecionado;

        FiltroPermissaoDTO filtro = new FiltroPermissaoDTO(txtBusca.getText(), modulo);
        PageRequest pageRequest = PageRequest.of(pagina, paginacaoController.getTamanhoPagina(),
                Sort.by(Sort.Direction.ASC, "modulo").and(Sort.by(Sort.Direction.ASC, "chave")));

        Page<Permissao> resultado = permissaoService.buscar(filtro, pageRequest);

        permissoesExibidas.setAll(resultado.getContent());
        paginacaoController.atualizar(resultado);
    }
}