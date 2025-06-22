# Sunbird Serve Fulfill

A Spring Boot microservice for managing volunteer need fulfillment and nominations in the Sunbird Serve platform.

## Features

- Volunteer need nomination management
- Fulfillment tracking and status updates
- Email notifications for nominations and status changes
- RESTful API with OpenAPI documentation
- PostgreSQL database integration

## Security Features

- Environment variable-based configuration
- Input validation with Bean Validation
- Proper CORS configuration
- Global exception handling
- Secure credential management

## Prerequisites

- Java 17 or higher
- Gradle 7.6.0 or higher
- PostgreSQL database
- SMTP server for email notifications

## Setup Instructions

### 1. Environment Configuration

Copy the example environment file and configure your settings:

```bash
cp env.example .env
```

Edit `.env` with your actual values:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/your-database
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_password

# External Service URLs
SERVE_URL=https://your-serve-url.com
SERVE_NEED_URL=https://your-need-service-url.com
SERVE_VOLUNTEERING_URL=https://your-volunteering-service-url.com

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_app_password

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend-url.com
```

### 2. Database Setup

Create a PostgreSQL database and update the connection details in your environment variables.

### 3. Build and Run

```bash
# Build the application
./gradlew build

# Run in development mode
./gradlew bootRun

# Or run with specific profile
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### 4. Docker Deployment

```bash
# Build Docker image
docker build -t sunbird-serve-fulfill .

# Run with environment variables
docker run -p 8090:8090 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/ev-vriddhi \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_password \
  sunbird-serve-fulfill
```

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8090/api/v1/serve-fulfill/swagger-ui.html
```

## Security Considerations

### Environment Variables
- Never commit sensitive data to version control
- Use environment variables for all credentials
- Use different configurations for dev/staging/production

### CORS Configuration
- Configure allowed origins based on your frontend URLs
- Avoid using `*` for production environments
- Regularly review and update allowed origins

### Input Validation
- All API endpoints validate input using Bean Validation
- Custom validation patterns for ID fields
- Proper error responses for validation failures

### Error Handling
- Global exception handler provides consistent error responses
- Sensitive information is not exposed in error messages
- Proper logging for debugging without exposing secrets

## Development

### Running Tests
```bash
./gradlew test
```

### Code Quality
```bash
./gradlew check
```

## Production Deployment

1. Set `SPRING_PROFILES_ACTIVE=prod`
2. Configure all required environment variables
3. Use HTTPS for all external communications
4. Set up proper monitoring and logging
5. Configure database connection pooling
6. Set up health checks

## Contributing

1. Follow the existing code style
2. Add tests for new features
3. Update documentation as needed
4. Ensure all security checks pass

## License

[Add your license information here]