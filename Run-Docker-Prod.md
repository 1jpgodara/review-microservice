# Production Docker Deployment Guide

This guide provides comprehensive instructions for deploying the Review Microservice in production using Docker containers.

## 🎯 Overview

This guide covers production deployment using Docker containers with proper configuration, monitoring, and scaling considerations.

## 📋 Prerequisites

### **System Requirements**
- Docker Engine 20.10+
- Docker Compose 2.0+
- Minimum 4GB RAM
- 20GB available disk space
- Linux/macOS/Windows with Docker support

### **AWS Requirements**
- AWS CLI configured
- S3 bucket with appropriate permissions
- IAM user with S3 access
- AWS credentials configured

## 🚀 Production Deployment

### **1. Environment Configuration**

Create a `.env` file in the project root:

```bash
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-production-access-key
AWS_SECRET_ACCESS_KEY=your-production-secret-key
S3_BUCKET=your-production-review-bucket
S3_PREFIX=daily-reviews/

# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/reviews
DATABASE_USERNAME=reviews_user
DATABASE_PASSWORD=your-secure-production-password

# Application Configuration
MAX_CONCURRENCY=10
BATCH_SIZE=500
SCHEDULING_ENABLED=true
LOG_LEVEL=INFO
SERVER_PORT=8080

# Production Settings
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
```

### **2. Security Configuration**

#### **Database Security**
```bash
# Generate secure password
openssl rand -base64 32

# Update .env with secure password
DATABASE_PASSWORD=your-generated-secure-password
```

#### **AWS Credentials Security**
```bash
# Use AWS IAM roles for production
# Or store credentials securely in environment variables
export AWS_ACCESS_KEY_ID=your-production-key
export AWS_SECRET_ACCESS_KEY=your-production-secret
```

### **3. Docker Compose Production Setup**

#### **docker-compose.prod.yml**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: review-postgres-prod
    environment:
      POSTGRES_DB: reviews
      POSTGRES_USER: reviews_user
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_prod_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U reviews_user -d reviews"]
      interval: 30s
      timeout: 10s
      retries: 5
    restart: unless-stopped
    networks:
      - review-network

  review-service:
    build: .
    container_name: review-microservice-prod
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/reviews
      DATABASE_USERNAME: reviews_user
      DATABASE_PASSWORD: ${DATABASE_PASSWORD}
      AWS_REGION: ${AWS_REGION}
      S3_BUCKET: ${S3_BUCKET}
      S3_PREFIX: ${S3_PREFIX}
      MAX_CONCURRENCY: ${MAX_CONCURRENCY}
      BATCH_SIZE: ${BATCH_SIZE}
      SCHEDULING_ENABLED: ${SCHEDULING_ENABLED}
      LOG_LEVEL: ${LOG_LEVEL}
      AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
      AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
      JAVA_OPTS: ${JAVA_OPTS}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/reviews/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - review-network
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'

volumes:
  postgres_prod_data:
    driver: local

networks:
  review-network:
    driver: bridge
```

### **4. Production Deployment Commands**

#### **Initial Deployment**
```bash
# Build and start services
docker-compose -f docker-compose.prod.yml up -d

# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f review-service
```

#### **Health Check**
```bash
# Application health
curl http://localhost:8080/api/reviews/health

# Actuator health
curl http://localhost:8080/actuator/health

# Database connectivity
docker-compose -f docker-compose.prod.yml exec postgres pg_isready -U reviews_user
```

#### **Service Management**
```bash
# Stop services
docker-compose -f docker-compose.prod.yml down

# Restart services
docker-compose -f docker-compose.prod.yml restart

# Update services
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## 📊 Monitoring and Logging

### **Application Monitoring**

#### **Health Endpoints**
```bash
# Basic health check
GET /api/reviews/health

# Detailed health check
GET /actuator/health

# Application info
GET /actuator/info

# Metrics
GET /actuator/metrics
```

#### **Log Monitoring**
```bash
# Follow application logs
docker-compose -f docker-compose.prod.yml logs -f review-service

# Follow database logs
docker-compose -f docker-compose.prod.yml logs -f postgres

# View recent logs
docker-compose -f docker-compose.prod.yml logs --tail=100 review-service
```

### **Resource Monitoring**

#### **Container Resources**
```bash
# Check container resource usage
docker stats

# Check specific container
docker stats review-microservice-prod
```

#### **Database Monitoring**
```bash
# Connect to database
docker-compose -f docker-compose.prod.yml exec postgres psql -U reviews_user -d reviews

# Check database size
SELECT pg_size_pretty(pg_database_size('reviews'));

# Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

## 🔧 Production Configuration

### **JVM Optimization**

#### **Memory Settings**
```bash
# Production JVM options
JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:+UseStringDeduplication"
```

#### **GC Settings**
```bash
# G1GC optimization
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### **Database Optimization**

#### **PostgreSQL Configuration**
```sql
-- Connection settings
ALTER SYSTEM SET max_connections = '200';
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';

-- Query optimization
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';

-- Reload configuration
SELECT pg_reload_conf();
```

