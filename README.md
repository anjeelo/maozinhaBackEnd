# Mãozinha Backend

Backend da plataforma Mãozinha, uma aplicação para conectar pessoas que precisam de pequenos serviços a pessoas interessadas em realizá-los.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Security
- JWT
- Refresh Token
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Java Mail Sender
- OpenAPI e Swagger
- Bucket4j
- MapStruct

## Funcionalidades implementadas

### Autenticação

- Cadastro de usuário
- Login com JWT
- Validação de Bearer Token
- Refresh Token com rotação
- Revogação de Refresh Tokens
- Recuperação de senha por e-mail
- Redefinição de senha
- Revogação de sessões após redefinição de senha

### Usuários

- Consulta do perfil autenticado
- Atualização de perfil
- Gerenciamento de habilidades
- Reputação por avaliações
- Suspensão e reativação administrativa

### Categorias

- Listagem de categorias
- Consulta de categoria por identificador

### Serviços

- Criação de serviço
- Consulta de serviço
- Listagem de serviços abertos
- Atualização de serviço
- Atualização de status
- Cancelamento de serviço
- Associação com categoria e solicitante

### Interesses

- Demonstração de interesse em serviço
- Listagem de interessados
- Listagem dos interesses do usuário autenticado
- Desistência de interesse
- Seleção de interessado
- Rejeição de interessado

### Avaliações

- Avaliação de usuários após serviço concluído
- Consulta das avaliações de um usuário
- Atualização automática da reputação

### Administração

- Suspensão de usuário
- Reativação de usuário
- Revogação dos Refresh Tokens do usuário suspenso
- Registro de auditoria das ações administrativas

## Requisitos

Antes de executar o projeto, instale:

- JDK 21
- PostgreSQL
- Maven ou Maven Wrapper
- Git

## Banco de dados

Crie o banco:

```sql
CREATE DATABASE maozinha;
```

Execute o script SQL do projeto para criar:

- tabelas;
- constraints;
- índices;
- triggers;
- funções;
- categorias iniciais.

O Hibernate está configurado com:

```yaml
ddl-auto: validate
```

Portanto, o backend valida o banco existente, mas não cria automaticamente as tabelas.

## Configuração principal

O arquivo versionado:

```text
src/main/resources/application.yml
```

deve conter somente configurações sem credenciais reais.

Credenciais e segredos devem ser fornecidos por variáveis de ambiente.

## Variáveis de ambiente

