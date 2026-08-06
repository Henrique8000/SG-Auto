package com.sgauto.app.model.patio;

import com.sgauto.app.enums.StatusEstadiaPatio;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.OrdemServico.OrdemServico;
import com.sgauto.app.model.Veiculo;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_estadia_patio")
public class EstadiaPatio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_ordem_servico_id")
    private OrdemServico ordemServico;

    @Column(name = "estadia_placa", length = 10)
    private String placa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_tarifa_id")
    private TabelaPrecoPatio tarifa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estadia_motivo_id")
    private MotivoEstadia motivo;

    @Column(name = "estadia_data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "estadia_data_saida")
    private LocalDateTime dataSaida;

    @Column(name = "estadia_localizacao", length = 50)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "estadia_status", nullable = false, length = 20)
    private StatusEstadiaPatio status;

    @Column(name = "estadia_valor_total")
    private BigDecimal valorTotal;

    public EstadiaPatio() {}

    public EstadiaPatio(Veiculo veiculo, Cliente cliente, OrdemServico ordemServico, String placa,
                        TabelaPrecoPatio tarifa, MotivoEstadia motivo, String localizacao) {
        this.veiculo = veiculo;
        this.cliente = cliente;
        this.ordemServico = ordemServico;
        this.placa = placa;
        this.tarifa = tarifa;
        this.motivo = motivo;
        this.localizacao = localizacao;
        this.status = StatusEstadiaPatio.NO_PATIO;
    }

    @PrePersist
    protected void aoCriar() {
        if (dataEntrada == null) {
            dataEntrada = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public TabelaPrecoPatio getTarifa() { return tarifa; }
    public void setTarifa(TabelaPrecoPatio tarifa) { this.tarifa = tarifa; }

    public MotivoEstadia getMotivo() { return motivo; }
    public void setMotivo(MotivoEstadia motivo) { this.motivo = motivo; }

    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public StatusEstadiaPatio getStatus() { return status; }
    public void setStatus(StatusEstadiaPatio status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}