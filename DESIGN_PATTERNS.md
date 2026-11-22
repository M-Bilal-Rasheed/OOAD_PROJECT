# Design Patterns, OOP Principles & Relationships — SPVSS Football Scouting & Valuation System

> Quick-reference map of every design pattern and OOP concept **actually implemented** in this
> project, with the file name and line numbers where each one lives.
> All paths are relative to `src/main/java/com/fsms/` unless stated otherwise.
> This document mirrors `report.md` (§13 OOP concepts, §14 Design patterns) — **no code was
> changed to produce it**, and nothing here claims a pattern the project does not implement.

---

## 1. Design Patterns Implemented

### 1.1 Strategy (GoF behavioural pattern)

| File | Lines | Role |
|---|---|---|
| `valuation/ValuationStrategy.java` | 15–20 | The **Strategy interface** — `calculateValue(Player)` |
| `valuation/BasicValuationStrategy.java` | 13, 51–107 | The **concrete strategy** (`@Component`) — per-sport valuation formulas |
| `service/PlayerService.java` | 27, 40, 148 | The **context** — `ValuationStrategy` injected by constructor; `calculateAndSaveValue` calls `valuationStrategy.calculateValue(player)` polymorphically |

Swap the valuation algorithm → only a new `ValuationStrategy` bean is needed; `PlayerService` never changes.

### 1.2 Registry (lookup by key)

| File | Lines | Role |
|---|---|---|
| `analytics/SportProfileRegistry.java` | 18–52 | `@Component` registry; Spring injects `List<SportProfile>` (all 4 profile beans) into the constructor (lines 25–34); `get(sport)` (lines 37–43) returns the matching abstract `SportProfile`; `SPORT_ORDER` constant at line 21 |

### 1.3 Template Method (simple form)

| File | Lines | Role |
|---|---|---|
| `analytics/SportProfile.java` | 14–42 | Abstract class with a **concrete** `getLabels()` (lines 34–41) that calls the **abstract** `getStatDefinitions()` implemented by each subclass; shared helper `ratio()` at lines 29–31 |
| `analytics/FootballProfile.java` | 11–44 | Subclass implementation |
| `analytics/CricketProfile.java` | 15–48 | Subclass implementation |
| `analytics/BasketballProfile.java` | 12–42 | Subclass implementation |
| `analytics/TennisProfile.java` | 13–50 | Subclass implementation |

### 1.4 MVC (Model–View–Controller) + Layered Architecture

| File | Lines | Role |
|---|---|---|
| `controller/AuthController.java` | 24–161 | Login / signup / logout |
| `controller/PlayerController.java` | 20–125 | Admin player CRUD (`/admin`) |
| `controller/ScoutController.java` | (whole file) | Scout dashboard, search, shortlist, compare |
| `controller/ScoutingReportController.java` | (whole file) | Report create/edit/delete/view |
| `controller/ComparisonController.java` | (whole file) | Save/load/delete comparisons |
| `controller/AnalyticsController.java` | 29–47 area | Performance analytics pages |
| `controller/HomeController.java`, `controller/ErrorHandlerController.java`, `controller/GlobalModelAttributes.java` | (whole files) | Home redirect, error page, shared model attributes |
| `service/*.java` (`PlayerService`, `ScoutingReportService`, `ComparisonService`, `UserService`, `PerformanceAnalyticsService`) | (whole files) | Business logic layer |
| `repository/*.java` | (whole files) | Persistence layer |
| `resources/templates/**` | (whole folder) | Thymeleaf views |

Layers: **controller → service → repository → database**.

### 1.5 Repository (persistence abstraction)

| File | Lines | Role |
|---|---|---|
| `repository/PlayerRepository.java` | 13–29 | `JpaRepository<Player, Long>` + custom `searchPlayers` query |
| `repository/ScoutingReportRepository.java` | 14–28 | incl. `@Modifying deleteByPlayerId` (lines 25–27) |
| `repository/ComparisonRepository.java` | 11–15 | `findByUserIdOrderByCreatedAtDesc` |
| `repository/ComparisonPlayerRepository.java` | 12–19 | `@Modifying deleteByPlayerId` — added for the player-delete FK fix |
| `repository/AppUserRepository.java` | 11–19 | `findByEmail`, `existsByRole` |
| `repository/ShortlistRepository.java` | 12–19 | shortlist lookups |

### 1.6 Dependency Injection / Inversion of Control

