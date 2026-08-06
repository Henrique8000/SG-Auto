package com.sgauto.app.util.mock;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.enums.TipoContratoFuncionario;
import com.sgauto.app.model.*;
import com.sgauto.app.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ModeloRepository modeloRepository;
    private final PecaRepository pecaRepository;
    private final ServicoRepository servicoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ClienteRepository clienteRepository; // Repositório que gerencia Cliente/ClientePF
    private final VeiculoRepository veiculoRepository;

    public DataSeeder(CategoriaRepository categoriaRepository,
                      ModeloRepository modeloRepository,
                      PecaRepository pecaRepository,
                      ServicoRepository servicoRepository,
                      FuncionarioRepository funcionarioRepository,
                      ClienteRepository clienteRepository,
                      VeiculoRepository veiculoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.modeloRepository = modeloRepository;
        this.pecaRepository = pecaRepository;
        this.servicoRepository = servicoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (categoriaRepository.count() == 0) {
            System.out.println("🌱 [DEV] Iniciando o povoamento do banco de dados (Mock Data)...");

            Categoria catPeca = new Categoria("Filtros e Lubrificantes", "Óleos e filtros em geral", "PECA", true);
            Categoria catServico = new Categoria("Mecânica Geral", "Serviços de rotina e manutenção", "SERVICO", true);
            categoriaRepository.saveAll(List.of(catPeca, catServico));

            Modelo modFiltro = new Modelo("Yamaha Factor 150", "Moto street 150cc", true);
            modeloRepository.save(modFiltro);

            Peca peca1 = new Peca("FILT-001", "Filtro de Óleo Yamaha", "Factor 150",
                    new BigDecimal("18.50"), new BigDecimal("35.00"), 20, 5);
            Peca peca2 = new Peca("LUB-10W40", "Óleo Motul 10W40", "Universal",
                    new BigDecimal("45.00"), new BigDecimal("75.00"), 50, 10);
            pecaRepository.saveAll(List.of(peca1, peca2));

            Servico serv1 = new Servico("SRV-TROCA", "Troca de Óleo e Filtro", catServico.getNome(),
                    "Procedimento padrão de esgotamento e troca", new BigDecimal("40.00"),
                    30, 30, new BigDecimal("10.00"), "Limpar bem o bujão antes de recolocar", true);
            servicoRepository.save(serv1);

            Funcionario func1 = new Funcionario();
            func1.setMatricula("MAT-001");
            func1.setNomeCompleto("Carlos Mecânico da Silva");
            func1.setCpf("12345678901");
            func1.setCelular("13988887777");
            func1.setCidade("Santos");
            func1.setEstado("SP");
            func1.setCargo(CargoFuncionario.MECANICO);
            func1.setTipoContrato(TipoContratoFuncionario.CLT);
            func1.setDataAdmissao(LocalDate.of(2025, 1, 15));
            func1.setExibeEmOs(true);
            func1.setStatus(StatusFuncionario.ATIVO);
            func1.setSalarioBase(new BigDecimal("2500.00"));
            func1.setComissaoPercentual(new BigDecimal("10.00"));
            funcionarioRepository.save(func1);

            ClientePF cliente1 = new ClientePF(
                    "João Gabriel Furtado Durval",
                    "98765432100",
                    "13999999999",
                    null,
                    "joao.gabriel@email.com",
                    "Cliente prefere contato via WhatsApp",
                    true,
                    "12345678X",
                    LocalDate.of(2005, 5, 20)
            );
            clienteRepository.save(cliente1);

            Veiculo veiculo1 = new Veiculo(
                    cliente1,
                    "SNT1A23",
                    "Yamaha",
                    "Factor 150",
                    2023,
                    12500,
                    true
            );
            veiculoRepository.save(veiculo1);

            System.out.println("✅ [DEV] Banco de dados populado com sucesso!");
        }
    }
}
