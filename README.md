# TaskFlow - Sistema de Gestão de Férias

Sistema full-stack para gestão de colaboradores e agendamento de férias corporativas, contando com controle de sobreposição de períodos em nível organizacional e níveis de permissão baseados em papéis (**Admin**, **Manager** e **Collaborator**).

---

## 🛠️ Stack Tecnológica

* **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, Bean Validation, SpringDoc OpenAPI (Swagger 3), Lombok
* **Frontend:** Next.js (App Router), React, TypeScript, Tailwind CSS, Lucide Icons, Axios, Date-fns
* **Banco de Dados:** PostgreSQL 15
* **Containerização & DevOps:** Docker, Docker Compose (Multi-stage builds)

---

## 🚀 Como Executar o Projeto

O projeto está totalmente containerizado e pode ser inicializado com um único comando a partir da raiz do repositório:

```bash
docker-compose up --build
```

Após a inicialização dos containers, os serviços estarão disponíveis nas seguintes portas:

Serviço				URL											Descrição
Frontend		http://localhost:3000Interface 					web do usuário
BackendAPI		http://localhost:8080API 						REST Spring Boot
Swagger UI		http://localhost:8080/swagger-ui/index.html		Documentação interativa dos endpoints
PostgreSQL		localhost:5432									Banco de dados relacional



*Documentação da API (Swagger / OpenAPI)
A API REST possui documentação interativa completa via OpenAPI 3.0:

Swagger UI: http://localhost:8080/swagger-ui/index.html

Especificação OpenAPI JSON: http://localhost:8080/v3/api-docs

Como testar requisições autenticadas no Swagger:

Execute o endpoint POST /api/auth/login informando as credenciais de um usuário.

Copie o token retornado no corpo da resposta.

Clique no botão Authorize (ícone de cadeado no topo direito), cole o token no campo correspondente e confirme.



*Contas de Teste (Carga Inicial)O banco de dados é inicializado automaticamente com usuários pré-configurados para simulação de todos os papéis:
Perfil				E-mail								Senha		Descrição / Hierarquia
ADMIN				admin@taskflow.com					123456		Acesso completo e gestão de colaboradores
MANAGER				carlos.manager@taskflow.com			123456		Gestor responsável pela equipe de desenvolvimento
COLLABORATOR		bruna.dev@taskflow.com				123456		Desenvolvedora (Liderada por Carlos)
COLLABORATOR		bruno.dev@taskflow.com				123456		Desenvolvedor (Liderado por Carlos)



*Principais Regras de Negócio & Funcionalidades
Regra de Ouro (Bloqueio de Sobreposição Global): O sistema impede que mais de um colaborador tire férias simultaneamente no mesmo período ou em intervalos com sobreposição parcial.

Alçadas de Aprovação:

Colaboradores solicitam férias e podem cancelar pedidos pendentes.

Gestores visualizam exclusivamente as solicitações de seus liderados para aprovação ou rejeição.

Administradores possuem acesso total à gestão de colaboradores e histórico geral.

Visão em Lista e Calendário: Alternância entre listagem de histórico e visão geral no calendário mensal com badges de status.

Tratamento de Períodos Rejeitados/Cancelados: Solicitações rejeitadas ou canceladas liberam imediatamente o intervalo de datas para novos agendamentos.