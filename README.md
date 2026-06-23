# GameMatch

GameMatch is a game-oriented social matching project with separate frontend and backend branches.

This `main` branch is the repository entry point. It does not contain the implementation code directly.  
Use the dedicated branches below for development and runtime.

## Branches

| Branch | Purpose | Stack |
| --- | --- | --- |
| `main` | Project overview and onboarding | Markdown |
| `dev` | Backend service | Spring Boot, JPA, MySQL, Java 17 |
| `front-end` | Frontend web application | Express, Pug, Node.js |

## Project Scope

Main business capabilities:

- User registration and login
- User profile and public information
- Team recruitment and team management
- Friend application and friend list management
- Team chat and private chat
- Notification management
- Admin-side management
- AI chat support
- File upload

## Run The Backend

Switch to the backend branch:

```bash
git checkout dev
```

Backend default port:

```text
8081
```

Backend local run:

```powershell
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

Backend runtime depends on:

- Java 17
- MySQL
- Local database `sprintpro_db`

Current backend datasource defaults:

```text
jdbc:mysql://localhost:3306/sprintpro_db
username: root
```

## Run The Frontend

Switch to the frontend branch:

```bash
git checkout front-end
```

Frontend default port:

```text
3000
```

Frontend local run:

```bash
npm install
npm start
```

Frontend stack summary:

- Express 4
- Pug
- Morgan
- Cookie Parser

## Recommended Local Workflow

1. Start backend from `dev`.
2. Start frontend from `front-end`.
3. Open `http://localhost:3000`.
4. Let frontend call backend on `http://localhost:8081`.

## Current Repository Status

- `dev` has been updated to the backend project source.
- `front-end` has been updated to the frontend project source.
- `main` is reserved for documentation and project navigation.

## Notes

- Do not place frontend and backend implementation together on `main`.
- Keep feature development in `dev` and `front-end`.
