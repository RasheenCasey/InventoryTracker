# Food Truck Inventory Tracker

A Spring Boot web application for managing food truck inventory, tracking stock levels, and placing orders with distributors.

## Features

- **Inventory Management**: Add, update, and delete inventory items
- **Stock Tracking**: Monitor quantities with low-stock alerts
- **Order Management**: Place orders with distributors and track deliveries
- **Dashboard**: View inventory statistics and order status
- **Calendar**: Track daily notes and comments
- **Menu Specials**: Manage current promotions and specials
- **AI Assistant**: Chat interface for inventory queries

## Tech Stack

- **Backend**: Spring Boot 3.5, Java 17
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Database**: H2 (development), PostgreSQL (production)
- **Security**: Spring Security (form-based authentication)
- **Build**: Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9 or higher

### Running Locally

1. Clone the repository:
```bash
git clone <repository-url>
cd productsheet
```

2. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

3. Open in browser:
```
http://localhost:8080
```

Login credentials:
- Username: `admin`
- Password: `admin`

### H2 Console

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

JDBC URL: `jdbc:h2:mem:appdb`
Username: `sa`
Password: (leave blank)

## Docker

### Build and run with Docker:

```bash
docker build -t foodtruck-inventory .
docker run -p 8080:8080 foodtruck-inventory
```

## Deployment (Render)

1. Create a PostgreSQL database on Render
2. Set environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JDBC_DATABASE_URL=jdbc:postgresql://...`
3. Deploy using the Dockerfile

## Project Structure

```
src/
├── main/
│   ├── java/com/Inventory/productsheet/
│   │   ├── config/         # Security configuration
│   │   ├── controller/     # Web controllers
│   │   ├── model/          # Entity classes
│   │   ├── repository/     # Data access layer
│   │   └── service/        # Business logic
│   └── resources/
│       ├── static/css/     # Stylesheets
│       ├── templates/      # Thymeleaf templates
│       ├── application.properties          # Dev config
│       └── application-prod.properties     # Production config
└── test/                   # Unit tests
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Home page |
| `/login` | GET/POST | Login page |
| `/inventory` | GET | Inventory dashboard |
| `/inventory/add` | POST | Add new item |
| `/inventory/update` | POST | Update stock |
| `/inventory/delete/{id}` | POST | Delete item |
| `/inventory/order` | POST | Place order |
| `/orders/{id}/deliver` | POST | Mark order delivered |
| `/orders/{id}/delete` | POST | Delete order |
| `/ai/chat` | GET/POST | AI assistant |

## Configuration

### Development (application.properties)
- H2 in-memory database
- Auto-creates schema
- Loads sample data from `data.sql`
- Thymeleaf caching disabled

### Production (application-prod.properties)
- PostgreSQL database
- Schema updates only
- No sample data
- Thymeleaf caching enabled
- H2 console disabled

## Testing

Run tests with Maven:
```bash
mvn test
```

Tests cover:
- Item CRUD operations
- Order management
- Stock updates
- Soft/hard deletion
- Validation rules

## License

MIT License
