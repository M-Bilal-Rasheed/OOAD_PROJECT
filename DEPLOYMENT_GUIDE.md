# Deployment Guide - Sports Player Valuation & Scouting System

**Application**: Java Spring Boot 3.2.5  
**Database**: PostgreSQL (Supabase)  
**Build Tool**: Maven  
**Target**: Production-ready deployment  

---

## Table of Contents
1. [Pre-Deployment Checklist](#pre-deployment-checklist)
2. [Deployment Options](#deployment-options)
3. [Docker Containerization](#docker-containerization)
4. [Deploy to Heroku](#deploy-to-heroku)
5. [Deploy to Railway](#deploy-to-railway)
6. [Deploy to AWS](#deploy-to-aws)
7. [Environment Configuration](#environment-configuration)
8. [Database Backup & Migration](#database-backup--migration)
9. [Security Best Practices](#security-best-practices)
10. [Monitoring & Logging](#monitoring--logging)

---

## Pre-Deployment Checklist

- [ ] All unit tests passing: `mvn test`
- [ ] Application builds successfully: `mvn clean package`
- [ ] Database schema validated
- [ ] Environment variables documented
- [ ] Admin account seeded via env vars
- [ ] SSL/HTTPS configured for production
- [ ] Security config reviewed in `SecurityConfig.java`
- [ ] No hardcoded passwords in code
- [ ] Logging level set appropriately (WARN for production)
- [ ] Database backups configured

---

## Deployment Options

### Option 1: Heroku (Easiest - Free tier available)
**Pros**: Simple Git-based deployment, free PostgreSQL  
**Cons**: Sleeps after 30 mins of inactivity (free tier)  
**Time**: ~15 minutes

### Option 2: Railway (Modern, Fast)
**Pros**: Pay-as-you-go, good free tier, easy UI  
**Cons**: Requires credit card  
**Time**: ~10 minutes

### Option 3: AWS (Professional, Scalable)
**Pros**: Production-grade, EC2/RDS/Elastic Beanstalk options  
**Cons**: More complex, cost management needed  
**Time**: ~30 minutes

### Option 4: Docker + Any Cloud Provider
**Pros**: Most control, portable  
**Cons**: Manual management  
**Time**: ~20 minutes

---

## Docker Containerization

### Step 1: Create Dockerfile

Create file `Dockerfile` in project root:

```dockerfile
# Multi-stage build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/sports-player-valuation-scouting-1.0.0.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
```

### Step 2: Create .dockerignore

```
.git
.gitignore
.mvn
target
*.md
run.ps1
.env
```

### Step 3: Create docker-compose.yml (for local testing)

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://db:5432/scouting
      DB_USERNAME: postgres
      DB_PASSWORD: your_secure_password
      ADMIN_FULL_NAME: Admin User
      ADMIN_EMAIL: admin@example.com
      ADMIN_PASSWORD: admin123
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: scouting
      POSTGRES_PASSWORD: your_secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

### Step 4: Build & Test Docker Image

```powershell
# Build image
docker build -t sports-scouting:1.0.0 .

# Run container
docker run -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/scouting" `
  -e DB_USERNAME="postgres" `
  -e DB_PASSWORD="your_password" `
  sports-scouting:1.0.0

# Or use docker-compose
docker-compose up -d
```

---

## Deploy to Heroku

### Prerequisites
- Heroku account (free): https://www.heroku.com
- Heroku CLI installed: https://devcenter.heroku.com/articles/heroku-cli
- Git repository initialized

### Step 1: Create Heroku App

```powershell
heroku login
heroku create sports-scouting-app
```

### Step 2: Add PostgreSQL Database

```powershell
heroku addons:create heroku-postgresql:hobby-dev -a sports-scouting-app
```

### Step 3: Configure Environment Variables

```powershell
heroku config:set ADMIN_FULL_NAME="System Admin" -a sports-scouting-app
heroku config:set ADMIN_EMAIL="admin@scouting.com" -a sports-scouting-app
heroku config:set ADMIN_PASSWORD="SecurePassword123!" -a sports-scouting-app

# Heroku automatically provides DATABASE_URL
# We need to convert it for our app:
# DATABASE_URL format: postgres://user:password@host:port/dbname
# Convert to: jdbc:postgresql://host:port/dbname
```

### Step 4: Create Procfile

Create `Procfile` in project root:

```
web: java -Dspring.profiles.active=heroku -Dserver.port=$PORT $JAVA_OPTS -jar target/sports-player-valuation-scouting-1.0.0.jar
```

### Step 5: Create application-heroku.properties

Create `src/main/resources/application-heroku.properties`:

```properties
# Heroku PostgreSQL
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.root=INFO
logging.level.com.fsms=INFO

# Server
server.port=${PORT:8080}
server.servlet.session.timeout=30m
```

### Step 6: Deploy

```powershell
git add .
git commit -m "Prepare for Heroku deployment"
git push heroku main

# View logs
heroku logs --tail -a sports-scouting-app

# Open app
heroku open -a sports-scouting-app
```

### Step 7: Verify Deployment

```powershell
# Check app status
heroku ps -a sports-scouting-app

# View database
heroku pg:info -a sports-scouting-app

# SSH into dyno
heroku ps:exec -a sports-scouting-app
```

---

## Deploy to Railway

### Prerequisites
- Railway account: https://railway.app
- GitHub repository connected

### Step 1: Connect Repository

1. Go to https://railway.app/dashboard
2. Click "New Project" → "Deploy from GitHub"
3. Select your repository
4. Railway auto-detects Maven project

### Step 2: Add PostgreSQL Database

1. Click "Add Service" → "PostgreSQL"
2. Railway creates database automatically

### Step 3: Configure Environment Variables

In Railway Dashboard:

```
ADMIN_FULL_NAME=System Admin
ADMIN_EMAIL=admin@scouting.com
ADMIN_PASSWORD=SecurePassword123!

# These are auto-provided:
# DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, DATABASE_HOST, DATABASE_PORT
```

### Step 4: Convert DATABASE_URL for Spring

In `application-railway.properties`:

```properties
# Extract from DATABASE_URL: postgres://user:password@host:port/dbname
spring.datasource.url=jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${RAILWAY_PRIVATE_DOMAIN}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

# Build command
maven.build.goal=clean package
maven.build.phases=clean package
```

### Step 5: Deploy

1. Click "Deploy"
2. Railway automatically builds with Maven
3. App starts and connects to PostgreSQL
4. View live logs in dashboard

---

## Deploy to AWS

### Option A: Elastic Beanstalk (Recommended)

#### Step 1: Prepare Application

```powershell
# Build JAR
mvn clean package -DskipTests

# Create .elasticbeanstalk/config.yml
mkdir .elasticbeanstalk
```

Create `.elasticbeanstalk/config.yml`:

```yaml
branch-defaults:
  main:
    environment: sports-scouting-env

environment-defaults:
  sports-scouting-env:
    branch: null
    repository: null

global:
  application_name: sports-scouting
  default_platform: Java 17 running on 64bit Amazon Linux 2
  default_region: us-east-1
  include_git_submodules: true
  instance_profile: null
  platform_name: null
  platform_version: null
  sc: git
  workspace_type: Application
```

#### Step 2: Create .ebextensions

Create `.ebextensions/java.config`:

```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: "production"
    ADMIN_FULL_NAME: "System Admin"
    ADMIN_EMAIL: "admin@scouting.com"

  aws:elasticbeanstalk:cloudwatch:logs:
    StreamLogs: true
    DeleteOnTerminate: false
    RetentionInDays: 7
```

#### Step 3: Deploy with EB CLI

```powershell
# Install AWS EB CLI
pip install awsebcli

# Initialize
eb init -p "Java 17 running on 64bit Amazon Linux 2" sports-scouting --region us-east-1

# Create environment with RDS PostgreSQL
eb create sports-scouting-env `
  --instance-type t3.micro `
  --database `
  --db.engine postgres `
  --db.size db.t3.micro

# Deploy
eb deploy

# Open application
eb open
```

### Option B: EC2 + RDS (More Control)

#### Step 1: Create RDS PostgreSQL Instance

```powershell
# Via AWS Console:
1. Services → RDS → Create Database
2. Engine: PostgreSQL 15
3. Template: Free tier
4. DB Instance: db.t3.micro
5. Multi-AZ: No (for dev/test)
6. Storage: 20 GB
7. Security group: Allow port 5432
```

#### Step 2: Launch EC2 Instance

```powershell
# Via AWS Console:
1. EC2 → Launch Instance
2. AMI: Ubuntu 22.04 LTS
3. Type: t3.micro (free tier)
4. Security Group: Allow ports 22, 8080
5. Launch
```

#### Step 3: SSH into Instance & Deploy

```bash
# Connect to EC2
ssh -i your-key.pem ubuntu@your-instance-ip

# Install Java
sudo apt update
sudo apt install openjdk-17-jre-headless -y

# Copy JAR file (use SCP or S3)
scp -i your-key.pem target/sports-player-valuation-scouting-1.0.0.jar ubuntu@your-instance-ip:/home/ubuntu/

# Run application
export DB_URL="jdbc:postgresql://your-rds-endpoint:5432/scouting"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-secure-password"
java -jar sports-player-valuation-scouting-1.0.0.jar &

# Application running on http://your-instance-ip:8080
```

---

## Environment Configuration

### Production Environment Variables

Create `.env.production`:

```
# Database (Supabase, Heroku, or RDS)
DB_URL=jdbc:postgresql://your-host:5432/dbname
DB_USERNAME=postgres
DB_PASSWORD=your-very-secure-password-here

# Admin Account (set once, disable after)
ADMIN_FULL_NAME=System Administrator
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_PASSWORD=GenerateSecurePassword123!

# Application
SPRING_APPLICATION_NAME=Sports Scouting System
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_FSMS=INFO

# Security
SERVER_SSL_ENABLED=true
SERVER_SSL_KEY_STORE=/path/to/keystore.p12
SERVER_SSL_KEY_STORE_PASSWORD=keystorepassword
```

### Load Environment Variables

```powershell
# PowerShell
$env:DB_URL = "jdbc:postgresql://your-host:5432/dbname"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "password"

# Bash
export DB_URL="jdbc:postgresql://your-host:5432/dbname"
export DB_USERNAME="postgres"
export DB_PASSWORD="password"
```

---

## Database Backup & Migration

### Backup from Supabase

```powershell
# Via Supabase CLI
supabase db push  # Backup to local

# Via pg_dump
$env:PGPASSWORD = "your-password"
pg_dump -U postgres -h your-host -d dbname > backup.sql
```

### Restore Database

```powershell
$env:PGPASSWORD = "your-password"
psql -U postgres -h your-host -d dbname < backup.sql
```

### Migrate to Different Database

1. Export schema and data from old database
2. Update `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
3. Set `spring.jpa.hibernate.ddl-auto=update`
4. Restart application
5. Verify data integrity

---

## Security Best Practices

### Before Production Deployment

- [ ] **Never commit secrets**: Remove `.env` from git, use `.env.example`
- [ ] **Use HTTPS/SSL**: Enable SSL certificates (Let's Encrypt is free)
- [ ] **Database Encryption**: Enable at-rest encryption on RDS/Supabase
- [ ] **Connection Pooling**: HikariCP already configured (max 10 connections)
- [ ] **SQL Injection Prevention**: Using parameterized queries (JPA handles this)
- [ ] **CSRF Protection**: Spring Security enabled
- [ ] **Password Hashing**: BCrypt configured in `SecurityConfig.java`
- [ ] **Logging**: Don't log passwords or sensitive data

### SSL/HTTPS Configuration

Create `application-production.properties`:

```properties
server.ssl.enabled=true
server.ssl.protocol=TLS
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

# Redirect HTTP to HTTPS
server.http2.enabled=true
```

---

## Monitoring & Logging

### Application Logging

Current configuration logs to console. For production:

```properties
# File logging
logging.file.name=/var/log/scouting/application.log
logging.file.max-size=10MB
logging.file.max-history=10

# Log levels
logging.level.root=WARN
logging.level.com.fsms=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=INFO
```

### Database Connection Monitoring

```properties
# HikariCP connection pool monitoring
logging.level.com.zaxxer.hikari=DEBUG
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

### Health Check Endpoint

Spring Boot Actuator (if enabled):

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

Access: `http://your-app/actuator/health`

---

## Troubleshooting Deployment

### Issue: Database Connection Timeout

**Solution**:
```properties
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.initialization-fail-timeout=30000
```

### Issue: Out of Memory

**Solution**:
```bash
java -Xmx512m -Xms256m -jar app.jar
```

### Issue: Port Already in Use

**Solution**:
```properties
server.port=8080
# Or in Heroku: uses $PORT automatically
```

### Issue: Database Migrations Failed

**Solution**:
```properties
# First run only:
spring.jpa.hibernate.ddl-auto=create

# Subsequent runs:
spring.jpa.hibernate.ddl-auto=update
```

---

## Deployment Checklist by Platform

### Heroku Checklist
- [ ] `Procfile` created
- [ ] `application-heroku.properties` created
- [ ] PostgreSQL addon added
- [ ] Environment variables set
- [ ] `git push heroku main` works
- [ ] Logs show "Started in X seconds"

### Railway Checklist
- [ ] Repository connected
- [ ] PostgreSQL added
- [ ] Environment variables configured
- [ ] Build succeeds
- [ ] App starts and connects to database

### AWS EB Checklist
- [ ] `.ebextensions` created
- [ ] RDS database provisioned
- [ ] IAM roles configured
- [ ] Security groups allow traffic
- [ ] `eb create` succeeds
- [ ] Application accessible via URL

### AWS EC2 + RDS Checklist
- [ ] EC2 instance running
- [ ] Java installed on instance
- [ ] RDS instance provisioned
- [ ] Security groups configured
- [ ] JAR file copied to instance
- [ ] Environment variables set
- [ ] Application running on port 8080

---

## Recommended Production Setup

**For University Project (Low Cost)**:
```
Railway or Heroku Free Tier
├── Application: Java Spring Boot (auto-deployed)
├── Database: PostgreSQL (included)
├── SSL: Auto-generated
└── Cost: Free to ~$7/month
```

**For Professional Deployment**:
```
AWS (Scalable)
├── Elastic Beanstalk (Application)
├── RDS Multi-AZ (Database)
├── CloudFront (CDN)
├── Route 53 (DNS)
├── CloudWatch (Monitoring)
└── Cost: $50-200/month
```

---

## Final Steps

1. **Choose a Platform** (Heroku recommended for simplicity)
2. **Build JAR**: `mvn clean package`
3. **Create Configuration Files** (Dockerfile, Procfile, properties)
4. **Set Environment Variables** (secrets never in git)
5. **Deploy** (git push or platform-specific command)
6. **Test** (signup, add player, verify database)
7. **Monitor** (logs, performance, errors)
8. **Backup** (regular database backups)

---

**Deployment Status**: Ready for Production ✅

**Support**: For Heroku issues → https://devcenter.heroku.com  
**Support**: For Railway issues → https://docs.railway.app  
**Support**: For AWS issues → https://docs.aws.amazon.com  

---

**Last Updated**: November 13, 2026  
**Java Version**: 17  
**Spring Boot**: 3.2.5  
**Database**: PostgreSQL 15+
