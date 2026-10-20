# API Reference — AI Resume Analyzer

Base URL: `http://localhost:8080/api`
All responses are wrapped in a standard envelope:

```json
{
  "success": true,
  "message": "OK",
  "data": { },
  "timestamp": "2025-01-01T12:00:00Z"
}
```

Authenticated endpoints require an `Authorization: Bearer <accessToken>` header.

---

## Authentication

### POST `/auth/register`
```json
{ "fullName": "Jane Doe", "email": "jane@example.com", "password": "Str0ng@Pass" }
```
Password policy: 8–72 chars, at least one lower, upper, digit and special
character. Returns `AuthResponse` (access token, refresh token, user).

### POST `/auth/login`
```json
{ "email": "jane@example.com", "password": "Str0ng@Pass" }
```
Returns `AuthResponse`.

### POST `/auth/refresh`
```json
{ "refreshToken": "<refresh-token>" }
```
Rotates the refresh token and returns a fresh `AuthResponse`.

### POST `/auth/logout`
```json
{ "refreshToken": "<refresh-token>" }
```
Revokes the supplied refresh token.

### POST `/auth/forgot-password`
```json
{ "email": "jane@example.com" }
```
Always returns success (user-enumeration safe). If SMTP is not configured the
reset link is logged to the backend console.

### POST `/auth/reset-password`
```json
{ "token": "<reset-token>", "newPassword": "N3w@Password" }
```

---

## User

### GET `/users/me`  🔒
Returns the currently authenticated user profile.

---

## Resumes  🔒

### POST `/resumes/upload`
`multipart/form-data` with field `file` (PDF, ≤ 5 MB). Stores the file, parses
it, creates a pending analysis and dispatches the AI job asynchronously.
Returns the created `ResumeResponse`.

### GET `/resumes`
Lists the current user's resumes (newest first).

### GET `/resumes/{id}`
Returns a single resume with its embedded `analysis` (poll until
`analysis.status == "COMPLETED"`).

### POST `/resumes/{id}/reanalyze`
Re-runs the AI analysis for an existing resume.

### DELETE `/resumes/{id}`
Deletes the resume and its stored file.

---

## Dashboard  🔒

### GET `/dashboard`
Returns aggregated stats for the current user:

```json
{
  "totalResumes": 3,
  "latestScore": 78,
  "latestAtsScore": 71,
  "averageScore": 74.5,
  "scoreTrend": [{ "label": "01 Jan", "resumeScore": 70, "atsScore": 65 }],
  "skillsDistribution": { "Java": 3, "React": 2 },
  "missingSkills": ["docker", "kubernetes"]
}
```

---

## Admin  🛡 (ADMIN role required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | List all users |
| GET | `/admin/resumes` | List all resumes |
| GET | `/admin/stats` | Platform analytics (totals, averages, upload trend) |
| DELETE | `/admin/users/{id}` | Delete a non-admin user |
| PATCH | `/admin/users/{id}/toggle` | Enable / disable a non-admin user |

---

## Error format

```json
{ "success": false, "message": "Invalid email or password", "data": null,
  "timestamp": "2025-01-01T12:00:00Z" }
```

| Status | Meaning |
|--------|---------|
| 400 | Validation / bad request |
| 401 | Missing or invalid token / credentials |
| 403 | Authenticated but not authorized (role) |
| 404 | Resource not found |
| 413 | Uploaded file too large |
| 500 | Unexpected server error |
