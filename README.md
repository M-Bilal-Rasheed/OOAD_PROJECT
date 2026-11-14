# Sports Player Valuation & Scouting System

**Complete Java Spring Boot Application - Production Ready**

---

## 🎯 Project Status

✅ **FULLY WORKING AND TESTED**

- Application running on `http://localhost:8080` (local) and deployed live on Railway
- Database connected to Supabase PostgreSQL
- All frontend-to-database connections verified
- Ready for deployment or teacher demonstration

---

## 📋 Quick Summary

| Aspect | Details |
|--------|---------|
| **Language** | 100% Java (Spring Boot 3.2.5) |
| **Framework** | Spring MVC + Spring Data JPA + Thymeleaf |
| **Database** | PostgreSQL (Supabase) |
| **Build Tool** | Maven |
| **Testing** | 18 JUnit 5 + Mockito unit tests (`mvn test`) |
| **Deployment** | Railway (live), Supabase PostgreSQL |
| **OOAD Concepts** | Encapsulation, Inheritance, Polymorphism, Aggregation, Composition |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ installed
- Maven configured
- Internet connection (for Supabase)

### Start Application

```powershell
cd OOAD_PROJECT
$env:DB_URL = "jdbc:postgresql://<supabase-pooler-host>:5432/postgres?sslmode=require"
$env:DB_USERNAME = "<username>"
$env:DB_PASSWORD = "<password>"
mvn spring-boot:run
```

Optional: set `ADMIN_FULL_NAME`, `ADMIN_EMAIL` and `ADMIN_PASSWORD` to create the first Admin account on startup. See `.env.example`.

**URL**: http://localhost:8080

---

## 📚 Documentation Files

Read these files in order:

1. **report.md** ← Start here!
   - Complete technical documentation
   - Features, routes, classes, database design
   - Algorithms, testing and test cases
   - Setup, run and Railway deployment guide

2. **diagrams.md**
   - Specifications for all 8 UML diagrams
   - Activity, Use Case, Sequence, Class
   - Object, Component, Communication, Deployment
   - Exact class, method and route names

3. **DEPLOYMENT_GUIDE.md**
   - How to deploy to production
   - Railway
   - AWS
   - Docker containerization

---

## 🎓 OOAD Concepts Demonstrated

### 1. Encapsulation
```java
public class Player {
    private Long id;           // Private fields
    private String name;
    private PlayerStatistics statistics;
    
    public Long getId() { return id; }      // Public getters
    public void setId(Long id) { this.id = id; }  // Public setters
}
```

### 2. Inheritance
```java
public class User {
    private String username;
    private String role;
    public String getRole() { return role; }
}

public class Scout extends User {    // Inherits from User
    @Override
    public String getRole() { return "SCOUT"; }
}

public class Admin extends User {     // Inherits from User
    @Override
    public String getRole() { return "ADMIN"; }
}
```

### 3. Polymorphism
```java
// Interface
public interface ValuationStrategy {
    long calculateValue(Player player);
}

// Implementation
public class BasicValuationStrategy implements ValuationStrategy {
    @Override
    public long calculateValue(Player player) { /* ... */ }
}

// Usage (polymorphic) — in PlayerService, strategy injected by Spring
private final ValuationStrategy valuationStrategy;
long value = valuationStrategy.calculateValue(player);  // Can be any implementation
```

### 4. Composition
```java
@Entity
public class Player {
    @Embedded  // Composition - PlayerStats owned by Player
    private PlayerStatistics statistics;  // If Player deleted → Stats deleted
}
```

### 5. Aggregation
```java
public class Scout {
    private List<Player> shortlist;  // Aggregation - Scout holds list
    
    public void addToShortlist(Player player) {
        shortlist.add(player);  // Player exists independently
    }
    // If Scout deleted → shortlist deleted, but Players persist in DB
}
```

