# SPVSS: UML Diagram Specifications (all 8 diagrams)

**System:** SPVSS – Sports Player Valuation & Scouting System (Java 17, Spring Boot 3.2.5, Thymeleaf, Spring Data JPA, PostgreSQL on Supabase, deployed on Railway).

> **Instructions for Claude (or any reader drawing these diagrams):**
> 1. Draw exactly what is written here. Every class, attribute, method, route, table, actor and message below exists in the current source code (`src/`). **Do not add** classes, methods, actors, use cases, patterns or relationships that are not listed.
> 2. Keep every name **exactly** as written (case, spelling, parameters).
> 3. Visibility: `-` private, `+` public, `#` protected, `~` package-private. `{abstract}` = abstract, `{static}` = static, `<<interface>>` = interface.
> 4. The eight diagrams are consistent with one another. Use the same names and the same sample values in all of them.
> 5. Any tool is fine (PlantUML, Mermaid, draw.io, StarUML). Standard UML 2.x notation.

---

## Shared context (applies to all diagrams)

**Actors**
| Actor | Kind | Description |
|---|---|---|
| Visitor | primary, human | Not logged in. Can see login/signup, register as Scout, log in, toggle theme. |
| Scout | primary, human | Registered through `/signup` (role `SCOUT`). Uses players, valuation, shortlist, comparisons, scouting reports, analytics. |
| Admin | primary, human | Created at startup by `AdminSeeder` from environment variables (never by signup). Manages players and can also use every Scout page. |
| Supabase PostgreSQL | secondary, system | Stores all persistent data. |

**Layers:** Browser → Thymeleaf views → Controllers → Services (+ valuation and analytics domain classes) → Spring Data JPA repositories → Supabase PostgreSQL.

**Database tables:** `app_users`, `players`, `shortlist`, `comparisons`, `comparison_players`, `scouting_reports`.

**Sports and their four statistics** (stored as `stat1..stat4` in `players`):
| Sport | stat1 | stat2 | stat3 | stat4 |
|---|---|---|---|---|
| Football | Goals | Assists | Appearances | Speed Rating (1-100) |
| Cricket | Runs | Wickets | Batting Average (×10) | Economy Rate (×10) — lower is better, 0 = "Did not bowl" |
| Basketball | Points | Assists | Rebounds | Shooting % (×10) |
| Tennis | Wins | Ranking (lower is better) — 0 = "Unranked" | Aces | 1st Serve % (×10) |

---

## 1. UML Activity Diagram

> In this project's set of eight diagrams, the "UML" diagram is the **Activity Diagram** (it is the eighth diagram type alongside Use Case, Sequence, Class, Object, Component, Communication and Deployment).

Draw **two** activity diagrams with swimlanes. Activity A is the main one.

### Activity A: Create, edit and delete a Scouting Report
**Swimlanes (partitions):** `Scout (Browser)` | `ScoutingReportController` | `ScoutingReportService` | `Database`

**Flow:**
1. ● Initial node → **[Scout]** "Open player profile" → "Click *Write scouting report*".
2. **[Controller]** "Check session role (isScoutOrAdmin)". Decision ◇ *SCOUT or ADMIN?*
   - [no] → "Redirect to /login" → ◉ final node.
   - [yes] → "Build empty ScoutingReportForm with playerId" → **[Scout]** "Fill strengths, weaknesses, potential + rating (1–10), performance observations, recommendation + summary" → "Submit".
3. **[Controller]** "Validate form (@Valid)". Decision ◇ *errors?*
   - [yes] → "Re-display form with field errors" → loop back to "Fill …".
   - [no] → "Check player exists". Decision ◇ *player exists?*
     - [no] → "Reject playerId ('That player no longer exists')" → loop back to the form.
     - [yes] → continue.
4. **[Service]** "Load AppUser (author) and Player" → "Create ScoutingReport; form.applyTo(report)" → **[Database]** "INSERT into scouting_reports (createdAt/updatedAt set by @PrePersist)".
5. **[Controller]** "Redirect to /scout/reports/{id} with flash 'Scouting report saved.'" → **[Scout]** "View report".
6. Decision ◇ *Edit, Delete or Done?*
   - [Edit] → **[Service]** "findOwnedReport(id, userId)". Decision ◇ *owner?*
     - [no] → **[Controller]** "Redirect to list with 'Report not found, or it belongs to another scout.'" → ◉.
     - [yes] → **[Controller]** "Pre-fill form (ScoutingReportForm.fromReport)" → **[Scout]** "Change fields & submit" → **[Controller]** "Re-check owner, fix playerId to original, validate" → **[Service]** "updateReport: applyTo + save" → **[Database]** "UPDATE (updatedAt set by @PreUpdate)" → back to "View report".
   - [Delete] → **[Scout]** "Confirm prompt". Decision ◇ *confirmed?*
     - [no] → back to "View report".
     - [yes] → **[Service]** "deleteReport(id, userId) (owner check)" → **[Database]** "DELETE" → **[Controller]** "Redirect to list (or to player profile) with 'Scouting report deleted.'" → ◉.
   - [Done] → ◉.

### Activity B: Compare two players
**Swimlanes:** `Scout` | `Browser JS (app.js)` | `ScoutController` | `PerformanceAnalyticsService` | `ComparisonController / Database`