### **Application Configuration**

#### **Processing Settings**
```yaml
# application-prod.yml
app:
  processing:
    max-concurrency: 10
    batch-size: 500
  scheduling:
    enabled: true
    cron: "0 0 2 * * ?"  # Daily at 2 AM
```

## 🔒 Security Considerations

### **Network Security**

#### **Firewall Configuration**
```bash
# Allow only necessary ports
sudo ufw allow 8080/tcp  # Application
sudo ufw allow 5432/tcp  # Database (if external access needed)
```

#### **Docker Network Security**
```yaml
# Use custom network with restricted access
networks:
  review-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### **Data Security**

#### **Database Encryption**
```sql
-- Enable SSL for database connections
ALTER SYSTEM SET ssl = on;
ALTER SYSTEM SET ssl_cert_file = '/etc/ssl/certs/ssl-cert-snakeoil.pem';
ALTER SYSTEM SET ssl_key_file = '/etc/ssl/private/ssl-cert-snakeoil.key';
```

#### **Application Security**
```yaml
# Enable HTTPS in production
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

## 📈 Scaling and Performance

### **Horizontal Scaling**

#### **Multiple Instances**
```bash
# Scale application service
docker-compose -f docker-compose.prod.yml up -d --scale review-service=3

# Load balancer configuration
# Use nginx or HAProxy for load balancing
```

#### **Database Scaling**
```yaml
# Read replicas configuration
services:
  postgres-read:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: reviews
      POSTGRES_USER: reviews_user
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - postgres_read_data:/var/lib/postgresql/data
```

### **Performance Optimization**

#### **Connection Pooling**
```yaml
# HikariCP configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### **Caching Configuration**
```yaml
# Redis cache configuration
spring:
  cache:
    type: redis
    redis:
      host: redis
      port: 6379
      timeout: 2000ms
```

## 🔄 Backup and Recovery

### **Database Backup**

#### **Automated Backup**
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
docker-compose -f docker-compose.prod.yml exec postgres pg_dump -U reviews_user reviews > backup_$DATE.sql
gzip backup_$DATE.sql
aws s3 cp backup_$DATE.sql.gz s3://your-backup-bucket/
```

#### **Scheduled Backups**
```bash
# Add to crontab
0 2 * * * /path/to/backup.sh
```

### **Application Backup**

#### **Configuration Backup**
```bash
# Backup configuration files
tar -czf config_backup_$(date +%Y%m%d).tar.gz config/
aws s3 cp config_backup_$(date +%Y%m%d).tar.gz s3://your-backup-bucket/
```

## 🚨 Troubleshooting

### **Common Production Issues**

#### **Memory Issues**
```bash
# Check memory usage
docker stats review-microservice-prod

# Increase memory limit
docker-compose -f docker-compose.prod.yml up -d --scale review-service=1
```

#### **Database Connection Issues**
```bash
# Check database connectivity
docker-compose -f docker-compose.prod.yml exec review-service ping postgres

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres
```

#### **AWS Credential Issues**
```bash
# Verify AWS credentials
docker-compose -f docker-compose.prod.yml exec review-service env | grep AWS

# Test S3 access
docker-compose -f docker-compose.prod.yml exec review-service aws s3 ls
```

### **Debug Mode**

#### **Enable Debug Logging**
```bash
# Set debug level
export LOG_LEVEL=DEBUG
docker-compose -f docker-compose.prod.yml up -d

# View debug logs
docker-compose -f docker-compose.prod.yml logs -f review-service
```

## 🔄 CI/CD Integration

### **GitHub Actions Example**

#### **.github/workflows/deploy.yml**
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and push Docker image
        run: |
          docker build -t review-microservice:${{ github.sha }} .
          docker push review-microservice:${{ github.sha }}
      
      - name: Deploy to production
        run: |
          docker-compose -f docker-compose.prod.yml pull
          docker-compose -f docker-compose.prod.yml up -d
```

## 📋 Production Checklist

### **Pre-Deployment**
- [ ] Environment variables configured
- [ ] AWS credentials verified
- [ ] Database backup completed
- [ ] SSL certificates installed
- [ ] Firewall rules configured
- [ ] Monitoring tools configured

### **Post-Deployment**
- [ ] Health checks passing
- [ ] Application logs reviewed
- [ ] Database connectivity verified
- [ ] S3 access tested
- [ ] Performance metrics monitored
- [ ] Backup procedures tested

## 🎯 Best Practices

### **Security**
1. **Use secrets management** for sensitive data
2. **Enable SSL/TLS** for all communications
3. **Regular security updates** for base images
4. **Network segmentation** with custom Docker networks

### **Performance**
1. **Monitor resource usage** regularly
2. **Optimize JVM settings** for your workload
3. **Use connection pooling** for database connections
4. **Implement caching** for frequently accessed data

### **Reliability**
1. **Set up automated backups**
2. **Configure health checks** for all services
3. **Use restart policies** for container recovery
4. **Monitor application metrics** continuously

---

*This guide provides comprehensive production deployment instructions for the Review Microservice using Docker containers.* 