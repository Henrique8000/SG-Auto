package com.sgauto.app.service.estoque;

import com.sgauto.app.enums.CampoPreco;
import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.enums.TipoAjustePreco;
import com.sgauto.app.model.estoque.Fornecedor;
import com.sgauto.app.model.estoque.Modelo;
import com.sgauto.app.model.estoque.Peca;
import com.sgauto.app.repository.estoque.ModeloRepository;
import com.sgauto.app.repository.estoque.PecaRepository;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class EstoqueService {

    private final PecaRepository pecaRepository;
    private final ModeloRepository modeloRepository;
    private final VerificaPermissaoUtil permissaoUtil;

    public EstoqueService(PecaRepository pecaRepository, ModeloRepository modeloRepository, VerificaPermissaoUtil permissaoUtil) {
        this.pecaRepository = pecaRepository;
        this.modeloRepository = modeloRepository;
        this.permissaoUtil = permissaoUtil;
    }

    @Transactional
    public Peca cadastrarPeca(Peca peca) {
        if (!permissaoUtil.verificar(PermissaoChave.PECA_CRIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para cadastrar peças.");
        }
        verificarCampos(peca);
        peca.setModelo(normalizarModelo(peca.getModelo()));

        pecaRepository.findByCodigo(peca.getCodigo()).ifPresent(p -> {
            throw new IllegalArgumentException("Já existe uma peça com esse código.");
        });

        return pecaRepository.save(peca);
    }

    @Transactional
    public Peca atualizar(Peca peca) {
        if (!permissaoUtil.verificar(PermissaoChave.PECA_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para editar peças.");
        }
        verificarCampos(peca);
        peca.setModelo(normalizarModelo(peca.getModelo()));

        pecaRepository.findByCodigo(peca.getCodigo()).ifPresent(p -> {
            if (!p.getId().equals(peca.getId())) {
                throw new IllegalArgumentException("Já existe uma peça com esse código.");
            }
        });

        return pecaRepository.save(peca);
    }

    @Transactional
    public void darEntradaEstoque(Long id, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero.");
        }
        Peca peca = buscarOuFalhar(id);
        peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() + quantidade);
        pecaRepository.save(peca);
    }

    @Transactional
    public void darSaidaEstoque(Long id, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de saída deve ser maior que zero.");
        }
        Peca peca = buscarOuFalhar(id);
        int nova = peca.getQuantidadeEstoque() - quantidade;
        if (nova < 0) {
            throw new IllegalStateException("Estoque insuficiente para: " + peca.getDescricao());
        }
        peca.setQuantidadeEstoque(nova);
        pecaRepository.save(peca);
    }

    @Transactional
    public void excluir(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.PECA_EXCLUIR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para excluir peças.");
        }
        pecaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Peca> listarTodas() {
        return pecaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Peca> listarComEstoqueBaixo() {
        return listarTodas().stream()
                .filter(p -> p.getQuantidadeEstoque() <= p.getEstoqueMinimo())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Peca> listarZeradas() {
        return listarTodas().stream()
                .filter(p -> p.getQuantidadeEstoque() == 0)
                .toList();
    }

    @Transactional
    public int ajustarPrecosEmMassa(List<Long> idsPecas, TipoAjustePreco tipo, BigDecimal valor, CampoPreco campo) {
        if (!permissaoUtil.verificar(PermissaoChave.PECA_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para atualização de peças em massa.");
        }
        if (idsPecas == null || idsPecas.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma peça para ajustar.");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Informe o valor do ajuste.");
        }

        List<Peca> pecas = pecaRepository.findAllById(idsPecas);
        for (Peca peca : pecas) {
            if (campo == CampoPreco.CUSTO || campo == CampoPreco.AMBOS) {
                peca.setPrecoCusto(calcularNovoValor(peca.getPrecoCusto(), tipo, valor));
            }
            if (campo == CampoPreco.VENDA || campo == CampoPreco.AMBOS) {
                peca.setPrecoVenda(calcularNovoValor(peca.getPrecoVenda(), tipo, valor));
            }
        }
        pecaRepository.saveAll(pecas);
        return pecas.size();
    }

    @Transactional(readOnly = true)
    public Set<Fornecedor> carregarFornecedoresDaPeca(Long pecaId) {
        Peca peca = pecaRepository.findById(pecaId)
                .orElseThrow(() -> new IllegalArgumentException("Peça não encontrada."));

        peca.getFornecedores().size();

        return peca.getFornecedores();
    }

    private BigDecimal calcularNovoValor(BigDecimal valorAtual, TipoAjustePreco tipo, BigDecimal valor) {
        BigDecimal novoValor = (tipo == TipoAjustePreco.VALOR_FIXO)
                ? valorAtual.add(valor)
                : valorAtual.multiply(BigDecimal.ONE.add(valor.divide(new BigDecimal("100"))))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        if (novoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("O ajuste resultaria em preço negativo. Revise o valor informado.");
        }
        return novoValor;
    }

    public List<String> listarModelosDistintos() {
        return modeloRepository.findByAtivoTrue()
                .stream()
                .map(Modelo::getNome)
                .distinct()
                .toList();
    }

    private Peca buscarOuFalhar(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Peça não encontrada: " + id));
    }

    private String normalizarModelo(String modelo) {
        return (modelo == null || modelo.isBlank()) ? "Geral" : modelo.trim();
    }

    private void verificarCampos(Peca peca) {
        if (peca.getCodigo() == null || peca.getCodigo().isBlank()) {
            throw new IllegalArgumentException("O código da peça é obrigatório");
        }
        if (peca.getDescricao() == null || peca.getDescricao().isBlank()) {
            throw new IllegalArgumentException("A descrição da peça é obrigatória");
        }
        if (peca.getPrecoCusto() == null || peca.getPrecoCusto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço de custo não pode ser negativo");
        }
        if (peca.getPrecoVenda() == null || peca.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço de venda não pode ser negativo");
        }
        if (peca.getQuantidadeEstoque() == null || peca.getQuantidadeEstoque() < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa");
        }
        if (peca.getEstoqueMinimo() == null || peca.getEstoqueMinimo() < 0) {
            throw new IllegalArgumentException("O estoque mínimo não pode ser negativo");
        }

        modeloRepository.findByNome(peca.getModelo())
                .filter(Modelo::getAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Modelo inválido ou inativo. Selecione novamente."));
    }
}