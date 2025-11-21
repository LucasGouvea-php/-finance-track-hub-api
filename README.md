# Finance Track Hub API

API REST desenvolvida em Spring Boot para gerenciamento de finanças pessoais. Sistema completo de controle financeiro com autenticação JWT, gerenciamento de transações, categorias e dashboard com estatísticas.

## Índice

- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Executando o Projeto](#-executando-o-projeto)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Endpoints da API](#-endpoints-da-api)
- [Autenticação](#-autenticação)
- [Banco de Dados](#-banco-de-dados)

## Tecnologias

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **MySQL** - Banco de dados principal
- **H2 Database** - Banco de dados para desenvolvimento/testes
- **JWT (JSON Web Token)** - Autenticação stateless
- **Lombok** - Redução de boilerplate
- **Maven** - Gerenciamento de dependências

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21** ou superior
- **Maven 3.6+**
- **MySQL 8.0+** (ou use H2 para desenvolvimento)
- **Git** (opcional)

## Configuração

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd finance-track-hub-api
```

### 2. Configure o banco de dados

Edite o arquivo `src/main/resources/application.yml` e ajuste as configurações do banco de dados:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 3. Crie o banco de dados MySQL

```sql
CREATE DATABASE finance_track_hub;
```

**Nota:** O projeto está configurado com `ddl-auto: update`, então as tabelas serão criadas automaticamente na primeira execução.

### 4. Configure o JWT Secret (Opcional)

Por padrão, o projeto já possui um JWT secret configurado. Para produção, recomenda-se alterar o valor em `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 horas em milissegundos
```

### 5. Configure CORS (Opcional)

Ajuste as origens permitidas em `application.yml` conforme necessário:

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:8081}
```

## Executando o Projeto

### Usando Maven Wrapper (Recomendado)

**Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

### Usando Maven instalado

```bash
mvn spring-boot:run
```

### Executando o JAR

```bash
mvn clean package
java -jar target/finance-track-hub-api-0.0.1-SNAPSHOT.jar
```

A API estará disponível em: `http://localhost:${SERVER_PORT:8080}`

## Estrutura do Projeto

```
src/main/java/br/com/financetrackhub/
├── config/              # Configurações (Security, JWT)
├── controller/          # Controllers REST
├── dto/                 # Data Transfer Objects
├── entity/              # Entidades JPA
├── exception/           # Tratamento de exceções
├── filter/              # Filtros (JWT Authentication)
├── repository/          # Repositórios JPA
├── service/             # Lógica de negócio
└── StartUp.java         # Classe principal
```

## Endpoints da API

### Autenticação

#### Registrar Usuário
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123"
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@email.com"
  }
}
```

### Usuários

- `GET /api/users/me` - Obter dados do usuário autenticado
- `PUT /api/users/me` - Atualizar dados do usuário autenticado

### Transações

- `GET /api/transactions` - Listar transações (com paginação)
- `GET /api/transactions/{id}` - Obter transação por ID
- `POST /api/transactions` - Criar nova transação
- `PUT /api/transactions/{id}` - Atualizar transação
- `DELETE /api/transactions/{id}` - Deletar transação

**Exemplo de criação de transação:**
```json
{
  "type": "EXPENSE",
  "value": 150.00,
  "description": "Compra no supermercado",
  "date": "2024-01-15",
  "categoryId": 1
}
```

### Categorias

- `GET /api/categories` - Listar todas as categorias
- `GET /api/categories/{id}` - Obter categoria por ID
- `POST /api/categories` - Criar nova categoria
- `PUT /api/categories/{id}` - Atualizar categoria
- `DELETE /api/categories/{id}` - Deletar categoria

**Exemplo de criação de categoria:**
```json
{
  "name": "Alimentação",
  "description": "Gastos com alimentação"
}
```

### Dashboard

- `GET /api/dashboard` - Obter estatísticas do dashboard

**Resposta:**
```json
{
  "totalIncome": 5000.00,
  "totalExpense": 2500.00,
  "balance": 2500.00,
  "transactionsByCategory": [...],
  "recentTransactions": [...]
}
```

## Autenticação

A API utiliza JWT (JSON Web Token) para autenticação. Após fazer login ou registro, você receberá um token que deve ser enviado no header de todas as requisições protegidas:

```
Authorization: Bearer <seu_token_jwt>
```

**Exemplo com cURL:**
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
     http://localhost:8080/api/transactions
```

## Banco de Dados

### Entidades Principais

- **User** - Usuários do sistema
- **Transaction** - Transações financeiras (receitas e despesas)
- **Category** - Categorias para organização das transações

### Relacionamentos

- Um **User** pode ter várias **Transactions**
- Uma **Transaction** pertence a uma **Category**
- Uma **Category** pode ter várias **Transactions**

## Testes

Execute os testes com:

```bash
mvn test
```

## Notas de Desenvolvimento

- O projeto utiliza **Lombok** para reduzir código boilerplate
- As senhas são criptografadas usando **BCrypt**
- O JWT expira em 24 horas (configurável)
- CORS está configurado para permitir requisições do frontend
- O projeto utiliza **Spring Boot DevTools** para hot reload durante desenvolvimento

## Autor

Desenvolvido como parte do projeto Finance Track Hub.
