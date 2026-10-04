package com.sgauto.app.util.mock;

import com.sgauto.app.enums.financeiro.*;
import com.sgauto.app.enums.funcionario.CargoFuncionario;
import com.sgauto.app.enums.funcionario.StatusFuncionario;
import com.sgauto.app.enums.funcionario.TipoContratoFuncionario;
import com.sgauto.app.enums.os.StatusOS;
import com.sgauto.app.enums.patio.CategoriaVeiculoPatio;
import com.sgauto.app.enums.patio.StatusEstadiaPatio;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.ClientePF;
import com.sgauto.app.model.Funcionario;
import com.sgauto.app.model.Servico;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.OrdemServico.OsPagamento;
import com.sgauto.app.model.OrdemServico.OsPeca;
import com.sgauto.app.model.OrdemServico.OsServico;
import com.sgauto.app.model.caixa.Caixa;
import com.sgauto.app.model.caixa.CaixaMovimentacao;
import com.sgauto.app.model.estoque.Peca;
import com.sgauto.app.model.patio.EstadiaPatio;
import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.repository.ClienteRepository;
import com.sgauto.app.repository.FuncionarioRepository;
import com.sgauto.app.repository.ServicoRepository;
import com.sgauto.app.repository.VeiculoRepository;
import com.sgauto.app.repository.OrdemServico.OrdemServicoRepository;
import com.sgauto.app.repository.caixa.CaixaMovimentacaoRepository;
import com.sgauto.app.repository.caixa.CaixaRepository;
import com.sgauto.app.repository.estoque.PecaRepository;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.repository.patio.MotivoEstadiaRepository;
import com.sgauto.app.repository.patio.TabelaPrecoPatioRepository;
import com.sgauto.app.service.financeiro.CaixaService;
import com.sgauto.app.service.PatioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Popula uma semana de operação da oficina (O.S., pagamentos, caixa e pátio),
 * com datas relativas ao dia em que o seeder roda (D-6 até hoje).
 *
 * - Só roda em banco sem O.S. e sem caixa (banco recém-criado).
 * - Roda depois do DataSeeder (@Order) e antes do CaixaInicializador (sem @Order).
 * - Grava direto pelos repositories: no start ninguém está logado, então os services
 *   barrariam as ações por permissão. Onde há cálculo de regra de negócio (totais do
 *   caixa, valor esperado, valor da estadia), usa o método real do service.
 * - Tudo numa transação: se algo falhar, nada é gravado e o seeder tenta de novo no próximo start.
 */
