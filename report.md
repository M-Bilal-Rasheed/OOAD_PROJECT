# SPVSS: Sports Player Valuation & Scouting System
## Complete technical source document for the OOAD academic report and the 8 UML diagrams

> **Purpose of this file.** This is the single source of truth for writing the final academic project report and for drawing all eight UML diagrams. Every class, method, route, table, rule and test described here exists in the current code (`src/`) and was checked against it. Nothing here is planned or imaginary, except §21 (*Future work*), which is clearly marked as future work.
>
> **Instructions for the reader or for Claude:**
> 1. Use only the facts in this file. Do not add features, classes, methods, patterns or test results that are not listed here.
> 2. Section **§16** holds the eight UML blueprints. They are consistent with one another and with §7 (classes) and §8 (database). Keep the names exactly as written.
> 3. Section **§23** maps every academic report heading to the sections of this file to draw from.
> 4. Screenshots are not embedded. **§19** lists each figure to capture and where it goes.

---

## Table of contents
1. Project identity
2. Background, problem statement, objectives, scope
3. Technologies and tools
4. Actors and user roles
5. Feature catalogue (existing and new)
6. System architecture
7. Source code inventory (every class)
8. Database design
9. Algorithms and business rules
10. New feature 1: Scout Notes / Player Scouting Reports
11. New feature 2: Player Performance Analytics
12. New feature 3: Complete UI redesign (dark/light, responsive)
13. Object-oriented programming concepts (with real code)
14. Design patterns (only those really implemented)
15. Workflows (step by step)
16. UML diagram blueprints (all 8)
17. Testing and test cases
18. Results
19. Screenshot plan (placeholders)
20. Challenges and solutions
21. Known limitations and future work
22. Setup and run guide
23. Mapping to the academic report sections
24. Glossary and naming rules

---

## 1. Project identity

| Item | Value |
|---|---|
| Full name | Sports Player Valuation and Scouting System |
| Short name used in UI | **SPVSS** |
| Maven artifact | `com.fsms:sports-player-valuation-scouting:1.0.0` |
| Root Java package | `com.fsms` (entry point `com.fsms.FsmsApplication`) |
| Application type | Server-rendered web application (Spring Boot MVC + Thymeleaf) |
| Database | PostgreSQL hosted on **Supabase** |
| Sports supported | Football, Cricket, Basketball, Tennis |
| Roles | Admin, Scout (plus unauthenticated Visitor) |
| Size (approx.) | ~6,600 lines across Java, HTML templates, CSS, JS and tests |
| Course context | Object-Oriented Analysis & Design (OOAD) university project |

**One-paragraph summary.** SPVSS is a multi-sport web platform where an **Admin** maintains player profiles and their four sport-specific statistics, and **Scouts** search players and calculate an educational estimate of each player's market value. Scouts can also compare two players of the same sport head to head, save comparisons, keep a personal shortlist, write private scouting reports, and study sport-specific performance analytics computed only from the statistics stored in the database. The interface has a modern, responsive design with a persistent dark/light theme.

---

## 2. Background, problem statement, objectives, scope

### 2.1 Background
Talent scouting across several sports usually relies on scattered spreadsheets and notes. Each sport measures performance differently. For example, in cricket a *lower* economy rate is better, and in tennis a *lower* ranking number is better. Generic tools often compare numbers blindly ("higher wins"), which produces wrong conclusions.

### 2.2 Problem statement
Scouts need one system that:
- stores player profiles and statistics for several sports in a consistent structure;
- calculates an indicative player value;
- compares players fairly, only within the same sport and following each statistic's own rule;
- lets each scout keep **private** written assessments, protected from other scouts;
- presents insights clearly on any device and in either light or dark mode.

### 2.3 Objectives
1. Provide secure account registration and login with role-based dashboards (Admin and Scout).
2. Let the Admin create, read, update and delete (CRUD) player profiles with four sport-specific statistics.
3. Let Scouts search and filter players and view full profiles.
4. Calculate an educational player valuation through a swappable **Strategy** (`ValuationStrategy`).
5. Compare two players of the same sport with correct "better value" rules, then save, reopen and delete comparisons.
6. Maintain a personal shortlist per scout.
7. **(New)** Let scouts write, view, edit and delete their own scouting reports, persisted in PostgreSQL and owner-protected.
8. **(New)** Provide sport-specific performance analytics: KPI cards, leaderboards, a performance index, per-player insights and visual comparisons, using stored data only.
9. **(New)** Deliver a modern, consistent and responsive UI with a dark/light theme that is remembered across refreshes and pages.
10. Apply and demonstrate OOP principles and design patterns in real, working code.

### 2.4 Scope
**In scope:** everything listed in §5.

**Out of scope:**
- live or external sports data feeds;
- real market valuations (the valuation is explicitly an educational estimate);
- payments;
- a public API;
- email verification and password reset;
- admin-side management of user accounts;
- sharing reports between scouts.

---

## 3. Technologies and tools

| Layer / purpose | Technology | Version / notes |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 (parent POM) |
| Web MVC | Spring Web MVC (`spring-boot-starter-web`) | Embedded Tomcat, port 8080 |
| Views | Thymeleaf (`spring-boot-starter-thymeleaf`) + `thymeleaf-extras-springsecurity6` | Server-side templates with shared fragments |
| Persistence | Spring Data JPA / Hibernate (`spring-boot-starter-data-jpa`) | `ddl-auto=update` |
| Database | PostgreSQL on Supabase | JDBC driver `org.postgresql:postgresql` |
| Security | Spring Security (`spring-boot-starter-security`) | Used for the **BCrypt** password encoder and for setting the security context at login; URL rules are `permitAll` (see §21) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) | `@NotBlank`, `@Min`, `@Max`, `@Size`, `@Email`, `@NotNull`, `@Valid` |
| Testing | JUnit 5 + Mockito (`spring-boot-starter-test`) | 18 unit tests, no database needed |
| Build | Maven (wrapper `mvnw.cmd` included) | `mvn spring-boot:run`, `mvn test`, `mvn package` |
| Front end | HTML5, CSS3 (custom design system with CSS variables), vanilla JavaScript (`static/js/app.js`) | No front-end framework, no chart library |
| Charts | Server-computed inline **SVG** radar chart + CSS bar charts | Generated by the `RadarChart` Java class |
| Typography | Inter (Google Fonts) with system-font fallback | |
| Browser storage | `localStorage` key `spvss-theme` | Remembers the dark/light theme only |
| Configuration | Environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, optional `ADMIN_FULL_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Loaded locally from a git-ignored `.env`/`run.ps1`-style file |
| Dev verification tools | Headless Google Chrome driven by Playwright (`playwright-core`) | Used during development for end-to-end checks; the script is **not** part of the repository |

---

## 4. Actors and user roles

| Actor | Type | How the account is created | Capabilities |
|---|---|---|---|
| **Visitor** | Primary, human | Not logged in | View login page, view signup page, register as a Scout, log in, toggle theme |
| **Scout** | Primary, human | Public signup (`/signup`) always creates role `SCOUT` | Scout dashboard; search, filter and view players; calculate valuation; shortlist add/view/remove; compare players (same sport); save, reopen and delete comparisons; **scouting reports CRUD (own only)**; **performance analytics**; theme toggle; logout |
| **Admin** | Primary, human | Created once at startup by `AdminSeeder` from environment variables (never through public signup) | Admin dashboard; player CRUD; plus everything a Scout can do, because Scout pages accept `SCOUT` or `ADMIN`. An Admin's own reports and comparisons are private to that Admin as well |
| **Supabase PostgreSQL** | Secondary, system | n/a | Stores all persistent data |
| **System clock / JPA lifecycle** | Secondary, system | n/a | Sets `createdAt` and `updatedAt` timestamps (`@PrePersist`, `@PreUpdate`) |

Role generalisation: **Admin** and **Scout** are both authenticated **Users**. In the use case diagram, Admin inherits all Scout use cases (see §16.1).

---

## 5. Feature catalogue

### 5.1 Pre-existing features (kept working)
| # | Feature | Main routes | Notes |
|---|---|---|---|
| F1 | Signup (Scout) | `GET/POST /signup` | Server-side checks: name required, email contains `@`, password ≥ 6 characters, passwords match, email unique (case-insensitive). Password stored as a BCrypt hash. |
| F2 | Login / logout | `GET/POST /login`, `GET /logout` | Session attributes `userId`, `role`, `scoutName`, `userEmail`, `fullName`; redirects by role. |
| F3 | Admin seeding | (startup) | `AdminSeeder` → `UserService.seedAdminIfNotExists()`; idempotent. |
| F4 | Admin dashboard | `GET /admin/dashboard` | Totals: players, valued players, count per sport. |
| F5 | Player CRUD (Admin) | `/admin/players`, `/admin/players/new`, `/admin/players/edit/{id}`, `POST /admin/players/save`, `POST /admin/players/delete/{id}` | Bean-validated; stat labels change with the sport. |
| F6 | Scout dashboard | `GET /scout/dashboard` | Totals plus shortlist count (and now report count). |
| F7 | Player search and filter | `GET /scout/players?name&sport&position&club` | JPQL `LIKE` search, case-insensitive. |
| F8 | Player profile | `GET /scout/players/{id}` | Details, statistics, valuation, shortlist (now with analytics and reports). |
| F9 | Valuation | `POST /scout/players/{id}/valuate` | Strategy pattern; stores `estimatedValue` in pounds. |
| F10 | Compare two players | `GET /scout/compare`, `GET /scout/compare/result` | Now restricted to the same sport, with correct winner rules. |
| F11 | Saved comparisons | `POST /scout/comparisons/save`, `GET /scout/comparisons`, `GET /scout/comparisons/{id}`, `POST /scout/comparisons/{id}/delete` | Owner-only view and delete. |
| F12 | Shortlist | `GET /scout/shortlist`, `POST /scout/shortlist/add/{playerId}`, `POST /scout/shortlist/remove/{entryId}` | Keyed by the scout's full name (see §21). |

### 5.2 New features (implemented and tested in this iteration)
| # | Feature | Main routes |
|---|---|---|
| N1 | **Scouting Reports** (create, list, filter by sport, view, edit, delete; owner-only) | `GET /scout/reports`, `GET /scout/reports/new`, `POST /scout/reports`, `GET /scout/reports/{id}`, `GET /scout/reports/{id}/edit`, `POST /scout/reports/{id}`, `POST /scout/reports/{id}/delete` |
| N2 | Reports integrated into the player profile ("My scouting reports" card, "Write scouting report" button) | `GET /scout/players/{id}` |
| N3 | **Performance Analytics page** per sport (KPI cards, top-5 leaderboards per statistic, performance-index ranking, full table with the best value per column highlighted) | `GET /scout/analytics?sport=…` |
| N4 | **Player performance insights** on the profile (per-stat score bar, sport average, rank, status badge, derived KPIs, performance index ring, strongest and weakest statistic) | `GET /scout/players/{id}` |
| N5 | **Analytics-powered comparison** (same sport only, per-stat winner following each stat's direction, decoded ×10 values, mirrored bar chart, SVG radar chart, win counts, verdict) | `GET /scout/compare/result`, `GET /scout/comparisons/{id}` |
| N6 | **UI redesign**: shared layout fragments, design-token CSS, dark/light theme with persistence, rounded outlined buttons with hover/focus/active/disabled states, responsive layouts (desktop, tablet, mobile), mobile navigation menu | all pages |

### 5.3 Complete route table (all controllers)
| Controller | Method + path | Handler | Access check | View / redirect |
|---|---|---|---|---|
| AuthController | GET `/` | `home` | none | redirect to role dashboard or `/login` |
| AuthController | GET `/login` | `showLogin` | none | `auth/login` |
| AuthController | POST `/login` | `processLogin` | none | redirect `/admin/dashboard` or `/scout/dashboard`; on failure `auth/login` |
| AuthController | GET `/signup` | `showSignup` | none | `auth/signup` |
| AuthController | POST `/signup` | `processSignup` | none | redirect `/login` (flash success) or `auth/signup` with error |
| AuthController | GET `/logout` | `logout` | none | redirect `/login?logout` |
| PlayerController | GET `/admin/dashboard` | `dashboard` | `isAdmin` | `admin/dashboard` |
| PlayerController | GET `/admin/players` | `listPlayers` | `isAdmin` | `admin/player-list` |
| PlayerController | GET `/admin/players/new` | `showAddForm` | `isAdmin` | `admin/player-form` |
| PlayerController | GET `/admin/players/edit/{id}` | `showEditForm` | `isAdmin` | `admin/player-form` |
| PlayerController | POST `/admin/players/save` | `savePlayer` | `isAdmin` | redirect `/admin/players` or the form with errors |
| PlayerController | POST `/admin/players/delete/{id}` | `deletePlayer` | `isAdmin` | redirect `/admin/players` |
| ScoutController | GET `/scout/dashboard` | `dashboard` | `isScoutOrAdmin` | `scout/dashboard` |
| ScoutController | GET `/scout/players` | `listPlayers` | `isScoutOrAdmin` | `scout/player-list` |
| ScoutController | GET `/scout/players/{id}` | `playerDetail` | `isScoutOrAdmin` | `scout/player-detail` |
| ScoutController | POST `/scout/players/{id}/valuate` | `valuatePlayer` | `isScoutOrAdmin` | redirect to the profile |
| ScoutController | GET `/scout/compare` | `compareForm` | `isScoutOrAdmin` | `scout/compare` |
| ScoutController | GET `/scout/compare/result` | `compareResult` | `isScoutOrAdmin` | `scout/compare-result` or `scout/compare` with error |
| ScoutController | GET `/scout/shortlist` | `viewShortlist` | `isScoutOrAdmin` | `scout/shortlist` |
| ScoutController | POST `/scout/shortlist/add/{playerId}` | `addToShortlist` | `isScoutOrAdmin` | redirect to the profile |
| ScoutController | POST `/scout/shortlist/remove/{entryId}` | `removeFromShortlist` | `isScoutOrAdmin` | redirect `/scout/shortlist` |
| ComparisonController | POST `/scout/comparisons/save` | `saveComparison` | logged in | redirect to the result |
| ComparisonController | GET `/scout/comparisons` | `viewHistory` | logged in | `scout/comparison-history` |
| ComparisonController | GET `/scout/comparisons/{id}` | `viewSavedComparison` | logged in + owner | `scout/compare-result` |
| ComparisonController | POST `/scout/comparisons/{id}/delete` | `deleteComparison` | logged in + owner | redirect `/scout/comparisons` |
| **ScoutingReportController** | GET `/scout/reports` | `list` | `isScoutOrAdmin` | `scout/reports/list` |
| **ScoutingReportController** | GET `/scout/reports/new` | `newForm` | `isScoutOrAdmin` | `scout/reports/form` |
| **ScoutingReportController** | POST `/scout/reports` | `create` | `isScoutOrAdmin` | redirect `/scout/reports/{id}` or the form with errors |
| **ScoutingReportController** | GET `/scout/reports/{id}` | `view` | `isScoutOrAdmin` + owner | `scout/reports/view` |
| **ScoutingReportController** | GET `/scout/reports/{id}/edit` | `editForm` | `isScoutOrAdmin` + owner | `scout/reports/form` |
| **ScoutingReportController** | POST `/scout/reports/{id}` | `update` | `isScoutOrAdmin` + owner | redirect `/scout/reports/{id}` |
| **ScoutingReportController** | POST `/scout/reports/{id}/delete` | `delete` | `isScoutOrAdmin` + owner | redirect `/scout/reports` or to the player profile |
| **AnalyticsController** | GET `/scout/analytics?sport=` | `analytics` | `isScoutOrAdmin` | `scout/analytics` |

All access checks read the `HttpSession` (`userId`, `role`). Failing a check redirects to `/login`. Failing an ownership check redirects to the list page with the flash message *"Report not found, or it belongs to another scout."* (for comparisons: *"Comparison not found or you do not have permission to view it."*).

---

## 6. System architecture

### 6.1 Architectural style
A **layered MVC architecture** inside one Spring Boot application (a monolith), talking to a managed PostgreSQL database.

```
Browser (HTML/CSS/JS, localStorage theme)
   │  HTTP (GET/POST forms, redirects)
   ▼