1. ● → **[Scout]** "Select Player 1".
2. **[Browser JS]** "Filter Player 2 options to the same sport (initCompareFilter)".
3. **[Scout]** "Select Player 2" → "Submit".
4. **[ScoutController]** Decision ◇ *same id?* → [yes] "Show form: 'Please select two different players to compare.'" → ◉.
5. Decision ◇ *both players exist?* → [no] "Show form: 'One of the selected players no longer exists.'" → ◉.
6. **[PerformanceAnalyticsService]** "comparePlayers(p1, p2)". Decision ◇ *same sport?*
   - [no] → **[ScoutController]** "Show form with same-sport error" → ◉.
   - [yes] → **Fork** ┬ "Compute per-stat winners (direction-aware compareRaw)" ┬ "Compute scores, performance indexes, radar chart" ┴ **Join** → **[ScoutController]** "Render compare-result".
7. Decision ◇ *save?*
   - [no] → ◉.
   - [yes] → **[Scout]** "Enter title" → **[ComparisonController]** "Re-check same sport" → "Save Comparison + 2 ComparisonPlayers" → "Redirect with flash 'Comparison saved successfully…'" → ◉.

---

## 2. Use Case Diagram

**System boundary:** "SPVSS – Sports Player Valuation & Scouting System".

**Actors:** Visitor, Scout, Admin (left side); Supabase PostgreSQL «system» (right side).
**Generalisation:** Admin ─▷ Scout (Admin inherits all Scout use cases).

**Use cases:**
| ID | Use case | Primary actor |
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
| UC13 | Create Scouting Report | Scout |
| UC14 | View My Scouting Reports (list, filter by sport, open) | Scout |
| UC15 | Edit Scouting Report | Scout |
| UC16 | Delete Scouting Report | Scout |
| UC17 | View Sport Performance Analytics | Scout |
| UC18 | View Player Performance Insights | Scout |
| UC19 | View Admin Dashboard | Admin |
| UC20 | Add Player | Admin |
| UC21 | Edit Player | Admin |
| UC22 | Delete Player | Admin |
| UC23 | View All Players (admin list) | Admin |

**Internal (included) use cases:** Verify Credentials (BCrypt), Validate Signup Data, Verify Report Ownership, Verify Comparison Ownership, Check Same Sport, Delete Player's Scouting Reports, Validate Player Data.

**Relationships:**
- UC2 Log In `«include»` Verify Credentials (BCrypt).
- UC1 Register Scout Account `«include»` Validate Signup Data.
- UC13, UC15, UC16 `«include»` Verify Report Ownership. UC12 `«include»` Verify Comparison Ownership.
- UC10 Compare `«include»` Check Same Sport. UC11 Save Comparison `«extend»` UC10 (extension point: "after result shown").
- UC7 View Player Profile `«include»` UC18 View Player Performance Insights.
- UC13 Create Scouting Report `«extend»` UC7 (extension point: "Write scouting report button").
- UC8 Calculate Player Valuation `«extend»` UC7. UC9 Manage Shortlist (add) `«extend»` UC7.
- UC22 Delete Player `«include»` Delete Player's Scouting Reports.
- UC20 Add Player and UC21 Edit Player `«include»` Validate Player Data.
- Supabase PostgreSQL is associated with every use case that reads or writes data (or draw one note "all data persisted in Supabase PostgreSQL").

**Notes to place on the diagram:**
- "Precondition for all Scout/Admin use cases except UC1, UC2, UC4: logged in as SCOUT or ADMIN."
- "Scouts can only see, edit or delete their own reports."
- "Comparisons are allowed only within the same sport."
- "Analytics use stored statistics only."

---

## 3. Sequence Diagram

Draw **two** sequence diagrams. SD-1 is the main one.

### SD-1: Create Scouting Report
**Lifelines (left to right):** `:Scout` (actor), `:Browser`, `:ScoutingReportController`, `:ScoutingReportService`, `:PlayerService`, `:AppUserRepository`, `:PlayerRepository`, `:ScoutingReportRepository`, `:Database`.

**Messages:**
1. Scout → Browser: click "Write scouting report".
2. Browser → ScoutingReportController: `GET /scout/reports/new?playerId=21`.
3. ScoutingReportController → itself: `isScoutOrAdmin(session)` → `true`.
4. ScoutingReportController → PlayerService: `getPlayerById(21)` → `p1`.
5. ScoutingReportController → itself: `populateForm(model, form, null)`; inside it, → PlayerService: `getAllPlayers()` → list.
6. ScoutingReportController → Browser: render view `scout/reports/form`.
7. Scout → Browser: fill fields, submit. Browser → ScoutingReportController: `POST /scout/reports` (form fields).
8. **alt** fragment on `@Valid` result (`BindingResult`):
   - [hasErrors] ScoutingReportController → Browser: re-render `scout/reports/form` with errors.
   - [else] continue.
9. ScoutingReportController → ScoutingReportService: `createReport(userId=12, form)`.
10. ScoutingReportService → AppUserRepository: `findById(12)` → `Optional<AppUser>`.
11. ScoutingReportService → PlayerRepository: `findById(21)` → `Optional<Player>`.
12. ScoutingReportService: `«create» r : ScoutingReport`; `r.setAuthor(user)`; `r.setPlayer(p1)`; `form.applyTo(r)`.
13. ScoutingReportService → ScoutingReportRepository: `save(r)` → Database: `INSERT INTO scouting_reports …` (`@PrePersist` sets timestamps) → returns `r (id=7)`.
14. ScoutingReportService → ScoutingReportController: `r`.
15. ScoutingReportController → Browser: `redirect:/scout/reports/7` + flash "Scouting report saved.".
16. Browser → ScoutingReportController: `GET /scout/reports/7` → ScoutingReportService: `findOwnedReport(7, 12)` → `r` → render `scout/reports/view`.

