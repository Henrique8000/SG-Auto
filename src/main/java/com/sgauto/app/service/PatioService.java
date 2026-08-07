package com.sgauto.app.service;

import com.sgauto.app.controller.dto.patio.PatioFiltroDTO;
import com.sgauto.app.controller.dto.patio.PatioItemDashboardDTO;
import com.sgauto.app.controller.dto.patio.PatioResumoDashboardDTO;
import com.sgauto.app.enums.FormaPagamento;
import com.sgauto.app.enums.OrigemMovimentacao;
import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.enums.TipoMovimentacao;
import com.sgauto.app.model.CaixaMovimentacao;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.model.patio.EstadiaPatio;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.repository.ClienteRepository;
import com.sgauto.app.repository.OrdemServico.OrdemServicoRepository;
import com.sgauto.app.repository.VeiculoRepository;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.repository.patio.EstadiaPatioSpecifications;
import com.sgauto.app.repository.patio.MotivoEstadiaRepository;
import com.sgauto.app.repository.patio.TabelaPrecoPatioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatioService {

    private final EstadiaPatioRepository estadiaPatioRepository;
    private final TabelaPrecoPatioRepository tabelaPrecoPatioRepository;
    private final MotivoEstadiaRepository motivoEstadiaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final CaixaService caixaService;

    public PatioService(EstadiaPatioRepository estadiaPatioRepository,
                        TabelaPrecoPatioRepository tabelaPrecoPatioRepository,
                        MotivoEstadiaRepository motivoEstadiaRepository, ClienteRepository clienteRepository,
                        VeiculoRepository veiculoRepository, OrdemServicoRepository ordemServicoRepository,
                        CaixaService caixaService) {
        this.estadiaPatioRepository = estadiaPatioRepository;
        this.tabelaPrecoPatioRepository = tabelaPrecoPatioRepository;
        this.motivoEstadiaRepository = motivoEstadiaRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.caixaService = caixaService;
    }

    private static final String DESCRICAO_MOTIVO_OS = "Ordem de Serviço";
    private static final String DESCRICAO_TARIFA_PADRAO_OS = "Tarifa Padrão - Ordem de Serviço (sem cobrança)";

    @Transactional
    public EstadiaPatio registrarEntradaViaOrdemServico(OrdemServico ordemServico) {
        if (ordemServico == null) {
            throw new IllegalArgumentException("A O.S. informada é obrigatória para gerar uma estadia no pátio.");
        }

        Cliente cliente = ordemServico.getCliente();
        Veiculo veiculo = ordemServico.getVeiculo();

        if (cliente == null || veiculo == null) {
            throw new IllegalStateException("A O.S. de id " + ordemServico.getId() + " está sem cliente ou veículo vinculado.");
        }

        if (veiculo.getCliente() == null || !veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalStateException("Inconsistência: o veículo da O.S. de id " + ordemServico.getId() + " não pertence ao cliente da mesma O.S.");
        }

        if (estadiaPatioRepository.existsByVeiculoIdAndStatus(veiculo.getId(), StatusEstadiaPatio.NO_PATIO)) {
            throw new IllegalStateException("O veículo de id " + veiculo.getId() + " já possui uma estadia em aberto no pátio.");
        }

        MotivoEstadia motivoOs = motivoEstadiaRepository.findByNome(DESCRICAO_MOTIVO_OS)
                .orElseThrow(() -> new IllegalStateException("Motivo protegido '" + DESCRICAO_MOTIVO_OS + "' não encontrado. Verifique as migrations."));

        TabelaPrecoPatio tarifaOs = tabelaPrecoPatioRepository.findByDescricao(DESCRICAO_TARIFA_PADRAO_OS)
                .orElseThrow(() -> new IllegalStateException("Tarifa padrão de O.S. não encontrada. Verifique as migrations."));

        EstadiaPatio novaEstadia = new EstadiaPatio(veiculo, cliente, ordemServico, veiculo.getPlaca(), tarifaOs, motivoOs, null);

        return estadiaPatioRepository.save(novaEstadia);
    }

    @Transactional
    public EstadiaPatio registrarEntradaManual(Long clienteId, Long veiculoId, Long ordemServicoId,
                                               Long tarifaId, Long motivoId, String localizacao) {
        if (clienteId == null || tarifaId == null || motivoId == null || localizacao == null || veiculoId == null) {
            throw new IllegalArgumentException("Foram identificados campos nulos ao tentar gerar uma estadia manual no pátio.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível dar entrada manual. Cliente de ID " + clienteId + " não localizado."));
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível dar entrada manual. Veículo de ID " + veiculoId + " não localizado."));


        if (veiculo.getCliente() == null || !veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("O veículo de id " + veiculoId + " não pertence ao cliente de id " + clienteId + ".");
        }

        OrdemServico os = null;
        if (ordemServicoId != null) {
            os = ordemServicoRepository.findById(ordemServicoId)
                    .orElseThrow(() -> new IllegalArgumentException("Não foi possível dar entrada manual. O.S. de ID " + ordemServicoId + " não localizada."));
        }

        if (estadiaPatioRepository.existsByVeiculoIdAndStatus(veiculo.getId(), StatusEstadiaPatio.NO_PATIO)) {
            throw new IllegalStateException("Já existe uma estadia em aberto no pátio para este veículo.");
        }

        TabelaPrecoPatio tarifa = tabelaPrecoPatioRepository.findById(tarifaId)
                .orElseThrow(() -> new IllegalArgumentException("Tabela de preço de id " + tarifaId + " não localizada."));

        MotivoEstadia motivo = motivoEstadiaRepository.findById(motivoId)
                .orElseThrow(() -> new IllegalArgumentException("Motivo de id " + motivoId + " não localizado."));

        EstadiaPatio novaEstadiaManual = new EstadiaPatio(veiculo, cliente, os, veiculo.getPlaca(), tarifa, motivo, localizacao);

        return estadiaPatioRepository.save(novaEstadiaManual);
    }

    @Transactional
    public EstadiaPatio registrarSaida(Long estadiaId, FormaPagamento formaPagamento) {
        if (estadiaId == null)
            throw new IllegalArgumentException("Não foi possível dar saída do pátio pois o ID da estadia está nulo.");

        EstadiaPatio es = estadiaPatioRepository.findById(estadiaId)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível dar saída do pátio. Estadia de ID " + estadiaId + " não foi localizada."));

        if (es.getStatus() != StatusEstadiaPatio.NO_PATIO)
            throw new IllegalStateException("Estadia de ID " + estadiaId + " já foi finalizada anteriormente.");

        BigDecimal valorTotal = calcularValorEstadia(es);

        FormaPagamento formaPagamentoFinal;
        if (valorTotal.compareTo(BigDecimal.ZERO) == 0) {
            formaPagamentoFinal = FormaPagamento.ISENTO;
        } else {
            if (formaPagamento == null || formaPagamento == FormaPagamento.ISENTO)
                throw new IllegalArgumentException("É necessário informar a forma de pagamento para dar saída de uma estadia com valor devido.");
            formaPagamentoFinal = formaPagamento;
        }

        String descricao = "Pátio - saída do veículo placa " + es.getPlaca();
        Long clienteId = es.getCliente() != null ? es.getCliente().getId() : null;

        CaixaMovimentacao movimentacao = caixaService.registrarMovimentacao(
                TipoMovimentacao.ENTRADA, OrigemMovimentacao.PATIO, formaPagamentoFinal,
                valorTotal, descricao, clienteId, es.getPlaca());
        movimentacao.setReferenciaId(es.getId());

        es.setValorTotal(valorTotal);
        es.setDataSaida(LocalDateTime.now());
        es.setStatus(StatusEstadiaPatio.FINALIZADO);

        return estadiaPatioRepository.save(es);
    }

    public BigDecimal calcularValorEstadia(EstadiaPatio estadia) {
        if (estadia == null)
            throw new IllegalStateException("Não foi possível calcular o valor da estadia pois a movimentação esta nula.");

        TabelaPrecoPatio tp = estadia.getTarifa();
        if (tp == null)
            throw new IllegalStateException("A estadia de ID " + estadia.getId() + " não possui tarifa vinculada.");

        BigDecimal valorDiaria = tp.getValorDiaria();
        Integer diasCarencia = tp.getDiasCarencia();
        LocalDateTime dataEntrada = estadia.getDataEntrada();
        LocalDateTime dataReferencia = estadia.getDataSaida() != null ? estadia.getDataSaida() : LocalDateTime.now();

        Duration duracao = Duration.between(dataEntrada, dataReferencia);
        long diarias = (long) Math.ceil(duracao.toMinutes() / 1440.0);
        long diasCobrados = Math.max(0, diarias - diasCarencia);

        return valorDiaria.multiply(BigDecimal.valueOf(diasCobrados));
    }

    @Transactional
    public EstadiaPatio atualizarLocalizacao(Long estadiaId, String novaLocalizacao) {
        EstadiaPatio es = estadiaPatioRepository.findById(estadiaId)
                .orElseThrow(() -> new IllegalArgumentException("Estadia de ID " + estadiaId + " não localizada."));
        es.setLocalizacao(novaLocalizacao);
        return estadiaPatioRepository.save(es);
    }

    @Transactional(readOnly = true)
    public boolean possuiEstadiaAberta(Long ordemServicoId) {
        if (ordemServicoId == null) return false;
        return estadiaPatioRepository.findByOrdemServicoIdAndStatus(ordemServicoId, StatusEstadiaPatio.NO_PATIO).isPresent();
    }

    @Transactional(readOnly = true)
    public EstadiaPatio buscarEstadiaPorOrdemServico(Long ordemServicoId) {
        if (ordemServicoId == null)
            throw new IllegalArgumentException("O ID da O.S. é obrigatório para buscar a estadia vinculada.");

        return estadiaPatioRepository.findByOrdemServicoIdAndStatus(ordemServicoId, StatusEstadiaPatio.NO_PATIO)
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma estadia em aberto encontrada para a O.S. de ID " + ordemServicoId + "."));
    }

    @Transactional(readOnly = true)
    public PatioItemDashboardDTO buscarItemPorId(Long estadiaId) {
        EstadiaPatio es = estadiaPatioRepository.findById(estadiaId)
                .orElseThrow(() -> new IllegalArgumentException("Estadia de ID " + estadiaId + " não localizada."));
        return montarItemDashboard(es);
    }

    @Transactional(readOnly = true)
    public List<PatioItemDashboardDTO> listarHistoricoPorOrdemServico(Long ordemServicoId) {
        if (ordemServicoId == null)
            throw new IllegalArgumentException("O ID da O.S. é obrigatório para buscar o histórico de estadias.");

        return estadiaPatioRepository.findByOrdemServicoIdOrderByDataEntradaDesc(ordemServicoId).stream()
                .map(this::montarItemDashboard)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatioResumoDashboardDTO obterResumoDashboard() {
        List<EstadiaPatio> abertas = estadiaPatioRepository.findByStatus(StatusEstadiaPatio.NO_PATIO);

        BigDecimal valorTotalEstimado = abertas.stream()
                .map(this::calcularValorEstadia)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double tempoMedioHoras = abertas.stream()
                .mapToLong(e -> Duration.between(e.getDataEntrada(), LocalDateTime.now()).toHours())
                .average()
                .orElse(0.0);

        return new PatioResumoDashboardDTO(abertas.size(), valorTotalEstimado, tempoMedioHoras);
    }

    @Transactional(readOnly = true)
    public Page<PatioItemDashboardDTO> listarPatioAtualPaginado(PatioFiltroDTO filtro, Pageable pageable) {
        filtro.setStatus(StatusEstadiaPatio.NO_PATIO);
        return estadiaPatioRepository.findAll(EstadiaPatioSpecifications.comFiltro(filtro), pageable)
                .map(this::montarItemDashboard);
    }

    /**
     * Histórico paginado — mostra tudo (aberto e finalizado), filtro de status é opcional.
     */
    @Transactional(readOnly = true)
    public Page<PatioItemDashboardDTO> listarHistoricoPaginado(PatioFiltroDTO filtro, Pageable pageable) {
        return estadiaPatioRepository.findAll(EstadiaPatioSpecifications.comFiltro(filtro), pageable)
                .map(this::montarItemDashboard);
    }

    private PatioItemDashboardDTO montarItemDashboard(EstadiaPatio estadia) {
        BigDecimal valor = estadia.getStatus() == StatusEstadiaPatio.NO_PATIO
                ? calcularValorEstadia(estadia)
                : estadia.getValorTotal();

        return new PatioItemDashboardDTO(
                estadia.getId(),
                estadia.getPlaca(),
                estadia.getCliente().getNome(),
                estadia.getMotivo() != null ? estadia.getMotivo().getNome() : null,
                estadia.getOrdemServico() != null ? estadia.getOrdemServico().getId() : null,
                estadia.getDataEntrada(),
                estadia.getDataSaida(),
                estadia.getStatus(),
                valor,
                estadia.getLocalizacao()
        );
    }
}