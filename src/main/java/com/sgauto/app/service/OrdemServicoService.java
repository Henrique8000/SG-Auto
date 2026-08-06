package com.sgauto.app.service;

import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.*;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.OrdemServico.OsPagamento;
import com.sgauto.app.model.OrdemServico.OsPeca;
import com.sgauto.app.model.OrdemServico.OsServico;
import com.sgauto.app.repository.*;
import com.sgauto.app.repository.OrdemServico.OsPagamentoRepository;
import com.sgauto.app.repository.OrdemServico.OsPecaRepository;
import com.sgauto.app.repository.OrdemServico.OsServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgauto.app.repository.OrdemServico.OrdemServicoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final OsPecaRepository osPecaRepository;
    private final OsServicoRepository osServicoRepository;
    private final OsPagamentoRepository osPagamentoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository;
    private final ServicoRepository servicoRepository;
    private final PatioService patioService;
    private final EstoqueService estoqueService;
    private final CaixaService caixaService;

    // CONSTRUTOR MANUAL (Substitui o @RequiredArgsConstructor do Lombok)
    // O Spring injeta automaticamente as dependências aqui.
    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               OsPecaRepository osPecaRepository,
                               OsServicoRepository osServicoRepository,
                               OsPagamentoRepository osPagamentoRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository,
                               FuncionarioRepository funcionarioRepository,
                               PecaRepository pecaRepository,
                               ServicoRepository servicoRepository,
                               EstoqueService estoqueService,
                               CaixaService caixaService, PatioService patioService) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.osPecaRepository = osPecaRepository;
        this.osServicoRepository = osServicoRepository;
        this.osPagamentoRepository = osPagamentoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.pecaRepository = pecaRepository;
        this.servicoRepository = servicoRepository;
        this.estoqueService = estoqueService;
        this.caixaService = caixaService;
        this.patioService = patioService;
    }

    List<StatusOS> status = List.of(
            StatusOS.CANCELADA,
            StatusOS.FINALIZADA,
            StatusOS.VERIFICANDO_ORCAMENTO
    );

    /*
        MANIPULACAO OS
    */

    @Transactional
    public OrdemServico criarOS(Long clienteId, Long veiculoId, Long funcionarioId,
                                String sintomasRelatados, LocalDateTime dataPrevisao,
                                boolean ficarNoPatio) {
        if(clienteId == null || veiculoId == null || funcionarioId == null)
            throw new IllegalArgumentException("É necessário preencher todos os campos para a criação de uma OS.");

        boolean jaTemPlacaAtiva = ordemServicoRepository.existsByVeiculoIdAndStatusNotIn(veiculoId, status);

        if(jaTemPlacaAtiva) {
            throw new IllegalArgumentException("Não é possível abrir uma OS para esta placa, pois ela já tem uma OS ativa.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID informado." + clienteId));

        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado com o ID informado." + veiculoId));

        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário responsável não encontrado." + funcionarioId));

        OrdemServico novaOs = new OrdemServico();
        novaOs.setCliente(cliente);
        novaOs.setVeiculo(veiculo);
        novaOs.setFuncionario(funcionario);
        novaOs.setSintomasRelatados(sintomasRelatados);
        novaOs.setDataPrevisao(dataPrevisao);
        novaOs.setFicarNoPatio(ficarNoPatio);

        OrdemServico osSalva = ordemServicoRepository.save(novaOs);

        if (ficarNoPatio) {
            patioService.registrarEntradaViaOrdemServico(osSalva);
        }

        return osSalva;
    }

    @Transactional
    public OrdemServico alterarStatus(Long osId, StatusOS novoStatus) {
        if(osId == null || novoStatus == null)
            throw new IllegalArgumentException("É necessário o envio do Id da OS e um novo Status para atualização");

        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada. ID: " + osId));

        StatusOS statusAtual = os.getStatus();

        if(statusAtual == novoStatus) {
            return os;
        }

        if(statusAtual == StatusOS.CANCELADA || statusAtual == StatusOS.FINALIZADA) {
            throw new IllegalStateException("Uma OS " + statusAtual + " já está encerrada e não pode mudar de status.");
        }

        if(statusAtual == StatusOS.ABERTA && novoStatus == StatusOS.FINALIZADA) {
            throw new IllegalStateException("Não é possível alterar de ABERTA direto para FINALIZADA. O serviço precisa ser executado.");
        }

        if(novoStatus == StatusOS.FINALIZADA){
            BigDecimal totalPago = osPagamentoRepository.somarPagamentosPorOsId(osId);
            BigDecimal totalOs = os.getValorTotalOs();

            if(totalPago.compareTo(totalOs) < 0) {
                BigDecimal saldoDevedor = totalOs.subtract(totalPago);
                throw new IllegalStateException("Não é possível finalizar a OS. Há um saldo devedor pendente de R$ " + saldoDevedor);
            }

            os.setFicarNoPatio(false);

            if(os.getDataConclusao() == null) {
                os.setDataConclusao(LocalDateTime.now());
            }
        }

        if(novoStatus == StatusOS.CONCLUIDA && os.getDataConclusao() == null) {
            os.setDataConclusao(LocalDateTime.now());
        }

        os.setStatus(novoStatus);

        return ordemServicoRepository.save(os);
    }

    @Transactional
    public void cancelarOS(Long osId) {
        if(osId == null)
            throw new IllegalArgumentException("É necessário o envio do Id da OS para cancelamento.");

        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada. ID: " + osId));

        if(os.getStatus() == StatusOS.CANCELADA || os.getStatus() == StatusOS.FINALIZADA) {
            throw new IllegalStateException("Não é possível cancelar uma OS que já se encontra com status: " + os.getStatus());
        }

        List<OsPeca> listaPecas = os.getPecas();

        if (listaPecas != null && !listaPecas.isEmpty()) {
            listaPecas.forEach(osPeca -> {
                Long pecaId = osPeca.getPeca().getId();
                int quantidadeParaDevolver = osPeca.getQuantidade();
                estoqueService.darEntradaEstoque(pecaId, quantidadeParaDevolver);
            });
        }

        os.setFicarNoPatio(false);

        os.setStatus(StatusOS.CANCELADA);
        ordemServicoRepository.save(os);
    }

    @Transactional
    public OsPagamento registrarPagamento(Long osId, FormaPagamento formaPagamento, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        OrdemServico os = buscarPorId(osId);

        if(os.getStatus() == StatusOS.CANCELADA || os.getStatus() == StatusOS.FINALIZADA)
            throw new IllegalArgumentException("Não é possível registrar pagamento em uma O.S. 'CANCELADA' ou 'FINALIZADA'");

        BigDecimal jaPago = osPagamentoRepository.somarPagamentosPorOsId(osId);
        BigDecimal saldoDevedor = os.getValorTotalOs().subtract(jaPago);

        if (valor.compareTo(saldoDevedor) > 0) {
            throw new IllegalArgumentException("O valor informado (R$ " + valor + ") excede o saldo devedor (R$ " + saldoDevedor + ").");
        }

        OsPagamento pagamento = new OsPagamento();
        pagamento.setOrdemServico(os);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setValorPago(valor);

        pagamento = osPagamentoRepository.save(pagamento);
        os.getPagamentos().add(pagamento);

        Long clienteId = os.getCliente() != null ? os.getCliente().getId() : null;
        String placa = os.getVeiculo() != null ? os.getVeiculo().getPlaca() : null;
        String descricao = "Pagamento O.S. #" + os.getId();

        CaixaMovimentacao movimentacao = caixaService.registrarMovimentacao(
                TipoMovimentacao.ENTRADA,
                OrigemMovimentacao.OS_PAGAMENTO,
                formaPagamento,
                valor,
                descricao,
                clienteId,
                placa
        );
        movimentacao.setReferenciaId(pagamento.getId());

        return pagamento;
    }

    /*
        GESTÃO DE PEÇAS E SERVIÇOS (ORÇAMENTO)
    */

    @Transactional
    public OsPeca adicionarPeca(Long osId, Long pecaId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade da peça deve ser maior que zero.");
        }

        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada."));
        validarEdicao(os);

        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new EntityNotFoundException("Peça não encontrada no catálogo."));

        estoqueService.darSaidaEstoque(pecaId, quantidade);

        BigDecimal valorUnitario = peca.getPrecoVenda();
        BigDecimal valorTotal = valorUnitario.multiply(new BigDecimal(quantidade));

        // Substituição do @Builder
        OsPeca novaOsPeca = new OsPeca();
        novaOsPeca.setOrdemServico(os);
        novaOsPeca.setPeca(peca);
        novaOsPeca.setQuantidade(quantidade);
        novaOsPeca.setValorUnitario(valorUnitario);
        novaOsPeca.setValorTotal(valorTotal);

        novaOsPeca = osPecaRepository.save(novaOsPeca);

        os.getPecas().add(novaOsPeca);
        recalcularTotais(os);

        return novaOsPeca;
    }

    @Transactional
    public void removerPeca(Long osId, Long osPecaId) {
        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada."));
        validarEdicao(os);

        OsPeca osPeca = osPecaRepository.findById(osPecaId)
                .orElseThrow(() -> new EntityNotFoundException("Item de peça não encontrado na OS."));

        if (!osPeca.getOrdemServico().getId().equals(osId)) {
            throw new IllegalStateException("Esta peça não pertence à Ordem de Serviço informada.");
        }

        estoqueService.darEntradaEstoque(osPeca.getPeca().getId(), osPeca.getQuantidade());

        osPecaRepository.delete(osPeca);
        os.getPecas().remove(osPeca);

        recalcularTotais(os);
    }

    @Transactional
    public OsServico adicionarServico(Long osId, Long servicoId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade do serviço deve ser maior que zero.");
        }

        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada."));
        validarEdicao(os);

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado no catálogo."));

        if (!servico.getAtivo()) {
            throw new IllegalStateException("Não é possível adicionar um serviço inativo.");
        }

        BigDecimal valorUnitario = servico.getValor();
        BigDecimal valorTotal = valorUnitario.multiply(new BigDecimal(quantidade));

        // Substituição do @Builder
        OsServico novoOsServico = new OsServico();
        novoOsServico.setOrdemServico(os);
        novoOsServico.setServico(servico);
        novoOsServico.setQuantidade(quantidade);
        novoOsServico.setValorUnitario(valorUnitario);
        novoOsServico.setValorTotal(valorTotal);

        novoOsServico = osServicoRepository.save(novoOsServico);

        os.getServicos().add(novoOsServico);
        recalcularTotais(os);

        return novoOsServico;
    }

    @Transactional
    public void removerServico(Long osId, Long osServicoId) {
        OrdemServico os = ordemServicoRepository.findById(osId)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada."));
        validarEdicao(os);

        OsServico osServico = osServicoRepository.findById(osServicoId)
                .orElseThrow(() -> new EntityNotFoundException("Item de serviço não encontrado na OS."));

        if (!osServico.getOrdemServico().getId().equals(osId)) {
            throw new IllegalStateException("Este serviço não pertence à Ordem de Serviço informada.");
        }

        osServicoRepository.delete(osServico);
        os.getServicos().remove(osServico);

        recalcularTotais(os);
    }

    /*
       MÉTODOS AUXILIARES
     */

    private void validarEdicao(OrdemServico os) {
        List<StatusOS> statusBloqueados = List.of(
                StatusOS.VERIFICANDO_ORCAMENTO,
                StatusOS.CONCLUIDA,
                StatusOS.FINALIZADA,
                StatusOS.CANCELADA
        );

        if (statusBloqueados.contains(os.getStatus())) {
            throw new IllegalStateException("A Ordem de Serviço não pode ter peças ou serviços alterados no status atual: " + os.getStatus());
        }
    }

    private void recalcularTotais(OrdemServico os) {
        BigDecimal totalPecas = BigDecimal.ZERO;
        BigDecimal totalServicos = BigDecimal.ZERO;

        for (OsPeca p : os.getPecas()) {
            totalPecas = totalPecas.add(p.getValorTotal());
        }

        for (OsServico s : os.getServicos()) {
            totalServicos = totalServicos.add(s.getValorTotal());
        }

        os.setValorTotalPecas(totalPecas);
        os.setValorTotalServicos(totalServicos);

        BigDecimal subTotal = totalPecas.add(totalServicos);
        BigDecimal totalComDesconto = subTotal.subtract(os.getValorDesconto());

        if (totalComDesconto.compareTo(BigDecimal.ZERO) < 0) {
            totalComDesconto = BigDecimal.ZERO;
        }

        os.setValorTotalOs(totalComDesconto);

        ordemServicoRepository.save(os);
    }

    /*
       DASHBOARDS E RELATÓRIOS
     */

    @Transactional(readOnly = true)
    public List<OrdemServico> listarVeiculosNoPatio() {
        return ordemServicoRepository.findByFicarNoPatioTrue();
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarOsAtrasadas() {
        List<StatusOS> statusEncerrados = List.of(StatusOS.CONCLUIDA, StatusOS.FINALIZADA, StatusOS.CANCELADA);
        return ordemServicoRepository.findByStatusNotInAndDataPrevisaoBefore(statusEncerrados, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long contarOsPorStatus(StatusOS statusAlvo) {
        if(statusAlvo == null) return 0;
        return ordemServicoRepository.countByStatus(statusAlvo);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularFaturamento(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Período de datas inválido para cálculo de faturamento.");
        }
        return ordemServicoRepository.somarFaturamentoPorPeriodo(StatusOS.FINALIZADA, inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarHistoricoVeiculo(Long veiculoId) {
        if (veiculoId == null) throw new IllegalArgumentException("ID do veículo é obrigatório.");
        return ordemServicoRepository.findByVeiculoIdOrderByDataAberturaDesc(veiculoId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTicketMedio(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal faturamento = calcularFaturamento(inicio, fim);
        long qtdOsFinalizadas = ordemServicoRepository.countByStatusAndDataConclusaoBetween(StatusOS.FINALIZADA, inicio, fim);
        if (qtdOsFinalizadas == 0)
            return BigDecimal.ZERO;
        return faturamento.divide(new BigDecimal(qtdOsFinalizadas), 2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarTodas() {
        return ordemServicoRepository.findAllComClienteEVeiculo();
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarPorId(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada. ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorStatus(StatusOS status) {
        return ordemServicoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoDevedor(Long osId) {
        OrdemServico os = buscarPorId(osId);
        BigDecimal jaPago = osPagamentoRepository.somarPagamentosPorOsId(osId);
        if (jaPago == null) jaPago = BigDecimal.ZERO; // Garantia contra retornos nulos
        return os.getValorTotalOs().subtract(jaPago);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarComDetalhesCompletos(Long id) {
        OrdemServico os = ordemServicoRepository.findByIdDetalhado(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada. ID: " + id));

        Hibernate.initialize(os.getPecas());
        Hibernate.initialize(os.getServicos());
        Hibernate.initialize(os.getPagamentos());

        return os;
    }

    @Transactional(readOnly = true)
    public List<OsPeca> listarPecasDaOs(Long osId) {
        return osPecaRepository.findByOrdemServicoIdComPeca(osId);
    }

    @Transactional(readOnly = true)
    public List<OsServico> listarServicosDaOs(Long osId) {
        return osServicoRepository.findByOrdemServicoIdComServico(osId);
    }

    @Transactional(readOnly = true)
    public List<OsPagamento> listarPagamentosDaOs(Long osId) {
        return osPagamentoRepository.findByOrdemServicoIdOrderByDataPagamentoDesc(osId);
    }
}