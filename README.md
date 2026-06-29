# TaskFlow — Gestão de Colaboradores e Férias

Sistema full-stack para gerenciamento de colaboradores e pedidos de férias, com controle rigoroso de sobreposição de períodos.

## 🚀 Tecnologias

- **Backend:** Java 21, Spring Boot 4.1, Clean Architecture, DDD, Flyway, JJWT, MapStruct, JUnit 5, Testcontainers.
- **Frontend:** React 19, Vite, TypeScript, Tailwind CSS, TanStack Query, Zustand, FullCalendar.
- **Infra:** PostgreSQL 16, Docker, Nginx.

## 📦 Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados.

### Passo a Passo

1. **Clonar o repositório:**
   ```bash
   git clone <url-do-repositorio>
   cd projeto-TaskFlow
   ```

2. **Configurar variáveis de ambiente:**
   Copie o arquivo de exemplo e ajuste se necessário (os valores padrão funcionam para o Docker):
   ```bash
   cp .env.example .env
   ```

3. **Subir os containers:**
   ```bash
   docker-compose up --build
   ```

4. **Acessar as aplicações:**
   - **Frontend:** [http://localhost](http://localhost)
   - **Backend API:** [http://localhost:8080](http://localhost:8080)
   - **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🔑 Usuários para Teste (Demo)

O sistema já vem populado (via Flyway seed) com os seguintes usuários (a senha para **todos** é `password`):

| Perfil | Nome | Email |
| :--- | :--- | :--- |
| **Admin** | `Ana Admin` | `admin@taskflow.com` |
| **Manager** | `Marcos Manager` | `manager1@taskflow.com` |
| **Manager** | `Marta Manager` | `manager2@taskflow.com` |
| **Colaborador** | `Carla Colab` | `carla@taskflow.com` |
| **Colaborador** | `Caio Colab` | `caio@taskflow.com` |
| **Colaborador** | `Cris Colab` | `cris@taskflow.com` |

## 🏗️ Arquitetura e Decisões

### Backend
- **Clean Architecture:** Divisão clara entre `Domain` (Regras de negócio puras), `Application` (Casos de uso), `Infrastructure` (Persistência, Segurança) e `Interfaces` (Web/API).
- **DDD Light:** Uso de Agregados (`Employee`, `VacationRequest`), Value Objects (`Email`, `VacationPeriod`) e Domain Services (`OverlapPolicy`).
- **Regra de Sobreposição (RN-1):** Implementada no domínio para garantir que dois colaboradores nunca estejam de férias no mesmo dia. A validação ocorre na criação e na aprovação (re-check).

### Frontend
- **Modularização:** Separação por componentes, hooks e páginas.
- **Segurança:** Interceptores Axios para injeção de token JWT e tratamento global de erros (401/403).
- **UX:** Calendário interativo para visualização rápida das férias aprovadas.

## 🧪 Testes

A suíte de testes cobre:
- **Unitários:** Regras de domínio e lógica de Casos de Uso.
- **Integração:** Endpoints da API usando MockMvc e Testcontainers para o banco de dados.

Para rodar os testes do backend manualmente:
```bash
cd projeto-TaskFlow
./mvnw test
```

---
Desenvolvido por Junie (AI Agent) como parte do desafio técnico LBC.