**Optional alt fragment** [another scout, userId = 13]: `findOwnedReport(7, 13)` → `null` → redirect to `/scout/reports` with "Report not found, or it belongs to another scout."

### SD-2: Analyse and compare two players
**Lifelines:** `:Scout`, `:Browser`, `:ScoutController`, `:PlayerService`, `:PerformanceAnalyticsService`, `:SportProfileRegistry`, `:CricketProfile`, `:PlayerRepository`.

1. Browser → ScoutController: `GET /scout/compare/result?player1Id=21&player2Id=22`.
2. ScoutController → PlayerService: `comparePlayers(21, 22)` → `[p1, p2]`.
3. ScoutController → PerformanceAnalyticsService: `comparePlayers(p1, p2)`.
4. PerformanceAnalyticsService → itself: `isSameSport(p1, p2)`; → SportProfileRegistry: `supports("Cricket")` → `true`.
   **alt** [different sport]: return `null`; ScoutController re-renders `scout/compare` with the same-sport error.
5. PerformanceAnalyticsService → PlayerRepository: `findBySportIgnoreCase("Cricket")` → `peers`.
6. PerformanceAnalyticsService → itself: `compare(p1, p2, peers)`; → SportProfileRegistry: `get("Cricket")` → `cricket : CricketProfile`.
7. **loop** [for each StatDefinition in `cricket.getStatDefinitions()`]: `def.rawValue(...)` ×2, `score(def, raw, values)` ×2, `def.compareRaw(rawA, rawB)` → winner, `«create» ComparisonRow`.
8. `performanceIndex(...)` ×2 → `«create» RadarChart(labels, scoresA, scoresB)` → `«create» PlayerComparison`.
9. Return `PlayerComparison` to ScoutController → `model.addAttribute("comparison", …)` → render `scout/compare-result`.

---

## 4. Class Diagram

Group classes into packages: `model`, `valuation`, `analytics`, `service`, `repository`, `controller`, `config`.

### 4.1 package `model`

**AppUser** «entity» (table `app_users`)
- Attributes: `-id: Long`, `-fullName: String`, `-email: String` (unique), `-passwordHash: String`, `-role: String` ("ADMIN" | "SCOUT"), `-createdAt: LocalDateTime`
- Operations: `+AppUser()`, `+prePersist(): void`, getters/setters for all attributes.

**Player** «entity» (table `players`)
- Attributes: `-id: Long`, `-name: String`, `-age: int`, `-nationality: String`, `-sport: String`, `-position: String`, `-club: String`, `-estimatedValue: Long`, `-statistics: PlayerStatistics`
- Operations: `+Player()`, `+getSportStatLabels(): String[]`, getters/setters.

**PlayerStatistics** «embeddable»
- Attributes: `-stat1: int`, `-stat2: int`, `-stat3: int`, `-stat4: int`
- Operations: `+PlayerStatistics()`, `+PlayerStatistics(stat1: int, stat2: int, stat3: int, stat4: int)`, getters/setters.

**ShortlistEntry** «entity» (table `shortlist`)
- Attributes: `-id: Long`, `-scoutName: String`, `-playerId: Long`
- Operations: `+ShortlistEntry()`, `+ShortlistEntry(scoutName: String, playerId: Long)`, getters/setters.

**Comparison** «entity» (table `comparisons`)
- Attributes: `-id: Long`, `-userId: Long`, `-title: String`, `-createdAt: LocalDateTime`, `-comparisonPlayers: List<ComparisonPlayer>`
- Operations: `+Comparison()`, `+prePersist(): void`, getters/setters.

**ComparisonPlayer** «entity» (table `comparison_players`)
- Attributes: `-id: Long`, `-comparison: Comparison`, `-playerId: Long`
- Operations: `+ComparisonPlayer()`, `+ComparisonPlayer(comparison: Comparison, playerId: Long)`, getters/setters.

**ScoutingReport** «entity» (table `scouting_reports`)
- Attributes: `+TEXT_LIMIT: int = 2000 {static}`, `-id: Long`, `-player: Player`, `-author: AppUser`, `-strengths: String`, `-weaknesses: String`, `-potential: String`, `-potentialRating: int`, `-performanceObservations: String`, `-recommendation: Recommendation`, `-recommendationSummary: String`, `-createdAt: LocalDateTime`, `-updatedAt: LocalDateTime`
- Operations: `+ScoutingReport()`, `+prePersist(): void`, `+preUpdate(): void`, `+isOwnedBy(userId: Long): boolean`, `+isEdited(): boolean`, getters/setters.

**ScoutingReportForm** «DTO»
- Attributes: `-playerId: Long`, `-strengths: String`, `-weaknesses: String`, `-potential: String`, `-potentialRating: Integer = 5`, `-performanceObservations: String`, `-recommendation: Recommendation`, `-recommendationSummary: String`
- Operations: `+ScoutingReportForm()`, `+fromReport(r: ScoutingReport): ScoutingReportForm {static}`, `+applyTo(r: ScoutingReport): void`, getters/setters.

**Recommendation** «enumeration»
- Literals: `STRONGLY_RECOMMEND`, `RECOMMEND`, `MONITOR`, `NOT_RECOMMENDED`
- Attributes: `-displayName: String`, `-cssClass: String`
- Operations: `+getDisplayName(): String`, `+getCssClass(): String`

**User** (note: «demonstration hierarchy, not persisted»)
- Attributes: `-username: String`, `-role: String`
- Operations: `+User(username: String, role: String)`, `+getUsername(): String`, `+setUsername(username: String): void`, `+getRole(): String`, `+setRole(role: String): void`, `+getDisplayInfo(): String`