Presentation layer: Thymeleaf templates + fragments/layout.html + static/css/style.css + static/js/app.js
   ▲ model attributes
Controller layer: AuthController, PlayerController, ScoutController, ComparisonController,
                  ScoutingReportController (new), AnalyticsController (new), GlobalModelAttributes (new, @ControllerAdvice)
   │ method calls
   ▼
Service layer: UserService, PlayerService, ComparisonService,
               ScoutingReportService (new), PerformanceAnalyticsService (new)
   │                │
   │                └── Domain helpers: valuation (ValuationStrategy ← BasicValuationStrategy)
   │                                    analytics (SportProfile ← Football/Cricket/Basketball/TennisProfile,
   │                                               SportProfileRegistry, StatDefinition, result value objects, RadarChart)
   ▼
Repository layer (Spring Data JPA interfaces): AppUserRepository, PlayerRepository, ShortlistRepository,
                  ComparisonRepository, ScoutingReportRepository (new)
   │ JDBC via Hibernate (HikariCP pool)
   ▼
Supabase PostgreSQL: app_users, players, shortlist, comparisons, comparison_players, scouting_reports (new)
```

Configuration components: `SecurityConfig` (BCrypt `PasswordEncoder` bean, `SecurityFilterChain`) and `AdminSeeder` (`ApplicationRunner`).

### 6.2 Package structure
```
com.fsms
├── FsmsApplication                (entry point, @SpringBootApplication)
├── config        AdminSeeder, SecurityConfig
├── controller    AuthController, PlayerController, ScoutController, ComparisonController,
│                 ScoutingReportController*, AnalyticsController*, GlobalModelAttributes*, HomeController (empty)
├── service       UserService, PlayerService, ComparisonService, ScoutingReportService*, PerformanceAnalyticsService*
├── repository    AppUserRepository, PlayerRepository, ShortlistRepository, ComparisonRepository, ScoutingReportRepository*
├── model         AppUser, Player, PlayerStatistics, ShortlistEntry, Comparison, ComparisonPlayer,
│                 ScoutingReport*, ScoutingReportForm*, Recommendation* (enum),
│                 User, Admin, Scout (OOP demonstration classes, not persisted)
├── valuation     ValuationStrategy (interface), BasicValuationStrategy
└── analytics*    SportProfile (abstract), FootballProfile, CricketProfile, BasketballProfile, TennisProfile,
                  SportProfileRegistry, StatDefinition, DerivedMetric, StatInsight, PlayerInsight,
                  RankedEntry, StatSummary, SportSummary, ComparisonRow, PlayerComparison, RadarChart
(* = added in this iteration)
```

Resources:
```
src/main/resources
├── application.properties
├── db/migration/V2__create_authentication_tables.sql, V3__create_scouting_reports.sql*
├── static/css/style.css (rewritten), static/js/app.js*
└── templates/
    ├── fragments/layout.html*   (head, topbar, themeToggle, alerts)
    ├── auth/login.html, auth/signup.html
    ├── admin/dashboard.html, admin/player-list.html, admin/player-form.html
    ├── scout/dashboard.html, player-list.html, player-detail.html, compare.html, compare-result.html,
    │   comparison-history.html, shortlist.html, analytics.html*
    ├── scout/reports/list.html*, form.html*, view.html*
    └── home.html (legacy, not routed by any controller)
src/test/java/com/fsms/service/PerformanceAnalyticsServiceTest*, ScoutingReportServiceTest*
```

### 6.3 Request lifecycle (example)
1. The browser sends `GET /scout/players/5`. Spring's `DispatcherServlet` routes it to `ScoutController.playerDetail`.
2. `GlobalModelAttributes` adds `currentRole` and `currentUserName` from the session.
3. The controller checks the session role, then calls `PlayerService.getPlayerById`, `PlayerService.isOnShortlist`, `PerformanceAnalyticsService.analysePlayer` and `ScoutingReportService.getReportsByAuthorForPlayer`.
4. The services call their repositories, which run SQL through Hibernate against Supabase.
5. The controller returns the view name `scout/player-detail`. Thymeleaf renders it, including the `fragments/layout` head and top bar.
6. In the browser, the inline head script applies the saved theme before paint, and `app.js` adds interactivity (theme toggle, mobile menu, confirm prompts, compare filter, character counters).

### 6.4 Session-based authentication model
- Login (`AuthController.processLogin`) verifies the BCrypt hash through `UserService.verifyLogin`.
- On success it stores `userId`, `role`, `scoutName`, `userEmail` and `fullName` in the `HttpSession`, and places a `UsernamePasswordAuthenticationToken` with authority `ROLE_<role>` in the `SecurityContextHolder`.
- Controllers authorise each request with private helpers: `isAdmin`, `isScoutOrAdmin`, `isLoggedIn`.
- Logout invalidates the session and clears the security context.

---

## 7. Source code inventory

Notation: `-` private, `+` public, `#` protected, `~` package-private. `{abstract}` means abstract and `<<interface>>` means interface.

### 7.1 Model / entity classes (package `com.fsms.model`)

**`AppUser`** `<<Entity>>`, table `app_users`
- Fields: `-id: Long` (PK, identity), `-fullName: String` (@NotBlank), `-email: String` (@NotBlank, @Email, unique), `-passwordHash: String` (BCrypt), `-role: String` ("ADMIN" or "SCOUT"), `-createdAt: LocalDateTime` (not updatable)
- Methods: `+AppUser()`, `+prePersist(): void` (@PrePersist sets createdAt), getters and setters for all fields.

**`Player`** `<<Entity>>`, table `players`
- Fields: `-id: Long`, `-name: String` (@NotBlank), `-age: int` (@Min 1), `-nationality: String` (@NotBlank), `-sport: String` (@NotBlank), `-position: String` (@NotBlank), `-club: String` (@NotBlank), `-estimatedValue: Long` (nullable), `-statistics: PlayerStatistics` (@Embedded, @Valid, @NotNull)
- Methods: `+Player()` (creates an empty `PlayerStatistics`, i.e. composition), `+getSportStatLabels(): String[]` (four labels per sport), getters and setters.
- Labels returned by `getSportStatLabels()`:
  - Football: `Goals`, `Assists`, `Appearances`, `Speed Rating (1-100)`
  - Cricket: `Runs`, `Wickets`, `Batting Average (×10)`, `Economy Rate (×10)`
  - Basketball: `Points`, `Assists`, `Rebounds`, `Shooting % (×10)`
  - Tennis: `Wins`, `Ranking (lower is better)`, `Aces`, `1st Serve % (×10)`

**`PlayerStatistics`** `<<Embeddable>>` (stored in columns of `players`)
- Fields: `-stat1: int`, `-stat2: int`, `-stat3: int`, `-stat4: int` (each @Min 0)
- Methods: `+PlayerStatistics()`, `+PlayerStatistics(stat1, stat2, stat3, stat4)`, getters and setters.

**`ShortlistEntry`** `<<Entity>>`, table `shortlist`
- Fields: `-id: Long`, `-scoutName: String`, `-playerId: Long`
- Methods: constructors `()` and `(scoutName, playerId)`, getters and setters.

**`Comparison`** `<<Entity>>`, table `comparisons`
- Fields: `-id: Long`, `-userId: Long` (owner, refers to AppUser.id), `-title: String`, `-createdAt: LocalDateTime`, `-comparisonPlayers: List<ComparisonPlayer>` (@OneToMany mappedBy="comparison", cascade ALL, orphanRemoval: **composition**)
- Methods: `+prePersist()`, getters and setters.

**`ComparisonPlayer`** `<<Entity>>`, table `comparison_players` (unique (comparison_id, player_id))
- Fields: `-id: Long`, `-comparison: Comparison` (@ManyToOne LAZY, join `comparison_id`), `-playerId: Long`
- Methods: constructors `()` and `(Comparison, Long)`, getters and setters.

**`ScoutingReport`** `<<Entity>>`, table `scouting_reports` **(new)**
- Constant: `+TEXT_LIMIT: int = 2000` (static final)
- Fields:
  - `-id: Long`
  - `-player: Player` (@ManyToOne EAGER, optional=false, join column `player_id`)
  - `-author: AppUser` (@ManyToOne EAGER, optional=false, join column **`scout_id`**)
  - `-strengths: String`, `-weaknesses: String`, `-potential: String`
  - `-potentialRating: int` (1–10)
  - `-performanceObservations: String`
  - `-recommendation: Recommendation` (@Enumerated STRING)
  - `-recommendationSummary: String`
  - `-createdAt: LocalDateTime`, `-updatedAt: LocalDateTime`
- Methods: `+ScoutingReport()`, `+prePersist()` (sets createdAt and updatedAt), `+preUpdate()` (sets updatedAt), `+isOwnedBy(userId: Long): boolean`, `+isEdited(): boolean`, getters and setters.
- Indexes declared: `idx_scouting_reports_scout(scout_id)`, `idx_scouting_reports_player(player_id)`.

**`ScoutingReportForm`** (form-backing object / DTO, **new**)
- Fields:
  - `-playerId: Long` (@NotNull)
  - `-strengths`, `-weaknesses`, `-potential`, `-performanceObservations`, `-recommendationSummary: String` (each @NotBlank and @Size max 2000)
  - `-potentialRating: Integer` (@NotNull, @Min 1, @Max 10; default 5)
  - `-recommendation: Recommendation` (@NotNull)
- Methods: `+fromReport(r: ScoutingReport): ScoutingReportForm` (static factory, pre-fills the edit form), `+applyTo(r: ScoutingReport): void` (copies trimmed values onto the entity), getters and setters.

**`Recommendation`** `<<enumeration>>` **(new)**
- Literals: `STRONGLY_RECOMMEND("Strongly recommend","strong")`, `RECOMMEND("Recommend","good")`, `MONITOR("Monitor","neutral")`, `NOT_RECOMMENDED("Do not recommend","weak")`
- Fields: `-displayName: String`, `-cssClass: String`. Methods: `+getDisplayName()`, `+getCssClass()`.

