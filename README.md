# Review System Microservice

A robust and scalable microservice for processing hotel reviews from AWS S3, built with Java Spring Boot.

## 🏗️ Architecture Overview

This microservice follows **Clean Architecture** principles with clear separation of concerns:

```
├── domain/           # Business entities and DTOs
├── application/      # Use cases and business logic
├── infrastructure/   # External integrations (DB, S3, etc.)
└── web/             # REST controllers and web layer
```

### Key Components

- **S3Service**: Handles AWS S3 operations with pagination support
- **ReviewProcessingService**: Core business logic for parsing and transforming data
- **Repositories**: JPA-based data access layer with PostgreSQL
- **Async Processing**: Concurrent file processing with configurable thread pools
- **Error Handling**: Comprehensive error handling and retry mechanisms

## 🚀 Features

### ✅ Must Have (Implemented)
- **Command-line & Web Service**: REST API + scheduled processing
- **Robust Error Handling**: AWS failures, data validation, DB write failures
- **Comprehensive Logging**: Structured logging to console and files
- **Dockerized Setup**: Multi-container setup with PostgreSQL
- **Complete Documentation**: This README with setup instructions

### 🌟 Nice to Have (Implemented)
- **Unit & Integration Tests**: JUnit 5 + Mockito + Testcontainers
- **Concurrent File Processing**: Configurable thread pool for parallel processing
- **Clean Architecture**: Modular, layered structure
- **JPA ORM**: Spring Data JPA with PostgreSQL

## 🛠️ Technology Stack

- **Java 17** - Runtime environment
- **Spring Boot 3.2** - Framework
- **Spring Data JPA** - ORM layer
- **PostgreSQL** - Primary database
- **AWS SDK v2** - S3 integration
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool
- **JUnit 5 + Mockito** - Testing

## 📋 Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- AWS credentials configured
- Maven 3.6+ (or use included wrapper)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd review-microservice
```

### 2. Configure Environment Variables
Create a `.env` file in the root directory:

```bash
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
S3_BUCKET=your-review-bucket
S3_PREFIX=daily-reviews/

# Processing Configuration
MAX_CONCURRENCY=5
BATCH_SIZE=100
SCHEDULING_ENABLED=false
LOG_LEVEL=INFO
```

### 3. Start with Docker Compose
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Review microservice on port 8080

### 4. Verify Installation
```bash
# Health check
curl http://localhost:8080/api/reviews/health

# Check logs
docker-compose logs -f review-service
```

## 🏃‍♂️ Running the Ingestion Flow

### Option 1: Manual Trigger (Recommended for testing)
```bash
# Trigger processing via REST API
curl -X POST http://localhost:8080/api/reviews/process
```

### Option 2: Scheduled Processing
Set `SCHEDULING_ENABLED=true` in your environment variables to enable daily processing at 2 AM.

### Option 3: Command Line
```bash
# Run locally with Maven
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/review-microservice-1.0.0.jar
```

## 📊 Monitoring and Management

### Health Checks
- **Application Health**: `GET /api/reviews/health`
- **Spring Actuator**: `GET /actuator/health`

### API Endpoints
- **Process Reviews**: `POST /api/reviews/process`
- **Get Reviews**: `GET /api/reviews?page=0&size=20`
- **Health Check**: `GET /api/reviews/health`

### Logs
- **Container logs**: `docker-compose logs -f review-service`
- **Local logs**: `tail -f logs/review-microservice.log`

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest"
```

## 🏗️ Design Decisions

### 1. **Spring Boot Framework**
- **Why**: Mature ecosystem, excellent AWS integration, built-in monitoring
- **Benefits**: Rapid development, production-ready features, extensive documentation

### 2. **Clean Architecture**
- **Why**: Separation of concerns, testability, maintainability
- **Structure**: Domain → Application → Infrastructure layers

### 3. **JPA with PostgreSQL**
- **Why**: ACID compliance, JSON support, excellent Spring integration
- **Benefits**: Strong consistency, complex queries, scalability

### 4. **Async Processing**
- **Why**: Handle multiple large files concurrently
- **Implementation**: Spring's `@Async` with custom thread pool

### 5. **Idempotent Processing**
- **Why**: Prevent duplicate processing, enable safe retries
- **Implementation**: Track processed files in database

### 6. **Comprehensive Error Handling**
- **Approach**: Fail gracefully, log detailed errors, continue processing
- **Strategy**: File-level errors don't stop batch processing

## 🔧 Configuration Options

### Application Properties
```yaml
# Processing Configuration
app:
  processing:
    max-concurrency: 5      # Concurrent file processing threads
    batch-size: 100         # Records per transaction batch
  scheduling:
    enabled: true           # Enable scheduled processing
    cron: "0 0 2 * * ?"    # Daily at 2 AM

# AWS Configuration
aws:
  region: us-east-1
  s3:
    bucket: review-data-bucket
    prefix: daily-reviews/
```

### Environment Variables
All configuration can be overridden via environment variables using Spring Boot's standard naming convention.

## 📈 Scaling Considerations

### Current Capacity
- **Concurrent Files**: 5 (configurable)
- **Memory Usage**: ~512MB for moderate workloads
- **Database**: Optimized indexes for common queries

### Scaling Options
1. **Horizontal Scaling**: Deploy multiple instances with load balancer  
2. **Vertical Scaling**: Increase memory and CPU resources
3. **Database Scaling**: Read replicas, connection pooling
4. **Async Processing**: Increase thread pool size

## 🔍 Troubleshooting

### Common Issues

1. **AWS Credentials Not Found**
   ```bash
   # Check AWS configuration
   docker-compose exec review-service env | grep AWS
   ```

2. **Database Connection Failed**
   ```bash
   # Check PostgreSQL health
   docker-compose exec postgres pg_isready -U reviews_user
   ```

3. **Out of Memory Errors**
   ```bash
   # Increase Docker memory limits
   # Or reduce MAX_CONCURRENCY and BATCH_SIZE
   ```

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
docker-compose up -d
```

## 🚀 Future Enhancements

### Planned Features
- [ ] **Multi-Provider Support**: Extend to Booking.com, Expedia
- [ ] **Real-time Processing**: AWS Lambda + SQS integration  
- [ ] **REST API**: Full CRUD operations for reviews
- [ ] **Metrics & Monitoring**: Prometheus/Grafana integration
- [ ] **CI/CD Pipeline**: GitHub Actions workflow

### Architecture Improvements
- [ ] **Event-Driven Architecture**: Publish processing events
- [ ] **Caching Layer**: Redis for frequently accessed data
- [ ] **Message Queue**: Decouple file discovery from processing
- [ ] **Data Validation**: Enhanced schema validation

## 📞 Support

For issues and questions:
1. Check the logs: `docker-compose logs -f review-service`
2. Verify configuration: Environment variables and AWS credentials
3. Review troubleshooting section above
4. Check application health endpoints

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
