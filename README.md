# SG-Auto

Sistema de gestão para **oficinas mecânicas de pequeno porte**. Aplicação desktop que roda de forma offline em Windows e Linux, cobrindo o dia a dia da oficina: cadastro de clientes e veículos, controle de estoque, ordens de serviço, pátio, caixa e gestão de funcionários e usuários.

---

## Sumário

- [Visão geral](#visão-geral)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Módulos](#módulos)
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
| Integração HTTP | Jackson (consulta de CEP via ViaCEP) |
| Build | Maven (`javafx-maven-plugin`) |

## Arquitetura

A aplicação segue uma arquitetura em camadas clássica, com o Spring gerenciando o ciclo de vida dos objetos e injetando as dependências:

```
Controller (FXML)  →  Service  →  Repository (Spring Data)  →  Banco (PostgreSQL)
     ↑ interface        ↑ regra de negócio   ↑ acesso a dados
```

- **Model** — entidades JPA que mapeiam as tabelas.
- **Repository** — interfaces Spring Data; consultas dinâmicas usam Specifications.
- **Service** — regra de negócio, validações e fronteiras transacionais (`@Transactional`).
- **Controller** — controladores JavaFX ligados aos arquivos `.fxml`. O Spring injeta os controllers via `setControllerFactory`, permitindo usar injeção de dependência normalmente na camada de tela.

Um ponto importante do desenho: por ser um app desktop (sem *open-session-in-view* do mundo web), as associações são carregadas de forma a evitar `LazyInitializationException` — por exemplo, um `Veiculo` conhece seu `Cliente` via `@ManyToOne` EAGER, e a busca de veículos de um cliente é feita pelo lado do veículo (`findByClienteId`), em vez de uma coleção lazy no cliente.

## Módulos

| Módulo | Descrição |
|---|---|
| **Clientes** | Pessoa Física e Jurídica num mesmo conceito, usando herança *single-table* (`Cliente` abstrata → `ClientePF` / `ClientePJ`). Documento (CPF/CNPJ) validado por dígito verificador, com suporte a CNPJ alfanumérico. |
| **Veículos** | Vinculados a um cliente (`@ManyToOne`). Placa única, validada nos formatos antigo e Mercosul. Modelo referenciado contra o catálogo. |
| **Estoque / Peças** | Cadastro de peças com preço de custo/venda, quantidade e estoque mínimo. |
| **Serviços / Categorias** | Catálogo de serviços com preço, tempo estimado e comissão, organizados por categoria. |
| **Modelos** | Catálogo de modelos de veículo, reutilizado por peças e veículos. |
| **Ordem de Serviço** | Módulo central. Amarra cliente + veículo + funcionário, com itens de peças, serviços e pagamentos. Máquina de estados via `StatusOS` (ABERTA → VERIFICANDO_ORCAMENTO → EM_EXECUCAO → AGUARDANDO → CONCLUIDA → FINALIZADA, além de CANCELADA). |
| **Pátio** | Controle de estadia de veículos, com tarifas por categoria e motivos de estadia. |
| **Caixa** | Abertura/fechamento, movimentações e configuração de caixa. |
| **Funcionários** | Cadastro completo, cargos, contratos e alocação em ordens de serviço. |
| **Usuários e Permissões** | Login, perfis de acesso e permissões granulares. |

## Banco de dados

O schema é 100% versionado com **Flyway** e a aplicação roda com `ddl-auto: validate` — ou seja, o Hibernate **não** altera o banco; ele apenas valida se as entidades batem com o schema criado pelas migrations. Toda mudança estrutural é uma migration nova.

**Nomenclatura das migrations:** o projeto usa versionamento por *timestamp* (`V<AAAAMMDDHHMMSS>__descricao.sql`), o que evita colisão de versão quando duas pessoas criam migrations em paralelo. As primeiras migrations históricas ainda usam numeração sequencial (`V1`–`V12`), por isso `flyway.out-of-order` está habilitado para os dois padrões coexistirem.

> **Regra de ouro:** migration já aplicada é **imutável**. Para corrigir algo, crie uma migration nova — nunca edite ou renomeie uma que já rodou, sob risco de quebrar a validação de checksum do Flyway.

## Segurança e permissões

O acesso é controlado por um módulo de segurança próprio:

- **Login** com senha protegida por **BCrypt**.
- **Troca de senha obrigatória** no primeiro acesso (quando marcado no usuário).
- **Permissões granulares** via o enum `PermissaoChave` (ex: `CLIENTE_CRIAR`, `OS_APROVAR`, `VEICULO_EXCLUIR`). Cada ação sensível é verificada com `VerificaPermissaoUtil.verificar(...)` contra o perfil do usuário logado, guardado em `SessaoUsuario`.

Ao criar qualquer funcionalidade nova, o padrão é: (1) definir a `PermissaoChave` correspondente, (2) inserir a permissão via migration e (3) proteger os botões/ações da tela com a verificação.

## Logs

A aplicação registra logs via **SLF4J + Logback** (configuração em `logback-spring.xml`), voltados a diagnóstico de bugs e incidentes.

- **Destinos:** console (desenvolvimento) + dois arquivos em `~/.sgauto/logs/`:
  - `sgauto.log` — fluxo completo;
  - `sgauto-erros.log` — apenas WARN/ERROR, para triagem rápida.
- **Rotação:** diária, comprimida em `.gz`, retenção de 7 dias.
- **Níveis:** código da aplicação (`com.sgauto.app`) em DEBUG; frameworks em INFO — este último para **evitar vazar dados pessoais** (CPF/CNPJ/senha) nos parâmetros de SQL, atendendo à LGPD.
- **Captura global:** um handler de exceções não tratadas (`TratadorErrosGlobal`) registra o erro e exibe ao usuário um alerta com o caminho do arquivo de log para envio ao suporte.

A pasta de logs fica em `user.home` (não na pasta da aplicação), garantindo permissão de escrita mesmo se o app estiver instalado em local protegido, e funcionando igual em Windows e Linux.

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

3. **Rode a aplicação:**

   ```bash
   mvn javafx:run
   ```

   No primeiro start, o Flyway cria todo o schema automaticamente, e — no perfil `dev` — um *seeder* popula dados de exemplo (clientes, veículo, peças, serviços).

4. **Faça login.** O ambiente de desenvolvimento já vem com usuários semeados (ver a migration `...__insert_usuarios_iniciais.sql`). As credenciais padrão de dev são de uso local apenas.

> ⚠️ **Antes de qualquer deploy real:** as senhas padrão semeadas no repositório **não** devem ir para a máquina do cliente. Configure o usuário administrador inicial para exigir troca de senha no primeiro login, ou defina a senha na instalação.

### Recriar o banco do zero (dev)

Durante o desenvolvimento, se o histórico de migrations divergir do banco local, a forma mais limpa de resolver é reconstruir (apaga os dados de teste):

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Ao subir a aplicação de novo, o Flyway aplica todas as migrations do zero.

## Convenções do projeto

- **Chave primária** sempre surrogate (`id`, `BIGSERIAL`), nunca chave natural como CPF/placa.
- **Tabelas** com prefixo `t_` (ex: `t_cliente`, `t_veiculo`).
- **Colunas** prefixadas pela entidade (ex: `cliente_nome`, `veiculo_placa`).
- **Exclusão lógica** (soft delete) via campo `ativo` — registros com histórico são desativados, não apagados. As FKs impedem exclusão física de registros em uso.
- **Timestamps** de auditoria (`data_criacao` / `data_atualizacao`) via `@PrePersist` / `@PreUpdate`.
- **Documentos e placas** armazenados apenas com dígitos/caracteres válidos (sem máscara); a formatação é aplicada só na exibição.
- **Validação de negócio** vive no Service, não no controller nem na entidade.
- **Sem `System.out` / `printStackTrace`** — toda saída passa pelo logger SLF4J.

## Estrutura de pastas

```
src/main/
├── java/com/sgauto/app/
│   ├── App.java                # bootstrap JavaFX + Spring
│   ├── controller/             # controllers por módulo (clientes, veiculos, os, patio, caixa...)
│   ├── model/                  # entidades JPA
│   ├── repository/             # interfaces Spring Data
│   ├── service/                # regra de negócio
│   ├── enums/                  # StatusOS, PermissaoChave, FormaPagamento...
│   └── util/                   # ModalUtil, SessaoUsuario, TratadorErrosGlobal, CepUtil...
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
