# Clinic Scheduler — Tech Challenge Fase 3 (Arquitetura e Desenvolvimento Java)

Sistema hospitalar simplificado para agendamento de consultas, histórico de pacientes e envio de lembretes automáticos, construído como um conjunto de microsserviços Spring Boot com autenticação JWT, GraphQL e comunicação assíncrona via RabbitMQ.

## Sumário

- [Visão geral e problema](#visão-geral-e-problema)
- [Arquitetura](#arquitetura)
- [Serviços](#serviços)
- [Segurança e perfis de acesso](#segurança-e-perfis-de-acesso)
- [Comunicação assíncrona](#comunicação-assíncrona)
- [Como executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [GraphQL (histórico)](#graphql-histórico)
- [Testes](#testes)
- [Collection do Postman](#collection-do-postman)

## Visão geral e problema

Em um ambiente hospitalar é essencial contar com um sistema que garanta o agendamento eficaz de consultas, o gerenciamento do histórico de pacientes e o envio de lembretes automáticos, com acesso controlado para médicos, enfermeiros e pacientes. Este projeto implementa esse backend de forma modular, escalável e segura.

## Arquitetura

```
                         ┌────────────────┐
                clientes │  api-gateway   │  :8080
                ───────► │ (Spring Cloud  │
                         │    Gateway)    │
                         └───────┬────────┘
                    ┌────────────┼───────────────┐
                    │ /api/v1/** │        /graphql│
                    ▼                             ▼
          ┌───────────────────┐        ┌──────────────────────┐
          │ agendamento-service│        │  historico-service   │
          │       :8081        │        │        :8082         │
          │  REST + JWT issuer │        │ GraphQL + JWT resource│
          └─────────┬──────────┘        │       server          │
                    │ publica evento     └──────────┬───────────┘
                    ▼ (RabbitMQ)                    │
          ┌───────────────────┐                     │
          │  RabbitMQ broker   │                     │ lê
          └─────────┬──────────┘                     │
                    │ consome evento                 ▼
                    ▼                        ┌──────────────────┐
          ┌───────────────────┐              │   PostgreSQL      │
          │ notificacao-service│◄─────────────┤  healthcare_db    │
          │       :8083         │  (mesma base)└──────────────────┘
          └───────────────────┘
```

- **api-gateway**: ponto único de entrada, roteia por path para os serviços downstream (Spring Cloud Gateway).
- **agendamento-service**: cadastro de usuários (médico/enfermeiro/paciente), autenticação (emite o JWT) e CRUD de consultas. Publica eventos no RabbitMQ quando uma consulta é criada ou editada.
- **historico-service**: expõe o histórico de consultas via GraphQL (consultas passadas, futuras, por paciente ou por médico), validando o mesmo JWT emitido pelo agendamento-service.
- **notificacao-service**: consome os eventos de consulta do RabbitMQ e dispara o lembrete ao paciente (atualmente registrado em log, simulando o envio).

Os serviços `agendamento-service` e `historico-service` compartilham o mesmo banco PostgreSQL (`healthcare_db`), pois ambos operam sobre a mesma entidade de consultas — o histórico é uma visão de leitura (GraphQL) sobre os dados gerados pelo agendamento.

## Serviços

| Serviço | Porta | Responsabilidade | Persistência |
|---|---|---|---|
| `api-gateway` | 8080 | Roteamento HTTP para os demais serviços | — |
| `agendamento-service` | 8081 | Autenticação (JWT), cadastro de usuários, criação/edição de consultas, publisher RabbitMQ | PostgreSQL |
| `historico-service` | 8082 | Consulta do histórico médico via GraphQL, resource server JWT | PostgreSQL (leitura) |
| `notificacao-service` | 8083 | Consumer RabbitMQ, envio (simulado) de lembretes | — |

## Segurança e perfis de acesso

Autenticação via **JWT** (HS256): o `agendamento-service` emite o token em `POST /api/v1/auth/login` a partir de login/senha (`BCrypt`), e tanto ele quanto o `historico-service` validam o token nas requisições subsequentes (o `historico-service` atua como *resource server* OAuth2, decodificando o mesmo segredo compartilhado).

Perfis de usuário (`tipoUsuario`): `MEDICO`, `ENFERMEIRO`, `PACIENTE`.

| Ação | Médico | Enfermeiro | Paciente |
|---|:---:|:---:|:---:|
| Cadastrar consulta | ✅ | ✅ | ❌ |
| Editar consulta | ✅ | ❌ | ❌ |
| Ver todas as consultas | ✅ | ✅ | ❌ |
| Ver consulta por ID / histórico de um paciente | ✅ | ✅ | ✅ (somente as próprias) |
| GraphQL: histórico por médico | ✅ | ✅ | ❌ |
| GraphQL: histórico/consultas futuras do paciente | ✅ | ✅ | ✅ (somente as próprias) |

A restrição "paciente só vê as próprias consultas" é reforçada na camada de serviço (não apenas na role), comparando o `subject` do JWT com o `pacienteId` solicitado.

## Comunicação assíncrona

- **Broker**: RabbitMQ, exchange `consultas.exchange` (topic).
- **Producer** (`agendamento-service`): ao criar (`consulta.criada`) ou atualizar (`consulta.atualizada`) uma consulta, publica um `ConsultaEventoDTO` no exchange.
- **Consumer** (`notificacao-service`): fila `notificacoes.queue`, associada às duas routing keys acima, processa o evento e registra o lembrete ao paciente.
- Management UI do RabbitMQ disponível em `http://localhost:15672` (usuário/senha: `guest`/`guest`).

## Como executar

### Pré-requisitos

- Docker e Docker Compose

### Subindo tudo com Docker Compose

Na raiz do projeto:

```bash
docker compose up --build
```

Isso sobe, na rede interna do compose: PostgreSQL, RabbitMQ, `agendamento-service`, `historico-service`, `notificacao-service` e `api-gateway`, já apontando uns para os outros pelos nomes dos containers.

Serviços expostos no host após subir:

| URL | Descrição |
|---|---|
| http://localhost:8080 | api-gateway (ponto de entrada recomendado) |
| http://localhost:8081 | agendamento-service (acesso direto) |
| http://localhost:8082/graphiql | historico-service — playground GraphQL |
| http://localhost:15672 | RabbitMQ management UI |

Para derrubar o ambiente:

```bash
docker compose down
```

Para remover também os dados do Postgres:

```bash
docker compose down -v
```

### Executando localmente sem Docker (modo dev)

Suba apenas a infraestrutura:

```bash
docker compose up postgres rabbitmq
```

E rode cada serviço em um terminal (Java 21 e Maven wrapper já incluídos):

```bash
cd agendamento-service && ./mvnw spring-boot:run
cd historico-service && ./mvnw spring-boot:run
cd notificacao-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

Nesse modo, os `application.yaml` já usam `localhost` como padrão (as variáveis `DB_HOST`, `RABBITMQ_HOST`, `AGENDAMENTO_SERVICE_URL` e `HISTORICO_SERVICE_URL` só são necessárias em ambientes containerizados).

## Endpoints da API

Todas as rotas abaixo podem ser acessadas via `api-gateway` (`http://localhost:8080`) ou diretamente no `agendamento-service` (`http://localhost:8081`).

### Cadastro de usuário — público

```http
POST /api/v1/usuarios
Content-Type: application/json

{
  "nome": "Dra. Ana Souza",
  "email": "ana.souza@hospital.com",
  "login": "ana.souza",
  "password": "senha123",
  "tipoUsuario": "MEDICO"
}
```

`tipoUsuario`: `MEDICO`, `ENFERMEIRO` ou `PACIENTE`.

### Login — público

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "login": "ana.souza",
  "password": "senha123"
}
```

Retorna um `AuthResponse` com o token JWT. Use-o no header `Authorization: Bearer <token>` nas demais chamadas.

### Consultas — autenticado

| Método | Rota | Perfis permitidos | Descrição |
|---|---|---|---|
| POST | `/api/v1/consultas` | MEDICO, ENFERMEIRO | Agendar consulta |
| PUT | `/api/v1/consultas/{id}` | MEDICO | Atualizar data/status/observações |
| GET | `/api/v1/consultas/{id}` | MEDICO, ENFERMEIRO, PACIENTE¹ | Buscar consulta por ID |
| GET | `/api/v1/consultas` | MEDICO, ENFERMEIRO | Listar todas as consultas |
| GET | `/api/v1/consultas/paciente/{pacienteId}` | MEDICO, ENFERMEIRO, PACIENTE¹ | Histórico de um paciente |

¹ Paciente só consegue acessar seus próprios dados.

Exemplo de criação:

```http
POST /api/v1/consultas
Authorization: Bearer <token>
Content-Type: application/json

{
  "pacienteId": 1,
  "medicoId": 2,
  "dataHora": "2026-10-01T14:30:00",
  "observacoes": "Retorno pós-cirúrgico"
}
```

## GraphQL (histórico)

Endpoint: `POST /graphql` no `historico-service` (via gateway ou diretamente em `:8082`). Playground interativo em `http://localhost:8082/graphiql`.

Requer o mesmo header `Authorization: Bearer <token>` emitido pelo `agendamento-service`.

```graphql
type Query {
  historicoPaciente(pacienteId: ID!): [Consulta!]!   # MEDICO, ENFERMEIRO, PACIENTE (próprio)
  historicoMedico(medicoId: ID!): [Consulta!]!        # MEDICO, ENFERMEIRO
  consultasFuturas(pacienteId: ID!): [Consulta!]!     # PACIENTE (próprio)
  consulta(id: ID!): Consulta                         # MEDICO, ENFERMEIRO, PACIENTE
}
```

Exemplo de query:

```graphql
query {
  historicoPaciente(pacienteId: "1") {
    id
    dataHora
    status
    diagnostico
  }
}
```

## Testes

Cada serviço possui testes unitários e/ou de integração via JUnit/Mockito (e Testcontainers no `notificacao-service`, para o consumer RabbitMQ). Para rodar os testes de um serviço:

```bash
cd agendamento-service && ./mvnw test
```

## Collection do Postman

O arquivo [`clinic-scheduler-postman-collection.json`](./clinic-scheduler-postman-collection.json), na raiz do repositório, contém os requests de cadastro de usuário, login, CRUD de consultas (agendamento-service) e queries GraphQL de histórico (historico-service) — pronto para importar no Postman (ou Insomnia) e validar os fluxos principais da API.

Para rodar a collection via linha de comando (com o ambiente já de pé via `docker compose up`), use o [Newman](https://github.com/postmanlabs/newman):

```bash
npm install -g newman
newman run clinic-scheduler-postman-collection.json
```
