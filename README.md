# TaskTrackr

> Ein Deep-Dive in die Entwicklung skalierbarer Anwendungen nach Industriestandards mit Spring Boot.

TaskTrackr ist eine flexible Projektmanagement-Anwendung, die Teams dabei unterstützt, sowohl klassische To-do-Listen als auch komplexe agile Scrum-Workflows zu verwalten – inklusive Sprint-Management und Scrum Boards.

Der technische Fokus lag auf sauberer Schichtentrennung, einem dynamischen RBAC-Rechtesystem sowie der gezielten Lösung von Performance-Engpässen (N+1 Problem, Batch Fetching, Entity Graphs). Alle Design-Entscheidungen sind detailliert im [GitHub Wiki](https://github.com/Dxme98/TaskTrackr/wiki) dokumentiert.

 **[Zum Projekt-Walkthrough-Video](#)**

---

## Das Projekt auf einen Blick

| Metrik | Detail |
|---|---|
| Entwicklungszeit | ~3 Monate |
| Backend-Logik | 6.000+ Zeilen Quellcode (Java 17 / Spring Boot) |
| Qualitätssicherung | 5.000+ Zeilen Testcode (Unit, Web & Integration) |
| Testabdeckung | ~80% |
| Authentifizierung | Keycloak |
| Datenbank | PostgreSQL (verwaltet mit Flyway) |
| DevOps / CI-Pipeline | GitHub Actions (automatischer Build & Test bei jedem Push) |

---

## Technologie-Stack

| Kategorie | Technologien |
|---|---|
| Backend | Java 17, Spring Boot 3 (Web, Data JPA, Security, Validation) |
| Security | Spring Security OAuth2 Resource Server (für Keycloak) |
| Datenbank | PostgreSQL, Flyway |
| API-Dokumentation | SpringDoc (OpenAPI 3 / Swagger UI) |
| Testing | JUnit 5, Mockito, Spring Security Test |
| Integrationstests | Testcontainers |
| Code-Utilities | Lombok, MapStruct |
| DevOps (CI/CD) | Docker, GitHub Actions, Spring Dotenv |
| Build-Management | Maven |

---

## Kern-Features

- **Duales Projekt-System** — Verwalte „Basic"-Projekte (geteilte To-do-Liste) oder „Scrum"-Projekte (inkl. Sprints, User Stories, Scrum Board & Sprint-Verlauf)
- **Dynamisches Rechtesystem** — Erstelle eigene Rollen (z.B. „Entwickler", „Tester") mit feingranularen Rechten (z.B. „Task erstellen", „User einladen")
- **Team-Kollaboration** — Lade Teammitglieder ein, weise ihnen Aufgaben zu und verfolge alle Aktionen in einem detaillierten Aktivitäts-Feed
- **API-Dokumentation** — Vollständige OpenAPI 3 (Swagger UI)-Dokumentation aller Endpunkte

---

## Architektur-Highlights

### Saubere Schichtentrennung
Strikte Trennung der Verantwortlichkeiten: Die Business-Logik (Service-Schicht) ist vollständig von der Web-Schicht (Controller) und der Datenbankschicht (Repositories) isoliert. JPA-Entitäten verlassen die Service-Schicht nie – die gesamte externe Kommunikation erfolgt ausschließlich über DTOs.

### Package-by-Feature
Die Codebasis ist nach Fachlichkeit strukturiert, nicht nach technischer Schicht. Jedes Feature-Modul (z.B. `scrumdetails`, `basicdetails`, `activity`) bündelt seinen eigenen Controller, Service, Repository und Domain-Code – für maximale Kohäsion und einfache Erweiterbarkeit.

### Dynamisches RBAC
Jedes Projekt startet mit zwei Standard-Rollen (Owner, Base). Projekt-Owner können beliebig viele eigene Rollen mit projekttyp-spezifischen Rechten erstellen. Die Berechtigungsprüfung erfolgt in der Service-Schicht über „Guardrail"-Methoden im Domain-Modell.

### Performance-Optimierung
Performance-Probleme werden von vornherein verhindert, statt nachträglich mit Caching überdeckt:
- **N+1-Vermeidung** via `@EntityGraph` und gezielten `JOIN`-Queries
- **Kartesisches Produkt verhindert** via Hibernate Batch Fetching
- **Kein In-Memory-Filtern** – Aggregationen werden an die Datenbank delegiert
- **Multi-Column-Indizes** für häufige Abfragemuster

### Teststrategie (300+ Tests, ~80% Abdeckung)
Dreistufige Teststrategie, die schnelles Feedback mit Zuverlässigkeit kombiniert:
- **Unit-Tests** — Isolierte Domain-/Entitätslogik mit JUnit 5 & Mockito
- **Controller-Tests** — API-Schicht-Validierung mit `@WebMvcTest`
- **Service-Integrationstests** — Vollständiger Business-Flow gegen eine echte PostgreSQL-Instanz via Testcontainers und Custom Test Slices

---

## Datenbank

PostgreSQL dient als primäre Datenbank. Alle Schema-Änderungen sind über **Flyway**-Migrationen versioniert und damit reproduzierbar.

Das Datenmodell spiegelt die modulare Architektur wider:
- 🟥 **Project Management** — Universelles Kernmodul (Mitglieder, Rollen, Rechte, Einladungen)
- 🟩 **Scrum-Modul** — Sprints, User Stories, Scrum Board
- 🟦 **Basic-Modul** — Tasks und Projektinfos

Ein Projekt hat entweder `ScrumDetails` oder `BasicDetails` – niemals beides. Diese `1:0..1`-Beziehung garantiert eine saubere Trennung und ermöglicht es, neue Projekttypen (z.B. „Kanban") als unabhängiges Modul hinzuzufügen, ohne bestehenden Code zu berühren.

---

## Wiki & Dokumentation

Alle Architektur-Entscheidungen, Feature-Flows und Design-Muster sind im [GitHub Wiki](https://github.com/Dxme98/TaskTrackr/wiki) dokumentiert.
