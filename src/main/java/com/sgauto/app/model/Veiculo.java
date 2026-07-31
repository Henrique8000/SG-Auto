package com.sgauto.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "veiculo_cliente_id")
    private Cliente cliente;

    @Column(name = "veiculo_placa", nullable = false, unique = true, length = 7)
    private String placa;

    @Column(name = "veiculo_marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "veiculo_modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "veiculo_ano")
    private Integer ano;

    @Column(name = "veiculo_km")
    private Integer km;

    @Column(name = "veiculo_ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "veiculo_data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "veiculo_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Veiculo() {}

    public Veiculo(Cliente cliente, String placa, String marca, String modelo,
                   Integer ano, Integer km, Boolean ativo) {
        this.cliente = cliente;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.km = km;
        this.ativo = ativo != null ? ativo : true;
    }

    @PrePersist
    protected void aoCriar() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getKm() { return km; }
    public void setKm(Integer km) { this.km = km; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}