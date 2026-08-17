# TaskFlow - Sistema de Gestão de Férias

Sistema full-stack para gestão de colaboradores e pedidos de férias com controle de sobreposição e níveis de acesso (Admin, Manager, Collaborator).

---

## Stack Tecnológica

* **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, Bean Validation, SpringDoc OpenAPI (Swagger), Lombok
* **Frontend:** Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS, Lucide Icons, Axios, Date-fns
* **Banco de Dados:** PostgreSQL 15
* **Containerização & DevOps:** Docker, Docker Compose (Multi-stage builds)

---

## Como Executar o Projeto com Docker Compose

O projeto está totalmente configurado para inicializar com um único comando na raiz do repositório:

```bash
docker-compose up --build