### 6. MVC Pattern
```
Model (Entity)           → @Entity classes (Player.java)
View (Presentation)      → Thymeleaf templates (HTML)
Controller (Request)     → @Controller classes (PlayerController.java)
Service (Business Logic) → @Service classes (PlayerService.java)
Repository (Data Access) → @Repository interfaces (PlayerRepository.java)
```

---

## 🗄️ Database Schema

### Tables Auto-Created by Hibernate

**app_users** - User accounts
```
id (PK) | full_name | email (UNIQUE) | password_hash (BCrypt) | role | created_at
```

**players** - Player profiles with embedded stats
```
id (PK) | name | age | nationality | sport | position | club | estimated_value | 
stat1 | stat2 | stat3 | stat4
```

**shortlist** - Scout shortlist entries (Aggregation)
```
id (PK) | scout_name | player_id
```

**scouting_reports** - Scout assessments
```
id (PK) | player_id (FK) | scout_id (FK) | strengths | weaknesses | potential | 
potential_rating | performance_observations | recommendation | recommendation_summary | 
created_at | updated_at
```

**comparisons** - Saved player comparisons
```
id (PK) | user_id (FK) | title | created_at
```

**comparison_players** - Players in each comparison (Composition)
```
id (PK) | comparison_id (FK) | player_id (FK)
```

---

## 🔄 Frontend-to-Database Data Flow

### Example: Adding a Player

```
1. User fills form → HTML form submission (POST)
                    ↓
2. Controller receives → @PostMapping("/admin/players/save")
                        PlayerController.savePlayer()
                    ↓
3. Service processes → playerService.addPlayer(player)
                       Validation, business logic
                    ↓
4. Repository saves → playerRepository.save(player)
                      Spring Data JPA generates SQL
                    ↓
5. Database stores → INSERT INTO players VALUES (...)
                     Hibernate persists to PostgreSQL
                    ↓
6. Response sent → Redirect to player list
                   ✅ New player visible immediately
```

---

## 🧪 Testing Connectivity

To verify frontend-to-database connection:

### Quick Test (2 minutes)
1. Go to http://localhost:8080/admin/dashboard
2. Add a player
3. Go to Supabase → SQL Editor
4. Run: `SELECT * FROM players ORDER BY id DESC LIMIT 1;`
5. ✅ Your player appears

### Complete Test Suite
Run `mvn test` for the 18 unit tests. See **report.md** (§17) for all unit, end-to-end and manual test cases.

---

## 📊 Feature Overview

### Admin Features
- ✅ Add/Edit/Delete Players
- ✅ View Dashboard (total players, count by sport)
- ✅ Seed the first Admin account (via environment variables)
- ✅ Manage player statistics for 4 sports

### Scout Features
- ✅ Browse and search players
- ✅ Build personal shortlist (persistent)
- ✅ Write detailed scouting reports
- ✅ Calculate player valuations
- ✅ Compare players (same sport only)
- ✅ Save comparisons for later review
- ✅ View sport performance analytics and player insights
- ✅ Dark/light theme (remembered across pages)

### Authentication
- ✅ User registration (creates SCOUT account)
- ✅ User login (password BCrypt-hashed)
- ✅ Admin account seeding (environment variables)
- ✅ Role-based access control
- ✅ Session management

### Data Persistence
- ✅ All data saved to PostgreSQL database
- ✅ Auto-timestamping (created_at, updated_at)
- ✅ Cascading deletes (player deletion → report deletion)
- ✅ Search and filtering
- ✅ Relationships (FK, one-to-many, many-to-many)

---

## 🛠️ Technology Stack

```
Frontend:
  - HTML 5
  - CSS 3 (with modern layout)
  - Thymeleaf template engine
  - Vanilla JavaScript (theme, menu, confirm prompts)
  - Client-side validation (HTML5)

Backend:
  - Java 17
  - Spring Boot 3.2.5
  - Spring MVC
  - Spring Data JPA
  - Spring Security (BCrypt)

Database:
  - PostgreSQL 15+ (via Supabase)
  - H2 in-memory (dependency for the "local" demo profile)
  - Hibernate ORM 6.4.4 (DDL auto-update)
  - HikariCP (connection pooling)

Build & Deploy:
  - Maven 3.9.6
  - JUnit 5 + Mockito (unit tests)
  - Railway (deployment)
```