Constructor injection everywhere — Spring wires the object graph:

| File | Lines |
|---|---|
| `service/PlayerService.java` | 37–47 |
| `service/ComparisonService.java` | 24–28 |
| `service/PerformanceAnalyticsService.java` | 36–40 |
| `controller/PlayerController.java` | 30–32 |
| `controller/AuthController.java` | 30–32 |
| `analytics/SportProfileRegistry.java` | 25–34 (`List<SportProfile>` — all profile beans injected) |
| `config/SecurityConfig.java` | 15–17 (`@Bean PasswordEncoder` → `BCryptPasswordEncoder`) |

### 1.7 DTO / Form Object

| File | Lines | Role |
|---|---|---|
| `model/ScoutingReportForm.java` | 14–102 | Separates HTML binding/validation from the entity; `static fromReport` (line 51) pre-fills the edit form; `applyTo` (line 65) copies only allowed fields — prevents over-posting of id/author |

### 1.8 Value Object (immutable result objects)

| File | Lines |
|---|---|
| `analytics/StatDefinition.java` | 22–120 (`final` class, `private final` fields, two constructors at 33 and 38 — also **overloading**) |
| `analytics/DerivedMetric.java` | 8–23 |
| `analytics/StatInsight.java` | 7–49 |
| `analytics/RankedEntry.java` | 6–26 |
| `analytics/StatSummary.java` | 10–32 |
| `analytics/SportSummary.java` | 9–39 |
| `analytics/ComparisonRow.java` | 8–42 |
| `analytics/PlayerComparison.java` | 12–56 |
| `analytics/PlayerInsight.java` | (whole file, `final` immutable) |

### 1.9 Post/Redirect/Get + Flash messages

| File | Lines | Role |
|---|---|---|
| `controller/PlayerController.java` | 92–114 (`savePlayer`), 117–124 (`deletePlayer`) | Every POST returns `redirect:` + `RedirectAttributes.addFlashAttribute` — prevents duplicate submission |
| `controller/AuthController.java` | 117–151 (`processSignup`) | Same pattern |

### 1.10 Front Controller (framework-provided)

Spring's `DispatcherServlet` routes every request — provided by Spring MVC, no project file.

### 1.11 Composite view / template fragments

| File | Lines | Role |
|---|---|---|
| `resources/templates/fragments/layout.html` | (whole file) | Shared `head`, `topbar`, `alerts`, `themeToggle` fragments included by every page via `th:replace` (e.g. line 81) |

### 1.12 Patterns NOT implemented (do not claim these)

Per `report.md` §14 — **none of these exist in the code:**

- **Singleton** — beyond Spring's default singleton bean scope (no hand-written singleton)
- **Factory Method / Abstract Factory** — no factory classes (closest thing is the static `ScoutingReportForm.fromReport`, a simple static factory helper, not the GoF pattern)
- **Adapter** — none
- **Facade** — none
- **Observer** — none
- Builder, Decorator — none

---

## 2. OOP Principles (4 Pillars + Relationships)

### Pillar 1 — Encapsulation

| File | Lines | Example |
|---|---|---|
| `model/Player.java` | 19–56 (fields), 85–113 (getters/setters) | All fields `private`, accessed only via getters/setters |
| `model/User.java` | 9–34 | `private username/role` + getters/setters |
| `model/ScoutingReport.java` | 87 | `isOwnedBy(userId)` hides ownership logic inside the object |
| `model/ScoutingReportForm.java` | 65–… | `applyTo(report)` controls exactly which fields a form may change |
| `analytics/*` result classes | see §1.8 | Immutable: `private final` fields, **no setters** |

### Pillar 2 — Abstraction

| File | Lines | Example |
|---|---|---|
| `valuation/ValuationStrategy.java` | 15–20 | Interface — *what* (`calculateValue`), not *how* |
| `analytics/SportProfile.java` | 14–26 | Abstract class — callers depend on `getStatDefinitions()`/`derivedMetrics()`, not the sport-specific code |

### Pillar 3 — Inheritance

| File | Lines | Example |
|---|---|---|
| `model/Admin.java` | 7–27 | `Admin extends User` (demonstration hierarchy — **not persisted**, auth uses `AppUser`) |
| `model/Scout.java` | 10–53 | `Scout extends User` (same note) |
| `model/User.java` | 7–40 | Parent class |
| `analytics/FootballProfile.java` | 11 | `extends SportProfile` |
| `analytics/CricketProfile.java` | 15 | `extends SportProfile` |
| `analytics/BasketballProfile.java` | 12 | `extends SportProfile` |
| `analytics/TennisProfile.java` | 13 | `extends SportProfile` |
| `analytics/SportProfile.java` | 14 | Parent abstract class — subclasses inherit `getLabels()` and `ratio()` |