**Admin** extends User
- Operations: `+Admin(username: String)`, `+getRole(): String` (override), `+getDisplayInfo(): String` (override)

**Scout** extends User
- Attributes: `-shortlist: List<Player>`
- Operations: `+Scout(username: String)`, `+getRole(): String` (override), `+getDisplayInfo(): String` (override), `+addToShortlist(player: Player): void`, `+removeFromShortlist(player: Player): void`, `+getShortlist(): List<Player>`

### 4.2 package `valuation`

**ValuationStrategy** «interface»
- `+calculateValue(player: Player): long`

**BasicValuationStrategy** «component», implements ValuationStrategy
- Attributes: 16 private static final weights (`-FB_STAT1..FB_STAT4`, `-CR_STAT1..CR_STAT4`, `-BB_STAT1..BB_STAT4`, `-TN_STAT1..TN_STAT4: long {static}`)
- Operations: `+calculateValue(player: Player): long`

### 4.3 package `analytics`

**SportProfile** {abstract}
- `+getSport(): String {abstract}`, `+getStatDefinitions(): List<StatDefinition> {abstract}`, `+derivedMetrics(stats: PlayerStatistics): List<DerivedMetric> {abstract}`, `+getDescription(): String {abstract}`, `#ratio(numerator: int, denominator: int): double {static}`, `+getLabels(): String[]`

**FootballProfile**, **CricketProfile**, **BasketballProfile**, **TennisProfile** «component», each extends SportProfile
- Attribute: `-STATS: List<StatDefinition> {static}` (4 definitions)
- Operations: `+getSport()`, `+getStatDefinitions()`, `+getDescription()`, `+derivedMetrics(s: PlayerStatistics)` (all overrides)
- TennisProfile also: `~rankingBand(ranking: int): String {static}`

**SportProfileRegistry** «component»
- Attributes: `+SPORT_ORDER: List<String> {static}`, `-profiles: Map<String, SportProfile>`
- Operations: `+SportProfileRegistry(allProfiles: List<SportProfile>)`, `+get(sport: String): SportProfile`, `+supports(sport: String): boolean`, `+getSports(): List<String>`

**StatDefinition** «value object» (final, immutable)
- Attributes: `-slot: int`, `-label: String`, `-shortLabel: String`, `-scale: int`, `-higherIsBetter: boolean`, `-zeroMeansMissing: boolean`, `-unit: String`, `-missingLabel: String`
- Operations: `+StatDefinition(slot, label, shortLabel, scale, higherIsBetter, zeroMeansMissing, unit)`, `+StatDefinition(slot, label, shortLabel, scale, higherIsBetter, zeroMeansMissing, unit, missingLabel)`, `+rawValue(stats: PlayerStatistics): int`, `+displayValue(raw: int): double`, `+isMissing(raw: int): boolean`, `+format(raw: int): String`, `+formatDecoded(value: double): String`, `-formatNumber(value: double): String`, `+compareRaw(a: int, b: int): int`, `+getDirectionLabel(): String`, `+isScaled(): boolean`, getters.

**DerivedMetric** «value object»: `-label: String`, `-value: String`, `-description: String`; constructor + getters.

**StatInsight** «value object»: `-definition: StatDefinition`, `-rawValue: int`, `-formattedValue: String`, `-sportAverageFormatted: String`, `-score: int`, `-rank: int`, `-rankedCount: int`, `-status: String`; `+isMissing(): boolean`, `+getStatusClass(): String`, getters.

**PlayerInsight** «value object»: `-player: Player`, `-profile: SportProfile`, `-stats: List<StatInsight>`, `-derivedMetrics: List<DerivedMetric>`, `-performanceIndex: int`, `-peerCount: int`, `-strongest: StatInsight`, `-weakest: StatInsight`; getters.

**RankedEntry** «value object»: `-rank: int`, `-player: Player`, `-formattedValue: String`, `-score: int`; `+getBarWidth(): int`, getters.

**StatSummary** «value object»: `-definition: StatDefinition`, `-averageFormatted: String`, `-best: RankedEntry`, `-leaders: List<RankedEntry>`, `-rankedCount: int`; getters.

**SportSummary** «value object»: `-profile: SportProfile`, `-playerCount: int`, `-averageAge: String`, `-valuedCount: long`, `-statSummaries: List<StatSummary>`, `-indexLeaderboard: List<RankedEntry>`; `+getSport(): String`, `+isEmpty(): boolean`, `+getTopPerformer(): RankedEntry`, getters.

**ComparisonRow** «value object»: `+A: String {static}`, `+B: String {static}`, `+TIE: String {static}`, `-definition: StatDefinition`, `-formattedA: String`, `-formattedB: String`, `-scoreA: int`, `-scoreB: int`, `-winner: String`; `+getBarA(): int`, `+getBarB(): int`, `+isAWins(): boolean`, `+isBWins(): boolean`, `+isTie(): boolean`, getters.

**PlayerComparison** «value object»: `-playerA: Player`, `-playerB: Player`, `-profile: SportProfile`, `-rows: List<ComparisonRow>`, `-winsA: int`, `-winsB: int`, `-ties: int`, `-indexA: int`, `-indexB: int`, `-radar: RadarChart`; `+getVerdict(): String`, getters.

