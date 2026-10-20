<div align="center">

# 🧠 AI Resume Analyzer

**An AI-powered, full-stack platform that scores resumes, runs ATS compatibility checks, detects skill gaps and recommends matching job roles.**

Java 21 · Spring Boot 3 · Spring Security (JWT) · PostgreSQL · React + Vite · TailwindCSS · Docker

</div>

---

## 📑 Table of Contents

1. [Overview](#-overview)
2. [Features](#-features)
3. [Architecture](#-architecture)
4. [Tech Stack](#-tech-stack)
5. [Project Structure](#-project-structure)
6. [Quick Start (Docker)](#-quick-start-docker)
7. [Local Development](#-local-development)
8. [Environment Variables](#-environment-variables)
9. [Demo Credentials](#-demo-credentials)
10. [API Documentation](#-api-documentation)
11. [Database Design](#-database-design)
12. [Security](#-security)
13. [Screenshots](#-screenshots)
14. [License](#-license)

---

## 🔍 Overview

AI Resume Analyzer lets a user upload a PDF resume and instantly receive a
comprehensive, AI-assisted assessment:

- **Resume score (0–100)** and **ATS compatibility score**
- **Missing keywords** and **weak sections**
- **Grammar / professionalism** and **rewrite recommendations**
- **Skills gap analysis** and a **personalised learning roadmap**
- **Suggested job roles** with **salary estimates** and **career roadmaps**
- **Keyword optimization** suggestions

The platform ships with a deterministic local analyzer so it is **fully
functional with or without an OpenAI API key** — ideal for demos, offline
development and CI.

---

## ✨ Features

| Area | Highlights |
|------|------------|
| **Authentication** | JWT access + refresh tokens, register, login, logout, refresh-token rotation, forgot/reset password, role-based access (USER / ADMIN) |
| **Resume Upload** | Drag-and-drop, PDF validation, 5 MB size limit, secure per-user storage, version tracking |
| **Parsing** | Name, email, phone, skills, experience, education, certifications, projects, keywords (Apache PDFBox) |
| **AI Analysis** | Scoring, ATS check, weaknesses, rewrite advice, skills gap, career roadmap (OpenAI + local fallback) |
| **Dashboard** | Score rings, trend charts, skills distribution, missing-skill chips |
| **Job Match** | Role-fit radar, salary estimates, skill gaps, roadmaps |
| **Admin Panel** | Platform analytics, user management, upload trends, enable/disable & delete users |
| **UX** | Glassmorphism, dark/light theme, framer-motion animations, fully responsive |

---

## 🏗 Architecture

```
                ┌──────────────┐         ┌────────────────────┐
   Browser ───▶ │  React (SPA) │ ──/api─▶│  Spring Boot API   │
                │  Vite/Tailwind│         │  (JWT secured)     │
                └──────────────┘         └─────────┬──────────┘
                                                   │
                              ┌────────────────────┼───────────────────┐
                              ▼                     ▼                   ▼
                       ┌────────────┐     ┌──────────────┐     ┌───────────────┐
                       │ PostgreSQL │     │  PDFBox parse │     │  OpenAI API   │
                       │  (JPA)     │     │  + heuristics │     │  (+ fallback) │
                       └────────────┘     └──────────────┘     └───────────────┘
```

**Request flow for an analysis:** upload → validate & store file → extract text
(PDFBox) → heuristic parse → persist `Resume` + `Skill` + `ResumeVersion` →
create `PENDING` `ResumeAnalysis` → **async** AI call → persist scores &
`JobRecommendation` rows → frontend polls until `COMPLETED`.

The backend follows a clean layered architecture:
`controller → service → repository → entity`, with DTOs at the boundary, a
global exception handler, and stateless JWT security.

---

## 🧰 Tech Stack

**Backend:** Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA,
PostgreSQL, JJWT, Apache PDFBox 3, Spring WebFlux (OpenAI client), Lombok,
Bean Validation, springdoc-openapi (Swagger UI), Maven.

**Frontend:** React 18, Vite 5, TailwindCSS 3, React Router 6, React Hook Form,
Axios (with refresh-token interceptor), Recharts, Framer Motion,
react-dropzone, react-hot-toast, lucide-react.

**DevOps:** Docker multi-stage builds, Docker Compose, nginx reverse proxy,
environment-driven config, non-root containers.

---

## 📂 Project Structure

```
ai-resume-analyzer/
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
├── docs/
│   ├── API.md
│   └── ARCHITECTURE.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/airesume/analyzer/
│       │   ├── config/        # Security, CORS, OpenAPI, async, seeder
│       │   ├── controller/    # REST endpoints
│       │   ├── dto/           # Request/response records
│       │   ├── entity/        # JPA entities + enums
│       │   ├── exception/     # Global handler + custom exceptions
│       │   ├── repository/    # Spring Data repositories
│       │   ├── security/      # JWT provider, filter, user details
│       │   ├── service/       # Business logic + OpenAI integration
│       │   └── util/          # PDF extractor, parser, security utils
│       └── resources/
│           └── application.yml
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── api/         # Axios client + service modules
        ├── components/  # UI kit, charts, layout, route guards
        ├── context/     # Auth + Theme providers
        └── pages/       # Landing, auth, dashboard, upload, admin…
```

---

## 🚀 Quick Start (Docker)

**Prerequisites:** Docker + Docker Compose.

```bash
git clone <your-repo-url> ai-resume-analyzer
cd ai-resume-analyzer

cp .env.example .env
# (optional) edit .env to set OPENAI_API_KEY and a strong JWT_SECRET

docker compose up --build
```

That's it — one command starts PostgreSQL, the backend and the frontend.

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

> Without an `OPENAI_API_KEY`, the built-in deterministic analyzer is used so
> the full experience still works end-to-end.

---

## 💻 Local Development

### Backend

```bash
cd backend
cp .env.example .env          # configure DB + secrets
# Start a local PostgreSQL, or: docker compose up postgres -d
./mvnw spring-boot:run        # or: mvn spring-boot:run
```

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev                   # http://localhost:5173 (proxies /api → :8080)
```

---

## 🔐 Environment Variables

See [`.env.example`](.env.example) (root, for Docker), plus
[`backend/.env.example`](backend/.env.example) and
[`frontend/.env.example`](frontend/.env.example).

Key variables: `POSTGRES_*`, `JWT_SECRET`, `OPENAI_API_KEY`, `OPENAI_MODEL`,
`OPENAI_ENABLED`, `CORS_ALLOWED_ORIGINS`, `FILE_STORAGE_LOCATION`.

**No secrets are hardcoded** — every sensitive value is supplied via the
environment.

---

## 🔑 Demo Credentials

Seeded automatically on first start:

| Role  | Email                  | Password      |
|-------|------------------------|---------------|
| Admin | `admin@airesume.dev`   | `Admin@12345` |
| User  | `demo@airesume.dev`    | `Demo@12345`  |

---

## 📡 API Documentation

Interactive docs are available at **`/swagger-ui.html`**. A written reference
lives in [`docs/API.md`](docs/API.md). Summary:

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | – | Register |
| POST | `/api/auth/login` | – | Login |
| POST | `/api/auth/refresh` | – | Rotate tokens |
| POST | `/api/auth/logout` | – | Revoke refresh token |
| POST | `/api/auth/forgot-password` | – | Request reset |
| POST | `/api/auth/reset-password` | – | Reset password |
| GET | `/api/users/me` | ✅ | Current user |
| POST | `/api/resumes/upload` | ✅ | Upload + analyze |
| GET | `/api/resumes` | ✅ | List my resumes |
| GET | `/api/resumes/{id}` | ✅ | Resume + analysis |
| POST | `/api/resumes/{id}/reanalyze` | ✅ | Re-run analysis |
| DELETE | `/api/resumes/{id}` | ✅ | Delete resume |
| GET | `/api/dashboard` | ✅ | Dashboard stats |
| GET | `/api/admin/users` | 🛡 ADMIN | List users |
| GET | `/api/admin/resumes` | 🛡 ADMIN | All resumes |
| GET | `/api/admin/stats` | 🛡 ADMIN | Platform analytics |
| DELETE | `/api/admin/users/{id}` | 🛡 ADMIN | Delete user |
| PATCH | `/api/admin/users/{id}/toggle` | 🛡 ADMIN | Enable/disable |

---

## 🗄 Database Design

Normalized entities (see [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)):

`User` 1—N `Resume` 1—1 `ResumeAnalysis` 1—N `JobRecommendation`
`Resume` 1—N `Skill`, 1—N `ResumeVersion`
`User` 1—N `RefreshToken`, 1—N `FeedbackHistory`

All tables carry `created_at` / `updated_at` audit columns via a shared
`BaseEntity` with JPA auditing.

---

## 🛡 Security

- BCrypt (strength 12) password hashing
- Stateless JWT auth with short-lived access + rotating refresh tokens
- Method- and URL-level role enforcement (`@PreAuthorize`, security filter chain)
- Strict Bean Validation on every request DTO
- PDF-only upload validation, size limits, path-traversal-safe storage
- Configurable CORS allow-list
- User-enumeration-safe forgot-password flow
- Non-root Docker containers, environment-only secrets

---

## 🖼 Screenshots

> Replace the placeholders below with real screenshots after running the app.

| Landing | Dashboard | Resume Analysis | Admin |
|---------|-----------|-----------------|-------|
| `docs/screenshots/landing.png` | `docs/screenshots/dashboard.png` | `docs/screenshots/analysis.png` | `docs/screenshots/admin.png` |

---

## 📄 License

Released under the MIT License — free for educational and portfolio use.
