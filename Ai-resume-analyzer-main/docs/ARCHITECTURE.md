# Architecture — AI Resume Analyzer

## 1. High-level design

The system is a classic three-tier application packaged as three Docker
services orchestrated by Docker Compose:

```
React SPA  ──/api──▶  Spring Boot REST API  ──▶  PostgreSQL
                              │
                              ├──▶ Apache PDFBox (text extraction)
                              └──▶ OpenAI API (with deterministic fallback)
```

### Backend layering

```
controller   → thin HTTP layer, validation, returns ApiResponse<T>
   │
service      → business logic, transactions, orchestration
   │
repository   → Spring Data JPA persistence
   │
entity       → JPA domain model (audited via BaseEntity)
```

Cross-cutting concerns:

- **security/** — stateless JWT: `JwtTokenProvider`, `JwtAuthenticationFilter`,
  `UserPrincipal`, `CustomUserDetailsService`, `JwtAuthenticationEntryPoint`.
- **config/** — `SecurityConfig` (filter chain + CORS + method security),
  `OpenApiConfig`, `AsyncConfig` (dedicated executor for analysis),
  `JpaAuditingConfig`, `DataSeeder` (demo accounts).
- **exception/** — `GlobalExceptionHandler` maps domain exceptions to a
  consistent JSON error envelope.
- **dto/** — Java `record` request/response objects so entities never leak
  across the API boundary.

### Asynchronous analysis pipeline

1. `ResumeController` receives the multipart upload.
2. `FileStorageService` validates (PDF, size, path safety) and stores the file
   under `storage/resumes/user-{id}/{uuid}.pdf`.
3. `PdfTextExtractor` (PDFBox) extracts raw text.
4. `ResumeParser` performs deterministic heuristic extraction (name, email,
   phone, skills, sections, keywords).
5. `Resume`, `Skill` and `ResumeVersion` rows are persisted; a `PENDING`
   `ResumeAnalysis` is created.
6. `AnalysisService.runAnalysisAsync` (on `analysisExecutor`) calls
   `OpenAiService`. If no API key is configured or the call fails, the
   `LocalAnalyzer` produces a deterministic, structured result so the product
   is never broken.
7. Scores, feedback and `JobRecommendation` rows are saved; status becomes
   `COMPLETED` (or `FAILED` with a message).
8. The frontend polls `GET /resumes/{id}` every ~2.5 s until completion.

This design keeps uploads fast and the request thread free while the
(potentially slow) AI call runs in the background.

---

## 2. Database design

All entities extend `BaseEntity` (`created_at`, `updated_at` via JPA auditing).

| Entity | Key fields | Relationships |
|--------|------------|---------------|
| **User** | email (unique), password (BCrypt), role, enabled, resetToken | 1—N Resume, 1—N RefreshToken, 1—N FeedbackHistory |
| **Resume** | originalFilename, storedPath, version, parsed fields, rawText | N—1 User; 1—1 ResumeAnalysis; 1—N Skill; 1—N ResumeVersion |
| **ResumeAnalysis** | status, resumeScore, atsScore, feedback fields | 1—1 Resume; 1—N JobRecommendation |
| **Skill** | name, category, present, relevanceScore | N—1 Resume |
| **ResumeVersion** | versionNumber, storedPath, scores | N—1 Resume |
| **JobRecommendation** | jobTitle, matchPercentage, salaryEstimate, roadmap | N—1 ResumeAnalysis |
| **FeedbackHistory** | action, resumeId, scores, snapshot | N—1 User |
| **RefreshToken** | token (unique), expiryDate, revoked | N—1 User |

### Entity-relationship diagram

```
User ───1:N──▶ Resume ───1:1──▶ ResumeAnalysis ───1:N──▶ JobRecommendation
  │               │
  │               ├──1:N──▶ Skill
  │               └──1:N──▶ ResumeVersion
  ├──1:N──▶ RefreshToken
  └──1:N──▶ FeedbackHistory
```

Hibernate `ddl-auto: update` builds the schema on first run; for production a
Flyway/Liquibase migration baseline is recommended.

---

## 3. Security model

- **Hashing:** BCrypt strength 12.
- **Tokens:** HMAC-signed JWT. Short-lived access token (15 min default) +
  rotating refresh token (7 days) persisted and revocable.
- **Authorization:** URL rules in `SecurityConfig` plus `@PreAuthorize` on the
  admin controller; `ROLE_USER` / `ROLE_ADMIN` authorities.
- **Input:** Jakarta Bean Validation on every request record.
- **Uploads:** content-type + extension + size checks; stored filenames are
  server-generated UUIDs and path-traversal is rejected.
- **Secrets:** sourced exclusively from environment variables.

---

## 4. Frontend architecture

- **State:** `AuthContext` (session bootstrap, login/register/logout) and
  `ThemeContext` (dark/light, persisted).
- **HTTP:** a single Axios instance with a request interceptor (Bearer token)
  and a response interceptor that transparently refreshes expired access
  tokens and retries the original request once.
- **Routing:** `react-router-dom` with `ProtectedRoute` / `AdminRoute` guards
  wrapping an `AppLayout` shell (sidebar + responsive drawer).
- **UI system:** Tailwind design tokens (brand palette, mesh gradients,
  glassmorphism), a reusable component kit (`ScoreRing`, `StatCard`,
  `EmptyState`, skeletons) and Recharts visualisations.

---

## 5. Scalability notes

- Stateless API → horizontally scalable behind a load balancer.
- Analysis runs on a bounded thread pool; can be promoted to a message queue
  (RabbitMQ/Kafka) + worker service without API changes.
- File storage abstracts behind `FileStorageService`; swapping to S3 is a
  single-class change.
- PostgreSQL connection pooling via HikariCP (Spring Boot default).