**RadarChart** (final): `+SIZE: int = 320 {static}`, `-CENTER: double {static}`, `-RADIUS: double = 110.0 {static}`, `-labels: List<String>`, `-scoresA: List<Integer>`, `-scoresB: List<Integer>`; `+RadarChart(labels, scoresA, scoresB)`, `-angle(i: int): double`, `-point(i: int, fraction: double): String`, `-polygon(scores: List<Integer>): String`, `+getPolygonA(): String`, `+getPolygonB(): String`, `+isHasB(): boolean`, `+getViewBox(): String`, `+getGrid(): List<String>`, `+getAxes(): List<Axis>`, `-fmt(v: double): String {static}`.
- Nested static class **RadarChart.Axis**: `-x`, `-y`, `-labelX`, `-labelY`, `-anchor`, `-label: String`; getters.

### 4.4 package `service` (all «service»)

**UserService** — `-userRepository: AppUserRepository`, `-passwordEncoder: PasswordEncoder`
- `+registerScout(fullName: String, email: String, rawPassword: String): String`, `+verifyLogin(email: String, rawPassword: String): AppUser`, `+findById(id: Long): AppUser`, `+seedAdminIfNotExists(fullName: String, email: String, rawPassword: String): void`

**PlayerService** — `-playerRepository: PlayerRepository`, `-shortlistRepository: ShortlistRepository`, `-scoutingReportRepository: ScoutingReportRepository`, `-valuationStrategy: ValuationStrategy`
- `+addPlayer(player: Player): Player`, `+updatePlayer(player: Player): Player`, `+deletePlayer(id: Long): void`, `+getAllPlayers(): List<Player>`, `+getPlayerById(id: Long): Player`, `+getTotalPlayerCount(): long`, `+getValuedPlayerCount(): long`, `+getCountBySport(sport: String): long`, `+searchPlayers(name, sport, position, club: String): List<Player>`, `+calculateAndSaveValue(playerId: Long): Player`, `+comparePlayers(id1: Long, id2: Long): List<Player>`, `+addToShortlist(scoutName: String, playerId: Long): boolean`, `+removeFromShortlist(entryId: Long): void`, `+getShortlist(scoutName: String): List<Player>`, `+getShortlistEntryId(scoutName: String, playerId: Long): Long`, `+isOnShortlist(scoutName: String, playerId: Long): boolean`

**ComparisonService** — `-comparisonRepository: ComparisonRepository`, `-playerRepository: PlayerRepository`
- `+saveComparison(userId: Long, title: String, player1Id: Long, player2Id: Long): Comparison`, `+getComparisonsForUser(userId: Long): List<Comparison>`, `+getComparisonForUser(comparisonId: Long, userId: Long): Comparison`, `+deleteComparison(comparisonId: Long, userId: Long): boolean`, `+getPlayersForComparison(comparison: Comparison): List<Player>`

**ScoutingReportService** — `-reportRepository: ScoutingReportRepository`, `-playerRepository: PlayerRepository`, `-userRepository: AppUserRepository`
- `+createReport(authorId: Long, form: ScoutingReportForm): ScoutingReport`, `+findOwnedReport(reportId: Long, userId: Long): ScoutingReport`, `+updateReport(reportId: Long, userId: Long, form: ScoutingReportForm): ScoutingReport`, `+deleteReport(reportId: Long, userId: Long): boolean`, `+getReportsByAuthor(authorId: Long): List<ScoutingReport>`, `+getReportsByAuthorForPlayer(authorId: Long, playerId: Long): List<ScoutingReport>`, `+countByAuthor(authorId: Long): long`

**PerformanceAnalyticsService** — `-playerRepository: PlayerRepository`, `-registry: SportProfileRegistry`
- `+getSports(): List<String>`, `+getProfile(sport: String): SportProfile`, `+getPlayersForSport(sport: String): List<Player>`, `+summarizeSport(sport: String): SportSummary`, `+analysePlayer(player: Player): PlayerInsight`, `+comparePlayers(a: Player, b: Player): PlayerComparison`, `+isSameSport(a: Player, b: Player): boolean`, `+summarize(profile: SportProfile, players: List<Player>): SportSummary`, `+analyse(player: Player, peers: List<Player>): PlayerInsight`, `+compare(a: Player, b: Player, peers: List<Player>): PlayerComparison`, `-summarizeStat(def: StatDefinition, players: List<Player>): StatSummary`, `~performanceIndex(profile: SportProfile, player: Player, pool: List<Player>): int`, `~score(def: StatDefinition, raw: int, values: List<Integer>): int {static}`, `~rank(def: StatDefinition, raw: int, values: List<Integer>): int {static}`, `-countReal(...): int {static}`, `-average(...): Double {static}`, `-values(...): List<Integer> {static}`, `-withPlayer(peers: List<Player>, player: Player): List<Player> {static}`

### 4.5 package `repository` (all «interface» «repository», each extends `JpaRepository<T, Long>`)
| Interface | Entity | Custom methods |
|---|---|---|
| AppUserRepository | AppUser | `+findByEmail(email: String): Optional<AppUser>`, `+existsByRole(role: String): boolean` |
| PlayerRepository | Player | `+searchPlayers(name, sport, position, club: String): List<Player>`, `+findBySportIgnoreCase(sport: String): List<Player>` |
| ShortlistRepository | ShortlistEntry | `+findByScoutName(scoutName: String): List<ShortlistEntry>`, `+findByScoutNameAndPlayerId(scoutName: String, playerId: Long): Optional<ShortlistEntry>` |
| ComparisonRepository | Comparison | `+findByUserIdOrderByCreatedAtDesc(userId: Long): List<Comparison>` |
| ScoutingReportRepository | ScoutingReport | `+findByAuthor_IdOrderByUpdatedAtDesc(authorId: Long)`, `+findByAuthor_IdAndPlayer_IdOrderByUpdatedAtDesc(authorId: Long, playerId: Long)`, `+countByAuthor_Id(authorId: Long): long`, `+deleteByPlayerId(playerId: Long): int` |

