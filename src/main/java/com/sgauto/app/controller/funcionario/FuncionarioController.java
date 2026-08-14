package com.sgauto.app.controller.funcionario;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.enums.TipoContratoFuncionario;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.service.FuncionarioService;
import com.sgauto.app.util.AutoCompleteComboBox;
import com.sgauto.app.util.ExibirMensagemBloqueioUtil;
import com.sgauto.app.util.ModalUtil;
import com.sgauto.app.util.VerificaPermissaoUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class FuncionarioController {

    @FXML private Label lblTotal;
    @FXML private Label lblAtivos;
    @FXML private Label lblAptosOs;
    @FXML private Label lblCnhAlerta;
    @FXML private Label lblContagem;
    @FXML private TextField txtBusca;
    @FXML private ToggleGroup grupoRemovido;
    @FXML private ToggleButton btnAtivos;
    @FXML private ToggleButton btnRemovidos;
    @FXML private ComboBox<String> cmbFiltroStatus;
    @FXML private ComboBox<String> cmbFiltroCargo;
    @FXML private ComboBox<String> cmbFiltroContrato;
    @FXML private CheckBox chkSomenteAptosOs;
    @FXML private CheckBox chkSomenteCnhVencida;
    @FXML private ComboBox<String> cmbOrdenar;
    @FXML private TableView<Funcionario> tabelaFuncionarios;
    @FXML private TableColumn<Funcionario, String> colMatricula;
    @FXML private TableColumn<Funcionario, String> colNome;
    @FXML private TableColumn<Funcionario, String> colCargo;
    @FXML private TableColumn<Funcionario, String> colContrato;
    @FXML private TableColumn<Funcionario, String> colCelular;
    @FXML private TableColumn<Funcionario, Void> colStatus;
    @FXML private TableColumn<Funcionario, Void> colApto;
    @FXML private TableColumn<Funcionario, Void> colAcoes;
    @FXML private VBox painelVazio;

    private final FuncionarioService funcionarioService;
    private final ApplicationContext applicationContext;
    private final ObservableList<Funcionario> funcionariosExibidos = FXCollections.observableArrayList();
    private final VerificaPermissaoUtil permissaoUtil;

    private List<Funcionario> todosFuncionarios = List.of();
    private AutoCompleteComboBox autoCompleteCargo;

    private static final String ORD_NOME = "Nome (A-Z)";
    private static final String ORD_MATRICULA = "Matrícula";
    private static final String ORD_ADMISSAO_RECENTE = "Admissão (mais recente)";
    private static final String ORD_ADMISSAO_ANTIGA = "Admissão (mais antiga)";

    public FuncionarioController(FuncionarioService funcionarioService, ApplicationContext applicationContext, VerificaPermissaoUtil permissaoUtil) {
        this.funcionarioService = funcionarioService;
        this.applicationContext = applicationContext;
        this.permissaoUtil = permissaoUtil;
    }

    @FXML
    public void initialize() {
        autoCompleteCargo = new AutoCompleteComboBox(cmbFiltroCargo);

        configurarColunas();
        configurarFiltros();
        carregarDados();
    }

    private void configurarFiltros() {
        List<String> statusOpcoes = new java.util.ArrayList<>();
        statusOpcoes.add("Todos os status");
        Arrays.stream(StatusFuncionario.values()).forEach(s -> statusOpcoes.add(s.getDescricao()));
        cmbFiltroStatus.setItems(FXCollections.observableArrayList(statusOpcoes));
        cmbFiltroStatus.getSelectionModel().selectFirst();

        List<String> cargoOpcoes = new java.util.ArrayList<>();
        cargoOpcoes.add("Todos os cargos");
        Arrays.stream(CargoFuncionario.values()).forEach(c -> cargoOpcoes.add(c.getDescricao()));
        autoCompleteCargo.definirItens(cargoOpcoes);
        cmbFiltroCargo.getSelectionModel().selectFirst();

        List<String> contratoOpcoes = new java.util.ArrayList<>();
        contratoOpcoes.add("Todos os contratos");
        Arrays.stream(TipoContratoFuncionario.values()).forEach(t -> contratoOpcoes.add(t.getDescricao()));
        cmbFiltroContrato.setItems(FXCollections.observableArrayList(contratoOpcoes));
        cmbFiltroContrato.getSelectionModel().selectFirst();

        cmbOrdenar.setItems(FXCollections.observableArrayList(
                ORD_NOME, ORD_MATRICULA, ORD_ADMISSAO_RECENTE, ORD_ADMISSAO_ANTIGA));
        cmbOrdenar.getSelectionModel().select(ORD_NOME);

        txtBusca.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroCargo.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbFiltroContrato.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        cmbOrdenar.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
        chkSomenteAptosOs.selectedProperty().addListener((obs, a, n) -> aplicarFiltros());
        chkSomenteCnhVencida.selectedProperty().addListener((obs, a, n) -> aplicarFiltros());
        grupoRemovido.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) antigo.setSelected(true);
            else carregarDados();
        });
    }

    private void configurarColunas() {
        colMatricula.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getMatricula()));
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNomeExibicao()));
        colCargo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCargo() != null ? data.getValue().getCargo().getDescricao() : "-"));
        colContrato.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTipoContrato() != null ? data.getValue().getTipoContrato().getDescricao() : "-"));
        colCelular.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCelular() != null ? data.getValue().getCelular() : "-"));

        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                StatusFuncionario status = f.getStatus();
                badge.setText(status != null ? status.getDescricao() : "-");
                badge.getStyleClass().setAll("badge", classeParaStatus(status));
                setGraphic(badge);
            }
        });

        colApto.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                setText(f.isAptoParaOrdemServico() ? "✓" : "✗");
                setStyle(f.isAptoParaOrdemServico() ? "-fx-text-fill: -success;" : "-fx-text-fill: -text-muted;");
            }
        });

        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnPerfil = new Button("Ver Perfil");
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final Button btnRestaurar = new Button("Restaurar");
            private final HBox container = new HBox(6);
            {
                btnPerfil.getStyleClass().add("btn-table-action");
                btnEditar.getStyleClass().add("btn-table-action");
                btnExcluir.getStyleClass().add("btn-table-action");
                btnRestaurar.getStyleClass().add("btn-table-toggle-off");

                btnPerfil.setOnAction(e -> exibirPerfil(getTableView().getItems().get(getIndex())));

                btnEditar.setOnAction(e -> {
                    if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_EDITAR)) {
                        abrirModalEdicao(getTableView().getItems().get(getIndex()));
                    } else {
                        ExibirMensagemBloqueioUtil.exibir();
                    }
                });

                btnExcluir.setOnAction(e -> {
                    if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_EXCLUIR)) {
                        confirmarExclusao(getTableView().getItems().get(getIndex()));
                    } else {
                        ExibirMensagemBloqueioUtil.exibir();
                    }
                });

                btnRestaurar.setOnAction(e -> {
                    if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_EDITAR)) {
                        restaurar(getTableView().getItems().get(getIndex()));
                    } else {
                        ExibirMensagemBloqueioUtil.exibir();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Funcionario f = getTableView().getItems().get(getIndex());
                boolean removido = f.getRemovidoEm() != null;
                container.getChildren().setAll(removido ? List.of(btnRestaurar) : List.of(btnPerfil, btnEditar, btnExcluir));
                setGraphic(container);
            }
        });

        tabelaFuncionarios.setItems(funcionariosExibidos);
    }

    private String classeParaStatus(StatusFuncionario status) {
        if (status == null) return "badge-normal";
        return switch (status) {
            case ATIVO -> "badge-active";
            case FERIAS, AFASTADO -> "badge-baixo";
            case INATIVO, DEMITIDO -> "badge-inactive";
        };
    }

    private void carregarDados() {
        boolean mostrarRemovidos = btnRemovidos.isSelected();

        todosFuncionarios = mostrarRemovidos
                ? funcionarioService.listarTodos().stream().filter(f -> f.getRemovidoEm() != null).toList()
                : funcionarioService.listarNaoRemovidos();

        atualizarCards();
        aplicarFiltros();
    }

    private void atualizarCards() {
        List<Funcionario> naoRemovidos = funcionarioService.listarNaoRemovidos();
        lblTotal.setText(String.valueOf(naoRemovidos.size()));

        long ativos = naoRemovidos.stream().filter(f -> f.getStatus() == StatusFuncionario.ATIVO).count();
        lblAtivos.setText(String.valueOf(ativos));

        long aptosOs = funcionarioService.listarAptosParaOrdemServico().size();
        lblAptosOs.setText(String.valueOf(aptosOs));

        LocalDate limite = LocalDate.now().plusDays(30);
        long cnhAlerta = naoRemovidos.stream()
                .filter(f -> f.getValidadeCnh() != null && !f.getValidadeCnh().isAfter(limite))
                .count();
        lblCnhAlerta.setText(String.valueOf(cnhAlerta));
    }

    private void aplicarFiltros() {
        String termo = txtBusca.getText() == null ? "" : txtBusca.getText().toLowerCase();
        String statusSelecionado = cmbFiltroStatus.getValue();
        String cargoSelecionado = cmbFiltroCargo.getValue();
        String contratoSelecionado = cmbFiltroContrato.getValue();
        boolean somenteAptos = chkSomenteAptosOs.isSelected();
        boolean somenteCnhVencida = chkSomenteCnhVencida.isSelected();

        List<Funcionario> filtrados = todosFuncionarios.stream()
                .filter(f -> termo.isBlank()
                        || f.getNomeCompleto().toLowerCase().contains(termo)
                        || f.getMatricula().toLowerCase().contains(termo)
                        || f.getCpf().contains(termo)
                        || (f.getCelular() != null && f.getCelular().contains(termo))
                        || (f.getEmail() != null && f.getEmail().toLowerCase().contains(termo)))
                .filter(f -> statusSelecionado == null || statusSelecionado.equals("Todos os status")
                        || (f.getStatus() != null && statusSelecionado.equals(f.getStatus().getDescricao())))
                .filter(f -> cargoSelecionado == null || cargoSelecionado.equals("Todos os cargos")
                        || (f.getCargo() != null && cargoSelecionado.equals(f.getCargo().getDescricao())))
                .filter(f -> contratoSelecionado == null || contratoSelecionado.equals("Todos os contratos")
                        || (f.getTipoContrato() != null && contratoSelecionado.equals(f.getTipoContrato().getDescricao())))
                .filter(f -> !somenteAptos || f.isAptoParaOrdemServico())
                .filter(f -> !somenteCnhVencida || (f.getValidadeCnh() != null && f.getValidadeCnh().isBefore(LocalDate.now())))
                .sorted(obterComparador())
                .toList();

        funcionariosExibidos.setAll(filtrados);
        lblContagem.setText(filtrados.size() + " resultado(s)");
        atualizarEstadoVazio(filtrados.isEmpty());
    }

    private Comparator<Funcionario> obterComparador() {
        String ordem = cmbOrdenar.getValue();
        if (ORD_MATRICULA.equals(ordem)) return Comparator.comparing(Funcionario::getMatricula);
        if (ORD_ADMISSAO_RECENTE.equals(ordem)) return Comparator.comparing(Funcionario::getDataAdmissao).reversed();
        if (ORD_ADMISSAO_ANTIGA.equals(ordem)) return Comparator.comparing(Funcionario::getDataAdmissao);
        return Comparator.comparing(Funcionario::getNomeExibicao);
    }

    private void atualizarEstadoVazio(boolean vazio) {
        painelVazio.setVisible(vazio);
        painelVazio.setManaged(vazio);
        tabelaFuncionarios.setVisible(!vazio);
        tabelaFuncionarios.setManaged(!vazio);
    }

    @FXML
    private void abrirModalNovoFuncionario() {
        if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_CRIAR)) {
            abrirModal(null);
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    private void abrirModalEdicao(Funcionario funcionario) {
        if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_EDITAR)) {
            abrirModal(funcionario);
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }

    private void abrirModal(Funcionario funcionarioExistente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/funcionario/funcionario-form.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            FuncionarioFormController controller = loader.getController();
            controller.configurar(funcionarioExistente, this::carregarDados);

            Stage modal = ModalUtil.abrir(root, funcionarioExistente == null ? "Novo Funcionário" : "Editar Funcionário",
                    tabelaFuncionarios.getScene().getWindow(), true);
            modal.showAndWait();

        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao abrir formulário de funcionário", e);
        }
    }

    private void exibirPerfil(Funcionario f) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Perfil de " + f.getNomeExibicao());
        alert.setHeaderText(null);
        alert.setContentText(
                "Matrícula: " + f.getMatricula() + "\n" +
                        "CPF: " + f.getCpf() + "\n" +
                        (f.getRg() != null ? "RG: " + f.getRg() + "\n" : "") +
                        (f.getDataNascimento() != null ? "Nascimento: " + f.getDataNascimento().format(fmt) + "\n" : "") +
                        "\nCargo: " + (f.getCargo() != null ? f.getCargo().getDescricao() : "-") + "\n" +
                        "Especialidade: " + (f.getEspecialidade() != null ? f.getEspecialidade() : "-") + "\n" +
                        "Contrato: " + (f.getTipoContrato() != null ? f.getTipoContrato().getDescricao() : "-") + "\n" +
                        "Admissão: " + f.getDataAdmissao().format(fmt) + "\n" +
                        (f.getDataDemissao() != null ? "Demissão: " + f.getDataDemissao().format(fmt) + "\n" : "") +
                        "Carga horária: " + f.getCargaHorariaSemanal() + "h/semana\n" +
                        "\nCelular: " + f.getCelular() + "\n" +
                        (f.getEmail() != null ? "E-mail: " + f.getEmail() + "\n" : "") +
                        (enderecoFormatado(f) != null ? "Endereço: " + enderecoFormatado(f) + "\n" : "") +
                        (f.getNumeroCnh() != null ? "\nCNH: " + f.getNumeroCnh() + " (Cat. " + f.getCategoriaCnh() + ")"
                                                    + (f.getValidadeCnh() != null ? " — válida até " + f.getValidadeCnh().format(fmt) : "") + "\n" : "") +
                        "\nDisponível para O.S.: " + (f.isAptoParaOrdemServico() ? "Sim" : "Não")
        );
        alert.showAndWait();
    }

    private String enderecoFormatado(Funcionario f) {
        if (f.getLogradouro() == null) return null;
        return f.getLogradouro() + ", " + (f.getNumero() != null ? f.getNumero() : "s/n")
                + (f.getBairro() != null ? " - " + f.getBairro() : "")
                + (f.getCidade() != null ? " - " + f.getCidade() : "")
                + (f.getEstado() != null ? "/" + f.getEstado() : "");
    }

    private void confirmarExclusao(Funcionario funcionario) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja realmente excluir \"" + funcionario.getNomeExibicao() + "\"? "
                + "O funcionário poderá ser restaurado depois, se necessário.");

        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                funcionarioService.remover(funcionario.getId());
                carregarDados();
            }
        });
    }

    private void restaurar(Funcionario funcionario) {
        funcionarioService.cancelarRemocao(funcionario.getId());
        carregarDados();
    }

    @FXML
    private void abrirModalAvancadas() {
        if (permissaoUtil.verificar(PermissaoChave.FUNCIONARIO_OPCOES_AVANCADAS)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgauto/app/view/funcionario/funcionario-configuracoes-avancadas-modal.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                Stage modal = ModalUtil.abrir(root, "Configurações Avançadas — Funcionários",
                        tabelaFuncionarios.getScene().getWindow());
                modal.showAndWait();

                carregarDados();
            } catch (java.io.IOException e) {
                throw new RuntimeException("Erro ao abrir configurações avançadas", e);
            }
        } else {
            ExibirMensagemBloqueioUtil.exibir();
        }
    }
}