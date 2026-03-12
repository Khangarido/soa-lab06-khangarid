# Лаб 06: JSON Сервис ба SOAP Сервисийн Интеграци

**Оюутан:** Хангарид | **Оюутны код:** 22B1NUM4730  
**Хичээл:** Үйлчилгээнд суурилсан архитектур (SOA) — МУИС  
**Улирал:** 2026 хавар

---

## Архитектурын тойм

Энэ төсөлд **Сонголт 2 — Бие даасан өгөгдлийн сангууд** болон **Бонус 3: JWT Token** хэрэгжүүлсэн.

```
┌─────────────────┐       SOAP/XML        ┌─────────────────────┐
│  frontend-app   │ ────────────────────►  │  user-soap-service  │
│  (index.html)   │                        │  Порт 8080          │
│                 │                        │  H2: authdb         │
│  Бүртгэл/Нэвтрэх│  ◄── JWT token ────   │  (UserAuth хүснэгт) │
└────────┬────────┘                        └──────────┬──────────┘
         │                                            │
         │  REST/JSON + Bearer token                  │ SOAP ValidateToken
         │                                            │
         ▼                                            │
┌─────────────────────┐                               │
│  user-json-service  │  ── SOAP XML дуудлага ──────► │
│  Порт 8081          │                               │
│  H2: profiledb      │  Interceptor нь токеныг
│  (UserProfile       │  шалгасны дараа зөвшөөрнө
│   хүснэгт)          │
└─────────────────────┘
```

### Өгөгдлийн сангийн сонголт: Сонголт 2 — Бие даасан өгөгдлийн сан

Сервис бүр өөрийн **H2 in-memory өгөгдлийн сан**-тай:

| Сервис             | DB URL                  | Хүснэгт        |
|--------------------|------------------------|----------------|
| user-soap-service  | `jdbc:h2:mem:authdb`    | `user_auth`    |
| user-json-service  | `jdbc:h2:mem:profiledb` | `user_profile` |

Ингэснээр сервисүүд хоорондоо **сул холбоотой (loose coupling)** байна — энэ бол SOA-ийн гол зарчим. Сервис бүр өөрийн өгөгдлийг эзэмшиж, бие даан deploy хийх, масштаблах боломжтой.

### Бонус 3: JWT Token

- Нэвтрэх үед SOAP сервис нь **JWT token** (HMAC-SHA256, 1 цагийн хугацаатай) үүсгэнэ.
- Токен нь клиент талын `localStorage`-д хадгалагдана.
- JSON сервис рүү хийх бүх REST дуудлагад `Authorization: Bearer <token>` header-ийг илгээнэ.
- JSON сервисийн **Interceptor** нь SOAP сервис рүү `validateTokenRequest` илгээж токеныг шалгана.
- Зөвхөн хүчинтэй токентой хүсэлтийг зөвшөөрнө, хүчингүй бол HTTP 401 буцаана.

---

## Ажиллуулах заавар

### Шаардлага
- **Java 17+** (JDK)
- **Maven 3.8+** (эсвэл төсөлд орсон `mvnw` wrapper ашиглах)

### 1-р алхам: SOAP сервисийг ажиллуулах (порт 8080)

```bash
cd user-soap-service
./mvnw clean spring-boot:run
```

Windows дээр:
```cmd
cd user-soap-service
mvnw.cmd clean spring-boot:run
```

Шалгах: http://localhost:8080/ws/auth.wsdl нээхэд WSDL файл харагдана.

### 2-р алхам: JSON сервисийг ажиллуулах (порт 8081)

```bash
cd user-json-service
./mvnw clean spring-boot:run
```

Windows дээр:
```cmd
cd user-json-service
mvnw.cmd clean spring-boot:run
```

Шалгах: Сервис ачаалагдахад Хангарид-ийн dummy профайл автоматаар үүснэ.

### 3-р алхам: Frontend нээх

`frontend-app/index.html` файлыг браузер дээр шууд нээнэ (вэб сервер шаардлагагүй).

### Ашиглах дараалал

1. **Бүртгүүлэх** — жишээ нь `khangarid` / `pass123` гэж бичээд Register дарна → SOAP XML-ээр порт 8080 руу илгээнэ
2. **Нэвтрэх** — Login дарахад JWT token буцаж ирнэ
3. **Профайл харах** — Токентой хамт порт 8081 руу REST дуудлага хийж, хэрэглэгчийн мэдээлэл харуулна
4. **Профайл засах** — Нэр, имэйл, био, утас зэргийг REST PUT-ээр шинэчилнэ

### H2 Console (дебаг хийхэд)

- SOAP сервис: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:authdb`)
- JSON сервис: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:profiledb`)

---

## Төслийн бүтэц

```
lab01/
├── user-soap-service/          # SOAP Баталгаажуулалтын сервис (Порт 8080)
│   ├── src/main/resources/
│   │   ├── auth.xsd            # SOAP үйлдлүүдийн XSD гэрээ
│   │   └── application.properties
│   └── src/main/java/com/example/demo/
│       ├── entity/             # UserAuth JPA entity
│       ├── repository/         # Spring Data JPA repository
│       ├── service/            # JWT үүсгэх ба шалгах логик
│       ├── endpoint/           # Spring WS SOAP @Endpoint
│       ├── gen/                # JAXB request/response классууд
│       └── config/             # WebService тохиргоо, CORS filter
│
├── user-json-service/          # REST Профайл сервис (Порт 8081)
│   └── src/main/java/com/example/demo/
│       ├── entity/             # UserProfile JPA entity
│       ├── repository/         # Spring Data JPA repository
│       ├── controller/         # REST CRUD controller
│       ├── interceptor/        # SOAP-оор токен шалгах middleware
│       └── config/             # WebMvc + CORS тохиргоо
│
├── frontend-app/               # Vanilla JS Frontend
│   └── index.html
│
└── README.md
```

---

## Технологи

| Технологи | Хэрэглээ |
|-----------|---------|
| Spring Boot 3.4.3 | Backend framework |
| Spring Web Services | SOAP endpoint |
| Spring Data JPA | Өгөгдлийн сангийн удирдлага |
| H2 Database | In-memory өгөгдлийн сан |
| JJWT 0.12.6 | JWT token үүсгэх/шалгах |
| Vanilla JavaScript | Frontend (Fetch API) |

---

*Баярлалаа! — МУИС, Үйлчилгээнд суурилсан архитектур, Лаб 06*
