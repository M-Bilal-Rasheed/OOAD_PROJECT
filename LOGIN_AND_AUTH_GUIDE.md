# Login & Authentication Guide

## Overview
The Sports Player Valuation & Scouting System now has complete authentication support for both **Admin** and **Scout** users.

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.9.6+ (included in `maven-dist/`)
- Supabase PostgreSQL account with credentials
- PowerShell (Windows)

### Quick Start

1. **Set up environment in `run.ps1`:**
   ```powershell
   # Edit G:\Desktop\ooad\run.ps1 and set:
   $env:DB_PASSWORD = "your_supabase_password_here"
   
   # Uncomment and set these for admin seeding:
   $env:ADMIN_FULL_NAME = "System Admin"
   $env:ADMIN_EMAIL     = "admin@example.com"
   $env:ADMIN_PASSWORD  = "admin123"
   ```

2. **Run the application:**
   ```powershell
   cd G:\Desktop\ooad
   Set-ExecutionPolicy -Scope Process Bypass
   .\run.ps1
   ```

3. **Open browser:**
   - Navigate to: `http://localhost:8080`
   - You'll be redirected to `/login`

## Testing Credentials

### Admin Account (created on first startup)
- **Email:** `admin@example.com`
- **Password:** `admin123`
- **Role:** ADMIN
- **Access:** Player management, admin dashboard

### Creating a Scout Account
1. Go to login page: `http://localhost:8080/login`
2. Click "Create a Scout account"
3. Fill in signup form:
   - Full Name: any name
   - Email: your email
   - Password: minimum 6 characters
   - Confirm Password: must match

## Login Flow

1. **Access login page:** `http://localhost:8080/login`
2. **Enter credentials:**
   - Email: (admin@example.com or scout email)
   - Password: (correct password)
3. **Submit form**
4. **Expected redirect:**
   - ADMIN users → `/admin/dashboard` (player management)
   - SCOUT users → `/scout/dashboard` (player search, comparison, shortlist)

## Authentication Details

### Implementation
- **Password Hashing:** BCrypt (Spring Security)
- **Session Management:** HttpSession + Spring Security
- **CSRF Protection:** Enabled on all POST forms
- **Database:** Supabase PostgreSQL (table: `app_users`)

### Key Features
- ✅ Secure password hashing (BCrypt)
- ✅ Email-based login
- ✅ Role-based access control (ADMIN/SCOUT)
- ✅ Session protection
- ✅ Logout functionality
- ✅ Duplicate email prevention
- ✅ Server-side validation

### Database Tables
```
app_users:
  - id (BIGINT, Primary Key)
  - full_name (VARCHAR 255)
  - email (VARCHAR 255, Unique)
  - password_hash (VARCHAR 255, BCrypt)
  - role (VARCHAR 10, ADMIN/SCOUT)
  - created_at (TIMESTAMP)
```

## URL Protection

### Public Pages
- `GET /login` - Login page
- `GET /signup` - Signup page
- `POST /login` - Login form submission
- `POST /signup` - Signup form submission
- `GET /logout` - Logout (clears session)
- `/css/**`, `/js/**`, `/images/**` - Static assets

### Protected Pages (require authentication)
- `/admin/**` - All admin pages (only ADMIN role)
- `/scout/**` - All scout pages (SCOUT or ADMIN role)
- `/player/**` - Player endpoints

## Features

### Admin Dashboard
- View all players
- Add new players
- Edit player details
- Delete players
- View player valuations

### Scout Dashboard
- Browse all players
- Search players by name/stats
- Compare players (2+ players)
- Save comparisons
- Manage shortlist
- View comparison history

### Common
- Change password (TODO - future feature)
- View account info
- Logout

## Troubleshooting

### Login fails with "Invalid email or password"
1. Check email is correct (case-insensitive)
2. Verify password is exact match
3. Ensure account exists (check admin seeding worked)
4. Check application logs for [LOGIN] messages

### Port 8080 already in use
```powershell
# Find and kill process using port 8080
netstat -ano | findstr ":8080"
taskkill /PID <PID> /F
```

### Database connection error
1. Verify Supabase credentials in `run.ps1`
2. Check internet connection to Supabase
3. Verify PostgreSQL database is accessible
4. Check firewall/network settings

### Signup fails with "email already exists"
- Email is already registered
- Use different email or reset account

## Application Logs

Important log messages:
```
[ADMIN_SEED] Admin account created: admin@example.com
[ADMIN_SEED] Admin already exists, skipping seed
[LOGIN] Attempting login for: user@example.com
[LOGIN] User found - Email: user@example.com, Role: ADMIN
[LOGIN] Password verification result: true
[LOGIN] ✓ Successful login for: user@example.com
[SIGNUP] Scout account created: scout@example.com
```

## Security Notes

✅ **Passwords are BCrypt hashed** - Never stored in plaintext
✅ **CSRF protection** - All POST forms protected
✅ **Session-based** - Credentials stored server-side only
✅ **Email validated** - Duplicate emails prevented
✅ **Input validation** - Server-side checks on all inputs
✅ **No console logging** - Passwords never logged

⚠️ **Do NOT:**
- Share `run.ps1` after entering database password
- Use the same password for Supabase and local testing
- Commit `.env` or credentials to Git
- Enable RLS on `app_users` table (this is server-side authentication)

## Files Modified/Created

### New Files
- `src/main/java/com/fsms/model/AppUser.java` - User entity
- `src/main/java/com/fsms/repository/AppUserRepository.java` - User repository
- `src/main/java/com/fsms/service/UserService.java` - User business logic
- `src/main/java/com/fsms/controller/AuthController.java` - Login/signup endpoints
- `src/main/java/com/fsms/config/SecurityConfig.java` - Spring Security config
- `src/main/java/com/fsms/config/AdminSeeder.java` - Admin account initialization
- `src/main/resources/templates/auth/login.html` - Login page
- `src/main/resources/templates/auth/signup.html` - Signup page
- `src/main/resources/db/migration/V2__create_authentication_tables.sql` - Database migration

### Modified Files
- `pom.xml` - Added Spring Security dependency
- `application.properties` - Database configuration

## Next Steps

### Future Enhancements
- [ ] Email verification on signup
- [ ] Password reset functionality
- [ ] Two-factor authentication
- [ ] Session timeout management
- [ ] Account deactivation
- [ ] Admin user management panel
- [ ] Login attempt tracking
- [ ] User profile editing

## Support

For issues or questions:
1. Check the application logs for [LOGIN] or [SIGNUP] messages
2. Verify database connection
3. Ensure credentials are correct
4. Verify `app_users` table exists in Supabase
5. Check that port 8080 is available

---

**Last Updated:** 2026-10-23
**Version:** 1.0.0
