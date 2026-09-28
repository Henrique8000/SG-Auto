package com.sgauto.app.service.financeiro;

import com.sgauto.app.dto.financeiro.RequisicaoContaReceberDTO;
import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.financeiro.StatusConta;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.model.financeiro.ContaReceber;
import com.sgauto.app.repository.financeiro.ContaReceberRepository;
import com.sgauto.app.service.CaixaService;
import com.sgauto.app.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContaReceberService {

    private final ContaReceberRepository contaReceberRepository;
    private final CaixaService caixaService;
    private final ClienteService clienteService;
    private final CategoriaFinanceiraService categoriaFinanceiraService;

    public ContaReceberService(ContaReceberRepository contaReceberRepository, CaixaService caixaService, ClienteService clienteService, CategoriaFinanceiraService categoriaFinanceiraService) {
        this.contaReceberRepository = contaReceberRepository;
        this.caixaService = caixaService;
        this.clienteService = clienteService;
        this.categoriaFinanceiraService = categoriaFinanceiraService;
    }

    /**
     * Cadastra uma única linha de conta a receber (uma parcela). Sempre nasce como PENDENTE,
     * independente do que vier em contaReceber.getStatus().
     */
    @Transactional
    public ContaReceber cadastrar(ContaReceber contaReceber) {
        if (contaReceber == null) {
            throw new IllegalArgumentException("Dados da conta a receber não informados.");
        }
        if (contaReceber.getDescricao() == null || contaReceber.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da conta a receber.");
        }
        if (contaReceber.getValorOriginal() == null || contaReceber.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor original deve ser maior que zero.");
        }
        if (contaReceber.getDataVencimento() == null) {
            throw new IllegalArgumentException("Informe a data de vencimento.");
        }
        if (contaReceber.getNumeroParcela() == null || contaReceber.getTotalParcelas() == null
                || contaReceber.getNumeroParcela() < 1 || contaReceber.getTotalParcelas() < 1) {
            throw new IllegalArgumentException("Número de parcela e total de parcelas devem ser maiores ou iguais a 1.");
        }
        if (contaReceber.getNumeroParcela() > contaReceber.getTotalParcelas()) {
            throw new IllegalArgumentException("O número da parcela não pode ser maior que o total de parcelas.");
        }

        ContaReceber conta = new ContaReceber();
        conta.setDescricao(contaReceber.getDescricao());
        conta.setCategoria(contaReceber.getCategoria());
        conta.setCliente(contaReceber.getCliente());
        conta.setOrdemServico(contaReceber.getOrdemServico());
        conta.setNumeroParcela(contaReceber.getNumeroParcela());
        conta.setTotalParcelas(contaReceber.getTotalParcelas());
        conta.setValorOriginal(contaReceber.getValorOriginal());
        conta.setValorDesconto(contaReceber.getValorDesconto() != null ? contaReceber.getValorDesconto() : BigDecimal.ZERO);
        conta.setValorJuros(contaReceber.getValorJuros() != null ? contaReceber.getValorJuros() : BigDecimal.ZERO);
        conta.setValorMulta(contaReceber.getValorMulta() != null ? contaReceber.getValorMulta() : BigDecimal.ZERO);
        conta.setDataVencimento(contaReceber.getDataVencimento());
        conta.setStatus(StatusConta.PENDENTE);
        conta.setOrigem(contaReceber.getOrigem() != null ? contaReceber.getOrigem() : "MANUAL");
        conta.setObservacoes(contaReceber.getObservacoes());

        return contaReceberRepository.save(conta);
    }

    // metodo para registrar contas marcadas como parcelas em outras telas do sistema
    @Transactional
    public ContaReceber cadastrarDiretoPeloSistema(RequisicaoContaReceberDTO dto){
        if (dto == null) {
            throw new IllegalArgumentException("Dados da conta a receber não informados.");
        }

        ContaReceber conta = new ContaReceber();

        conta.setDescricao(dto.getDescricao());
        conta.setOrigem("Sistema");
        conta.setDataVencimento(dto.getDataVencimentoInicial());
        conta.setTotalParcelas(dto.getQuantidadeParcelas());
        conta.setNumeroParcela(1);
        conta.setStatus(StatusConta.PENDENTE);

        BigDecimal valorEntrada = dto.getValorEntrada() != null ? dto.getValorEntrada() : BigDecimal.ZERO;
        conta.setValorOriginal(dto.getValorTotal().subtract(valorEntrada));

        String refOS = dto.getOrdemServicoId() != null ? " | Ref OS: " + dto.getOrdemServicoId() : "";
        conta.setObservacoes("Parcelamento gerado automaticamente. Origem: " + "Sistema" + refOS);

        if (dto.getClienteId() != null) {
            Cliente cliente;
            cliente = clienteService.buscarPorId(dto.getClienteId());
            conta.setCliente(cliente);
        }

        if (dto.getCategoriaFinanceiraId() != null) {
            CategoriaFinanceira categoria;
            categoria = categoriaFinanceiraService.procurarPeloId(dto.getCategoriaFinanceiraId());
            conta.setCategoria(categoria);
        }

    }

    /**
     * Gera N parcelas a partir de um valor total, dividindo igualmente e jogando qualquer
     * sobra de arredondamento (centavos) na última parcela, para a soma nunca fugir do valor total.
     * dataVencimentoBase é a data da 1ª parcela; as seguintes somam intervaloDias a cada uma.
     */
    @Transactional
    public List<ContaReceber> gerarParcelas(ContaReceber dadosBase, int totalParcelas, int intervaloDias) {
        if (dadosBase == null) {
            throw new IllegalArgumentException("Dados base da conta a receber não informados.");
        }
        if (totalParcelas < 1) {
            throw new IllegalArgumentException("Quantidade de parcelas deve ser maior ou igual a 1.");
        }
        if (dadosBase.getValorOriginal() == null || dadosBase.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Informe o valor total a ser parcelado.");
        }
        if (dadosBase.getDataVencimento() == null) {
            throw new IllegalArgumentException("Informe a data de vencimento da 1ª parcela.");
        }

        BigDecimal valorParcela = dadosBase.getValorOriginal()
                .divide(BigDecimal.valueOf(totalParcelas), 2, RoundingMode.HALF_UP);
        BigDecimal somaParcelas = valorParcela.multiply(BigDecimal.valueOf(totalParcelas));
        BigDecimal diferencaArredondamento = dadosBase.getValorOriginal().subtract(somaParcelas);

        List<ContaReceber> parcelasGeradas = new ArrayList<>();
        for (int numero = 1; numero <= totalParcelas; numero++) {
            ContaReceber parcela = new ContaReceber();
            parcela.setDescricao(dadosBase.getDescricao());
            parcela.setCategoria(dadosBase.getCategoria());
            parcela.setCliente(dadosBase.getCliente());
            parcela.setOrdemServico(dadosBase.getOrdemServico());
            parcela.setNumeroParcela(numero);
            parcela.setTotalParcelas(totalParcelas);

            BigDecimal valorDaParcela = valorParcela;
            if (numero == totalParcelas) {
                valorDaParcela = valorDaParcela.add(diferencaArredondamento);
            }
            parcela.setValorOriginal(valorDaParcela);
            parcela.setDataVencimento(dadosBase.getDataVencimento().plusDays((long) intervaloDias * (numero - 1)));
            parcela.setOrigem(dadosBase.getOrigem());
            parcela.setObservacoes(dadosBase.getObservacoes());

            parcelasGeradas.add(cadastrar(parcela));
        }
        return parcelasGeradas;
    }

    /**
     * Registra a entrada de uma venda/OS parcelada diretamente no caixa (não vira linha
     * em t_conta_receber — é dinheiro que já entrou no ato).
     */
    @Transactional
    public void registrarEntrada(OrigemMovimentacao origemVenda, FormaPagamento formaPagamento,
                                 BigDecimal valorEntrada, String descricao, Long clienteId) {
        if (valorEntrada == null || valorEntrada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor de entrada deve ser maior que zero.");
        }
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Informe a forma de pagamento da entrada.");
        }
        caixaService.registrarMovimentacao(TipoMovimentacao.ENTRADA, origemVenda, formaPagamento,
                valorEntrada, descricao, clienteId, null);
    }

    /**
     * Atualiza dados de uma conta a receber ainda não quitada/cancelada. Não altera parcelamento,
     * valor recebido nem status — isso é responsabilidade de darBaixa/cancelar.
     */
    @Transactional
    public ContaReceber atualizar(Long id, ContaReceber dadosAtualizados) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada: " + id));

        if (conta.getStatus() == StatusConta.PAGO || conta.getStatus() == StatusConta.CANCELADO) {
            throw new IllegalStateException("Não é possível editar uma conta já quitada ou cancelada.");
        }
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("Dados de atualização não informados.");
        }
        if (dadosAtualizados.getDescricao() == null || dadosAtualizados.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da conta a receber.");
        }
        if (dadosAtualizados.getValorOriginal() == null || dadosAtualizados.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor original deve ser maior que zero.");
        }
        if (dadosAtualizados.getDataVencimento() == null) {
            throw new IllegalArgumentException("Informe a data de vencimento.");
        }

        conta.setDescricao(dadosAtualizados.getDescricao());
        conta.setCategoria(dadosAtualizados.getCategoria());
        conta.setCliente(dadosAtualizados.getCliente());
        conta.setOrdemServico(dadosAtualizados.getOrdemServico());
        conta.setValorOriginal(dadosAtualizados.getValorOriginal());
        conta.setValorDesconto(dadosAtualizados.getValorDesconto() != null ? dadosAtualizados.getValorDesconto() : BigDecimal.ZERO);
        conta.setValorJuros(dadosAtualizados.getValorJuros() != null ? dadosAtualizados.getValorJuros() : BigDecimal.ZERO);
        conta.setValorMulta(dadosAtualizados.getValorMulta() != null ? dadosAtualizados.getValorMulta() : BigDecimal.ZERO);
        conta.setDataVencimento(dadosAtualizados.getDataVencimento());
        conta.setObservacoes(dadosAtualizados.getObservacoes());

        return contaReceberRepository.save(conta);
    }

    public Optional<ContaReceber> buscarPorId(Long id) {
        return contaReceberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> listarTodas() {
        return contaReceberRepository.findAllComRelacionamentos();
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> listarPorStatus(StatusConta status) {
        return contaReceberRepository.findByStatusComRelacionamentos(status);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> listarPorCliente(Long clienteId) {
        return contaReceberRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return contaReceberRepository.findByDataVencimentoBetween(inicio, fim);
    }

    /**
     * Lista as contas vencidas, atualizando o status delas para ATRASADO antes de retornar
     * (garante que a tela sempre reflita o status real, mesmo sem job agendado rodando).
     */
    public List<ContaReceber> listarVencidas() {
        atualizarStatusVencidas();
        return contaReceberRepository.findByStatus(StatusConta.ATRASADO);
    }

    /**
     * Dá baixa (total ou parcial) em uma parcela. valorRecebido é o valor pago NESTA baixa
     * (não o acumulado) — o método soma com o que já foi recebido anteriormente, se houver.
     * Gera automaticamente a movimentação de caixa correspondente.
     */
    @Transactional
    public ContaReceber darBaixa(Long id, BigDecimal valorRecebido, LocalDate dataRecebimento, FormaPagamento formaRecebimento) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada: " + id));

        if (conta.getStatus() == StatusConta.PAGO) {
            throw new IllegalStateException("Esta conta já está quitada.");
        }
        if (conta.getStatus() == StatusConta.CANCELADO) {
            throw new IllegalStateException("Não é possível dar baixa em uma conta cancelada.");
        }
        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor recebido deve ser maior que zero.");
        }
        if (dataRecebimento == null) {
            throw new IllegalArgumentException("Informe a data do recebimento.");
        }
        if (formaRecebimento == null) {
            throw new IllegalArgumentException("Informe a forma de recebimento.");
        }

        BigDecimal valorDevido = conta.getValorOriginal()
                .subtract(conta.getValorDesconto())
                .add(conta.getValorJuros())
                .add(conta.getValorMulta());

        BigDecimal jaRecebido = conta.getValorRecebido() != null ? conta.getValorRecebido() : BigDecimal.ZERO;
        BigDecimal totalRecebido = jaRecebido.add(valorRecebido);

        if (totalRecebido.compareTo(valorDevido) > 0) {
            throw new IllegalArgumentException("Valor recebido excede o valor devido da conta (restante: "
                    + valorDevido.subtract(jaRecebido) + ").");
        }

        conta.setValorRecebido(totalRecebido);
        conta.setDataRecebimento(dataRecebimento);
        conta.setFormaRecebimento(formaRecebimento);
        conta.setStatus(totalRecebido.compareTo(valorDevido) == 0 ? StatusConta.PAGO : StatusConta.PARCIAL);

        ContaReceber contaAtualizada = contaReceberRepository.save(conta);

        String descricaoMovimentacao = "Recebimento - " + conta.getDescricao()
                + " (parcela " + conta.getNumeroParcela() + "/" + conta.getTotalParcelas() + ")";
        Long clienteId = conta.getCliente() != null ? conta.getCliente().getId() : null;

        caixaService.registrarMovimentacao(TipoMovimentacao.ENTRADA, OrigemMovimentacao.CONTA_RECEBER,
                formaRecebimento, valorRecebido, descricaoMovimentacao, clienteId, null, conta.getId());

        return contaAtualizada;
    }

    /**
     * Cancela uma conta a receber - softdelete
     */
    @Transactional
    public ContaReceber cancelar(Long id, String motivo) {
        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada: " + id));

        if (conta.getStatus() == StatusConta.PAGO) {
            throw new IllegalStateException("Não é possível cancelar uma conta já quitada.");
        }
        if (conta.getStatus() == StatusConta.CANCELADO) {
            throw new IllegalStateException("Esta conta já está cancelada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        }

        conta.setStatus(StatusConta.CANCELADO);
        String observacaoAtual = conta.getObservacoes();
        String observacaoCancelamento = "Cancelada em " + LocalDate.now() + ". Motivo: " + motivo;
        conta.setObservacoes(observacaoAtual == null || observacaoAtual.isBlank()
                ? observacaoCancelamento
                : observacaoAtual + " | " + observacaoCancelamento);

        return contaReceberRepository.save(conta);
    }

    /**
     * Passa para ATRASADO toda conta PENDENTE ou PARCIAL cujo vencimento já passou.
     */
    @Transactional
    public void atualizarStatusVencidas() {
        LocalDate hoje = LocalDate.now();

        List<ContaReceber> pendentesVencidas = contaReceberRepository.findByStatusAndDataVencimentoBefore(StatusConta.PENDENTE, hoje);
        List<ContaReceber> parciaisVencidas = contaReceberRepository.findByStatusAndDataVencimentoBefore(StatusConta.PARCIAL, hoje);

        pendentesVencidas.forEach(c -> c.setStatus(StatusConta.ATRASADO));
        parciaisVencidas.forEach(c -> c.setStatus(StatusConta.ATRASADO));

        contaReceberRepository.saveAll(pendentesVencidas);
        contaReceberRepository.saveAll(parciaisVencidas);
    }
}