**`User`** (plain class, OOP demonstration, not persisted)
- Fields: `-username: String`, `-role: String`. Methods: `+User(username, role)`, getters and setters, `+getDisplayInfo(): String`.

**`Admin extends User`**: `+Admin(username)` calls `super(username,"ADMIN")`. Overrides `+getRole()` and `+getDisplayInfo()`.

**`Scout extends User`**: field `-shortlist: List<Player>` (aggregation). `+Scout(username)`, overrides `+getRole()` and `+getDisplayInfo()`, plus `+addToShortlist(Player)`, `+removeFromShortlist(Player)`, `+getShortlist()`.

> Important for accuracy: `User`, `Admin` and `Scout` are **never instantiated by the running application**. Authentication and persistence use `AppUser`, and the shortlist is persisted by `ShortlistEntry`. These three classes exist to demonstrate inheritance, overriding and aggregation, as their code comments state. In the class diagram, mark them with a note: *«demonstration hierarchy, not persisted»*.

### 7.2 Valuation (package `com.fsms.valuation`)
- **`ValuationStrategy`** `<<interface>>`: `+calculateValue(player: Player): long`
- **`BasicValuationStrategy`** `<<Component>> implements ValuationStrategy`
  - Private static final weights for each sport (§9.1).
  - `+calculateValue(player): long` (@Override).

### 7.3 Analytics domain (package `com.fsms.analytics`, all new)
| Class | Kind | Key members |
|---|---|---|
| `SportProfile` | `{abstract}` class | `+getSport(): String {abstract}`, `+getStatDefinitions(): List<StatDefinition> {abstract}`, `+derivedMetrics(stats: PlayerStatistics): List<DerivedMetric> {abstract}`, `+getDescription(): String {abstract}`, `#ratio(n: int, d: int): double {static}`, `+getLabels(): String[]` (concrete, built from `getStatDefinitions()`) |
| `FootballProfile` | `<<Component>>`, extends `SportProfile` | static `STATS` list; overrides all four abstract methods; derived: Goal involvements, Goals per appearance, Involvements per appearance |
| `CricketProfile` | `<<Component>>`, extends `SportProfile` | economy (slot 4) is lower-is-better and 0 means "Did not bowl"; derived: Batting average, Economy rate, Runs + wickets |
| `BasketballProfile` | `<<Component>>`, extends `SportProfile` | Shooting % (slot 4) ×10 with unit `%`; derived: Combined contributions, Shooting percentage |
| `TennisProfile` | `<<Component>>`, extends `SportProfile` | Ranking (slot 2) is lower-is-better and 0 means "Unranked"; 1st Serve % ×10; derived: Ranking band, 1st serve percentage; `~rankingBand(ranking: int): String {static}` |
| `SportProfileRegistry` | `<<Component>>` | `+SPORT_ORDER: List<String> {static}`, `-profiles: Map<String,SportProfile>`, `+SportProfileRegistry(allProfiles: List<SportProfile>)`, `+get(sport: String): SportProfile`, `+supports(sport: String): boolean`, `+getSports(): List<String>` |
| `StatDefinition` | final, immutable value class | fields `slot`, `label`, `shortLabel`, `scale` (1 or 10), `higherIsBetter`, `zeroMeansMissing`, `unit`, `missingLabel`; methods `+rawValue(PlayerStatistics): int`, `+displayValue(raw): double`, `+isMissing(raw): boolean`, `+format(raw): String`, `+formatDecoded(double): String`, `+compareRaw(a, b): int`, `+getDirectionLabel(): String`, getters |
| `DerivedMetric` | final value class | `label`, `value`, `description` |
| `StatInsight` | final value class | `definition`, `rawValue`, `formattedValue`, `sportAverageFormatted`, `score` (0–100), `rank`, `rankedCount`, `status`; `+isMissing()`, `+getStatusClass()` |
| `PlayerInsight` | final value class | `player`, `profile`, `stats: List<StatInsight>`, `derivedMetrics`, `performanceIndex`, `peerCount`, `strongest`, `weakest` |
| `RankedEntry` | final value class | `rank`, `player`, `formattedValue`, `score`; `+getBarWidth()` |
| `StatSummary` | final value class | `definition`, `averageFormatted`, `best: RankedEntry`, `leaders: List<RankedEntry>` (top 5), `rankedCount` |
| `SportSummary` | final value class | `profile`, `playerCount`, `averageAge`, `valuedCount`, `statSummaries`, `indexLeaderboard`; `+getSport()`, `+isEmpty()`, `+getTopPerformer()` |
| `ComparisonRow` | final value class | constants `A`, `B`, `TIE`; `definition`, `formattedA`, `formattedB`, `scoreA`, `scoreB`, `winner`; `+getBarA()`, `+getBarB()`, `+isAWins()`, `+isBWins()`, `+isTie()` |
| `PlayerComparison` | final value class | `playerA`, `playerB`, `profile`, `rows: List<ComparisonRow>`, `winsA`, `winsB`, `ties`, `indexA`, `indexB`, `radar: RadarChart`; `+getVerdict(): String` |
| `RadarChart` | final class | `SIZE=320`, `CENTER`, `RADIUS=110`; `labels`, `scoresA`, `scoresB`; `+getPolygonA()`, `+getPolygonB()`, `+getGrid(): List<String>`, `+getAxes(): List<Axis>`, `+isHasB()`, `+getViewBox()`; nested static class `Axis` (x, y, labelX, labelY, anchor, label) |

### 7.4 Services (package `com.fsms.service`)
**`UserService`** `<<Service>>`: depends on `AppUserRepository`, `PasswordEncoder`
- `+registerScout(fullName, email, rawPassword): String` (null on success, otherwise an error message)
- `+verifyLogin(email, rawPassword): AppUser` (null on failure)
- `+findById(id): AppUser`
- `+seedAdminIfNotExists(fullName, email, rawPassword): void`

**`PlayerService`** `<<Service>>`: depends on `PlayerRepository`, `ShortlistRepository`, `ScoutingReportRepository` (new dependency), `ValuationStrategy`
- `+addPlayer(Player)`, `+updatePlayer(Player)`
- `+deletePlayer(id)`: @Transactional; **deletes the player's scouting reports first**, then the player
- `+getAllPlayers()`, `+getPlayerById(id)`
- `+getTotalPlayerCount()`, `+getValuedPlayerCount()`, `+getCountBySport(sport)`
- `+searchPlayers(name, sport, position, club)`
- `+calculateAndSaveValue(playerId): Player`
- `+comparePlayers(id1, id2): List<Player>`
- `+addToShortlist(scoutName, playerId): boolean`, `+removeFromShortlist(entryId)`, `+getShortlist(scoutName): List<Player>`
- `+getShortlistEntryId(scoutName, playerId): Long`, `+isOnShortlist(scoutName, playerId): boolean`

**`ComparisonService`** `<<Service>>`: depends on `ComparisonRepository`, `PlayerRepository`
- `+saveComparison(userId, title, player1Id, player2Id): Comparison`
- `+getComparisonsForUser(userId)`
- `+getComparisonForUser(comparisonId, userId)` (owner check)
- `+deleteComparison(comparisonId, userId): boolean`
- `+getPlayersForComparison(Comparison): List<Player>`

**`ScoutingReportService`** `<<Service>>` **(new)**: depends on `ScoutingReportRepository`, `PlayerRepository`, `AppUserRepository`
- `+createReport(authorId: Long, form: ScoutingReportForm): ScoutingReport` (@Transactional; null if the author or player is missing)
- `+findOwnedReport(reportId: Long, userId: Long): ScoutingReport` (null unless the report exists **and** `isOwnedBy(userId)`)
- `+updateReport(reportId, userId, form): ScoutingReport` (@Transactional; owner only; player and author never change)
- `+deleteReport(reportId, userId): boolean` (@Transactional; owner only)
- `+getReportsByAuthor(authorId): List<ScoutingReport>`
- `+getReportsByAuthorForPlayer(authorId, playerId): List<ScoutingReport>`
- `+countByAuthor(authorId): long`

**`PerformanceAnalyticsService`** `<<Service>>` **(new)**: depends on `PlayerRepository`, `SportProfileRegistry`
- Database-backed:
  - `+getSports()`, `+getProfile(sport)`, `+getPlayersForSport(sport)`
  - `+summarizeSport(sport): SportSummary`
  - `+analysePlayer(player): PlayerInsight`
  - `+comparePlayers(a, b): PlayerComparison` (null if the sports differ)
  - `+isSameSport(a, b): boolean`
- Pure calculations:
  - `+summarize(profile, players): SportSummary`
  - `+analyse(player, peers): PlayerInsight`
  - `+compare(a, b, peers): PlayerComparison` (throws `IllegalArgumentException` when the sports differ)
  - `~performanceIndex(profile, player, pool): int`
  - `~score(def, raw, values): int {static}`
  - `~rank(def, raw, values): int {static}`
  - private helpers `summarizeStat`, `countReal`, `average`, `values`, `withPlayer`

### 7.5 Repositories (package `com.fsms.repository`, all extend `JpaRepository<T, Long>`)
| Interface | Custom methods |
|---|---|
| `AppUserRepository` | `findByEmail(String): Optional<AppUser>`, `existsByRole(String): boolean` |
| `PlayerRepository` | `searchPlayers(name, sport, position, club): List<Player>` (JPQL @Query), **`findBySportIgnoreCase(String): List<Player>` (new)** |
| `ShortlistRepository` | `findByScoutName(String)`, `findByScoutNameAndPlayerId(String, Long): Optional<ShortlistEntry>` |
| `ComparisonRepository` | `findByUserIdOrderByCreatedAtDesc(Long)` |
| **`ScoutingReportRepository`** (new) | `findByAuthor_IdOrderByUpdatedAtDesc(Long)`, `findByAuthor_IdAndPlayer_IdOrderByUpdatedAtDesc(Long, Long)`, `countByAuthor_Id(Long): long`, `deleteByPlayerId(Long): int` (@Modifying JPQL `DELETE FROM ScoutingReport r WHERE r.player.id = :playerId`) |

Inherited from `JpaRepository`: `save`, `findById`, `findAll`, `deleteById`, `delete`, `count`.

### 7.6 Controllers (package `com.fsms.controller`)
| Class | Dependencies | Private helpers | Handlers |
|---|---|---|---|
| `AuthController` | `UserService` | — | `home`, `showLogin`, `processLogin`, `showSignup`, `processSignup`, `logout` |
| `PlayerController` (`/admin`) | `PlayerService` | `isAdmin` | `dashboard`, `listPlayers`, `showAddForm`, `showEditForm`, `savePlayer`, `deletePlayer`; constant `SPORTS` |
| `ScoutController` (`/scout`) | `PlayerService`, **`PerformanceAnalyticsService`**, **`ScoutingReportService`** | `isLoggedIn`, `isScoutOrAdmin`, `getScoutName`, `populateCompareForm` | `dashboard`, `listPlayers`, `playerDetail`, `valuatePlayer`, `compareForm`, `compareResult`, `viewShortlist`, `addToShortlist`, `removeFromShortlist` |
| `ComparisonController` (`/scout/comparisons`) | `ComparisonService`, `PlayerService`, **`PerformanceAnalyticsService`** | `getUserId`, `isLoggedIn` | `saveComparison`, `viewHistory`, `viewSavedComparison`, `deleteComparison` |
| **`ScoutingReportController`** (`/scout/reports`) | `ScoutingReportService`, `PlayerService` | `getUserId`, `isScoutOrAdmin`, `populateForm` | `list`, `newForm`, `create`, `view`, `editForm`, `update`, `delete` |
| **`AnalyticsController`** (`/scout/analytics`) | `PerformanceAnalyticsService` | `isScoutOrAdmin` | `analytics` |
| **`GlobalModelAttributes`** (`@ControllerAdvice`) | — | — | `@ModelAttribute currentRole(HttpSession)`, `@ModelAttribute currentUserName(HttpSession)` |
| `HomeController` | — | — | none (empty file kept for history) |

### 7.7 Configuration (package `com.fsms.config`)
- **`SecurityConfig`** (`@Configuration @EnableWebSecurity`): `+passwordEncoder(): PasswordEncoder` returns `BCryptPasswordEncoder`; `+filterChain(HttpSecurity): SecurityFilterChain` disables CSRF, form login and HTTP basic, and uses `anyRequest().permitAll()`.
- **`AdminSeeder implements ApplicationRunner`**: `+run(ApplicationArguments)` reads the `ADMIN_*` environment variables and calls `UserService.seedAdminIfNotExists`.

### 7.8 Front-end assets
- `fragments/layout.html`:
  - `head(title)`: meta viewport, inline SVG favicon, **theme bootstrap script**, Inter font, `style.css`, deferred `app.js`
  - `topbar(active)`: brand, role-aware navigation, mobile "Menu" toggle, theme toggle, user chip, log-out button
  - `themeToggle`: moon/sun icon button
  - `alerts`: success, error and info flash messages