### Pillar 4 — Polymorphism (runtime / dynamic dispatch)

| File | Lines | Example |
|---|---|---|
| `service/PlayerService.java` | 148 | `valuationStrategy.calculateValue(player)` — same call, different implementation |
| `service/PerformanceAnalyticsService.java` | 89–91, 128–133, 163, 171–180, 225–228 | `profile.getStatDefinitions()` / `profile.derivedMetrics(...)` called on a `SportProfile` reference from `registry.get(sport)` — runs Football/Cricket/Basketball/Tennis code depending on the player's sport |

### Method Overriding

| File | Lines |
|---|---|
| `model/Admin.java` | 17–26 (`getRole`, `getDisplayInfo`) |
| `model/Scout.java` | 28–37 (`getRole`, `getDisplayInfo`) |
| `valuation/BasicValuationStrategy.java` | 51–52 (`@Override calculateValue`) |
| Each `analytics/*Profile.java` | `@Override getSport / getStatDefinitions / derivedMetrics / getDescription` |

### Method Overloading

| File | Lines |
|---|---|
| `analytics/StatDefinition.java` | constructors at 33 and 38 (7-arg and 8-arg) |
| `model/PlayerStatistics.java` | constructors at 35 and 39 |
| `model/ShortlistEntry.java` | constructors at 25 and 28 |

### Relationship Types

#### Composition (strong "has-a" — part dies with the whole ◆)

| File | Lines | Whole → Part |
|---|---|---|
| `model/Player.java` | 53–56, 59–61, 111–113 | `Player` ◆ `PlayerStatistics` (`@Embedded`, created in constructor) |
| `model/PlayerStatistics.java` | 7–18 | The owned part |
| `model/Comparison.java` | 34–35 | `Comparison` ◆ `ComparisonPlayer` (`@OneToMany cascade=ALL, orphanRemoval=true`) |
| `model/ComparisonPlayer.java` | 19–25 | Links comparison to player |
| `analytics/PlayerComparison.java` | 17–23 | ◆ `List<ComparisonRow>`, ◆ `RadarChart` |
| `analytics/SportProfile` subclasses | e.g. `FootballProfile.java` | ◆ `List<StatDefinition>` returned by `getStatDefinitions()` |
| `analytics/StatSummary.java` / `SportSummary.java` / `PlayerInsight.java` | 10–16 / 9–16 / (whole) | ◆ `StatSummary`, `RankedEntry`, `StatInsight`, `DerivedMetric` lists |

#### Aggregation (weak "has-a" — independent lifetimes ◇)

| File | Lines | Example |
|---|---|---|
| `model/Scout.java` | 18, 40–47 | `Scout` ◇ `List<Player>` shortlist — **demonstration only, not persisted** |
| `service/PlayerService.java` | 172–194 | `addToShortlist` / `getShortlist` — shortlist entries reference players that live independently; removing an entry never deletes the player |
| `model/ShortlistEntry.java` | 21–22 | Holds `playerId` — just a reference (aggregation) |
| `analytics/SportProfileRegistry.java` | 23–33 | Registry ◇ `Map<String, SportProfile>` |

#### Association (navigable reference →)

| File | Lines | Relationship |
|---|---|---|
| `model/ScoutingReport.java` | 31–33 | → `Player` (`@ManyToOne`, `player_id`) — report is *about* a player |
| `model/ScoutingReport.java` | 37–39 | → `AppUser` author (`@ManyToOne`, `scout_id`) — report *written by* a user |
| `model/ScoutingReport.java` | (recommendation field) | → `Recommendation` enum |
| `model/ComparisonPlayer.java` | 19–25 | → `Comparison` (`@ManyToOne`); → `Player` by `playerId` |
| `model/Comparison.java` | (userId field) | → `AppUser` owner by `userId` |
| `model/ShortlistEntry.java` | 22 | → `Player` by `playerId` |
| `service/PlayerService.java` | 27, 40 | → `ValuationStrategy` (PlayerService is the strategy **context**) |
| `service/PerformanceAnalyticsService.java` | 36–38 | → `SportProfileRegistry`, `PlayerRepository` |
| Controllers → Services → Repositories | constructor injection everywhere | Dependency direction: controller → service → repository |