---

## 🚢 Deployment Options

### Current Deployment
**Railway** → 10 minutes
- New Project → Deploy from GitHub repo
- Add Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (+ optional `ADMIN_*`), without quotes
- Railway sets `PORT`; the app reads it via `server.port=${PORT:8080}`
- Settings → Networking → Generate Domain for the public URL
- Auto-deploys on push

### Professional & Scalable
**AWS** → 30 minutes
- Elastic Beanstalk or EC2 + RDS

See **DEPLOYMENT_GUIDE.md** for detailed instructions on all platforms.

---

## 🔐 Security Features

- ✅ Password hashing with BCrypt (not plaintext)
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ Role-based access control (session checks in every controller)
- ✅ Owner-only access to scouting reports and saved comparisons
- ✅ Session management
- ✅ HTTPS on the Railway domain
- ✅ Environment variables for secrets (not hardcoded)

---

## 📁 Project Structure

```
OOAD_PROJECT/
├── src/main/java/com/fsms/
│   ├── analytics/              ← Performance analytics
│   │   ├── PlayerComparison.java
│   │   ├── SportProfile.java
│   │   └── ... (14 more analytics classes)
│   ├── config/                 ← Application setup
│   │   ├── SecurityConfig.java
│   │   ├── AdminSeeder.java
│   │   └── LocalDataSeeder.java
│   ├── controller/             ← MVC Controllers
│   │   ├── PlayerController.java
│   │   ├── ScoutController.java
│   │   ├── AuthController.java
│   │   └── ... (6 more controllers)
│   ├── model/                  ← JPA Entities
│   │   ├── Player.java
│   │   ├── PlayerStatistics.java
│   │   ├── AppUser.java
│   │   ├── ScoutingReport.java
│   │   └── ... (8 more entities)
│   ├── repository/             ← Data access
│   │   ├── PlayerRepository.java
│   │   ├── AppUserRepository.java
│   │   └── ... (3 more repositories)
│   ├── service/                ← Business logic
│   │   ├── PlayerService.java
│   │   ├── UserService.java
│   │   ├── ScoutingReportService.java
│   │   └── ... (2 more services)
│   ├── valuation/              ← Strategy Pattern
│   │   ├── ValuationStrategy.java
│   │   └── BasicValuationStrategy.java
│   └── FsmsApplication.java
├── src/main/resources/
│   ├── templates/              ← Thymeleaf HTML
│   │   ├── admin/              ← Admin pages
│   │   ├── scout/              ← Scout pages
│   │   ├── auth/               ← Login/Signup
│   │   └── fragments/          ← Reusable components
│   ├── static/                 ← CSS, JS, images
│   ├── db/migration/           ← Database migrations
│   └── application.properties  ← App config (env vars, port, logging)
├── src/test/java/com/fsms/service/  ← Unit tests (18)
├── pom.xml                     ← Maven dependencies
├── .env.example                ← Environment variable template
├── report.md                   ← Full technical documentation
├── diagrams.md                 ← 8 UML diagram specifications
├── DEPLOYMENT_GUIDE.md         ← Deployment guide
└── README.md                   ← This file
```

---

## 🎯 Key Files for Understanding Connectivity

| File | Purpose | Key Concept |
|------|---------|-------------|
| `Player.java` | Entity mapped to `players` table | @Entity, @Embedded |
| `PlayerRepository.java` | Database queries auto-generated | JpaRepository, @Query |
| `PlayerService.java` | Business logic layer | Repository.save() |
| `PlayerController.java` | Routes requests to service | @PostMapping, @Valid |
| `player-form.html` | HTML form submission | th:object, th:field |
| `application.properties` | Database connection config | spring.datasource.* |
| `SecurityConfig.java` | Password hashing & security filter chain | BCrypt, @EnableWebSecurity |

