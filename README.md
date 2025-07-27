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
- **Complete Documentation**: Comprehensive setup and deployment guides

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

## 📋 Quick Start

**Choose your deployment approach:**

- **[Local Development Guide](Run-Local.md)** - Complete setup for local development with direct Java execution
- **[Production Docker Guide](Run-Docker-Prod.md)** - Production deployment using Docker containers
- **[Docker vs Local Comparison](DockerVsLocal.md)** - Detailed comparison of both approaches

## 🏃‍♂️ Running the Application

### **Development Options:**

1. **Local Development** - Fast iteration with direct Java execution
2. **Docker Development** - Containerized environment for consistency
3. **Production Deployment** - Full production setup with monitoring

**For detailed instructions, see the respective guides above.**

## 📊 API Endpoints

### **Core Endpoints**
- `GET /api/reviews/health` - Health check
- `GET /api/reviews` - List reviews (paginated)
- `POST /api/reviews/process` - Trigger review processing

### **Monitoring Endpoints**
- `GET /actuator/health` - Spring Boot actuator health
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

## 🏗️ Design Decisions

### **1. Spring Boot Framework**
- **Why**: Mature ecosystem, excellent AWS integration, built-in monitoring
- **Benefits**: Rapid development, production-ready features, extensive documentation

### **2. Clean Architecture**
- **Why**: Separation of concerns, testability, maintainability
- **Structure**: Domain → Application → Infrastructure layers

### **3. JPA with PostgreSQL**
- **Why**: ACID compliance, JSON support, excellent Spring integration
- **Benefits**: Strong consistency, complex queries, scalability

### **4. Async Processing**
- **Why**: Handle multiple large files concurrently
- **Implementation**: Spring's `@Async` with custom thread pool

### **5. Idempotent Processing**
- **Why**: Prevent duplicate processing, enable safe retries
- **Implementation**: Track processed files in database

### **6. Comprehensive Error Handling**
- **Approach**: Fail gracefully, log detailed errors, continue processing
- **Strategy**: File-level errors don't stop batch processing

## 🔧 Configuration

### **Environment Variables**
All configuration can be overridden via environment variables:

```bash
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
S3_BUCKET=your-review-bucket
S3_PREFIX=daily-reviews/

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/reviews
DATABASE_USERNAME=reviews_user
DATABASE_PASSWORD=reviews_pass

# Application Configuration
MAX_CONCURRENCY=5
BATCH_SIZE=100
SCHEDULING_ENABLED=false
LOG_LEVEL=INFO
```

### **Application Properties**
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

## 📈 Scaling Considerations

### **Current Capacity**
- **Concurrent Files**: 5 (configurable)
- **Memory Usage**: ~512MB for moderate workloads
- **Database**: Optimized indexes for common queries

### **Scaling Options**
1. **Horizontal Scaling**: Deploy multiple instances with load balancer  
2. **Vertical Scaling**: Increase memory and CPU resources
3. **Database Scaling**: Read replicas, connection pooling
4. **Async Processing**: Increase thread pool size

## 🧪 Testing

### **Running Tests**
```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest"
```

## 🚀 Future Enhancements

### **Planned Features**
- [ ] **Multi-Provider Support**: Extend to Booking.com, Expedia
- [ ] **Real-time Processing**: AWS Lambda + SQS integration  
- [ ] **REST API**: Full CRUD operations for reviews
- [ ] **Metrics & Monitoring**: Prometheus/Grafana integration
- [ ] **CI/CD Pipeline**: GitHub Actions workflow

### **Architecture Improvements**
- [ ] **Event-Driven Architecture**: Publish processing events
- [ ] **Caching Layer**: Redis for frequently accessed data
- [ ] **Message Queue**: Decouple file discovery from processing
- [ ] **Data Validation**: Enhanced schema validation

## 📞 Support

For issues and questions:
1. Check the appropriate setup guide for your deployment method
2. Review troubleshooting sections in the respective guides
3. Check application health endpoints
4. Verify configuration and AWS credentials

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📚 Documentation

- **[Local Development Guide](Run-Local.md)** - Complete setup for local development
- **[Production Docker Guide](Run-Docker-Prod.md)** - Production deployment instructions
- **[Docker vs Local Comparison](DockerVsLocal.md)** - Detailed comparison and decision guide




// Updating this line just to run with github actions
