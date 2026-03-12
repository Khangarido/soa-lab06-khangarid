# Lab 06: JSON Service and SOAP Service Integration

**Student:** Khangarid | **ID:** 22B1NUM4730  
**Course:** Service Oriented Architecture — National University of Mongolia (NUM)  
**Semester:** Spring 2026

---

## Architecture Overview

This project implements **Option 2 — Independent Databases** with **Bonus 3: JWT Token**.

```
┌─────────────────┐       SOAP/XML        ┌─────────────────────┐
│  frontend-app   │ ────────────────────►  │  user-soap-service  │
│  (index.html)   │                        │  Port 8080          │
│                 │                        │  H2: authdb         │
│  Register/Login │   ◄── JWT token ────   │  (UserAuth table)   │
└────────┬────────┘                        └──────────┬──────────┘
         │                                            │
         │  REST/JSON + Bearer token                  │ SOAP ValidateToken
         │                                            │
         ▼                                            │
┌─────────────────────┐                               │
│  user-json-service  │  ── SOAP XML call ──────────► │
│  Port 8081          │                               │
│  H2: profiledb      │  Interceptor validates token
│  (UserProfile table)│  before allowing REST access
└─────────────────────┘
```

### Database Choice: Option 2 — Independent Databases

Each service maintains its own **H2 in-memory database**:

| Service            | DB URL                | Table        |
|--------------------|----------------------|--------------|
| user-soap-service  | `jdbc:h2:mem:authdb`    | `user_auth`    |
| user-json-service  | `jdbc:h2:mem:profiledb` | `user_profile` |

This enforces **loose coupling** between services — a core SOA principle. Each service owns its data and can be deployed, scaled, and maintained independently.

### Bonus 3: JWT Token

- On login, the SOAP service generates a **signed JWT token** (HMAC-SHA256, 1-hour expiry).
- The token is stored in `localStorage` on the client.
- Every REST call to the JSON service includes `Authorization: Bearer <token>`.
- The JSON service's **Interceptor** sends a SOAP `validateTokenRequest` to the SOAP service.
- Only valid tokens are allowed through; invalid tokens receive HTTP 401.

---

## How to Run

### Prerequisites
- **Java 17+** (JDK)
- **Maven 3.8+** (or use the included `mvnw` wrapper)

### Step 1: Start the SOAP Service (port 8080)

```bash
cd user-soap-service
./mvnw clean spring-boot:run
```

On Windows:
```cmd
cd user-soap-service
mvnw.cmd clean spring-boot:run
```

Verify: Open http://localhost:8080/ws/auth.wsdl — you should see the generated WSDL.

### Step 2: Start the JSON Service (port 8081)

```bash
cd user-json-service
./mvnw clean spring-boot:run
```

On Windows:
```cmd
cd user-json-service
mvnw.cmd clean spring-boot:run
```

Verify: The service starts and pre-loads a dummy profile for Khangarid.

### Step 3: Open the Frontend

Open `frontend-app/index.html` directly in a browser (no web server needed).

### Usage Flow

1. **Register** a user (e.g., `khangarid` / `pass123`) — sends SOAP XML to port 8080
2. **Login** with the same credentials — receives JWT token
3. **View Profile** — automatically calls REST on port 8081 with the JWT token
4. **Edit Profile** — update name, email, bio, phone via REST PUT

### H2 Console (for debugging)

- SOAP service: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:authdb`)
- JSON service: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:profiledb`)

---

## Project Structure

```
lab01/
├── user-soap-service/          # SOAP Authentication Service (Port 8080)
│   ├── src/main/resources/
│   │   ├── auth.xsd            # XSD contract for SOAP operations
│   │   └── application.properties
│   └── src/main/java/com/example/demo/
│       ├── entity/             # UserAuth JPA entity
│       ├── repository/         # Spring Data JPA repository
│       ├── service/            # JWT generation & validation logic
│       ├── endpoint/           # Spring WS SOAP @Endpoint
│       ├── gen/                # JAXB-annotated request/response classes
│       └── config/             # WebService config, CORS filter
│
├── user-json-service/          # REST Profile Service (Port 8081)
│   └── src/main/java/com/example/demo/
│       ├── entity/             # UserProfile JPA entity
│       ├── repository/         # Spring Data JPA repository
│       ├── controller/         # REST CRUD controller
│       ├── interceptor/        # Token validation via SOAP call
│       └── config/             # WebMvc + CORS config
│
├── frontend-app/               # Vanilla JS Frontend
│   └── index.html
│
└── README.md
```

---

*Bayarlalaa! — NUM SOA Lab 06*