---

## ❓ FAQ

**Q: How do I start the application?**  
A: Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, then run `mvn spring-boot:run`. See report.md (§22)

**Q: Where is my data stored?**  
A: PostgreSQL database on Supabase. View at https://supabase.com → SQL Editor

**Q: If I restart the app, will data persist?**  
A: Yes! Data is in database, not in application memory.

**Q: How do I deploy to production?**  
A: It is deployed on Railway. See the Deployment Options section above and DEPLOYMENT_GUIDE.md.

**Q: Is this secure for production?**  
A: Passwords are hashed with BCrypt, SQL injection is prevented and Railway serves HTTPS. CSRF protection is disabled and access checks are done per controller (see report.md §21).

**Q: Can I use this locally without internet?**  
A: Not as-is. A `local` profile with H2 and demo data (`LocalDataSeeder`) exists, but the repository has no `application-local.properties`, so the app needs the Supabase database.

**Q: How do I show this to my teacher?**  
A: Start app, create account, add player, show it in database. See report.md

---

## 🔗 Important Links

- **Application**: http://localhost:8080
- **Supabase Dashboard**: https://supabase.com
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **JPA Documentation**: https://docs.jboss.org/hibernate/orm/6.4/
- **Thymeleaf Docs**: https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html
- **Maven Central**: https://mvnrepository.com/

---

## 📞 Support

For specific topics, see:

- **Getting Started** → report.md (§22)
- **Database Design** → report.md (§8)
- **Testing & Verification** → report.md (§17)
- **Production Deployment** → DEPLOYMENT_GUIDE.md
- **OOAD Implementation** → report.md (§13–§16), diagrams.md

---

## ✅ Verification Checklist

- ✅ Application starts without errors
- ✅ Can create user accounts (signup)
- ✅ Can login with credentials
- ✅ Admin can add/edit/delete players
- ✅ Scout can browse players
- ✅ Scout can add to shortlist
- ✅ Scout can write reports
- ✅ Shortlist entries appear in database
- ✅ Reports appear in database
- ✅ Player deletion cascades to reports
- ✅ All entities properly mapped to tables
- ✅ Relationships (FK, JOINs) working
- ✅ Timestamps auto-managed
- ✅ Role-based access working
- ✅ Search and filtering functional

---

## 🎓 For Your Teacher/Viva

Key points to mention:

1. **Database Connectivity**: "Frontend forms submit to Spring Boot controllers which save to PostgreSQL via Spring Data JPA"

2. **OOP Concepts**: "Implemented encapsulation (private fields with getters), inheritance (User/Admin/Scout hierarchy), polymorphism (ValuationStrategy), aggregation (Scout shortlist), and composition (Player statistics)"

3. **Architecture**: "MVC pattern with clear separation - Controllers handle requests, Services contain business logic, Repositories handle database access"

4. **Data Persistence**: "All data saved to Supabase PostgreSQL database, survives application restart, relationships maintained via foreign keys"

5. **Security**: "Passwords hashed with BCrypt, SQL injection prevented via JPA parameterized queries, role-based access control"

---

## 📝 License & Attribution

**Created**: November 13, 2026  
**Framework**: Spring Boot 3.2.5  
**Language**: Java 17  
**Database**: PostgreSQL (Supabase)  
**Status**: Production Ready ✅

---

## 🚀 Next Steps

1. **Read**: report.md
2. **Test**: Run `mvn test` and follow report.md (§17)
3. **Demo**: Show admin adding player, verify in database
4. **Diagrams**: Draw the 8 UML diagrams from diagrams.md

---

**Application is ready for demonstration, testing, and deployment! ✅**

For detailed guidance on any topic, refer to the documentation files listed above.
