# SG-Auto

Sistema de gestão para **oficinas mecânicas de pequeno porte**. Aplicação desktop que roda de forma offline, cobrindo o dia a dia da oficina: clientes e veículos, estoque e fornecedores, ordens de serviço, pátio, caixa, contas a receber e a pagar, funcionários e usuários, além de dashboard, relatório diário em PDF e backup automático.

---

## Sumário

- [Visão geral](#visão-geral)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Módulos](#módulos)
- [Relatório diário](#relatório-diário)
- [Backup](#backup)
- [Banco de dados](#banco-de-dados)
- [Segurança e permissões](#segurança-e-permissões)
- [Logs](#logs)
- [Como rodar (desenvolvimento)](#como-rodar-desenvolvimento)
- [Convenções do projeto](#convenções-do-projeto)
- [Estrutura de pastas](#estrutura-de-pastas)

---

## Visão geral

O SG-Auto foi pensado para atender o dono da oficina, mecânicos e atendentes, com foco em funcionar sem depender de internet e sem custo de serviços pagos (hospedagem, licenças). É uma aplicação **desktop com backend embutido**: a interface é feita em JavaFX, mas por baixo roda um contexto Spring Boot completo, com acesso a um banco PostgreSQL local.

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework de aplicação | Spring Boot 4.1 |
| Interface gráfica | JavaFX 25 (FXML) |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | PostgreSQL 18 |
| Versionamento de schema | Flyway |
| Segurança | Spring Security (BCrypt) |
| Logs | SLF4J + Logback |
| Geração de PDF | OpenPDF 3.0.5 |
| Integração HTTP | Jackson (consulta de CEP via ViaCEP) |
| Build | Maven (`javafx-maven-plugin`) |

## Arquitetura

A aplicação segue uma arquitetura em camadas clássica, com o Spring gerenciando o ciclo de vida dos objetos e injetando as dependências:

```
Controller (FXML)  →  Service  →  Repository (Spring Data)  →  Banco (PostgreSQL)
     ↑ interface        ↑ regra de negócio   ↑ acesso a dados
```

- **Model**: entidades JPA que mapeiam as tabelas.
- **Repository**: interfaces Spring Data; consultas dinâmicas usam Specifications.
- **Service**: regra de negócio, validações, verificação de permissões e fronteiras transacionais (`@Transactional`).
- **Controller**: controladores JavaFX ligados aos arquivos `.fxml`. O Spring injeta os controllers via `setControllerFactory`, permitindo usar injeção de dependência normalmente na camada de tela.
- **DTO**: records usados para transportar dados agregados para a tela (dashboard, relatório, pátio, financeiro).

Por ser um app desktop (sem *open-session-in-view* do mundo web), as associações são carregadas de forma a evitar `LazyInitializationException`. Por exemplo, um `Veiculo` conhece seu `Cliente` via `@ManyToOne` EAGER, e a busca de veículos de um cliente é feita pelo lado do veículo (`findByClienteId`), em vez de uma coleção lazy no cliente.

Telas que fazem consultas pesadas (dashboard, relatório diário, exportação de PDF) executam o trabalho em uma `Task` em segundo plano, para não travar a interface.

## Módulos

### Operação

| Módulo | Descrição |
|---|---|
| **Dashboard** | Visão geral da oficina: faturamento por período e por forma de pagamento, O.S. por status, ticket médio, serviços mais realizados, veículos no pátio, estoque crítico e situação do backup. |
| **Ordem de Serviço** | Módulo central. Amarra cliente + veículo + funcionário, com itens de peças, serviços e pagamentos. Máquina de estados via `StatusOS` (ABERTA → VERIFICANDO_ORCAMENTO → EM_EXECUCAO → AGUARDANDO → CONCLUIDA → FINALIZADA, além de CANCELADA). Registra as datas de abertura, conclusão, finalização e cancelamento; se uma O.S. concluída volta para uma etapa de trabalho, a data de conclusão é limpa. O preço de venda **e o custo** de cada peça são congelados no momento em que ela entra na O.S. |
| **Clientes** | Pessoa Física e Jurídica num mesmo conceito, usando herança *single-table* (`Cliente` abstrata → `ClientePF` / `ClientePJ`). Documento (CPF/CNPJ) validado por dígito verificador, com suporte a CNPJ alfanumérico. |
| **Veículos** | Vinculados a um cliente (`@ManyToOne`). Placa única, validada nos formatos antigo e Mercosul. Modelo referenciado contra o catálogo. |
| **Pátio** | Controle de estadia de veículos, com tarifas por categoria, carência e motivos de estadia. Veículos de O.S. podem permanecer no pátio sem cobrança. A saída pode ser paga à vista ou parcelada (gerando contas a receber). |

### Gestão

| Módulo | Descrição |
|---|---|
| **Estoque / Peças** | Cadastro de peças com preço de custo/venda, quantidade e estoque mínimo, ajuste de preço em massa e alerta de estoque crítico. |
| **Fornecedores** | Cadastro de fornecedores (PF/PJ), categorias de fornecedor e vínculo peça ↔ fornecedor. |
| **Modelos** | Catálogo de modelos de veículo, reutilizado por peças e veículos. |
| **Serviços / Categorias** | Catálogo de serviços com preço, tempo estimado, garantia e comissão, organizados por categoria. O valor de **mão de obra** é registrado como uma parcela do valor do serviço. |
| **Funcionários** | Cadastro completo, cargos, contratos, comissão e alocação em ordens de serviço. |
| **Caixa** | Abertura e fechamento, movimentações (venda avulsa, despesa, suprimento e sangria) e histórico de fechamentos. A conferência no fechamento é configurável (`OBRIGATORIA`, `OPCIONAL` ou `SEM_CONFERENCIA`), assim como as formas de pagamento consideradas no valor esperado da gaveta. O fechamento grava totais por categoria e por forma de pagamento. |
| **Financeiro** | Contas a receber e contas a pagar, com parcelas, vencimento, desconto, juros, multa e baixa (total ou parcial). Categorias financeiras de receita e despesa. Pagamentos de O.S., saída do pátio e vendas avulsas podem ser parcelados, gerando contas a receber; cada baixa feita pelo caixa vira uma movimentação no caixa. |
| **Relatório Diário** | Resumo financeiro e operacional de um dia, com exportação em PDF (ver [Relatório diário](#relatório-diário)). |

### Sistema

| Módulo | Descrição |
|---|---|
| **Usuários e Permissões** | Login, perfis de acesso, permissões granulares, bloqueio por tentativas e troca de senha obrigatória. |
| **Configurações** | Parâmetros do sistema (chave/valor em `t_config`): backup e regras do caixa. |

## Relatório diário

Tela voltada ao gestor, com um resumo de qualquer dia (não é possível escolher datas futuras), exportável em PDF.

**Conceitos usados:**

- **Dia de calendário, não sessão de caixa.** O relatório cobre de 00:00 a 23:59 da data escolhida, porque um caixa pode ficar aberto por vários dias.
- **Recebido x Produzido:**
    - **Recebido** (regime de caixa) é tudo o que entrou no caixa no dia (O.S., pátio, vendas avulsas e parcelas de contas a receber), sem contar suprimento.
    - **Produzido** (regime de competência) é o valor das O.S. concluídas no dia, pagas ou não.
- **Seções do dia x seções de posição:**
    - Financeiro, movimentações, fechamentos, O.S. do dia, produção por mecânico e entradas/saídas do pátio consideram apenas os eventos da data.
    - Pendências (O.S. em andamento, atrasadas e a receber) e veículos no pátio mostram a situação **ao fim daquele dia**, reconstruída pelas datas da O.S. e dos pagamentos. Para o dia de hoje, a posição é a do momento da geração.
    - Estoque crítico é sempre a posição atual.
- **Margem bruta** = produzido − custo das peças, usando o custo congelado na O.S.

**Exportação em PDF:** o PDF é gerado com OpenPDF a partir do mesmo relatório exibido na tela, sem consultar o banco de novo, então o arquivo sempre corresponde ao que o usuário viu. O documento contém dados pessoais (nome de cliente e placa); o log registra apenas a data do relatório exportado.

Acesso protegido pela permissão `RELATORIO_DIARIO_VISUALIZAR`.

## Backup

O backup usa o `pg_dump` do PostgreSQL, compacta o resultado em `.zip` e registra cada tentativa (sucesso ou falha) em `t_backup_historico`. Há três destinos:

| Tipo | Quando acontece |
|---|---|
| **Local** (pasta do PC) | Automaticamente, conforme o intervalo configurado (verificado de hora em hora), e opcionalmente após cada fechamento de caixa. |
| **Nuvem** (Google Drive) | Junto com o backup local. A aplicação copia o arquivo para uma pasta sincronizada pelo **Google Drive para computador**, que cuida do envio para a nuvem quando houver internet. |
| **Manual** (pendrive, HD externo ou qualquer pasta) | Quando o usuário pede, pela tela de Configurações. |

Para a nuvem: instale o [Google Drive para computador](https://www.google.com/intl/pt-BR/drive/download/), configure uma pasta sincronizada e informe o caminho dela nas Configurações do SG-Auto.

> O caminho da pasta `bin` do PostgreSQL (onde fica o `pg_dump`) precisa ser informado nas Configurações. Hoje o backup procura por `pg_dump.exe`, ou seja, funciona apenas no Windows.

## Banco de dados

O schema é 100% versionado com **Flyway** e a aplicação roda com `ddl-auto: validate`: o Hibernate **não** altera o banco, apenas valida se as entidades batem com o schema criado pelas migrations. Toda mudança estrutural é uma migration nova.

**Nomenclatura das migrations:** o projeto usa versionamento por *timestamp* (`V<AAAAMMDDHHMMSS>__descricao.sql`, com **dois** underscores), o que evita colisão de versão quando duas pessoas criam migrations em paralelo. As primeiras migrations ainda usam numeração sequencial (`V1` a `V11`). O `flyway.out-of-order` está habilitado para os dois padrões coexistirem e para que migrations criadas em branches paralelas, com data anterior à última aplicada, ainda sejam executadas.

Configurações de proteção no `application.yml`:

- `validate-on-migrate: true`: a aplicação não sobe se uma migration já aplicada tiver sido alterada.
- `clean-disabled: true`: impede um `flyway clean` acidental.

> **Regra de ouro:** migration já aplicada é **imutável**. Para corrigir algo, crie uma migration nova. Nunca edite ou renomeie uma que já rodou, sob risco de quebrar a validação de checksum do Flyway em todas as máquinas.

## Segurança e permissões

O acesso é controlado por um módulo de segurança próprio:

- **Login** com senha protegida por **BCrypt** e bloqueio temporário após tentativas inválidas.
- **Troca de senha obrigatória** no primeiro acesso (quando marcado no usuário).
- **Permissões granulares** via o enum `PermissaoChave` (ex.: `CLIENTE_CRIAR`, `OS_APROVAR`, `CAIXA_FECHAR`, `RELATORIO_DIARIO_VISUALIZAR`). Cada ação sensível é verificada com `VerificaPermissaoUtil.verificar(...)` contra o perfil do usuário logado, guardado em `SessaoUsuario`.
- **Rastreio:** o fechamento de caixa registra o login de quem fechou.

Ao criar qualquer funcionalidade nova, o padrão é:

1. definir a `PermissaoChave` correspondente;
2. inserir a permissão (e o vínculo com os perfis) via migration;
3. verificar a permissão no service e proteger os botões/ações da tela.

## Logs

A aplicação registra logs via **SLF4J + Logback** (configuração em `logback-spring.xml`), voltados a diagnóstico de bugs e incidentes.

- **Destinos:** console (desenvolvimento) + dois arquivos em `~/.sgauto/logs/`:
    - `sgauto.log`: fluxo completo;
    - `sgauto-erros.log`: apenas WARN/ERROR, para triagem rápida.
- **Rotação:** diária, comprimida em `.gz`, retenção de 7 dias.
- **Níveis:** código da aplicação (`com.sgauto.app`) em DEBUG; frameworks em INFO. Isso **evita vazar dados pessoais** (CPF/CNPJ/senha) nos parâmetros de SQL, atendendo à LGPD.
- **Captura global:** um handler de exceções não tratadas (`TratadorErrosGlobal`) registra o erro e exibe ao usuário um alerta com o caminho do arquivo de log para envio ao suporte.

A pasta de logs fica em `user.home` (não na pasta da aplicação), garantindo permissão de escrita mesmo se o app estiver instalado em local protegido. No Windows, o caminho é `%USERPROFILE%\.sgauto\logs`.

## Como rodar (desenvolvimento)

### Pré-requisitos

- **JDK 25**
- **PostgreSQL 18** rodando localmente
- **Maven** (ou o wrapper do projeto)

### Passos

1. **Crie o banco** no PostgreSQL:

   ```sql
   CREATE DATABASE sgauto;
   ```

2. **Configure o acesso** em `src/main/resources/application.yml` (usuário/senha do seu PostgreSQL). O perfil ativo padrão é `dev`.

   > Cada desenvolvedor costuma ter uma senha diferente no banco local. Evite commitar a troca de senha; prefira ajustar só localmente.

3. **Rode a aplicação:**

   ```bash
   mvn javafx:run
   ```

   No primeiro start, o Flyway cria todo o schema automaticamente.

4. **Faça login.** O ambiente de desenvolvimento já vem com usuários semeados (ver a migration `...__insert_usuarios_iniciais.sql`). As credenciais padrão de dev são de uso local apenas.

> ⚠️ **Antes de qualquer deploy real:** as senhas padrão semeadas no repositório **não** devem ir para a máquina do cliente. Configure o usuário administrador inicial para exigir troca de senha no primeiro login, ou defina a senha na instalação.

### Dados de exemplo (perfil `dev`)

Dois *seeders* rodam no start, apenas no perfil `dev`:

| Seeder | O que cria | Quando roda |
|---|---|---|
| `DataSeeder` | Cadastros básicos: categorias, peças, serviço, funcionário, clientes e veículos. | Banco sem categorias. |
| `DadosTransacionaisSeeder` | Uma semana de operação (de 6 dias atrás até hoje): 8 O.S. em status variados, pagamentos (inclusive parciais), 3 caixas (um com quebra de caixa), estadias no pátio e uma peça abaixo do estoque mínimo. | Banco sem nenhuma O.S. e sem nenhum caixa. |

As datas da semana de operação são relativas ao dia em que o seeder rodou, então os dados "envelhecem". Para regenerá-los, recrie o banco (abaixo).

### Recriar o banco do zero (dev)

Durante o desenvolvimento, se o histórico de migrations divergir do banco local ou se você quiser dados de exemplo novos, reconstrua o schema (apaga os dados de teste):

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Ao subir a aplicação de novo, o Flyway aplica todas as migrations do zero e os seeders repovoam o banco.

## Convenções do projeto

**Banco de dados:**

- **Chave primária** sempre surrogate (`id`, `BIGSERIAL`), nunca chave natural como CPF/placa (estas são `UNIQUE`).
- **Tabelas** com prefixo `t_` (ex.: `t_cliente`, `t_veiculo`).
- **Colunas** prefixadas pela entidade (ex.: `cliente_nome`, `veiculo_placa`).
- **Tipos:** usar `VARCHAR`, nunca `CHAR` (evita divergência de tipo no `validate`). Valores monetários em `NUMERIC` / `BigDecimal`.
- **Exclusão lógica** (soft delete) via campo `ativo`: registros com histórico são desativados, não apagados. As FKs impedem exclusão física de registros em uso.
- **Timestamps** de auditoria (`data_criacao` / `data_atualizacao`) via `@PrePersist` / `@PreUpdate`.
- **Documentos e placas** armazenados apenas com dígitos/caracteres válidos (sem máscara); a formatação é aplicada só na exibição.

**Código:**

- **Validação de negócio e permissões** vivem no Service, não no controller nem na entidade. O controller só orquestra a tela.
- **Enums** organizados por módulo (`enums/os`, `enums/financeiro`, `enums/patio`...).
- **Consultas por período** usam intervalo meio-aberto `[início, fim)`, para não perder nem duplicar eventos na virada do dia.
- **Dados de histórico** que podem mudar depois (preço de venda e custo da peça na O.S.) são congelados no momento da operação.
- **Logs:** logar o `id` das entidades, nunca o objeto inteiro (LGPD).
- **Sem `System.out` / `printStackTrace`:** toda saída passa pelo logger SLF4J.

## Estrutura de pastas

```
src/main/
├── java/com/sgauto/app/
│   ├── App.java                # bootstrap JavaFX + Spring
│   ├── controller/             # controllers por módulo (os, clientes, veiculos, patio, caixa,
│   │                           #   financeiro, estoque, relatorio, configuracoes, usuario...)
│   ├── dto/                    # records de transporte (dashboard, relatorio, financeiro, patio...)
│   ├── enums/                  # enums por módulo (os, financeiro, patio, usuario, backup...)
│   ├── model/                  # entidades JPA
│   ├── repository/             # interfaces Spring Data
│   ├── service/                # regra de negócio (subpastas: backup, estoque, financeiro, usuario)
│   └── util/                   # SessaoUsuario, VerificaPermissaoUtil, TratadorErrosGlobal,
│       │                       #   FormatoRelatorioUtil, ParcelasUtil, CepUtil...
│       └── mock/               # seeders do perfil dev
└── resources/
    ├── application.yml         # configuração (perfil, datasource, flyway, jpa)
    ├── logback-spring.xml      # configuração de logs
    ├── db/migration/           # migrations Flyway
    └── com/sgauto/app/
        ├── view/               # arquivos .fxml (telas)
        └── css/estilo.css      # tema visual
```

---

*Projeto desenvolvido como sistema de gestão para oficinas mecânicas de pequeno porte.*