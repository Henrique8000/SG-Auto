package com.sgauto.app.controller.clientes;

import com.sgauto.app.model.Cliente;
import com.sgauto.app.service.ClienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class ModalSelecaoClienteController {

    private final ClienteService clienteService;

    @FXML private TextField txtBusca;
    @FXML private TableView<Cliente> tbClientes;

    private ObservableList<Cliente> dadosOriginais;
    private Cliente clienteSelecionado;

    public ModalSelecaoClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        // Carrega apenas os clientes ativos para o modal
        dadosOriginais = FXCollections.observableArrayList(clienteService.listarAtivos());

        // Configura a lista filtrável
        FilteredList<Cliente> dadosFiltrados = new FilteredList<>(dadosOriginais, p -> true);

        // Ouve as alterações na caixa de texto e aplica o filtro em tempo real
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            dadosFiltrados.setPredicate(cliente -> {
                // Se o texto estiver vazio, mostra todos
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String busca = newValue.toLowerCase().trim();

                // Verifica se o Nome, Documento ou Telefone contêm o termo pesquisado
                boolean bateNome = cliente.getNome() != null && cliente.getNome().toLowerCase().contains(busca);
                boolean bateDocumento = cliente.getDocumento() != null && cliente.getDocumento().contains(busca);
                boolean bateTelefone = cliente.getTelefone() != null && cliente.getTelefone().contains(busca);

                return bateNome || bateDocumento || bateTelefone;
            });
        });

        // Vincula a lista filtrada e ordenada à tabela
        SortedList<Cliente> dadosOrdenados = new SortedList<>(dadosFiltrados);
        dadosOrdenados.comparatorProperty().bind(tbClientes.comparatorProperty());
        tbClientes.setItems(dadosOrdenados);

        // Permite selecionar com um duplo clique na linha da tabela
        tbClientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tbClientes.getSelectionModel().getSelectedItem() != null) {
                selecionar();
            }
        });
    }

    @FXML
    public void selecionar() {
        clienteSelecionado = tbClientes.getSelectionModel().getSelectedItem();
        if (clienteSelecionado != null) {
            fecharModal();
        }
    }

    @FXML
    public void cancelar() {
        clienteSelecionado = null;
        fecharModal();
    }

    public Cliente getClienteSelecionado() {
        return clienteSelecionado;
    }

    private void fecharModal() {
        Stage stage = (Stage) txtBusca.getScene().getWindow();
        stage.close();
    }
}