- `static/js/app.js`: `initThemeToggle`, `initNavToggle`, `initConfirmForms` (for `data-confirm` forms), `initCompareFilter` (Player 2 list limited to Player 1's sport), `initCounters` (textarea character counters).
- `static/css/style.css`: design tokens for light and dark (`:root`, `:root[data-theme="dark"]`, plus a `prefers-color-scheme` fallback when JS is off); components (top bar, cards, KPI cards, tiles, buttons, forms, tables, alerts, badges, tabs, bars, leaderboards, insight rows, index ring, versus and duel layout, radar, report cards, empty states, auth pages); breakpoints at 1100, 960, 680 and 400 px; `prefers-reduced-motion` support.

---

## 8. Database design

### 8.1 Tables (PostgreSQL, schema `public`)

**`app_users`** (AppUser)
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| full_name | VARCHAR(255) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL (BCrypt) |
| role | VARCHAR(10) | NOT NULL, CHECK in ('ADMIN','SCOUT') (per V2 script) |
| created_at | TIMESTAMP | NOT NULL |

**`players`** (Player + embedded PlayerStatistics)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT identity | PK |
| name, nationality, sport, position, club | VARCHAR | required (bean validation) |
| age | INTEGER | ≥ 1 |
| estimated_value | BIGINT | nullable; set by valuation |
| stat1, stat2, stat3, stat4 | INTEGER | ≥ 0; meaning depends on sport (see §9.2) |

**`shortlist`** (ShortlistEntry)
| Column | Type | Notes |
|---|---|---|
| id | BIGINT identity | PK |
| scout_name | VARCHAR | the scout's full name (no FK) |
| player_id | BIGINT | player reference (no FK declared in the entity) |

**`comparisons`** (Comparison)
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | NOT NULL, FK → app_users(id) ON DELETE CASCADE (V2) |
| title | VARCHAR(255) | default "Comparison" when blank |
| created_at | TIMESTAMP | NOT NULL |

**`comparison_players`** (ComparisonPlayer)
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL | PK |
| comparison_id | BIGINT | NOT NULL, FK → comparisons(id) ON DELETE CASCADE |
| player_id | BIGINT | NOT NULL, FK → players(id) ON DELETE RESTRICT (V2) |
| created_at | TIMESTAMP | from the V2 script only; not mapped in the entity |
| | | UNIQUE (comparison_id, player_id) |

**`scouting_reports`** (ScoutingReport, **new feature**). This is the **live schema, verified directly in Supabase**.
| Column | Type | Constraints / notes |
|---|---|---|
| id | BIGINT (sequence) | PK |
| player_id | BIGINT | NOT NULL, FK → players(id) |
| scout_id | BIGINT | NOT NULL, FK → app_users(id), **the report owner** |
| strengths | TEXT | required by the application |
| weaknesses | TEXT | required by the application |
| potential | VARCHAR(2000) | required by the application |
| potential_rating | INTEGER | NOT NULL, 1–10 (validated in the application) |
| performance_observations | TEXT | required by the application |
| recommendation | VARCHAR(32) | NOT NULL, CHECK in ('STRONGLY_RECOMMEND','RECOMMEND','MONITOR','NOT_RECOMMENDED') |
| recommendation_summary | VARCHAR(2000) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| scout_name, player_name, player_sport, overall_recommendation | VARCHAR/TEXT, nullable | **legacy columns** from an earlier, empty version of the table; not used by the application and left in place |

Indexes: `idx_scouting_reports_scout (scout_id)`, `idx_scouting_reports_player (player_id)`.

### 8.2 Relationships (for the ER part of the report and the class diagram)
| Relationship | Cardinality | Implementation | Delete behaviour |
|---|---|---|---|
| AppUser writes ScoutingReport | 1 — 0..* | `scouting_reports.scout_id` FK, `@ManyToOne author` | — |
| Player is subject of ScoutingReport | 1 — 0..* | `scouting_reports.player_id` FK, `@ManyToOne player` | `PlayerService.deletePlayer` deletes the player's reports first |
| AppUser owns Comparison | 1 — 0..* | `comparisons.user_id` (FK in DB, `Long userId` in Java) | DB cascade on user delete |
| Comparison contains ComparisonPlayer | 1 — 2 (application always saves 2) | `@OneToMany(cascade=ALL, orphanRemoval=true)` | composition: entries deleted with the comparison |
| ComparisonPlayer references Player | * — 1 | `player_id` (Long id) | DB `ON DELETE RESTRICT` |
| ShortlistEntry references Player | * — 1 | `player_id` (Long id) | Missing players are skipped when the list is read |
| Scout (by full name) has ShortlistEntry | 1 — 0..* | `scout_name` string | — |
| Player owns PlayerStatistics | 1 — 1 | `@Embedded` (same row) | composition: lives and dies with the player |

### 8.3 Migration files
- `V2__create_authentication_tables.sql`: app_users, comparisons, comparison_players and indexes (manual run in the Supabase SQL editor).
- `V3__create_scouting_reports.sql` (new): idempotent `CREATE TABLE IF NOT EXISTS` and `ALTER … IF NOT EXISTS` statements for scouting_reports, plus indexes.
- At runtime, `spring.jpa.hibernate.ddl-auto=update` lets Hibernate create or extend tables automatically.

---

## 9. Algorithms and business rules

### 9.1 Valuation (`BasicValuationStrategy.calculateValue`, educational estimate in £)
| Sport | Formula |
|---|---|
| Football | goals×100,000 + assists×50,000 + appearances×10,000 + speed×20,000 |
| Cricket | runs×5,000 + wickets×200,000 + battingAvg(×10)×15,000 + max(0, 100 − economy(×10))×10,000 |
| Basketball | points×80,000 + assists×40,000 + rebounds×30,000 + shooting%(×10)×25,000 |
| Tennis | wins×150,000 + max(0, 200 − ranking)×50,000 + aces×10,000 + firstServe%(×10)×20,000 |
| Other / null sport | sport defaults to Football; the generic fallback is 50k/30k/20k/10k |

### 9.2 Statistic definitions (`SportProfile` subclasses)
| Sport | Slot | Label | Scale | Better | 0 means |
|---|---|---|---|---|---|
| Football | 1 | Goals | 1 | higher | — |
| Football | 2 | Assists | 1 | higher | — |
| Football | 3 | Appearances | 1 | higher | — |
| Football | 4 | Speed Rating (1-100) | 1 | higher | — |
| Cricket | 1 | Runs | 1 | higher | — |
| Cricket | 2 | Wickets | 1 | higher | — |
| Cricket | 3 | Batting Average (×10) | 10 | higher | — |
| Cricket | 4 | Economy Rate (×10) | 10 | **lower** | **"Did not bowl"** (excluded) |
| Basketball | 1 | Points | 1 | higher | — |
| Basketball | 2 | Assists | 1 | higher | — |
| Basketball | 3 | Rebounds | 1 | higher | — |
| Basketball | 4 | Shooting % (×10) | 10 (unit %) | higher | — |
| Tennis | 1 | Wins | 1 | higher | — |
| Tennis | 2 | Ranking (lower is better) | 1 | **lower** | **"Unranked"** (excluded) |
| Tennis | 3 | Aces | 1 | higher | — |
| Tennis | 4 | 1st Serve % (×10) | 10 (unit %) | higher | — |

**Scaled (×10) handling.** The database stores integers. For example, an economy of 7.4 is stored as 74. `StatDefinition.displayValue(raw)` divides by 10, `format()` prints one decimal place, and sport averages are calculated on the **decoded** values.

### 9.3 Analytics calculations (`PerformanceAnalyticsService`)
- **Pool:** all players whose `sport` equals the player's sport (`findBySportIgnoreCase`). The analysed player is always included.
- **Missing values:** values for which `isMissing(raw)` is true (Cricket economy 0, Tennis ranking 0) are excluded from min, max, average, rank and leaderboards. They score 0, are shown as their missing label, and never win a comparison.
- **Score (0–100), min-max normalisation within the sport:**
  - Higher-is-better: `(raw − min) / (max − min) × 100`.
  - Lower-is-better: `(max − raw) / (max − min) × 100`.
  - If `max == min` (including a sport with a single player), the score is 100.
- **Rank:** 1 + the number of players whose value is strictly better (ties share a rank).
- **Status badge:** compares the decoded value with the sport average, in the stat's direction, using a ±2% tolerance band. The result is "Above average", "Below average", "Average" or "No data".
- **Performance index:** the rounded average of the player's available stat scores (0–100). It is a **relative** measure inside this database, not a real-world rating, and the UI says so.
- **Strongest / weakest:** the stats with the highest and lowest score. Weakest is hidden if it is the same stat as strongest.
- **Leaderboards:** for each stat, the top 5 players by `compareRaw` (direction-aware). The index leaderboard is sorted by index descending, then by name.
- **Comparison:**
  - Allowed only if `isSameSport(a, b)`.
  - For each stat, `compareRaw(rawA, rawB)` decides A, B or TIE.
  - Win counts and both indexes are reported, and the verdict reads "X leads on n of 4 statistics" or "Evenly matched".
  - Radar polygons use the per-stat scores.
- **Derived KPIs:** computed only from the stored stats (for example goals ÷ appearances, shown as "—" when appearances are 0).

### 9.4 Scouting report rules
- All text fields are required, trimmed, and limited to 2,000 characters. The potential rating must be between 1 and 10. A recommendation is required.
- `playerId` must refer to an existing player. On update, the player is forced back to the original value, so a report can never move to another player.
- The owner is the logged-in user (`session.userId`). Ownership is checked in **every** read, update and delete through `findOwnedReport`.
- The list shows only the current user's reports, newest update first, and can be filtered by sport.
- Deleting a player deletes all of that player's reports (in the same transaction).

### 9.5 Comparison rules
- The two IDs must differ, and both players must exist.
- **The two players must play the same sport.** This is enforced:
  - in `ScoutController.compareResult` (error message);
  - in `ComparisonController.saveComparison` (blocked);
  - in the UI, by the Player 2 filter in `app.js`.
- Older saved comparisons that mix sports still open, with a warning and no winner highlighting.
- Only the owner can open or delete a saved comparison.

---

## 10. New feature 1: Scout Notes / Player Scouting Reports

**What it does:** each scout keeps private, persistent written assessments of players. Each report has:
- **Strengths**
- **Weaknesses**
- **Potential:** free text plus a 1–10 rating
- **Performance Observations**
- **Overall Recommendation:** a verdict (Strongly recommend / Recommend / Monitor / Do not recommend) plus a written justification

**Where it appears:**
- **Reports** in the top navigation, leading to the list (`scout/reports/list.html`). The list has sport filter tabs, report cards showing sport, verdict badge, potential dots, an excerpt, the update date, and Open/Edit/Delete buttons. It also has empty states.
- **New / Edit form** (`scout/reports/form.html`):
  - player selector (fixed while editing);
  - textareas with live character counters;
  - range slider for the rating, with a live "n/10" output;
  - radio "choice cards" for the verdict;
  - inline validation errors.
- **Report page** (`scout/reports/view.html`): all sections, author, created and edited dates, Edit and Delete.
- **Player profile:** a "My scouting reports" card listing the current scout's reports on that player with Open/Edit/Delete, plus "Write scouting report" and "New report" buttons that pre-select the player.
- **Scout dashboard:** a KPI card "My scouting reports" with the count.

**Persistence:** table `scouting_reports` in Supabase PostgreSQL, linked to `players` and `app_users` by foreign keys.

**Security and ownership:**
- `ScoutingReportService.findOwnedReport` returns `null` for another user's report, so viewing, editing, updating or deleting it is impossible.
- This holds even with typed URLs or forged POST requests; this was verified in end-to-end testing, see §17.
- The response gives no hint whether the report exists ("not found, or it belongs to another scout").

**Classes involved:**
- `ScoutingReport` and `Recommendation` (domain)
- `ScoutingReportForm` (DTO)
- `ScoutingReportRepository`
- `ScoutingReportService`
- `ScoutingReportController`
- `ScoutController.playerDetail` and `dashboard` (integration)
- `PlayerService.deletePlayer` (cleanup)

---

## 11. New feature 2: Player Performance Analytics

**What it does:** turns the four stored statistics into sport-aware insights, and never fetches or invents data.

**Analytics page** (`/scout/analytics?sport=X`, tabs for the four sports):
- **KPI cards:**
  - Players tracked, with average age.
  - Top performance index, with the player's name.
  - One "Avg <stat>" card per statistic, showing the best value and the player who holds it.
- **Statistic leaders:** one card per statistic with its direction label ("Higher is better" or "Lower is better"), a "stored ×10, shown decoded" note where relevant, the sport average, and a top-5 ranked list with bars.
- **Performance index ranking:** top 10, with bars.
- **All players table:** decoded values, with the best value per column highlighted according to the direction rule.
- **Empty state** when a sport has no players.
- Footnote: "All figures come from the player statistics stored in this system. No external or live data is used."

**Player profile insights:**
- For each stat: value, direction, sport average, rank "n of m", status badge, and a coloured score bar (red < 34, amber < 67, green otherwise).
- KPI tiles built from the derived metrics.
- **Performance index** ring (CSS conic gradient), with the strongest and weakest statistic.

**Comparison** (`/scout/compare/result`):
- Same-sport only, with Player 2 options filtered in the browser.
- "Versus" header with stats won and index for each player.
- Mirrored **head-to-head bar chart**, with the winner marked by a check.
- **SVG radar chart** of both players' score shapes.
- Full attribute table with direction-aware highlighting. Equal values are not highlighted. Younger age and higher estimated value are also highlighted.
- Save form.

**Statistical correctness guarantees** (each one unit-tested, §17):
- lower economy wins in Cricket;
- lower ranking wins in Tennis;
- 0 economy means "Did not bowl" and 0 ranking means "Unranked", both excluded;
- ×10 values are decoded for display and for averages;
- cross-sport comparison is rejected;
- profile labels match the `Player` labels for every sport (parity test).

---

## 12. New feature 3: Complete UI redesign

| Requirement | Implementation |
|---|---|
| Dark/Light toggle on all pages | `themeToggle` fragment in the top bar of every authenticated page and in the corner of the login and signup pages |
| Theme remembered | `localStorage['spvss-theme']`. An inline script in `<head>` applies it **before the first paint** (no light flash). The first visit follows the OS `prefers-color-scheme`. A CSS-only fallback applies when JS is disabled |
| Rounded, clearly outlined buttons | `.btn` base: 1.5px solid border, 10px radius, min-height 40px. Variants: primary/success (filled accent), outline/secondary, accent-outline, danger (outline that fills on hover), warning, small, large, full-width. The theme toggle and menu button are also outlined. An automated check confirmed every visible button has a border ≥ 1px and a radius ≥ 4px |
| Hover / focus / active / disabled | hover: background or border shift plus shadow on primary; `:focus-visible`: 3px accent ring; `:active`: 1px press; `:disabled`/`.is-disabled`: 50% opacity and not-allowed cursor; transitions 0.15s; `prefers-reduced-motion` respected |
| Better dashboards, cards, tables, forms, navigation, typography, spacing, colours | design tokens (surface layers, borders, text levels, accent, semantic colours, series A/B colours), Inter font, sticky blurred top bar with active-pill navigation, KPI cards, action tiles, sport-coloured badges, empty states, scroll-safe tables (`.table-wrap`), two-column form grids, breadcrumb and page-header pattern |
| Responsive on desktop, tablet and mobile | breakpoints 1100 / 960 / 680 / 400 px. At ≤ 960px the navigation collapses into a "Menu" button with an expandable vertical list, and grids reflow. At ≤ 680px grids become single-column, the comparison layout stacks, and KPIs show two per row. Automated checks found no horizontal page scroll at 390px (mobile) or 820px (tablet) |
| Consistent design everywhere | all pages (Login, Signup, Admin dashboard/list/form, Scout dashboard, Player search, Player details, Compare, Comparison result, History, Shortlist, Analytics, Reports list/form/view) share `fragments/layout.html` and one stylesheet |
| Readability and contrast | separate dark palette (for example text #e8edf1 on #151a1f, accent #3cc68f on dark, #0b7a53 on light); badge colours are tuned for each theme |

---

## 13. Object-oriented programming concepts (with real code)

| Concept | Where (real classes) | Explanation / example |
|---|---|---|
| **Class and object** | `Player`, `ScoutingReport`, `StatDefinition` | `new Player()` objects are created by forms and by JPA. `new ScoutingReport()` is created in `ScoutingReportService.createReport`. |
| **Encapsulation** | All entities have private fields with getters and setters. `StatDefinition` and the analytics result classes are **immutable** (private final fields, no setters) | `ScoutingReport.isOwnedBy(userId)` hides the ownership logic inside the object. `ScoutingReportForm.applyTo(report)` controls exactly which fields a form may change (never the author, id or player). |
| **Abstraction** | `ValuationStrategy` (interface); `SportProfile` (abstract class) | Callers depend on *what* (`calculateValue`, `getStatDefinitions`, `derivedMetrics`), not *how*. |
| **Inheritance** | `FootballProfile`, `CricketProfile`, `BasketballProfile` and `TennisProfile` **extend** `SportProfile`; `Admin` and `Scout` **extend** `User` | The subclasses inherit the concrete `getLabels()` and the protected helper `ratio()` from `SportProfile`. |
| **Polymorphism (runtime / dynamic dispatch)** | `PlayerService` calls `valuationStrategy.calculateValue(player)`; `PerformanceAnalyticsService` calls `profile.derivedMetrics(...)` and `profile.getStatDefinitions()` on a `SportProfile` reference returned by `SportProfileRegistry.get(sport)` | The same call runs `CricketProfile`'s or `TennisProfile`'s code depending on the player's sport. |
| **Method overriding** | `@Override getSport/getStatDefinitions/derivedMetrics/getDescription` in each profile; `@Override calculateValue` in `BasicValuationStrategy`; `@Override getRole/getDisplayInfo` in `Admin` and `Scout` | |
| **Method overloading** | `StatDefinition` has two constructors (7 and 8 parameters); `PlayerStatistics()` and `PlayerStatistics(int,int,int,int)`; `ShortlistEntry()` and `ShortlistEntry(String, Long)` | |
| **Composition** | `Player` ◆ `PlayerStatistics` (`@Embedded`, created in the constructor); `Comparison` ◆ `ComparisonPlayer` (cascade ALL + orphanRemoval); `PlayerComparison` ◆ `ComparisonRow` / `RadarChart` | The parts cannot exist without the whole. |
| **Aggregation** | `Scout` ◇ `List<Player>` (demonstration); shortlist entries reference players that live independently | Removing an entry never deletes the player. |
| **Association** | `ScoutingReport` → `Player` (many-to-one); `ScoutingReport` → `AppUser` author (many-to-one) | Navigable from report to player and author. |
| **Dependency** | Controllers depend on services; services depend on repositories (constructor injection) | For example, `ScoutingReportController(ScoutingReportService, PlayerService)`. |
| **Enumeration** | `Recommendation` with fields and methods | A type-safe verdict that the database also enforces with a CHECK constraint. |
| **Static members** | `SportProfileRegistry.SPORT_ORDER`, `ScoutingReport.TEXT_LIMIT`, `PerformanceAnalyticsService.score/rank`, `ScoutingReportForm.fromReport` (static factory) | |
| **Interfaces for persistence** | Repositories are interfaces; Spring generates the implementations | |
| **Exception handling** | `PerformanceAnalyticsService.compare` throws `IllegalArgumentException` for cross-sport input; `StatDefinition` validates its constructor arguments | |

---

## 14. Design patterns (only those really implemented)

| Pattern | Evidence in the code | Participants |
|---|---|---|
| **MVC (Model–View–Controller)** | The whole web tier | Model: entities and analytics result objects. View: Thymeleaf templates. Controller: `*Controller` classes |
| **Layered architecture** | Packages controller → service → repository → DB | §6 |
| **Strategy** (GoF) | `ValuationStrategy` interface, `BasicValuationStrategy` concrete strategy, `PlayerService` context with an injected interface reference | Swapping the valuation algorithm needs no change to `PlayerService` |
| **Repository** | Spring Data JPA interfaces (`PlayerRepository`, `ScoutingReportRepository`, …) | Hide SQL and persistence behind interfaces |
| **Dependency Injection / Inversion of Control** | Constructor injection in every controller and service; Spring injects `List<SportProfile>` into `SportProfileRegistry` | The framework wires the object graph |
| **Registry** (lookup by key) | `SportProfileRegistry.get(sport)` returns the matching `SportProfile` subclass | Clients receive the abstract type, which supports polymorphism |
| **Template Method (simple form)** | `SportProfile.getLabels()` is a concrete algorithm that calls the abstract `getStatDefinitions()` supplied by subclasses | `SportProfile` and its four subclasses |
| **DTO / Form Object** | `ScoutingReportForm` separates HTML binding and validation from the `ScoutingReport` entity (`fromReport`, `applyTo`) | It prevents over-posting of owner or id |
| **Value Object** | Immutable `StatDefinition`, `DerivedMetric`, `StatInsight`, `RankedEntry`, `StatSummary`, `SportSummary`, `ComparisonRow`, `PlayerComparison` | |
| **Post/Redirect/Get + Flash messages** | Every POST handler returns `redirect:` with `RedirectAttributes.addFlashAttribute` | Prevents duplicate form submission |
| **Front Controller** (provided by the framework) | Spring `DispatcherServlet` | Mention as framework-provided only |
| **Composite view / template fragments** | `fragments/layout.html` (`head`, `topbar`, `alerts`, `themeToggle`) included by every page | |

Do **not** claim Singleton (beyond Spring's default bean scope), Factory Method, Observer, Builder, Decorator or Adapter; they are not implemented.

---

## 15. Workflows (step by step, for activity and sequence diagrams)

### W1. Sign up and log in
1. The Visitor opens `/signup` and submits name, email, password and confirmation.
2. If the passwords differ, the form is shown again with an error.
3. `UserService.registerScout` validates the input, checks that the email is unique, hashes the password with BCrypt, and saves an `AppUser` with role `SCOUT`.
4. The Visitor is redirected to `/login` with a success flash message.
5. The Visitor submits the login form, and `UserService.verifyLogin` checks the email and BCrypt hash.
6. On failure, the login page is shown again with an error.
7. On success, the session attributes and security context are set, and the user is redirected by role (`/admin/dashboard` or `/scout/dashboard`).

### W2. Admin manages a player
1. The Admin opens `/admin/players/new` and chooses a sport; the stat labels update in JS.
2. On submit, `@Valid Player` is validated. If there are errors, the form is shown again.
3. `PlayerService.addPlayer` or `updatePlayer` saves the player, and the Admin is redirected to the list with a flash message.
4. Delete: a confirmation prompt appears, then `POST /admin/players/delete/{id}` runs `PlayerService.deletePlayer`. That method deletes the player's scouting reports and then the player, in one transaction.

### W3. Scout writes, edits and deletes a scouting report *(new)*
1. The Scout opens a player profile and clicks **Write scouting report** (`/scout/reports/new?playerId=…`).
2. The controller builds an empty `ScoutingReportForm` with `playerId` preset and shows the form.
3. The Scout fills in the fields and submits `POST /scout/reports`.
4. Validation:
   - On errors, the form is shown again with field messages.
   - If the player no longer exists, a `playerId` error is shown.
5. `ScoutingReportService.createReport(userId, form)` loads the `AppUser` and `Player`, builds a `ScoutingReport`, calls `form.applyTo(report)` and saves it. `@PrePersist` sets the timestamps.
6. The Scout is redirected to `/scout/reports/{id}` with "Scouting report saved."
7. Edit: `GET /{id}/edit` calls `findOwnedReport`.
   - If it is not the owner's report, the Scout is redirected to the list with an error.
   - Otherwise the form is pre-filled through `ScoutingReportForm.fromReport`.
8. On submit (`POST /{id}`), the controller checks ownership again, fixes the `playerId` and validates. `updateReport` applies the changes and `@PreUpdate` sets `updatedAt`. The Scout is redirected with "Scouting report updated."
9. Delete: a confirmation prompt appears, then `POST /{id}/delete` runs `deleteReport`. Only the owner succeeds. The Scout is redirected to the list (or back to the player profile when the delete started there).

### W4. Scout studies analytics *(new)*
1. The Scout opens **Analytics** (default Football) or selects a sport tab.
2. `AnalyticsController.analytics(sport)` calls `PerformanceAnalyticsService.summarizeSport(sport)`.
3. The service:
   1. gets the `SportProfile` from the registry;
   2. loads the players of that sport;
   3. for each `StatDefinition`, computes the average, scores, ranks and top 5;
   4. computes each player's performance index and sorts the leaderboard;
   5. computes the average age and the valued count;
   6. returns a `SportSummary`.
4. The view renders the KPI cards, leaderboards, index ranking and the full table. If the sport has no players, it shows an empty state.

### W5. Scout compares two players *(updated)*
1. The Scout opens **Compare** and selects Player 1. JS limits the Player 2 options to the same sport.
2. Submit: `GET /scout/compare/result?player1Id&player2Id`.
3. The controller rejects identical IDs and missing players, then calls `analyticsService.comparePlayers(p1, p2)`.
4. If they play different sports, the form is shown again with "Players can only be compared with players of the same sport…".
5. Otherwise the result is rendered: versus header, duel bars, radar, table and save form.
6. Save: `POST /scout/comparisons/save` re-checks that both players play the same sport, calls `ComparisonService.saveComparison` (Comparison plus 2 ComparisonPlayers), and redirects with a flash message.
7. History: list, open (owner only, re-analysed) or delete (owner only).

### W6. Valuation
The Scout clicks **Calculate valuation**. `PlayerService.calculateAndSaveValue` calls `ValuationStrategy.calculateValue` (polymorphic), stores `estimatedValue`, and redirects with the formatted amount.

### W7. Shortlist
Add, view or remove, keyed by the session `scoutName`. A duplicate add returns the message "already on your shortlist".

### W8. Theme toggle
1. When any page loads, the head script reads `localStorage['spvss-theme']`, falling back to the OS preference, and sets `<html data-theme>`.
2. When the user clicks the toggle, `app.js` flips the attribute, saves it to `localStorage` and updates the button's aria labels.
3. The next page or a refresh repeats step 1, so the theme persists.

---

## 16. UML diagram blueprints

General naming rules for all eight diagrams: use the exact class, method and route names from §7 and §5.3. Actors are **Visitor**, **Scout** and **Admin**. External systems are **Supabase PostgreSQL** and **Web Browser**.

### 16.1 Use Case Diagram
**System boundary:** "SPVSS – Sports Player Valuation & Scouting System".

**Actors and generalisation:**
- **Visitor** (primary, left)
- **Scout** (primary, left)
- **Admin** (primary, left). **Admin ─▷ Scout** (generalisation: the Admin inherits all Scout use cases).
- **Supabase PostgreSQL** (secondary, right; «system»). Associate it with every use case that persists data, or with a single "Persist data" note.

**Use cases:**
| ID | Use case | Primary actor(s) |
|---|---|---|
| UC1 | Register Scout Account | Visitor |
| UC2 | Log In | Visitor |
| UC3 | Log Out | Scout (Admin inherits) |
| UC4 | Toggle Dark/Light Theme | Visitor, Scout |
| UC5 | View Scout Dashboard | Scout |
| UC6 | Search / Filter Players | Scout |
| UC7 | View Player Profile | Scout |
| UC8 | Calculate Player Valuation | Scout |
| UC9 | Manage Shortlist (add / view / remove) | Scout |
| UC10 | Compare Two Players (same sport) | Scout |
| UC11 | Save Comparison | Scout |
| UC12 | Manage Comparison History (view / reopen / delete) | Scout |
| UC13 | **Create Scouting Report** | Scout |
| UC14 | **View My Scouting Reports** (list, filter by sport, open) | Scout |
| UC15 | **Edit Scouting Report** | Scout |
| UC16 | **Delete Scouting Report** | Scout |
| UC17 | **View Sport Performance Analytics** | Scout |
| UC18 | **View Player Performance Insights** | Scout |
| UC19 | View Admin Dashboard | Admin |
| UC20 | Add Player | Admin |
| UC21 | Edit Player | Admin |
| UC22 | Delete Player | Admin |
| UC23 | View All Players (admin list) | Admin |

**Relationships:**
- UC2 Log In: `«include»` *Verify Credentials (BCrypt)*.
- UC1 Register: `«include»` *Validate Signup Data*.
- UC13, UC15, UC16, UC12: `«include»` **Verify Report/Comparison Ownership** (an internal use case; for reports name it *Verify Report Ownership*).
- UC10 Compare: `«include»` **Check Same Sport**. UC11 Save Comparison: `«extend»` UC10, with extension point "after result shown".
- UC7 View Player Profile: `«include»` UC18 View Player Performance Insights. UC13 Create Scouting Report: `«extend»` UC7, with extension point "Write scouting report button".
- UC8 Calculate Valuation: `«extend»` UC7. UC9 Manage Shortlist (add): `«extend»` UC7.
- UC22 Delete Player: `«include»` *Delete Player's Scouting Reports*.
- UC20 and UC21: `«include»` *Validate Player Data*.
- All Scout/Admin use cases except UC1, UC2 and UC4 require an authenticated session. Show this as a note: "precondition: logged in as SCOUT or ADMIN".

**Notes to place:**
- "Scouts can only see, edit or delete their own reports."
- "Comparisons are allowed only within the same sport."
- "Analytics use stored statistics only."

### 16.2 Activity Diagram
Draw **two** activity diagrams (or one with two swimlane sets). The main one is required and the second is recommended.

**Activity A: Create, edit and delete a Scouting Report.** Swimlanes: **Scout (Browser)** | **ScoutingReportController** | **ScoutingReportService** | **Database**.
1. ● Initial node, then [Scout] "Open player profile", then "Click *Write scouting report*".
2. [Controller] "Check session role". Decision: *SCOUT or ADMIN?*
   - [no] → "Redirect to /login" → ◉ final.
   - [yes] → "Build empty form with playerId" → [Scout] "Fill strengths, weaknesses, observations, potential + rating, verdict + summary" → "Submit".
3. [Controller] "Validate form (@Valid)". Decision: *errors?*
   - [yes] → "Re-display form with field errors" → back to "Fill…" (loop).
   - [no] → "Check player exists". Decision: [no] → "Reject playerId" → loop back to the form.
4. [Service] "Load AppUser (author) and Player" → "Create ScoutingReport, applyTo(form)" → [Database] "INSERT scouting_reports (timestamps set)".
5. [Controller] "Redirect to report page with *saved* flash" → [Scout] "View report".
6. Decision: *Edit, delete or done?*
   - [Edit] → [Service] "findOwnedReport(id, userId)". Decision: *owner?*
     - [no] → "Redirect to list with error" → ◉.
     - [yes] → "Pre-fill form (fromReport)" → [Scout] "Change fields & submit" → [Controller] "Re-check owner, fix playerId, validate" → [Service] "applyTo + save" → [DB] "UPDATE (updated_at)" → back to "View report".
   - [Delete] → [Scout] "Confirm prompt" (decision: cancel → back to View) → [Service] "deleteReport (owner check)" → [DB] "DELETE" → [Controller] "Redirect to list with *deleted* flash" → ◉.
   - [Done] → ◉.

**Activity B: Compare two players** (swimlanes: Scout | Browser JS | ScoutController | PerformanceAnalyticsService | ComparisonController/DB):
1. Select Player 1.
2. [JS] "Filter Player 2 to same sport".
3. Select Player 2 → Submit.
4. Decision: *same id?* → error.
5. Decision: *both exist?* → error.
6. "comparePlayers". Decision: *same sport?*
   - [no] → "Show form with same-sport error".
   - [yes] → fork ┬ "Compute per-stat winners (direction-aware)" ┬ "Compute scores, indexes, radar" ┴ join → "Render result".
7. Decision: *save?* → [yes] "Enter title" → [ComparisonController] "Re-check same sport" → "Save Comparison + 2 ComparisonPlayers" → "Redirect with flash" → ◉.

### 16.3 Class Diagram
Draw five packages: `model`, `analytics`, `valuation`, `service`, `repository`, plus `controller` (it may be drawn compactly). Show the attributes and operations from §7, using the visibility markers given there.

**Key classes and stereotypes:**
- `«entity»`: AppUser, Player, ScoutingReport, ShortlistEntry, Comparison, ComparisonPlayer
- `«embeddable»`: PlayerStatistics
- `«enumeration»`: Recommendation
- `«DTO»`: ScoutingReportForm
- `«interface»`: ValuationStrategy, plus all repositories marked `«interface» «repository»`
- `{abstract}`: SportProfile
- `«service»`: UserService, PlayerService, ComparisonService, ScoutingReportService, PerformanceAnalyticsService
- `«controller»`: AuthController, PlayerController, ScoutController, ComparisonController, ScoutingReportController, AnalyticsController; `«controllerAdvice»` GlobalModelAttributes
- `«component»`: SportProfileRegistry, BasicValuationStrategy, the 4 profiles, AdminSeeder; `«configuration»`: SecurityConfig
- Value objects (`«value object»`): StatDefinition, DerivedMetric, StatInsight, PlayerInsight, RankedEntry, StatSummary, SportSummary, ComparisonRow, PlayerComparison, RadarChart (+ nested Axis)
- Demonstration hierarchy (note «not persisted»): User, Admin, Scout

**Relationships, with multiplicities:**
| From | Relationship | To | Multiplicity / label |
|---|---|---|---|
| Player | composition ◆ | PlayerStatistics | 1 — 1 "statistics" |
| ScoutingReport | association → | Player | 0..* — 1 "player" (is about) |
| ScoutingReport | association → | AppUser | 0..* — 1 "author" (written by; FK scout_id) |
| ScoutingReport | association → | Recommendation | * — 1 |
| ScoutingReportForm | dependency ⇢ | ScoutingReport | «uses» (fromReport/applyTo) |
| Comparison | composition ◆ | ComparisonPlayer | 1 — 1..* (always 2 in practice) |
| ComparisonPlayer | association → | Player | * — 1 (by playerId) |
| Comparison | association → | AppUser | * — 1 (owner, by userId) |
| ShortlistEntry | association → | Player | * — 1 (by playerId) |
| Admin, Scout | generalisation ─▷ | User | |
| Scout | aggregation ◇ | Player | 1 — 0..* "shortlist" |
| BasicValuationStrategy | realisation ┄▷ | ValuationStrategy | |
| PlayerService | association → | ValuationStrategy | 1 — 1 (strategy context) |
| FootballProfile, CricketProfile, BasketballProfile, TennisProfile | generalisation ─▷ | SportProfile | |
| SportProfile | composition ◆ | StatDefinition | 1 — 4 |
| SportProfileRegistry | aggregation ◇ | SportProfile | 1 — 1..* (map by sport) |
| PerformanceAnalyticsService | association → | SportProfileRegistry, PlayerRepository | 1 — 1 |
| PerformanceAnalyticsService | dependency ⇢ (creates) | SportSummary, PlayerInsight, PlayerComparison | «create» |
| SportSummary | composition ◆ | StatSummary (4), RankedEntry (0..*) | |
| StatSummary | composition ◆ | RankedEntry | 1 — 0..5 |
| PlayerInsight | composition ◆ | StatInsight (4), DerivedMetric (1..*) | |
| PlayerComparison | composition ◆ | ComparisonRow (4), RadarChart (1) | |
| StatInsight, ComparisonRow, StatSummary | association → | StatDefinition | * — 1 |
| ScoutingReportService | association → | ScoutingReportRepository, PlayerRepository, AppUserRepository | 1 — 1 each |
| PlayerService | association → | PlayerRepository, ShortlistRepository, ScoutingReportRepository | 1 — 1 each |
| ComparisonService | association → | ComparisonRepository, PlayerRepository | |
| UserService | association → | AppUserRepository, PasswordEncoder | |
| Controllers | dependency ⇢ | the services listed in §7.6 | |
| Repositories | dependency ⇢ | their entity types | `JpaRepository<T, Long>` |

### 16.4 Object Diagram
A snapshot **after** a scout writes a report and runs a cricket comparison. Use these consistent instance names and values (illustrative values, consistent with the rules in §9):

- `scoutA : AppUser` {id = 12, fullName = "Ayesha Khan", email = "ayesha@example.com", role = "SCOUT"}
- `scoutB : AppUser` {id = 13, fullName = "Bilal Ahmed", role = "SCOUT"}
- `admin1 : AppUser` {id = 1, role = "ADMIN"}
- `p1 : Player` {id = 21, name = "Hamza Ali", sport = "Cricket", position = "Bowler", age = 24, estimatedValue = 24,120,000}  (= 420×5,000 + 95×200,000 + 180×15,000 + (100−68)×10,000, per §9.1)
  - ◆ `p1Stats : PlayerStatistics` {stat1 = 420, stat2 = 95, stat3 = 180, stat4 = 68}  (runs 420, wickets 95, batting avg 18.0, economy 6.8)
- `p2 : Player` {id = 22, name = "Usman Tariq", sport = "Cricket", position = "Batsman", age = 27}
  - ◆ `p2Stats : PlayerStatistics` {stat1 = 1500, stat2 = 0, stat3 = 452, stat4 = 0}  (economy 0 means "Did not bowl")
- `r1 : ScoutingReport` {id = 7, potentialRating = 8, recommendation = RECOMMEND, strengths = "Accurate yorkers at the death", weaknesses = "Fitness in long spells", potential = "International white-ball option", performanceObservations = "Economy under 7 across the season", recommendationSummary = "Recommend for trial", createdAt = 2026-11-12 10:00, updatedAt = 2026-11-12 10:00}
  - links: `r1 —player→ p1`, `r1 —author→ scoutA`
- `RECOMMEND : Recommendation`, linked from `r1`
- `cricket : CricketProfile` ◆ `econ : StatDefinition` {slot = 4, label = "Economy Rate (×10)", scale = 10, higherIsBetter = false, zeroMeansMissing = true, missingLabel = "Did not bowl"}
- `registry : SportProfileRegistry`, linked to `cricket` (key "Cricket")
- `cmp : PlayerComparison` {playerA = p1, playerB = p2, winsA = 2, winsB = 2, ties = 0}
  - ◆ `row4 : ComparisonRow` {definition = econ, formattedA = "6.8", formattedB = "Did not bowl", winner = "A"}
  - ◆ `radar : RadarChart`
- `c1 : Comparison` {id = 3, userId = 12, title = "Bowler vs Batsman"} ◆ `cp1 : ComparisonPlayer` {playerId = 21}, ◆ `cp2 : ComparisonPlayer` {playerId = 22}

Check of the win counts (for consistency): runs → B (1500 > 420); wickets → A (95 > 0); batting average → B (45.2 > 18.0); economy → A (B did not bowl). The result is 2–2, verdict "Evenly matched across 4 statistics".

Add a note: "scoutB has no link to r1. `findOwnedReport(7, 13)` returns null."

### 16.5 Component Diagram
**Components** (UML component boxes with provided ○ and required ◖ interfaces):
1. **Web Browser Client**: contains the *HTML views*, *style.css* and *app.js (Theme, Nav, Confirm, CompareFilter, Counters)*. It requires «HTTP» and uses the «localStorage» artifact.
2. **Presentation (Thymeleaf Views)**: templates and `fragments/layout`. It provides «HTML pages» and requires «Model attributes».
3. **Web Controllers**: AuthController, PlayerController, ScoutController, ComparisonController, ScoutingReportController, AnalyticsController, GlobalModelAttributes. It provides «HTTP endpoints (§5.3)» and requires «AuthService», «PlayerService», «ComparisonService», «ReportService», «AnalyticsService».
4. **User & Auth Service** (UserService + SecurityConfig/PasswordEncoder + AdminSeeder). It provides «AuthService» and requires «UserRepository».
5. **Player Management Service** (PlayerService). It provides «PlayerService» and requires «ValuationStrategy», «PlayerRepository», «ShortlistRepository», «ReportRepository».
6. **Valuation Engine** (ValuationStrategy, BasicValuationStrategy). It provides «ValuationStrategy».
7. **Comparison Service** (ComparisonService). It provides «ComparisonService» and requires «ComparisonRepository», «PlayerRepository».
8. **Scouting Reports Module** *(new)* (ScoutingReportService, ScoutingReport, ScoutingReportForm, Recommendation). It provides «ReportService» and requires «ReportRepository», «PlayerRepository», «UserRepository».
9. **Performance Analytics Module** *(new)* (PerformanceAnalyticsService, SportProfileRegistry, SportProfile + 4 profiles, value objects, RadarChart). It provides «AnalyticsService» and requires «PlayerRepository».
10. **Data Access (Spring Data JPA Repositories)**. It provides the five «…Repository» interfaces and requires «JDBC».
11. **Supabase PostgreSQL**, an external «database» component. It provides «JDBC/SQL».

Connections:
- Browser ↔ Views/Controllers over HTTP.
- Controllers → service components.
- Services → Repositories → PostgreSQL.
- Analytics → Repositories (read-only).
- Player Management → Valuation Engine.

Mark components 8 and 9 as «new».

### 16.6 Sequence Diagram
Draw **two** sequence diagrams: the first is required and the second is recommended.

**SD-1: Create Scouting Report.** Lifelines: `:Scout` (actor), `:Browser`, `:ScoutingReportController`, `:ScoutingReportService`, `:PlayerService`, `:AppUserRepository`, `:PlayerRepository`, `:ScoutingReportRepository`, `:Database`.
1. Scout → Browser: click "Write scouting report".
2. Browser → Controller: `GET /scout/reports/new?playerId=21`.
3. Controller: `isScoutOrAdmin(session)` (self-call) → true.
4. Controller → PlayerService: `getPlayerById(21)` → `p1`.
5. Controller → PlayerService: `getAllPlayers()` → list; the controller then calls `populateForm(model, form, null)` (self).
6. Controller → Browser: render `scout/reports/form`.
7. Scout → Browser: fill the fields and submit. Browser → Controller: `POST /scout/reports` (form fields).
8. Controller: `@Valid` binding → `BindingResult`. Draw an **alt** fragment:
   - [hasErrors] Controller → Browser: re-render the form with errors.
   - [else] continue with step 9.
9. Controller → ScoutingReportService: `createReport(userId=12, form)`.
10. Service → AppUserRepository: `findById(12)` → `Optional<AppUser>`.
11. Service → PlayerRepository: `findById(21)` → `Optional<Player>`.
12. Service: `«create» r : ScoutingReport`; `r.setAuthor(user)`, `r.setPlayer(p1)`, `form.applyTo(r)`.
13. Service → ScoutingReportRepository: `save(r)` → Database: `INSERT INTO scouting_reports …` (`@PrePersist` sets the timestamps) → returns `r(id=7)`.
14. Service → Controller: `r`.
15. Controller → Browser: `redirect:/scout/reports/7` with flash "Scouting report saved.".
16. Browser → Controller: `GET /scout/reports/7` → `findOwnedReport(7, 12)` → `r` → render `scout/reports/view`.

Optional **alt** fragment: [another scout, userId = 13] `findOwnedReport(7, 13)` → `null`, then redirect to `/scout/reports` with "Report not found, or it belongs to another scout."

**SD-2: Analyse and compare players.** Lifelines: `:Scout`, `:Browser`, `:ScoutController`, `:PlayerService`, `:PerformanceAnalyticsService`, `:SportProfileRegistry`, `:CricketProfile`, `:PlayerRepository`.
1. Browser → ScoutController: `GET /scout/compare/result?player1Id=21&player2Id=22`.
2. ScoutController → PlayerService: `comparePlayers(21, 22)` → `[p1, p2]`.
3. ScoutController → AnalyticsService: `comparePlayers(p1, p2)`.
4. AnalyticsService: `isSameSport(p1, p2)` → AnalyticsService → Registry: `supports("Cricket")` → true. Draw an **alt** [different sport]: return `null`, the controller re-renders the compare form with an error.
5. AnalyticsService → PlayerRepository: `findBySportIgnoreCase("Cricket")` → `peers`.
6. AnalyticsService → AnalyticsService: `compare(p1, p2, peers)`. Inside, AnalyticsService → Registry: `get("Cricket")` → `cricket:SportProfile`.
7. **loop** [for each StatDefinition in `cricket.getStatDefinitions()`]: `def.rawValue(...)` ×2, `score(def, raw, values)` ×2, `def.compareRaw(rawA, rawB)` → winner, then `«create» ComparisonRow`.
8. `performanceIndex(...)` ×2, then `«create» RadarChart(labels, scoresA, scoresB)`, then `«create» PlayerComparison`.
9. Return to ScoutController → `model.addAttribute("comparison", …)` → render `scout/compare-result`.

### 16.7 Communication Diagram
Show the same interaction as **SD-1** (Create Scouting Report), drawn as numbered messages on links between objects:

Objects: `:Scout`, `:Browser`, `c:ScoutingReportController`, `s:ScoutingReportService`, `ps:PlayerService`, `ur:AppUserRepository`, `pr:PlayerRepository`, `rr:ScoutingReportRepository`, `r:ScoutingReport`, `f:ScoutingReportForm`.

Links and messages:
- Scout — Browser: `1: submitReportForm()`
- Browser — c: `2: POST /scout/reports(f)`
- c (self-link): `2.1: isScoutOrAdmin(session)`, `2.2: validate(f) [@Valid]`
- c — ps: `2.3: getPlayerById(f.playerId)`
- c — s: `2.4: createReport(userId, f)`
- s — ur: `2.4.1: findById(userId)`
- s — pr: `2.4.2: findById(f.playerId)`
- s — r: `2.4.3: «create» new ScoutingReport()`, `2.4.4: setAuthor(user)`, `2.4.5: setPlayer(player)`
- s — f: `2.4.6: applyTo(r)`
- s — rr: `2.4.7: save(r)`
- c — Browser: `2.5: redirect /scout/reports/{id} + flash`

Add a second, smaller communication diagram for the **ownership check on delete**:
- `1: POST /scout/reports/7/delete`
- `1.1: deleteReport(7, userId)`
- `1.1.1: findOwnedReport(7, userId)`
- `1.1.1.1: rr.findById(7)`
- `1.1.1.2: r.isOwnedBy(userId)`
- `[owner] 1.1.2: rr.delete(r)`
- `1.2: redirect + flash`

### 16.8 Deployment Diagram
**Nodes:**
1. **Client Device** «device» (desktop, tablet or mobile) → **Web Browser** «execution environment» (Chrome, Edge, Firefox, Safari).
   - Artifacts: rendered HTML pages, `style.css`, `app.js`; «localStorage» `spvss-theme`.
2. **Application Server** «device» (developer PC / host machine, Windows, JDK 17) → **JVM 17** «execution environment» → **Embedded Apache Tomcat** «web server» (port 8080).
   - Artifact: `sports-player-valuation-scouting-1.0.0.jar` «executable jar» (Spring Boot fat jar) containing the Spring MVC controllers, services, Thymeleaf templates, static resources, Hibernate/JPA, HikariCP and the PostgreSQL JDBC driver.
   - Configuration artifact: environment variables / `.env` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_*`), «not committed».
3. **Supabase Cloud** «cloud node» → **PostgreSQL Server** «database server».
   - Artifact: database schema `public` (tables app_users, players, shortlist, comparisons, comparison_players, scouting_reports).
4. **Google Fonts CDN** «external node» (optional): Inter font files.

**Communication paths:**
- Browser ↔ Tomcat: «HTTP» (localhost:8080 in development; HTTPS if deployed behind a proxy).
- JVM ↔ PostgreSQL: «JDBC over TCP/SSL», `sslmode=require`, through the Supabase connection pooler (pool size limited).
- Browser ↔ Google Fonts: «HTTPS».

Note: "All database access is server-side; the browser never connects to the database."

---

## 17. Testing and test cases

### 17.1 Automated unit tests (in the repository, `src/test/java/com/fsms/service/`)
Run with `mvn test`. **Result: 18 tests, 0 failures, 0 errors** (latest run: build `mvn package`, BUILD SUCCESS).

| ID | Test class → method | Verifies | Result |
|---|---|---|---|
| UT-01 | PerformanceAnalyticsServiceTest → `cricketLowerEconomyRateWins` | Economy 6.5 beats 8.2; scores 100/0; win/tie counts | Pass |
| UT-02 | → `tennisLowerRankingWinsAndZeroIsUnranked` | Rank 3 beats 80; an unranked (0) player never wins; shows "Unranked" | Pass |
| UT-03 | → `cricketZeroEconomyMeansDidNotBowlAndNeverWins` | 0 economy shows "Did not bowl", loses, is excluded from the average (7.5) and from best, status "No data" | Pass |
| UT-04 | → `scaledStatisticsAreDecodedForDisplay` | 452 → "45.2", 74 → "7.4", 475 → "47.5%" | Pass |
| UT-05 | → `sportAverageUsesDecodedValues` | Averages 200 / 40.0 / 7.0; economy leaderboard lowest first | Pass |
| UT-06 | → `comparisonAcrossSportsIsRejected` | `isSameSport` false, `comparePlayers` null, `compare` throws | Pass |
| UT-07 | → `performanceIndexAndStrongestWeakest` | Index 100 / 0; strongest stat; tied best shares rank 1 | Pass |
| UT-08 | → `singlePlayerSportScoresFull` | A single player gets peer count 1 and index 100 | Pass |
| UT-09 | → `profileLabelsMatchPlayerLabelsForEverySport` | Parity of labels between `Player.getSportStatLabels()` and the `SportProfile` labels | Pass |
| UT-10 | → `footballDerivedMetricsUseStoredStatsOnly` | Involvements 20, 0.30, 0.50; "—" when appearances are 0 | Pass |
| UT-11 | ScoutingReportServiceTest → `createAssociatesPlayerAndAuthorAndTrimsText` | Author and player links, trimming, rating, verdict | Pass |
| UT-12 | → `createFailsForUnknownPlayer` | Returns null and never saves | Pass |
| UT-13 | → `ownerCanReadButOtherScoutCannot` | `findOwnedReport` owner vs other vs null | Pass |
| UT-14 | → `otherScoutCannotUpdate` | Report unchanged, no save | Pass |
| UT-15 | → `ownerCanUpdate` | Verdict changed; player unchanged | Pass |
| UT-16 | → `otherScoutCannotDelete` | No delete call | Pass |
| UT-17 | → `ownerCanDelete` | Delete called | Pass |
| UT-18 | → `missingReportBehavesLikeNotOwned` | Missing id gives null / false | Pass |

The tests use plain JUnit 5 and Mockito, with mocked repositories and no Spring context or database.

### 17.2 End-to-end browser testing (performed during development)
The full application ran against the real Supabase database, driven by headless Chrome through Playwright. The script lives outside the repository.

**Results:**
- **Final run: 50 of 50 checks passed.** The signup check was skipped because the QA account already existed.
- The run before it: **51 of 51 passed**, signup included.
- An earlier run found two defects, now fixed:
  - a database NOT NULL error on report save (see §20);
  - a 404 for the missing favicon.

| Area | Checks performed |
|---|---|
| Theme | Toggle to dark; dark persists after refresh; persists across navigation; toggles back to light |
| Auth | Signup redirects to login with a success message; scout login reaches the dashboard; dashboard shows the "My scouting reports" KPI |
| Players | List loads (7 players: Basketball 2, Cricket 3, Football 1, Tennis 1); sport filter returns only that sport |
| Player profile | Performance insights visible; index ring visible; reports section visible; insight bar widths match their scores after the animation |
| Analytics | Football, Cricket, Basketball and Tennis pages render; Cricket economy leaderboard is lowest-first (0 economy excluded); Tennis ranking leaderboard is lowest-first |
| Compare | Player 2 options limited to the same sport; head-to-head shown; radar chart shown; comparison saved; listed in history; reopens with analytics; deleted; cross-sport request rejected with the same-sport error |
| Reports CRUD | Empty state shown; empty submit shows ≥ 4 validation errors; report created and opened; potential "8/10" shown; edit saved (verdict changed to Monitor, text updated); report appears in the list; report listed on the player profile |
| Admin | Admin login reaches the admin dashboard; admin player list renders; player form relabels stats for Cricket ("Economy") |
| Ownership | A different user (the Admin account) cannot view the scout's report; cannot open its edit form; forged POST update and delete requests are redirected; that user's report list does not contain it; the report is unchanged afterwards |
| Responsive | No horizontal page scroll at 390px on the dashboard, player, analytics and report pages; mobile menu opens; no horizontal page scroll at 820px on the analytics page |
| UI rule | Every visible button has a border ≥ 1px and a corner radius ≥ 4px |
| Cleanup | Owner deletes the report successfully |
| Console | No browser console errors |

Test data from these runs (saved comparisons and reports) was deleted by the script. Two QA scout accounts with `@example.com` emails remain in `app_users`; the application has no account-deletion feature.

### 17.3 Manual test cases (for the report's test-case table)
| TC | Scenario | Steps | Expected | Actual |
|---|---|---|---|---|
| TC-01 | Register with mismatched passwords | Signup with different confirmation | "Passwords do not match." | *Not executed in this iteration; record when run* |
| TC-02 | Duplicate email | Register an existing email | "An account with this email address already exists." | *Not executed in this iteration; record when run* |
| TC-03 | Wrong password | Login with a bad password | "Invalid email or password. Please try again." | *Not executed in this iteration; record when run* |
| TC-04 | Scout opens an admin page | GET /admin/players as a Scout | Redirect to /login (`PlayerController.isAdmin`) | *Not executed in this iteration; record when run* |
| TC-05 | Add player with blank name | Submit the form | "Name is required" | *Not executed in this iteration; record when run* |
| TC-06 | Calculate valuation | Click Calculate | Value stored and shown in £ | *Not executed in this iteration; record when run* |
| TC-07 | Compare players of different sports | Force URL ids from two sports | Same-sport error shown | Pass (E2E: Compare) |
| TC-08 | Cricket economy direction | Compare two bowlers | Lower economy highlighted | Pass (UT-01; E2E: Analytics) |
| TC-09 | Create report with empty fields | Submit an empty form | Field errors on all required fields | Pass (E2E: Reports CRUD) |
| TC-10 | Edit another scout's report | Open /scout/reports/{id}/edit as another user | Redirect with "not found, or belongs to another scout" | Pass (E2E: Ownership) |
| TC-11 | Delete a player who has reports | Admin deletes the player | Player and their reports removed together | Implemented in `PlayerService.deletePlayer`; *not executed against the live DB (to avoid deleting real players); record when run* |
| TC-12 | Theme persistence | Toggle dark, refresh, navigate | Stays dark | Pass (E2E: Theme) |
| TC-13 | Mobile layout | 390px width | No sideways scroll; menu button works | Pass (E2E: Responsive) |

### 17.4 Build verification
`mvn package` → BUILD SUCCESS. Executable jar: `target/sports-player-valuation-scouting-1.0.0.jar`. A secret scan of the source (excluding `.env`) found no credentials.

---

## 18. Results
- All three requested features work end to end against Supabase.
- Scouting reports persist in PostgreSQL, are linked to the player and the author, and cannot be read, modified or deleted by another user.
- Analytics show sport-specific KPIs, leaderboards, insights and comparison charts. They follow each statistic's direction, decode ×10 values, treat 0 economy and 0 ranking as missing, and use only stored data.
- The whole UI shares one modern design system, with dark/light themes that persist and responsive layouts verified at 1440px, 820px and 390px widths.
- Existing features (signup, login, admin CRUD, search, valuation, shortlist, saved comparisons) still work. The comparison feature was improved: it previously highlighted the higher value for every statistic (wrong for economy and ranking) and allowed cross-sport comparisons.

### Before → after fixes made during this iteration
| Area | Before | After |
|---|---|---|
| Comparison winner | Highlighted the higher (`>=`) value for every stat, so a worse cricket economy or tennis ranking won; ties highlighted both | Direction-aware winner per stat; ties not highlighted |
| Cross-sport comparison | Allowed (with only a warning) | Blocked in the controller, on save and in the UI; legacy saved ones open with a warning |
| Tennis ranking label | "Ranking (lower=better, inverted)" (confusing) | "Ranking (lower is better)" everywhere (parity test) |
| ×10 statistics | Raw integers shown (for example 74) | Decoded in analytics (7.4) |
| Player deletion | Would be blocked by a report foreign key | Deletes the player's reports in the same transaction |
| UI | Basic CSS, emoji icons, no dark mode, not responsive | Design system, dark/light, responsive, accessible focus states |

---

## 19. Screenshot plan (placeholders for the report)
Capture each screenshot at 1440px wide (light theme unless stated) and insert it at the matching figure:

| Figure | Page / state | URL |
|---|---|---|
| Fig. 1 | Login, light | `/login` |
| Fig. 2 | Login, dark | `/login` after the toggle |
| Fig. 3 | Signup | `/signup` |
| Fig. 4 | Scout dashboard | `/scout/dashboard` |
| Fig. 5 | Admin dashboard | `/admin/dashboard` |
| Fig. 6 | Admin player list | `/admin/players` |
| Fig. 7 | Admin player form (Cricket labels) | `/admin/players/new` |
| Fig. 8 | Player search with filter | `/scout/players?sport=Cricket` |
| Fig. 9 | Player profile with performance insights, index ring, KPIs, reports card | `/scout/players/{id}` |
| Fig. 10 | Analytics: Cricket (economy lowest-first) | `/scout/analytics?sport=Cricket` |
| Fig. 11 | Analytics: Basketball, dark | `/scout/analytics?sport=Basketball` |
| Fig. 12 | Compare form (Player 2 filtered) | `/scout/compare` |
| Fig. 13 | Comparison result: duel bars and radar | `/scout/compare/result?...` |
| Fig. 14 | Comparison result, dark | same |
| Fig. 15 | Comparison history | `/scout/comparisons` |
| Fig. 16 | Shortlist | `/scout/shortlist` |
| Fig. 17 | Scouting report form with validation errors | `/scout/reports/new` |
| Fig. 18 | Scouting report view | `/scout/reports/{id}` |
| Fig. 19 | Scouting reports list with sport tabs | `/scout/reports` |
| Fig. 20 | Ownership error message (another user) | `/scout/reports` after a blocked access |
| Fig. 21 | Mobile (390px) dashboard with menu open | `/scout/dashboard` |
| Fig. 22 | Mobile analytics | `/scout/analytics` |
| Fig. 23 | Tablet (820px) analytics | `/scout/analytics` |
| Fig. 24 | Supabase table editor showing `scouting_reports` | Supabase dashboard |
| Fig. 25 | `mvn test` console output showing 18 tests passed | terminal |

---

## 20. Challenges and solutions (real ones from this project)
| Challenge | Solution |
|---|---|
| Different sports measure performance differently, and two statistics are "lower is better" | Introduced `StatDefinition` (direction, scale, missing-value rule) and one `SportProfile` subclass per sport, so every comparison uses `compareRaw` |
| ×10 integer storage made values look wrong (74 instead of 7.4) | `StatDefinition.displayValue/format`; averages computed on decoded values |
| 0 economy (did not bowl) and 0 ranking (unranked) were being ranked as the best values | `zeroMeansMissing` with labels "Did not bowl" and "Unranked"; excluded from stats; covered by tests |
| The Supabase database already had an empty `scouting_reports` table with a different layout (`scout_id NOT NULL`, extra columns). The first save failed with a NOT NULL violation | Inspected the live schema read-only, mapped the report owner to the existing `scout_id` column, removed only the empty column created during development, widened `potential` to 2000 characters, and documented the legacy columns. The empty table was checked before any change |
| Supabase connection pooler limit (pool_size 15) was reached while the application held its pool | Stopped the app before running admin queries; kept the application's Hikari settings |
| Ownership had to be enforced even against typed URLs and forged POSTs | A single `findOwnedReport` gate used by view, edit, update and delete; unit-tested and verified end to end |
| Avoiding a flash of the light theme in dark mode | Inline theme bootstrap script in `<head>` before the CSS loads |
| Keeping all page templates consistent | Thymeleaf fragments (`head`, `topbar`, `alerts`, `themeToggle`) and design tokens |
| Charts without adding a JS library | Server-computed SVG (`RadarChart`) and CSS bars, which work with both themes through CSS variables |
| Deleting a player would break the report foreign key | `PlayerService.deletePlayer` became `@Transactional` and deletes the player's reports first |

---

## 21. Known limitations (honest) and future work
**Limitations in the current code (pre-existing unless noted):**
1. `SecurityConfig` permits all URLs and disables CSRF. Authorisation is done by session checks in each controller, not by Spring Security rules.
2. The shortlist is keyed by the scout's **full name** (`scoutName`), not by user id, so two scouts with identical names would share a shortlist. `removeFromShortlist(entryId)` does not check the owner.
3. `comparison_players.player_id` has `ON DELETE RESTRICT` (V2 script). Deleting a player who appears in a saved comparison is blocked by the database.
4. `spring.jpa.hibernate.ddl-auto=update` manages the schema; there is no migration tool (the SQL files are run manually).
5. `User`, `Admin` and `Scout` are demonstration classes only.
6. The performance index is relative to the players stored in the database; with very few players it is not meaningful.
7. `scouting_reports` keeps four unused legacy columns.
8. There are no controller or integration tests in the repository; the end-to-end checks were run with an external script.

**Future work:**
- Spring Security URL rules with CSRF tokens.
- A shortlist keyed by user id.
- Flyway migrations.
- Sharing reports with a team.
- Per-match statistics, enabling time-series analytics.
- PDF export of reports.
- Pagination and sorting for large player sets.
- Integration tests with Testcontainers.

---

## 22. Setup and run guide
1. Install JDK 17 and Maven (or use `mvnw.cmd`).
2. Provide the environment variables `DB_URL` (for example `jdbc:postgresql://<host>:5432/postgres?sslmode=require`), `DB_USERNAME` and `DB_PASSWORD`. Optionally provide `ADMIN_FULL_NAME`, `ADMIN_EMAIL` and `ADMIN_PASSWORD` to seed the first Admin. See `.env.example`; never commit real values.
3. Optionally run `V2__…sql` and `V3__…sql` in the Supabase SQL editor (Hibernate also creates the tables).
4. `mvn spring-boot:run`, then open `http://localhost:8080`.
5. Tests: `mvn test`. Build the jar: `mvn package`, then `java -jar target/sports-player-valuation-scouting-1.0.0.jar`.

---

## 23. Mapping to the academic report sections
| Report section | Use sections |
|---|---|
| Introduction and Background | §1, §2.1 |
| Project Objectives | §2.3 |
| Problem Statement | §2.2 |
| Proposed Solution and Methodology | §1 summary, §6.1, §15; methodology: iterative development (inspect → design → implement → unit test → end-to-end test → fix → document) |
| Technologies and Tools Used | §3 |
| System Architecture | §6 (+ §16.5, §16.8) |
| Object-Oriented Programming Concepts | §13 |
| Design Patterns Used | §14 |
| All 8 UML Diagram Specifications | §16.1–§16.8 |
| Implementation Details | §7, §9, §12 |
| Scout Notes / Player Scouting Reports Feature | §10, §9.4, §15 W3, §16.6 SD-1 |
| Player Performance Analytics Feature | §11, §9.2–9.3, §15 W4–W5, §16.6 SD-2 |
| Database Design and Relationships | §8 |
| User Roles and System Functionality | §4, §5 |
| Testing and Test Cases | §17 |
| Results and Screenshots | §18, §19 |
| Challenges and Solutions | §20 |
| Conclusion and Learning Outcome | Draw from §18 and §20. Learning outcomes: applying OOP (abstraction and polymorphism through `SportProfile`), the Strategy pattern, layered MVC with Spring, JPA relationships and ownership rules, domain-correct statistics, accessible responsive UI with theming, unit testing with mocks, and diagnosing a live schema mismatch safely |

---

## 24. Glossary and naming rules
- **Scout notes / scouting report:** a `ScoutingReport` entity (the terms are used interchangeably in the UI: "Scout notes", "Scouting reports").
- **Performance index:** the average 0–100 score across a player's available statistics, relative to the same sport in the database.
- **Score:** the per-statistic min-max normalised value (0–100) in the stat's direction.
- **Direction:** "Higher is better" or "Lower is better" (`StatDefinition.higherIsBetter`).
- **Scaled (×10) stat:** a value stored multiplied by 10 to keep one decimal place in an integer column.
- **Missing value:** a raw 0 for Cricket economy ("Did not bowl") or Tennis ranking ("Unranked").
- **Owner / author:** the `AppUser` in `ScoutingReport.author` (DB column `scout_id`).
- Always write class names in `UpperCamelCase` and methods exactly as in §7. Write routes exactly as in §5.3.