### 4.6 package `controller`
| Class | Stereotype | Attributes (dependencies) | Operations |
|---|---|---|---|
| AuthController | «controller» | `-userService: UserService`, `-logger {static}` | `+home`, `+showLogin`, `+processLogin`, `+showSignup`, `+processSignup`, `+logout` |
| PlayerController | «controller» `/admin` | `-playerService: PlayerService`, `-SPORTS: List<String> {static}` | `-isAdmin(session)`, `+dashboard`, `+listPlayers`, `+showAddForm`, `+showEditForm`, `+savePlayer`, `+deletePlayer` |
| ScoutController | «controller» `/scout` | `-playerService`, `-analyticsService: PerformanceAnalyticsService`, `-reportService: ScoutingReportService` | `-isLoggedIn`, `-isScoutOrAdmin`, `-getScoutName`, `-populateCompareForm`, `+dashboard`, `+listPlayers`, `+playerDetail`, `+valuatePlayer`, `+compareForm`, `+compareResult`, `+viewShortlist`, `+addToShortlist`, `+removeFromShortlist` |
| ComparisonController | «controller» `/scout/comparisons` | `-comparisonService`, `-playerService`, `-analyticsService` | `-getUserId`, `-isLoggedIn`, `+saveComparison`, `+viewHistory`, `+viewSavedComparison`, `+deleteComparison` |
| ScoutingReportController | «controller» `/scout/reports` | `-NOT_FOUND: String {static}`, `-reportService`, `-playerService` | `-getUserId`, `-isScoutOrAdmin`, `-populateForm`, `+list`, `+newForm`, `+create`, `+view`, `+editForm`, `+update`, `+delete` |
| AnalyticsController | «controller» `/scout/analytics` | `-analyticsService` | `-isScoutOrAdmin`, `+analytics` |
| GlobalModelAttributes | «controllerAdvice» | — | `+currentRole(session): String`, `+currentUserName(session): String` |
| ErrorHandlerController | «controller», realises Spring `ErrorController` | `-logger {static}` | `+handleError(request: HttpServletRequest, model: Model): String` |

(`HomeController` is an empty file with no class body; do not draw it.)

### 4.7 package `config`
| Class | Stereotype | Members |
|---|---|---|
| SecurityConfig | «configuration» | `+passwordEncoder(): PasswordEncoder`, `+filterChain(http: HttpSecurity): SecurityFilterChain` |
| AdminSeeder | «component», implements `ApplicationRunner` | `-userService: UserService`; `+run(args: ApplicationArguments): void` |
| LocalDataSeeder | «component», `@Profile("local")`, implements `ApplicationRunner` | `-userService: UserService`, `-playerService: PlayerService`; `+run(args)`, `-seedPlayers(): void`, `-addPlayer(name, age, nationality, sport, position, club, stat1, stat2, stat3, stat4): void` |

### 4.8 Relationships (with multiplicities)
| From | Relationship | To | Multiplicity / label |
|---|---|---|---|
| Player | composition ◆ | PlayerStatistics | 1 — 1 "statistics" |
| ScoutingReport | association → | Player | 0..* — 1 "player" |
| ScoutingReport | association → | AppUser | 0..* — 1 "author" (FK `scout_id`) |
| ScoutingReport | association → | Recommendation | * — 1 |
| ScoutingReportForm | dependency ⇢ | ScoutingReport | «uses» (fromReport / applyTo) |
| ScoutingReportForm | association → | Recommendation | * — 1 |
| Comparison | composition ◆ | ComparisonPlayer | 1 — 1..* (always 2 saved) |
| ComparisonPlayer | association → | Player | * — 1 (by `playerId`) |
| Comparison | association → | AppUser | * — 1 owner (by `userId`) |
| ShortlistEntry | association → | Player | * — 1 (by `playerId`) |
| Admin, Scout | generalisation ─▷ | User | |
| Scout | aggregation ◇ | Player | 1 — 0..* "shortlist" |
| BasicValuationStrategy | realisation ┄▷ | ValuationStrategy | |
| PlayerService | association → | ValuationStrategy | 1 — 1 (Strategy context) |
| FootballProfile, CricketProfile, BasketballProfile, TennisProfile | generalisation ─▷ | SportProfile | |
| SportProfile | composition ◆ | StatDefinition | 1 — 4 |
| SportProfileRegistry | aggregation ◇ | SportProfile | 1 — 1..* (map by sport) |
| PerformanceAnalyticsService | association → | SportProfileRegistry, PlayerRepository | 1 — 1 |
| PerformanceAnalyticsService | dependency ⇢ «create» | SportSummary, PlayerInsight, PlayerComparison | |
| SportSummary | composition ◆ | StatSummary | 1 — 4 |
| SportSummary | composition ◆ | RankedEntry | 1 — 0..* (index leaderboard) |
| StatSummary | composition ◆ | RankedEntry | 1 — 0..5 |
| PlayerInsight | composition ◆ | StatInsight (4), DerivedMetric (1..*) | |
| PlayerComparison | composition ◆ | ComparisonRow (4), RadarChart (1) | |
| RadarChart | dependency ⇢ «create» | RadarChart.Axis | `getAxes()` creates one Axis per label (nested class) |
| StatInsight, ComparisonRow, StatSummary | association → | StatDefinition | * — 1 |
| UserService | association → | AppUserRepository, PasswordEncoder | 1 — 1 |
| PlayerService | association → | PlayerRepository, ShortlistRepository, ScoutingReportRepository | 1 — 1 each |
| ComparisonService | association → | ComparisonRepository, PlayerRepository | 1 — 1 each |
| ScoutingReportService | association → | ScoutingReportRepository, PlayerRepository, AppUserRepository | 1 — 1 each |
| Controllers | dependency ⇢ | the services in their attribute column (4.6) | |
| AdminSeeder | dependency ⇢ | UserService | |
| LocalDataSeeder | dependency ⇢ | UserService, PlayerService | |
| SecurityConfig | dependency ⇢ «create» | PasswordEncoder (BCryptPasswordEncoder) | |
| Each repository | dependency ⇢ | its entity | `JpaRepository<T, Long>` |

