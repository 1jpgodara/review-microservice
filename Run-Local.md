# Local Setup and Run Guide: Review Microservice

This guide provides step-by-step instructions to set up and run the Review Microservice project locally, including all dependencies, testing, and troubleshooting procedures.

## Prerequisites

### 1. Install Required Software

**Java 17:**
```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Add Homebrew to PATH
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
eval "$(/opt/homebrew/bin/brew shellenv)"

# Install Java 17
brew install openjdk@17

# Add Java to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

# Verify installation
java -version
```

**PostgreSQL 15:**
```bash
# Install PostgreSQL
brew install postgresql@15

# Add PostgreSQL to PATH
echo 'export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"' >> ~/.zshrc
export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"

# Start PostgreSQL service
brew services start postgresql@15

# Create database and user
createdb reviews
createuser -s reviews_user
psql -U reviews_user -d reviews -c "ALTER USER reviews_user PASSWORD 'reviews_pass';"
```

## Project Setup

### 2. Clone and Navigate to Project
```bash
cd /path/to/review-microservice
```

### 3. Verify Project Structure
```bash
ls -la
# Should show: pom.xml, src/, mvnw, etc.
```

### 4. Verify Dependencies
```bash
# Check if Lombok is in pom.xml
grep -A 5 -B 5 "lombok" pom.xml
```

## Running the Application

### 5. Compile the Project
```bash
./mvnw clean compile
```

### 6. Run the Application

**Option A: With PostgreSQL (Production-like)**
```bash
./mvnw spring-boot:run
```

**Option B: With H2 Database (Development/Testing)**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=test
```

### 7. Verify Application is Running
```bash
# Wait 10-15 seconds for startup, then test
curl -s http://localhost:8080/api/reviews/health
# Expected output: "Review service is healthy"
```

## Testing the Application

### 8. Test All Endpoints
```bash
# Health check
curl -s http://localhost:8080/api/reviews/health

# Get reviews (will be empty initially)
curl -s http://localhost:8080/api/reviews

# Spring Boot actuator health
curl -s http://localhost:8080/actuator/health

# Test processing endpoint (will fail without AWS credentials - expected)
curl -X POST http://localhost:8080/api/reviews/process
```

### 9. Expected Responses

**Health Check:**
```json
"Review service is healthy"
```

**Reviews Endpoint:**
```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {"sorted": false, "unsorted": true, "empty": true},
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 0,
  "totalElements": 0,
  "first": true,
  "numberOfElements": 0,
  "size": 20,
  "number": 0,
  "sort": {"sorted": false, "unsorted": true, "empty": true},
  "empty": true
}
```

**Actuator Health:**
```json
{"status":"UP"}
```

## Running Unit Tests

### 10. Run All Tests
```bash
./mvnw test
```

### 11. Run Tests with Coverage
```bash
./mvnw test jacoco:report
```

### 12. Run Specific Test Classes
```bash
# Run specific test class
./mvnw test -Dtest=ReviewProcessingServiceTest

# Run tests with specific pattern
./mvnw test -Dtest="*ServiceTest"
```

### 13. Run Integration Tests
```bash
./mvnw test -Dtest="*IntegrationTest"
```

## Troubleshooting

### 14. Common Issues and Solutions

**Issue: Java not found**
```bash
# Solution: Verify Java installation
java -version
# If not found, reinstall Java 17
brew install openjdk@17
```

**Issue: PostgreSQL connection refused**
```bash
# Solution: Start PostgreSQL service
brew services start postgresql@15

# Verify PostgreSQL is running
psql -U reviews_user -d reviews -c "SELECT 1;"
```

**Issue: Compilation errors with Lombok**
```bash
# Solution: Verify Lombok dependency
grep -A 3 -B 3 "lombok" pom.xml

# Clean and recompile
./mvnw clean compile
```

**Issue: Port 8080 already in use**
```bash
# Solution: Find and kill the process
lsof -ti:8080 | xargs kill -9

# Or use different port
./mvnw spring-boot:run -Dserver.port=8081
```

### 15. Check Application Logs
```bash
# View real-time logs
tail -f logs/review-microservice.log

# Or view in console if running with spring-boot:run
```

## Development Workflow

### 16. Making Changes and Testing
```bash
# 1. Make code changes
# 2. Compile
./mvnw clean compile

# 3. Run tests
./mvnw test

# 4. Run application
./mvnw spring-boot:run

# 5. Test endpoints
curl -s http://localhost:8080/api/reviews/health
```

### 17. Git Workflow
```bash
# Create new branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "Add new feature"

# Push to remote
git push origin feature/new-feature
```

## Environment Configuration

### 18. Environment Variables (Optional)
```bash
# Set AWS credentials (if you have them)
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
export S3_BUCKET=your-bucket-name

# Set application properties
export DATABASE_URL=jdbc:postgresql://localhost:5432/reviews
export DATABASE_USERNAME=reviews_user
export DATABASE_PASSWORD=reviews_pass
```

## Error Analysis and Solutions

### 19. Common Error Patterns

**Database Connection Error:**
```
Connection to localhost:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
```
**Solution:** Start PostgreSQL service with `brew services start postgresql@15`

**AWS Credentials Error:**
```
Unable to load credentials from any of the providers in the chain
```
**Solution:** This is expected when testing without AWS credentials. The application will start but S3 operations will fail.

**Lombok Compilation Error:**
```
cannot find symbol method getXxx()
```
**Solution:** Ensure Lombok dependency is in pom.xml and annotation processing is enabled in your IDE.

### 20. Application Architecture

The application follows a clean architecture pattern:

- **Domain Layer**: Business entities and DTOs
- **Application Layer**: Use cases and business logic  
- **Infrastructure Layer**: External integrations (DB, S3, etc.)
- **Web Layer**: REST controllers and web layer

### 21. Key Components

- **S3Service**: Handles AWS S3 operations
- **ReviewProcessingService**: Core business logic for parsing and transforming data
- **Repositories**: JPA-based data access layer with PostgreSQL
- **Async Processing**: Concurrent file processing with configurable thread pools
- **Error Handling**: Comprehensive error handling and retry mechanisms

## Summary

✅ **Successfully Running Components:**
- Java 17 with Lombok
- PostgreSQL 15 database
- Spring Boot application on port 8080
- All REST endpoints responding
- Unit tests compiling and running
- Health checks working

✅ **Verified Features:**
- Lombok getters/setters generation
- Database connectivity
- REST API endpoints
- Spring Boot actuator
- JPA entity management
- Async processing capabilities

The application is now fully functional and ready for development and testing!

## Quick Start Commands

For a quick setup, run these commands in sequence:

```bash
# 1. Install dependencies
brew install openjdk@17 postgresql@15

# 2. Setup database
brew services start postgresql@15
createdb reviews
createuser -s reviews_user
psql -U reviews_user -d reviews -c "ALTER USER reviews_user PASSWORD 'reviews_pass';"

# 3. Run application
cd /path/to/review-microservice
./mvnw spring-boot:run

# 4. Test (wait 15 seconds after starting)
curl -s http://localhost:8080/api/reviews/health
```