#### Other OOP concepts present

| Concept | File | Lines |
| Static members | `analytics/SportProfileRegistry.java` | 21 (`SPORT_ORDER`) |
| Static members | `model/ScoutingReport.java` | 24 (`TEXT_LIMIT`) |
| Static members | `service/PerformanceAnalyticsService.java` | 238 (`score`), 255 (`rank`) |
| Static factory helper | `model/ScoutingReportForm.java` | 51 (`fromReport`) |
| Class & object | `model/Player.java` | 15–114; objects created by `PlayerService.addPlayer` (`service/PlayerService.java` 53–55) and by JPA |
| Exception handling | `service/PerformanceAnalyticsService.java` | `IllegalArgumentException` for cross-sport comparison |
| Constructor validation | `analytics/StatDefinition.java` | validates its arguments |

---1) SRP — Single Responsibility Principle
Matlab: Ek class ka ek hi kaam ho.

File: RadarChart.java
Line: ~11 (class declaration)
Code:

java
public final class RadarChart {
    // sirf SVG coordinates banata hai
}
Kaam: RadarChart sirf chart ka data (SVG coordinates) banata hai. Koi database, koi validation, koi business logic nahi. 

File: StatDefinition.java
Line: ~20
Code:

java
public final class StatDefinition {
    // sirf ek stat ka rule rakhta hai
}
Kaam: StatDefinition sirf stat ka rule (scale, higher/lower better, missing label) rakhta hai. Comparison/score alag classes mein hai.

2) OCP — Open/Closed Principle
Matlab: Naya code add karo, purana change mat karo.

File: SportProfile.java
Line: ~13
Code:

java
public abstract class SportProfile {
    public abstract String getSport();
    public abstract List<StatDefinition> getStatDefinitions();
}
File: CricketProfile.java / BasketballProfile.java / FootballProfile.java / TennisProfile.java
Line: ~15 (har file mein extends SportProfile)
Code:

java
public class CricketProfile extends SportProfile {
    // ...
}
Kaam: Naya sport (Tennis) add kiya — bas TennisProfile extends SportProfile. PerformanceAnalyticsService change nahi karni parti. Service SportProfile (abstract) pe kaam karti hai. 

3) LSP — Liskov Substitution Principle
Matlab: Child class parent ki jagah use ho sake.

File: SportProfileRegistry.java
Line: ~23
Code:

java
private final Map<String, SportProfile> profiles = new LinkedHashMap<>();
Kaam: Registry SportProfile type pe kaam karti hai. CricketProfile, BasketballProfile, FootballProfile, TennisProfile — koi bhi SportProfile ki jagah use ho sakta hai, bina registry ko farq pare. 

4) ISP — Interface Segregation Principle
Matlab: Chhote interfaces, bade nahi.

File: ValuationStrategy.java (valuation folder)
Line: ~12
Code:

java
public interface ValuationStrategy {
    long calculateValue(Player player);   // sirf ek method
}
Kaam: Interface mein sirf ek method hai. Koi class majboor nahi extra methods likhne. 

5) DIP — Dependency Inversion Principle
Matlab: Abstract pe depend karo, concrete pe nahi.

File: PlayerService.java
Line: ~31
Code:

java
private final ValuationStrategy valuationStrategy;   // interface
File: PlayerService.java
Line: ~116
Code:

java
long value = valuationStrategy.calculateValue(player);   // interface call
Kaam: PlayerService ValuationStrategy (interface) pe depend karti hai, 
BasicValuationStrategy
 (concrete) pe nahi. Kal naya strategy banao, service change nahi. 

File: PerformanceAnalyticsService.java
Line: ~39
Code:

java
private final SportProfileRegistry registry;
Kaam: Service SportProfile (abstract) pe depend karti hai — CricketProfile pe nahi.

## 3. Consistency check vs `report.md`

- Everything above matches `report.md` §13 (OOP concepts) and §14 (design patterns).
- `User` / `Admin` / `Scout` are a **demonstration hierarchy only** — they are never instantiated by the running app; real auth/persistence uses `AppUser`. Keep that note wherever these classes are shown.
- The new `ComparisonPlayerRepository.java` (player-delete FK fix) is a standard Spring Data repository — it adds no new pattern and requires **no report changes**.