**Notes:** "User, Admin, Scout: demonstration hierarchy, not persisted (authentication uses AppUser, shortlist uses ShortlistEntry)." "Strategy pattern: PlayerService → ValuationStrategy ← BasicValuationStrategy."

---

## 5. Object Diagram

Snapshot **after** Scout "Ayesha Khan" writes a report on a cricket bowler and compares him with a cricket batsman. These values are illustrative and consistent with the system's rules.

**Objects (instance : Class {attribute = value})**
- `scoutA : AppUser` {id = 12, fullName = "Ayesha Khan", email = "ayesha@example.com", role = "SCOUT"}
- `scoutB : AppUser` {id = 13, fullName = "Bilal Ahmed", role = "SCOUT"}
- `admin1 : AppUser` {id = 1, role = "ADMIN"}
- `p1 : Player` {id = 21, name = "Hamza Ali", sport = "Cricket", position = "Bowler", age = 24, estimatedValue = 24,120,000}
  - ◆ `p1Stats : PlayerStatistics` {stat1 = 420, stat2 = 95, stat3 = 180, stat4 = 68} (runs 420, wickets 95, batting avg 18.0, economy 6.8)
  - Value check: 420×5,000 + 95×200,000 + 180×15,000 + (100−68)×10,000 = 24,120,000.
- `p2 : Player` {id = 22, name = "Usman Tariq", sport = "Cricket", position = "Batsman", age = 27}
  - ◆ `p2Stats : PlayerStatistics` {stat1 = 1500, stat2 = 0, stat3 = 452, stat4 = 0} (economy 0 = "Did not bowl")
- `r1 : ScoutingReport` {id = 7, potentialRating = 8, recommendation = RECOMMEND, strengths = "Accurate yorkers at the death", weaknesses = "Fitness in long spells", potential = "International white-ball option", performanceObservations = "Economy under 7 across the season", recommendationSummary = "Recommend for trial", createdAt = 2026-11-12 10:00, updatedAt = 2026-11-12 10:00}
- `RECOMMEND : Recommendation` {displayName = "Recommend", cssClass = "good"}
- `cricket : CricketProfile`
  - ◆ `econ : StatDefinition` {slot = 4, label = "Economy Rate (×10)", shortLabel = "Economy", scale = 10, higherIsBetter = false, zeroMeansMissing = true, missingLabel = "Did not bowl"}
- `registry : SportProfileRegistry`
- `cmp : PlayerComparison` {playerA = p1, playerB = p2, winsA = 2, winsB = 2, ties = 0}
  - ◆ `row4 : ComparisonRow` {definition = econ, formattedA = "6.8", formattedB = "Did not bowl", winner = "A"}
  - ◆ `radar : RadarChart`
- `c1 : Comparison` {id = 3, userId = 12, title = "Bowler vs Batsman"}
  - ◆ `cp1 : ComparisonPlayer` {playerId = 21}
  - ◆ `cp2 : ComparisonPlayer` {playerId = 22}

**Links**
- `r1 —player→ p1`
- `r1 —author→ scoutA`
- `r1 — RECOMMEND`
- `p1 ◆— p1Stats`, `p2 ◆— p2Stats`
- `registry — cricket` (key "Cricket")
- `cricket ◆— econ`
- `cmp — p1` (playerA), `cmp — p2` (playerB), `cmp — cricket` (profile)
- `cmp ◆— row4`, `cmp ◆— radar`, `row4 — econ`
- `c1 ◆— cp1`, `c1 ◆— cp2`; `cp1 ⇢ p1` (playerId 21), `cp2 ⇢ p2` (playerId 22); `c1 ⇢ scoutA` (userId 12)

**Consistency of win counts:** runs → B (1500 > 420); wickets → A (95 > 0); batting average → B (45.2 > 18.0); economy → A (B did not bowl). Result 2–2, verdict "Evenly matched across 4 statistics".

**Note:** "scoutB has no link to r1: `findOwnedReport(7, 13)` returns null."

---

## 6. Component Diagram

**Components** (UML component boxes; provided interface = lollipop ○, required interface = socket ◖):

