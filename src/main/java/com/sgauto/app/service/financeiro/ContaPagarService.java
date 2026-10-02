package com.sgauto.app.service.financeiro;

import com.sgauto.app.enums.financeiro.FormaPagamento;
import com.sgauto.app.enums.financeiro.OrigemMovimentacao;
import com.sgauto.app.enums.financeiro.StatusConta;
import com.sgauto.app.enums.financeiro.TipoMovimentacao;
import com.sgauto.app.model.financeiro.ContaPagar;
import com.sgauto.app.repository.financeiro.ContaPagarRepository;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContaPagarService {

    @Autowired
    private ContaPagarRepository contaPagarRepository;

    @Autowired
    private CaixaService caixaService;

    /**
     * Cadastra uma única linha de conta a pagar (uma parcela). Sempre nasce como PENDENTE,
     * independente do que vier em contaPagar.getStatus().
     */
    @Transactional
    public ContaPagar cadastrar(ContaPagar contaPagar) {
        if (contaPagar == null) {
            throw new IllegalArgumentException("Dados da conta a pagar não informados.");
        }
        if (contaPagar.getDescricao() == null || contaPagar.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da conta a pagar.");
        }
        if (contaPagar.getValorOriginal() == null || contaPagar.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor original deve ser maior que zero.");
        }
        if (contaPagar.getDataVencimento() == null) {
            throw new IllegalArgumentException("Informe a data de vencimento.");
        }
        if (contaPagar.getNumeroParcela() == null || contaPagar.getTotalParcelas() == null
                || contaPagar.getNumeroParcela() < 1 || contaPagar.getTotalParcelas() < 1) {
            throw new IllegalArgumentException("Número de parcela e total de parcelas devem ser maiores ou iguais a 1.");
        }
        if (contaPagar.getNumeroParcela() > contaPagar.getTotalParcelas()) {
            throw new IllegalArgumentException("O número da parcela não pode ser maior que o total de parcelas.");
        }

        ContaPagar conta = new ContaPagar();
        conta.setDescricao(contaPagar.getDescricao());
        conta.setCategoria(contaPagar.getCategoria());
        conta.setFornecedor(contaPagar.getFornecedor());
        conta.setNumeroParcela(contaPagar.getNumeroParcela());
        conta.setTotalParcelas(contaPagar.getTotalParcelas());
        conta.setValorOriginal(contaPagar.getValorOriginal());
        conta.setValorDesconto(contaPagar.getValorDesconto() != null ? contaPagar.getValorDesconto() : BigDecimal.ZERO);
        conta.setValorJuros(contaPagar.getValorJuros() != null ? contaPagar.getValorJuros() : BigDecimal.ZERO);
        conta.setValorMulta(contaPagar.getValorMulta() != null ? contaPagar.getValorMulta() : BigDecimal.ZERO);
        conta.setDataVencimento(contaPagar.getDataVencimento());
        conta.setStatus(StatusConta.PENDENTE);
        conta.setOrigem(contaPagar.getOrigem() != null ? contaPagar.getOrigem() : "MANUAL");
        conta.setObservacoes(contaPagar.getObservacoes());

        return contaPagarRepository.save(conta);
    }

    /**
     * Gera N parcelas a partir de um valor total, dividindo igualmente e jogando qualquer
     * sobra de arredondamento (centavos) na última parcela, para a soma nunca fugir do valor total.
     * dataVencimentoBase é a data da 1ª parcela; as seguintes somam intervaloDias a cada uma.
     */
    @Transactional
    public List<ContaPagar> gerarParcelas(ContaPagar dadosBase, int totalParcelas, int intervaloDias) {
        if (dadosBase == null) {
            throw new IllegalArgumentException("Dados base da conta a pagar não informados.");
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

        List<ContaPagar> parcelasGeradas = new ArrayList<>();
        for (int numero = 1; numero <= totalParcelas; numero++) {
            ContaPagar parcela = new ContaPagar();
            parcela.setDescricao(dadosBase.getDescricao());
            parcela.setCategoria(dadosBase.getCategoria());
            parcela.setFornecedor(dadosBase.getFornecedor());
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
     * Atualiza dados de uma conta a pagar ainda não quitada/cancelada. Não altera parcelamento,
     * valor pago nem status — isso é responsabilidade de darBaixa/cancelar.
     */
    @Transactional
    public ContaPagar atualizar(Long id, ContaPagar dadosAtualizados) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a pagar não encontrada: " + id));

        if (conta.getStatus() == StatusConta.PAGO || conta.getStatus() == StatusConta.CANCELADO) {
            throw new IllegalStateException("Não é possível editar uma conta já quitada ou cancelada.");
        }
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("Dados de atualização não informados.");
        }
        if (dadosAtualizados.getDescricao() == null || dadosAtualizados.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da conta a pagar.");
        }
        if (dadosAtualizados.getValorOriginal() == null || dadosAtualizados.getValorOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor original deve ser maior que zero.");
        }
        if (dadosAtualizados.getDataVencimento() == null) {
            throw new IllegalArgumentException("Informe a data de vencimento.");
        }

        conta.setDescricao(dadosAtualizados.getDescricao());
        conta.setCategoria(dadosAtualizados.getCategoria());
        conta.setFornecedor(dadosAtualizados.getFornecedor());
        conta.setValorOriginal(dadosAtualizados.getValorOriginal());
        conta.setValorDesconto(dadosAtualizados.getValorDesconto() != null ? dadosAtualizados.getValorDesconto() : BigDecimal.ZERO);
        conta.setValorJuros(dadosAtualizados.getValorJuros() != null ? dadosAtualizados.getValorJuros() : BigDecimal.ZERO);
        conta.setValorMulta(dadosAtualizados.getValorMulta() != null ? dadosAtualizados.getValorMulta() : BigDecimal.ZERO);
        conta.setDataVencimento(dadosAtualizados.getDataVencimento());
        conta.setObservacoes(dadosAtualizados.getObservacoes());

        return contaPagarRepository.save(conta);
    }

    @Transactional(readOnly = true)
    public Optional<ContaPagar> buscarPorId(Long id) {
        return contaPagarRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ContaPagar> listarTodas() {
        return contaPagarRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ContaPagar> listarPorStatus(StatusConta status) {
        return contaPagarRepository.findByStatusComRelacionamentos(status);
    }

    @Transactional(readOnly = true)
    public List<ContaPagar> listarPorFornecedor(Long fornecedorId) {
        return contaPagarRepository.findByFornecedorId(fornecedorId);
    }

    @Transactional(readOnly = true)
    public List<ContaPagar> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return contaPagarRepository.findByDataVencimentoBetween(inicio, fim);
    }

    /**
     * Busca paginada com filtros opcionais (qualquer um pode vir null/vazio).
     * O LEFT JOIN FETCH do fornecedor só é aplicado na query de dados, nunca na de count.
     */
    @Transactional(readOnly = true)
    public Page<ContaPagar> buscarComFiltros(StatusConta status, String nomeFornecedor,
                                             LocalDate vencimentoDe, LocalDate vencimentoAte,
                                             Pageable pageable) {
        Specification<ContaPagar> spec = (root, query, cb) -> {
            boolean isCount = Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());
            List<Predicate> predicados = new ArrayList<>();

            From<?, ?> fornecedor = isCount
                    ? root.join("fornecedor", JoinType.LEFT)
                    : (From<?, ?>) root.fetch("fornecedor", JoinType.LEFT);

            if (status != null) {
                predicados.add(cb.equal(root.get("status"), status));
            }
            if (nomeFornecedor != null && !nomeFornecedor.isBlank()) {
                predicados.add(cb.like(cb.lower(fornecedor.<String>get("nomeFantasia")),
                        "%" + nomeFornecedor.trim().toLowerCase() + "%"));
            }
            if (vencimentoDe != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("dataVencimento"), vencimentoDe));
            }
            if (vencimentoAte != null) {
                predicados.add(cb.lessThanOrEqualTo(root.<LocalDate>get("dataVencimento"), vencimentoAte));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
        return contaPagarRepository.findAll(spec, pageable);
    }

    /**
     * Lista as contas vencidas, atualizando o status delas para ATRASADO antes de retornar
     * (garante que a tela sempre reflita o status real, mesmo sem job agendado rodando).
     */
    @Transactional(readOnly = true)
    public List<ContaPagar> listarVencidas() {
        atualizarStatusVencidas();
        return contaPagarRepository.findByStatusComRelacionamentos(StatusConta.ATRASADO);
    }

    /**
     * Dá baixa (total ou parcial) em uma parcela. valorPago é o valor pago NESTA baixa
     * (não o acumulado) — o método soma com o que já foi pago anteriormente, se houver.
     * Gera automaticamente a movimentação de caixa correspondente (saída).
     */
    @Transactional
    public ContaPagar darBaixa(Long id, BigDecimal valorPago, LocalDate dataPagamento, FormaPagamento formaPagamento) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a pagar não encontrada: " + id));

        if (conta.getStatus() == StatusConta.PAGO) {
            throw new IllegalStateException("Esta conta já está quitada.");
        }
        if (conta.getStatus() == StatusConta.CANCELADO) {
            throw new IllegalStateException("Não é possível dar baixa em uma conta cancelada.");
        }
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor pago deve ser maior que zero.");
        }
        if (dataPagamento == null) {
            throw new IllegalArgumentException("Informe a data do pagamento.");
        }
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Informe a forma de pagamento.");
        }

        BigDecimal valorDevido = conta.getValorOriginal()
                .subtract(conta.getValorDesconto())
                .add(conta.getValorJuros())
                .add(conta.getValorMulta());

        BigDecimal jaPago = conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO;
        BigDecimal totalPago = jaPago.add(valorPago);

        if (totalPago.compareTo(valorDevido) > 0) {
            throw new IllegalArgumentException("Valor pago excede o valor devido da conta (restante: "
                    + valorDevido.subtract(jaPago) + ").");
        }

        conta.setValorPago(totalPago);
        conta.setDataPagamento(dataPagamento);
        conta.setFormaPagamento(formaPagamento);
        conta.setStatus(totalPago.compareTo(valorDevido) == 0 ? StatusConta.PAGO : StatusConta.PARCIAL);

        ContaPagar contaAtualizada = contaPagarRepository.save(conta);

        String descricaoMovimentacao = "Pagamento - " + conta.getDescricao()
                + " (parcela " + conta.getNumeroParcela() + "/" + conta.getTotalParcelas() + ")";

        caixaService.registrarMovimentacao(TipoMovimentacao.SAIDA, OrigemMovimentacao.CONTA_PAGAR,
                formaPagamento, valorPago, descricaoMovimentacao, null, null, conta.getId());

        return contaAtualizada;
    }

    /**
     * Cancela uma conta a pagar (nunca deletar fisicamente — histórico financeiro precisa ficar rastreável).
     */
    @Transactional
    public ContaPagar cancelar(Long id, String motivo) {
        ContaPagar conta = contaPagarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta a pagar não encontrada: " + id));

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

        return contaPagarRepository.save(conta);
    }

    /**
     * Passa para ATRASADO toda conta PENDENTE ou PARCIAL cujo vencimento já passou.
     */
    @Transactional
    public void atualizarStatusVencidas() {
        LocalDate hoje = LocalDate.now();

        List<ContaPagar> pendentesVencidas = contaPagarRepository.findByStatusAndDataVencimentoBefore(StatusConta.PENDENTE, hoje);
        List<ContaPagar> parciaisVencidas = contaPagarRepository.findByStatusAndDataVencimentoBefore(StatusConta.PARCIAL, hoje);

        pendentesVencidas.forEach(c -> c.setStatus(StatusConta.ATRASADO));
        parciaisVencidas.forEach(c -> c.setStatus(StatusConta.ATRASADO));

        contaPagarRepository.saveAll(pendentesVencidas);
        contaPagarRepository.saveAll(parciaisVencidas);
    }
}