@Component
@Profile("dev")
@Order(2)
public class DadosTransacionaisSeeder implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DadosTransacionaisSeeder.class);

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ServicoRepository servicoRepository;
    private final PecaRepository pecaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final CaixaRepository caixaRepository;
    private final CaixaMovimentacaoRepository caixaMovimentacaoRepository;
    private final EstadiaPatioRepository estadiaPatioRepository;
    private final MotivoEstadiaRepository motivoEstadiaRepository;
    private final TabelaPrecoPatioRepository tabelaPrecoPatioRepository;
    private final CaixaService caixaService;
    private final PatioService patioService;

    private LocalDate hoje;

    public DadosTransacionaisSeeder(ClienteRepository clienteRepository,
                                    VeiculoRepository veiculoRepository,
                                    FuncionarioRepository funcionarioRepository,
                                    ServicoRepository servicoRepository,
                                    PecaRepository pecaRepository,
                                    OrdemServicoRepository ordemServicoRepository,
                                    CaixaRepository caixaRepository,
                                    CaixaMovimentacaoRepository caixaMovimentacaoRepository,
                                    EstadiaPatioRepository estadiaPatioRepository,
                                    MotivoEstadiaRepository motivoEstadiaRepository,
                                    TabelaPrecoPatioRepository tabelaPrecoPatioRepository,
                                    CaixaService caixaService,
                                    PatioService patioService) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.servicoRepository = servicoRepository;
        this.pecaRepository = pecaRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.caixaRepository = caixaRepository;
        this.caixaMovimentacaoRepository = caixaMovimentacaoRepository;
        this.estadiaPatioRepository = estadiaPatioRepository;
        this.motivoEstadiaRepository = motivoEstadiaRepository;
        this.tabelaPrecoPatioRepository = tabelaPrecoPatioRepository;
        this.caixaService = caixaService;
        this.patioService = patioService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (ordemServicoRepository.count() > 0 || caixaRepository.count() > 0) {
            log.info("[DEV] Dados transacionais já existem. Seeder transacional ignorado.");
            return;
        }

        log.info("[DEV] Povoando uma semana de operação (O.S., caixa e pátio)...");
        hoje = LocalDate.now();

        // ===== Cadastros já criados pelo DataSeeder =====
        Cliente joao = clienteRepository.findByDocumento("98765432100").orElseThrow();
        Cliente henrique = clienteRepository.findByDocumento("46505931832").orElseThrow();
        Veiculo moto = veiculoRepository.findByPlaca("SNT1A23").orElseThrow();
        Veiculo lambo = veiculoRepository.findByPlaca("JMN3963").orElseThrow();
        Funcionario carlos = funcionarioRepository.findByMatricula("MAT-001").orElseThrow();
        Servico troca = servicoRepository.findByCodigo("SRV-TROCA").orElseThrow();
        Peca filtro = pecaRepository.findByCodigo("FILT-001").orElseThrow();
        Peca oleo = pecaRepository.findByCodigo("LUB-10W40").orElseThrow();

        // ===== Cadastros extras =====
        ClientePF marina = new ClientePF("Marina Oliveira Santos", "32165498700", "13988776655", null,
                "marina.santos@email.com", null, true, "33445566X", LocalDate.of(1990, 8, 12));
        ClientePF paulo = new ClientePF("Paulo Henrique Mendes", "74185296300", "13977665544", null,
                "paulo.mendes@email.com", null, true, "22113344X", LocalDate.of(1982, 11, 3));
        clienteRepository.saveAll(List.of(marina, paulo));

        Veiculo onix = new Veiculo(marina, "FRT2B45", "Chevrolet", "Onix 1.0", 2020, 58000, true);
        Veiculo strada = new Veiculo(paulo, "GHI4J56", "Fiat", "Strada Freedom", 2021, 72000, true);
        veiculoRepository.saveAll(List.of(onix, strada));

        Funcionario rafael = new Funcionario();
        rafael.setMatricula("MAT-002");
        rafael.setNomeCompleto("Rafael Souza Lima");
        rafael.setCpf("15935745600");
        rafael.setCelular("13966554433");
        rafael.setCidade("Santos");
        rafael.setEstado("SP");
        rafael.setCargo(CargoFuncionario.MECANICO);
        rafael.setTipoContrato(TipoContratoFuncionario.CLT);
        rafael.setDataAdmissao(LocalDate.of(2025, 6, 2));
        rafael.setExibeEmOs(true);
        rafael.setStatus(StatusFuncionario.ATIVO);
        rafael.setSalarioBase(new BigDecimal("2800.00"));
        rafael.setComissaoPercentual(new BigDecimal("12.00"));
        funcionarioRepository.save(rafael);

        Servico freio = new Servico("SRV-FREIO", "Revisão de Freios", "Mecânica Geral",
                "Inspeção e troca de pastilhas", new BigDecimal("120.00"), new BigDecimal("90.00"),
                90, 90, new BigDecimal("10.00"), null, true);
        Servico diagnostico = new Servico("SRV-DIAG", "Diagnóstico Eletrônico", "Mecânica Geral",
                "Leitura de falhas com scanner", new BigDecimal("80.00"), new BigDecimal("80.00"),
                60, 0, new BigDecimal("10.00"), null, true);
        servicoRepository.saveAll(List.of(freio, diagnostico));

        // Estoque inicial 4 com mínimo 4: depois da O.S. B fica em 3 (alerta de estoque crítico)
        Peca pastilha = new Peca("PAST-001", "Pastilha de Freio Dianteira", "Universal",
                new BigDecimal("38.00"), new BigDecimal("70.00"), 4, 4);
        pecaRepository.save(pastilha);

        // ===== Ordens de serviço =====
        // A: finalizada no mesmo dia (D-6)
        OrdemServico osA = novaOs(joao, moto, carlos, "Troca de óleo de rotina", em(6, 8, 30), em(6, 12, 0));
        addServico(osA, troca, 1);
        addPeca(osA, filtro, 1);
        addPeca(osA, oleo, 1);
        OsPagamento pagA = addPagamento(osA, FormaPagamento.DINHEIRO, "150.00", em(6, 10, 30));
        concluir(osA, em(6, 10, 0));
        finalizar(osA, em(6, 10, 30));

        // B: com desconto, paga em Pix (D-5)
        OrdemServico osB = novaOs(marina, onix, rafael, "Freio chiando ao parar", em(5, 9, 0), em(5, 17, 0));
        addServico(osB, freio, 1);
        addPeca(osB, pastilha, 1);
        osB.setValorDesconto(new BigDecimal("10.00"));
        OsPagamento pagB = addPagamento(osB, FormaPagamento.PIX, "180.00", em(5, 15, 20));
        concluir(osB, em(5, 15, 0));
        finalizar(osB, em(5, 15, 20));

        // C: concluída em D-3, paga em duas vezes, finalizada só em D-1 (produzido != recebido)
        OrdemServico osC = novaOs(henrique, lambo, rafael, "Luz de injeção acesa", em(4, 10, 0), em(3, 18, 0));
        addServico(osC, diagnostico, 1);
        addServico(osC, troca, 1);
        addPeca(osC, oleo, 2);
        OsPagamento pagC1 = addPagamento(osC, FormaPagamento.CREDITO, "150.00", em(3, 11, 30));
        OsPagamento pagC2 = addPagamento(osC, FormaPagamento.DEBITO, "120.00", em(1, 9, 0));
        concluir(osC, em(3, 11, 0));
        finalizar(osC, em(1, 9, 0));

        // D: cancelada em D-2, sem pagamento
        OrdemServico osD = novaOs(joao, moto, carlos, "Troca de óleo", em(3, 14, 0), em(2, 18, 0));
        addServico(osD, troca, 1);
        osD.setObservacoesInternas("Cliente desistiu do serviço");
        osD.setStatus(StatusOS.CANCELADA);
        osD.setDataCancelamento(em(2, 10, 0));

        // E: pausada aguardando peça, previsão vencida (atrasada) e veículo no pátio
        OrdemServico osE = novaOs(marina, onix, carlos, "Freio traseiro rangendo", em(2, 8, 0), em(1, 12, 0));
        addServico(osE, freio, 1);
        osE.setStatus(StatusOS.AGUARDANDO);
        osE.setMotivoPausa("Aguardando chegada da pastilha de freio");
        osE.setFicarNoPatio(true);

        // F: concluída hoje, paga pela metade (saldo a receber de R$ 100,00)
        OrdemServico osF = novaOs(henrique, lambo, rafael, "Pedal de freio baixo", em(1, 16, 0), em(0, 12, 0));
        addServico(osF, diagnostico, 1);
        addServico(osF, freio, 1);
        OsPagamento pagF = addPagamento(osF, FormaPagamento.PIX, "100.00", em(0, 10, 15));
        concluir(osF, em(0, 10, 0));

        // G: aberta hoje, em execução
        OrdemServico osG = novaOs(joao, moto, carlos, "Revisão dos 15.000 km", em(0, 8, 15), em(-1, 12, 0));
        addServico(osG, troca, 1);
        addPeca(osG, filtro, 1);
        addPeca(osG, oleo, 1);
        osG.setStatus(StatusOS.EM_EXECUCAO);

        // H: aberta e finalizada hoje
        OrdemServico osH = novaOs(paulo, strada, rafael, "Barulho no motor", em(0, 9, 0), em(0, 12, 0));
        addServico(osH, diagnostico, 1);
        OsPagamento pagH = addPagamento(osH, FormaPagamento.DINHEIRO, "80.00", em(0, 11, 30));
        concluir(osH, em(0, 11, 0));
        finalizar(osH, em(0, 11, 30));

        List<OrdemServico> todas = List.of(osA, osB, osC, osD, osE, osF, osG, osH);
        todas.forEach(this::recalcularTotais);
        ordemServicoRepository.saveAll(todas);
        pecaRepository.saveAll(List.of(filtro, oleo, pastilha));

        // ===== Pátio =====
        TabelaPrecoPatio tarifaPasseio = new TabelaPrecoPatio("Diária - Carro de passeio",
                CategoriaVeiculoPatio.PASSEIO, new BigDecimal("30.00"), 0, true);
        tabelaPrecoPatioRepository.save(tarifaPasseio);
        MotivoEstadia motivoGuarda = new MotivoEstadia("Guarda de veículo",
                "Veículo guardado a pedido do cliente", true, false);
        motivoEstadiaRepository.save(motivoGuarda);

        // Guarda paga: D-5 18:00 até D-2 17:00 = 3 diárias de R$ 30,00
        EstadiaPatio guarda = new EstadiaPatio(strada, paulo, null, strada.getPlaca(), tarifaPasseio, motivoGuarda, "Vaga 1");
        guarda.setDataEntrada(em(5, 18, 0));
        guarda.setDataSaida(em(2, 17, 0));
        guarda.setValorTotal(patioService.calcularValorEstadia(guarda));
        guarda.setStatus(StatusEstadiaPatio.FINALIZADO);
        estadiaPatioRepository.save(guarda);

        // Veículo da O.S. E, ainda no pátio
        MotivoEstadia motivoOs = motivoEstadiaRepository.findByNome("Ordem de Serviço").orElseThrow();
        TabelaPrecoPatio tarifaOs = tabelaPrecoPatioRepository
                .findByDescricao("Tarifa Padrão - Ordem de Serviço (sem cobrança)").orElseThrow();
        EstadiaPatio estadiaOsE = new EstadiaPatio(onix, marina, osE, onix.getPlaca(), tarifaOs, motivoOs, "Vaga 3");
        estadiaOsE.setDataEntrada(em(2, 8, 0));
        estadiaPatioRepository.save(estadiaOsE);

        // ===== Caixa 1: D-6 07:50 até D-4 18:10 (atravessa três dias), sem diferença =====
        Caixa caixa1 = abrirCaixa(em(6, 7, 50));
        movimentar(caixa1, TipoMovimentacao.ENTRADA, OrigemMovimentacao.SUPRIMENTO, FormaPagamento.DINHEIRO,
                "100.00", "Troco inicial", em(6, 8, 0));
        movimentarPagamentoOs(caixa1, osA, pagA);
        movimentarPagamentoOs(caixa1, osB, pagB);
        movimentar(caixa1, TipoMovimentacao.ENTRADA, OrigemMovimentacao.AVULSO, FormaPagamento.DEBITO,
                "25.00", "Venda avulsa - aditivo de radiador", em(5, 16, 40));
        movimentar(caixa1, TipoMovimentacao.SAIDA, OrigemMovimentacao.AVULSO, FormaPagamento.DINHEIRO,
                "45.00", "Compra de material de limpeza", em(4, 12, 0));
        fecharCaixa(caixa1, em(4, 18, 10), "admin", "205.00", null);

        // ===== Caixa 2: D-4 18:10 até D-2 18:20, quebra de R$ 20,00 =====
        Caixa caixa2 = abrirCaixa(em(4, 18, 10));
        movimentar(caixa2, TipoMovimentacao.ENTRADA, OrigemMovimentacao.SUPRIMENTO, FormaPagamento.DINHEIRO,
                "100.00", "Troco inicial", em(3, 8, 0));
        movimentarPagamentoOs(caixa2, osC, pagC1);
        movimentar(caixa2, TipoMovimentacao.SAIDA, OrigemMovimentacao.SANGRIA, FormaPagamento.DINHEIRO,
                "50.00", "Retirada para depósito bancário", em(3, 16, 0));
        CaixaMovimentacao movPatio = movimentar(caixa2, TipoMovimentacao.ENTRADA, OrigemMovimentacao.PATIO,
                FormaPagamento.DINHEIRO, guarda.getValorTotal().toPlainString(),
                "Pátio - saída do veículo placa " + guarda.getPlaca(), guarda.getDataSaida());
        movPatio.setReferenciaId(guarda.getId());
        movPatio.setClienteId(paulo.getId());
        movPatio.setPlaca(guarda.getPlaca());
        fecharCaixa(caixa2, em(2, 18, 20), "operador", "120.00", "Troco devolvido a mais para cliente");

        // ===== Caixa 3: aberto desde D-2 18:20 (caixa atual) =====
        Caixa caixa3 = abrirCaixa(em(2, 18, 20));
        movimentar(caixa3, TipoMovimentacao.ENTRADA, OrigemMovimentacao.SUPRIMENTO, FormaPagamento.DINHEIRO,
                "100.00", "Troco inicial", em(1, 8, 0));
        movimentarPagamentoOs(caixa3, osC, pagC2);
        movimentar(caixa3, TipoMovimentacao.ENTRADA, OrigemMovimentacao.AVULSO, FormaPagamento.DINHEIRO,
                "30.00", "Venda avulsa - lavagem de moto", em(1, 14, 0));
        movimentarPagamentoOs(caixa3, osF, pagF);
        movimentarPagamentoOs(caixa3, osH, pagH);

        log.info("[DEV] Semana de operação povoada: {} O.S., 3 caixas, 2 estadias.", todas.size());
    }

    // ===== Datas =====

    // diasAtras: 0 = hoje, 1 = ontem, -1 = amanhã
    private LocalDateTime em(int diasAtras, int hora, int minuto) {
        return hoje.minusDays(diasAtras).atTime(hora, minuto);
    }

    // ===== O.S. =====

    private OrdemServico novaOs(Cliente cliente, Veiculo veiculo, Funcionario funcionario,
                                String sintomas, LocalDateTime abertura, LocalDateTime previsao) {
        OrdemServico os = new OrdemServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        os.setFuncionario(funcionario);
        os.setSintomasRelatados(sintomas);
        os.setDataAbertura(abertura);
        os.setDataPrevisao(previsao);
        return os;
    }

    private void addServico(OrdemServico os, Servico servico, int quantidade) {
        OsServico item = new OsServico();
        item.setOrdemServico(os);
        item.setServico(servico);
        item.setQuantidade(quantidade);
        item.setValorUnitario(servico.getValor());
        item.setValorTotal(servico.getValor().multiply(BigDecimal.valueOf(quantidade)));
        os.getServicos().add(item);
    }

    // Congela preço de venda e custo, e dá baixa no estoque (como o adicionarPeca do service)
    private void addPeca(OrdemServico os, Peca peca, int quantidade) {
        OsPeca item = new OsPeca();
        item.setOrdemServico(os);
        item.setPeca(peca);
        item.setQuantidade(quantidade);
        item.setValorUnitario(peca.getPrecoVenda());
        item.setValorCustoUnitario(peca.getPrecoCusto());
        item.setValorTotal(peca.getPrecoVenda().multiply(BigDecimal.valueOf(quantidade)));
        os.getPecas().add(item);
        peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() - quantidade);
    }

    private OsPagamento addPagamento(OrdemServico os, FormaPagamento forma, String valor, LocalDateTime data) {
        OsPagamento pagamento = new OsPagamento();
        pagamento.setOrdemServico(os);
        pagamento.setFormaPagamento(forma);
        pagamento.setValorPago(new BigDecimal(valor));
        pagamento.setDataPagamento(data);
        os.getPagamentos().add(pagamento);
        return pagamento;
    }

    private void concluir(OrdemServico os, LocalDateTime data) {
        os.setStatus(StatusOS.CONCLUIDA);
        os.setDataConclusao(data);
    }

    private void finalizar(OrdemServico os, LocalDateTime data) {
        os.setStatus(StatusOS.FINALIZADA);
        os.setDataFinalizacao(data);
    }

    // Mesma regra do recalcularTotais do OrdemServicoService
    private void recalcularTotais(OrdemServico os) {
        BigDecimal totalPecas = os.getPecas().stream()
                .map(OsPeca::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalServicos = os.getServicos().stream()
                .map(OsServico::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        os.setValorTotalPecas(totalPecas);
        os.setValorTotalServicos(totalServicos);
        os.setValorTotalOs(totalPecas.add(totalServicos).subtract(os.getValorDesconto()).max(BigDecimal.ZERO));
    }

    // ===== Caixa =====

    private Caixa abrirCaixa(LocalDateTime abertura) {
        Caixa caixa = new Caixa("Sistema", BigDecimal.ZERO);
        caixa.setDataAbertura(abertura);
        return caixaRepository.save(caixa);
    }

    private CaixaMovimentacao movimentar(Caixa caixa, TipoMovimentacao tipo, OrigemMovimentacao origem,
                                         FormaPagamento forma, String valor, String descricao, LocalDateTime data) {
        CaixaMovimentacao mov = new CaixaMovimentacao(caixa, tipo, origem, forma, new BigDecimal(valor), descricao);
        mov.setData(data);
        return caixaMovimentacaoRepository.save(mov);
    }

    // Mesmo formato do registrarPagamento do OrdemServicoService
    private void movimentarPagamentoOs(Caixa caixa, OrdemServico os, OsPagamento pagamento) {
        CaixaMovimentacao mov = movimentar(caixa, TipoMovimentacao.ENTRADA, OrigemMovimentacao.OS_PAGAMENTO,
                pagamento.getFormaPagamento(), pagamento.getValorPago().toPlainString(),
                "Pagamento O.S. #" + os.getId(), pagamento.getDataPagamento());
        mov.setReferenciaId(pagamento.getId());
        mov.setClienteId(os.getCliente().getId());
        mov.setPlaca(os.getVeiculo().getPlaca());
    }

    // Usa os cálculos reais do CaixaService para valor esperado e totais
    private void fecharCaixa(Caixa caixa, LocalDateTime fechamento, String usuario,
                             String valorContado, String justificativa) {
        BigDecimal esperado = caixaService.calcularValorEsperado(caixa.getId());
        BigDecimal contado = new BigDecimal(valorContado);

        caixaService.preencherTotais(caixa);
        caixa.setValorEsperado(esperado);
        caixa.setValorContado(contado);
        caixa.setDiferenca(contado.subtract(esperado));
        caixa.setModoConferenciaUsado(ModoConferencia.OBRIGATORIA.name());
        caixa.setJustificativaDiferenca(justificativa);
        caixa.setUsuarioFechamento(usuario);
        caixa.setDataFechamento(fechamento);
        caixa.setStatus(StatusCaixa.FECHADO);
        caixaRepository.save(caixa);
    }
}