Configure as seguintes variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
MAIL_USERNAME
MAIL_PASSWORD
```

Exemplo:

```text
DB_URL=jdbc:postgresql://localhost:5432/maozinha
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_local
JWT_SECRET=um_segredo_longo_e_aleatorio
MAIL_USERNAME=conta_remetente@gmail.com
MAIL_PASSWORD=senha_de_app_do_google
```

Nunca envie valores reais dessas variáveis para o Git.

## Configuração no IntelliJ IDEA

Abra:

```text
Run
Edit Configurations
MaozinhaApplication
Modify options
Environment variables
```

Adicione as variáveis no formato:

```text
DB_URL=jdbc:postgresql://localhost:5432/maozinha;DB_USERNAME=postgres;DB_PASSWORD=sua_senha;JWT_SECRET=seu_segredo;MAIL_USERNAME=seu_email;MAIL_PASSWORD=sua_senha_de_app
```

Depois clique em:

```text
Apply
OK
```

## Configuração do Gmail

Para envio de recuperação de senha:

1. Ative a verificação em duas etapas da conta Google.
2. Gere uma senha de app para o Mãozinha.
3. Configure o endereço da conta em `MAIL_USERNAME`.
4. Configure a senha de app em `MAIL_PASSWORD`.

Não utilize a senha normal da conta Google.

## Compilar e validar

No Windows:

```powershell
.\mvnw.cmd clean verify
```

No Linux ou macOS:

```bash
./mvnw clean verify
```

Resultado esperado:

```text
BUILD SUCCESS
```

## Executar a aplicação

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A API será disponibilizada em:

```text
http://localhost:8080
```

## Fluxo principal

O fluxo principal da plataforma é:

```text
Cadastro
Login
Criação de serviço
Demonstração de interesse
Seleção do interessado
Serviço em andamento
Serviço concluído
Avaliação
Atualização da reputação
```

## Endpoints principais

### Autenticação

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

### Usuários

```http
GET /api/users/me
GET /api/users/me/interests
GET /api/users/{id}/reviews
```

### Categorias

```http
GET /api/categories
GET /api/categories/{id}
```

### Serviços

```http
POST   /api/services
GET    /api/services
GET    /api/services/{id}
PUT    /api/services/{id}
DELETE /api/services/{id}
```

### Interesses

```http
POST  /api/services/{id}/interests
GET   /api/services/{id}/interests
PATCH /api/interests/{id}
PATCH /api/interests/{id}/select
PATCH /api/interests/{id}/reject
```

### Avaliações

```http
POST /api/services/{id}/reviews
GET  /api/users/{id}/reviews
```

### Administração

```http
PATCH /api/admin/users/{id}/suspend
PATCH /api/admin/users/{id}/reactivate
```

As rotas administrativas recebem:

```json
{
  "reason": "Motivo da ação administrativa."
}
```

## Autenticação nas rotas protegidas

Após o login, envie o access token:

```http
Authorization: Bearer SEU_ACCESS_TOKEN
```

Exemplo no PowerShell:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/users/me" `
    -Method GET `
    -Headers @{
        Authorization = "Bearer $token"
    }
```

## Refresh Token

O login retorna:

```json
{
  "accessToken": "access-token",
  "refreshToken": "refresh-token",
  "expiresIn": 900
}
```

Para renovar:

```http
POST /api/auth/refresh
```

Corpo:

```json
{
  "refreshToken": "refresh-token"
}
```

Cada utilização revoga o Refresh Token anterior e gera um novo.

## Recuperação de senha

Solicitar recuperação:

```http
POST /api/auth/forgot-password
```

Corpo:

```json
{
  "email": "usuario@email.com"
}
```

O token será enviado por e-mail.

Redefinir senha:

```http
POST /api/auth/reset-password
```

Corpo:

```json
{
  "token": "token-recebido-por-email",
  "newPassword": "NovaSenha123!"
}
```

Após a redefinição:

- o token de recuperação é marcado como usado;
- os Refresh Tokens ativos do usuário são revogados;
- a senha antiga deixa de funcionar.

## Segurança

O projeto utiliza:

- senhas protegidas com BCrypt;
- JWT de curta duração;
- Refresh Token com rotação;
- hash SHA-256 para tokens persistidos;
- tokens de recuperação de uso único;
- expiração de tokens;
- controle de acesso administrativo;
- auditoria de moderação;
- credenciais externas por variáveis de ambiente.

## Arquivos que não devem ser versionados

```text
.env
.env.*
application-dev.yml
application-local.yml
application-secrets.yml
target
.idea
```

## Verificação antes do commit

```powershell
.\mvnw.cmd clean verify
git status
git diff
git diff --cached
```

Confira se não existem:

- senhas;
- tokens JWT;
- Refresh Tokens;
- tokens de recuperação;
- credenciais do banco;
- senha de app do Gmail.

## Situação atual

O núcleo funcional do MVP está implementado:

- autenticação;
- usuários;
- categorias;
- serviços;
- interesses;
- seleção;
- avaliações;
- reputação;
- refresh token;
- recuperação de senha;
- ações administrativas sobre usuários;
- auditoria inicial de moderação.

## Próximas melhorias

- moderação de serviços;
- moderação de avaliações;
- exclusão administrativa de usuários;
- consulta paginada do histórico de auditoria;
- testes automatizados adicionais;
- documentação OpenAPI habilitada por ambiente;
- tratamento do aviso de configuração do MapStruct.