| # | Component | Contains | Provides | Requires |
|---|---|---|---|---|
| 1 | Web Browser Client | HTML views, `style.css`, `app.js` (initThemeToggle, initNavToggle, initConfirmForms, initCompareFilter, initCounters) | — | «HTTP»; uses «localStorage» artifact `spvss-theme` |
| 2 | Presentation (Thymeleaf Views) | templates + `fragments/layout.html` (`head`, `topbar`, `themeToggle`, `alerts`), `error.html` | «HTML pages» | «Model attributes» |
| 3 | Web Controllers | AuthController, PlayerController, ScoutController, ComparisonController, ScoutingReportController, AnalyticsController, GlobalModelAttributes, ErrorHandlerController | «HTTP endpoints» | «AuthService», «PlayerService», «ComparisonService», «ReportService», «AnalyticsService» |
| 4 | User & Auth Service | UserService, SecurityConfig (PasswordEncoder, SecurityFilterChain), AdminSeeder, LocalDataSeeder (`local` profile only) | «AuthService» | «UserRepository» |
| 5 | Player Management Service | PlayerService | «PlayerService» | «ValuationStrategy», «PlayerRepository», «ShortlistRepository», «ReportRepository» |
| 6 | Valuation Engine | ValuationStrategy, BasicValuationStrategy | «ValuationStrategy» | — |
| 7 | Comparison Service | ComparisonService | «ComparisonService» | «ComparisonRepository», «PlayerRepository» |
| 8 | Scouting Reports Module «new» | ScoutingReportService, ScoutingReport, ScoutingReportForm, Recommendation | «ReportService» | «ReportRepository», «PlayerRepository», «UserRepository» |
| 9 | Performance Analytics Module «new» | PerformanceAnalyticsService, SportProfileRegistry, SportProfile + 4 profiles, StatDefinition, result value objects, RadarChart | «AnalyticsService» | «PlayerRepository» |
| 10 | Data Access (Spring Data JPA Repositories) | AppUserRepository, PlayerRepository, ShortlistRepository, ComparisonRepository, ScoutingReportRepository | «UserRepository», «PlayerRepository», «ShortlistRepository», «ComparisonRepository», «ReportRepository» | «JDBC» |
| 11 | Supabase PostgreSQL «database» (external) | tables app_users, players, shortlist, comparisons, comparison_players, scouting_reports | «JDBC/SQL» | — |

**Connections:**
- Web Browser Client ↔ Presentation / Web Controllers over «HTTP».
- Web Controllers → components 4, 5, 7, 8, 9 (through their provided interfaces).
- Web Controllers → Presentation («Model attributes» → «HTML pages»).
- Player Management Service → Valuation Engine.
- Services → Data Access → Supabase PostgreSQL.
- Performance Analytics Module → Data Access (read-only).

---

## 7. Communication Diagram

### 7.1 Create Scouting Report (same interaction as SD-1)
**Objects:** `:Scout`, `:Browser`, `c:ScoutingReportController`, `s:ScoutingReportService`, `ps:PlayerService`, `ur:AppUserRepository`, `pr:PlayerRepository`, `rr:ScoutingReportRepository`, `r:ScoutingReport`, `f:ScoutingReportForm`.

**Links and numbered messages:**
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

### 7.2 Ownership check on delete
**Objects:** `:Browser`, `c:ScoutingReportController`, `s:ScoutingReportService`, `rr:ScoutingReportRepository`, `r:ScoutingReport`.
- Browser — c: `1: POST /scout/reports/7/delete`
- c — s: `1.1: deleteReport(7, userId)`
- s (self-link): `1.1.1: findOwnedReport(7, userId)`
- s — rr: `1.1.1.1: findById(7)`
- s — r: `1.1.1.2: isOwnedBy(userId)`
- s — rr: `[owner] 1.1.2: delete(r)`
- c — Browser: `1.2: redirect + flash`

---

## 8. Deployment Diagram

**Nodes and artifacts:**
1. **Client Device** «device» (desktop, tablet or mobile)
   - **Web Browser** «execution environment» (Chrome, Edge, Firefox, Safari)
   - Artifacts: rendered HTML pages, `style.css`, `app.js`; «localStorage» `spvss-theme`.
2. **Railway Cloud** «cloud node» (production)
   - **Railway service container** «execution environment»
     - **JVM** «execution environment»
       - **Embedded Apache Tomcat** «web server» (listens on the port in `PORT`)
   - Artifact: `sports-player-valuation-scouting-1.0.0.jar` «executable jar» (Spring Boot fat jar: controllers, services, Thymeleaf templates, static resources, Hibernate/JPA, HikariCP, PostgreSQL JDBC driver), built by Railway with Maven from GitHub.
   - Configuration artifact: Railway Variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_FULL_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`; `PORT` supplied by Railway. «not committed»
   - Public endpoint: generated domain `*.up.railway.app` (HTTPS).
3. **Developer PC** «device» (development; Windows, JDK 17)
   - **JVM 17** → **Embedded Apache Tomcat** (port 8080); same jar; configuration from git-ignored `.env` / `run.ps1`-style file.
4. **GitHub** «external node»
   - Artifact: repository `M-Bilal-Rasheed/OOAD_PROJECT` (branch `main`).
5. **Supabase Cloud** «cloud node»
   - **PostgreSQL Server** «database server»
   - Artifact: schema `public` (tables app_users, players, shortlist, comparisons, comparison_players, scouting_reports).
6. **Google Fonts CDN** «external node» (optional): Inter font files.

**Communication paths:**
- Web Browser ↔ Railway (Tomcat): «HTTPS» to the public domain, forwarded to `PORT`. (Development: «HTTP» `localhost:8080`.)
- JVM ↔ PostgreSQL Server: «JDBC over TCP/SSL», `sslmode=require`, through the Supabase connection pooler.
- GitHub → Railway Cloud: «git / build» (Railway pulls the repository and builds the jar).
- Developer PC → GitHub: «git push».
- Web Browser ↔ Google Fonts CDN: «HTTPS».

**Note:** "All database access is server-side; the browser never connects to the database."
