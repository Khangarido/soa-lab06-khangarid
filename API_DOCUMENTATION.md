# API Documentation — Lab 06

**Оюутан:** Хангарид | **Код:** 22B1NUM4730 | **МУИС SOA 2026 хавар**

---

## 1. SOAP Service — Баталгаажуулалтын үйлчилгээ (Port 8080)

**WSDL:** `http://localhost:8080/ws/auth.wsdl`  
**Endpoint URL:** `http://localhost:8080/ws`  
**Protocol:** SOAP 1.1 / XML  
**Namespace:** `http://example.com/auth`

### 1.1 RegisterUser — Хэрэглэгч бүртгэх

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:auth="http://example.com/auth">
  <soapenv:Body>
    <auth:registerUserRequest>
      <auth:username>khangarid</auth:username>
      <auth:password>pass123</auth:password>
    </auth:registerUserRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

**Response (амжилттай):**
```xml
<registerUserResponse xmlns="http://example.com/auth">
  <success>true</success>
  <message>Amjilttai burtgegdlee (Registered successfully)</message>
</registerUserResponse>
```

**Response (алдаа — хэрэглэгч бүртгэлтэй):**
```xml
<registerUserResponse xmlns="http://example.com/auth">
  <success>false</success>
  <message>Hereglegchiin ner ashiglagdsan baina (Username already taken)</message>
</registerUserResponse>
```

---

### 1.2 LoginUser — Хэрэглэгч нэвтрэх (JWT token авах)

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:auth="http://example.com/auth">
  <soapenv:Body>
    <auth:loginUserRequest>
      <auth:username>khangarid</auth:username>
      <auth:password>pass123</auth:password>
    </auth:loginUserRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

**Response (амжилттай):**
```xml
<loginUserResponse xmlns="http://example.com/auth">
  <success>true</success>
  <token>eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJraGFu...</token>
  <message>Amjilttai nevterlee (Login successful)</message>
</loginUserResponse>
```

**Response (нууц үг буруу):**
```xml
<loginUserResponse xmlns="http://example.com/auth">
  <success>false</success>
  <token></token>
  <message>Nuuts ug buruu baina (Invalid password)</message>
</loginUserResponse>
```

---

### 1.3 ValidateToken — Token шалгах

Энэ үйлдлийг JSON service-ийн Interceptor дотроосоо дуудна.

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:auth="http://example.com/auth">
  <soapenv:Body>
    <auth:validateTokenRequest>
      <auth:token>eyJhbGciOiJIUzI1NiJ9...</auth:token>
    </auth:validateTokenRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

**Response (хүчинтэй):**
```xml
<validateTokenResponse xmlns="http://example.com/auth">
  <valid>true</valid>
  <username>khangarid</username>
</validateTokenResponse>
```

**Response (хүчингүй):**
```xml
<validateTokenResponse xmlns="http://example.com/auth">
  <valid>false</valid>
  <username></username>
</validateTokenResponse>
```

---

### SOAP Entity: UserAuth

| Талбар    | Төрөл   | Тайлбар                    |
|-----------|---------|---------------------------|
| id        | Long    | Auto-generated primary key |
| username  | String  | Давтагдашгүй (unique)     |
| password  | String  | Нууц үг                   |
| token     | String  | Сүүлд үүсгэсэн JWT token  |

---

## 2. REST/JSON Service — Профайл удирдлагын үйлчилгээ (Port 8081)

**Base URL:** `http://localhost:8081`  
**Protocol:** REST / JSON  
**Authentication:** Бүх `/users/**` endpoint-д `Authorization: Bearer <JWT_TOKEN>` header шаардлагатай.

### 2.1 GET /users — Бүх профайл авах

**Request:**
```
GET http://localhost:8081/users
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": "22B1NUM4730",
    "name": "Khangarid",
    "email": "khangarid@num.edu.mn",
    "bio": "Software Engineering student at NUM. SOA Lab 06.",
    "phone": "+976-9911-2233"
  }
]
```

**Response (401 Unauthorized — token байхгүй):**
```json
{
  "error": "Authorization header with Bearer token required"
}
```

---

### 2.2 GET /users/{id} — Нэг профайл авах

**Request:**
```
GET http://localhost:8081/users/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": "22B1NUM4730",
  "name": "Khangarid",
  "email": "khangarid@num.edu.mn",
  "bio": "Software Engineering student at NUM. SOA Lab 06.",
  "phone": "+976-9911-2233"
}
```

**Response (404 Not Found):** хоосон

---

### 2.3 POST /users — Шинэ профайл үүсгэх

**Request:**
```
POST http://localhost:8081/users
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

```json
{
  "userId": "22B1NUM9999",
  "name": "Bat",
  "email": "bat@num.edu.mn",
  "bio": "CS student",
  "phone": "+976-9900-1122"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "userId": "22B1NUM9999",
  "name": "Bat",
  "email": "bat@num.edu.mn",
  "bio": "CS student",
  "phone": "+976-9900-1122"
}
```

---

### 2.4 PUT /users/{id} — Профайл шинэчлэх

**Request:**
```
PUT http://localhost:8081/users/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

```json
{
  "name": "Khangarid Updated",
  "email": "khangarid.new@num.edu.mn",
  "bio": "Updated bio",
  "phone": "+976-8800-1234"
}
```

**Response (200 OK):** Шинэчлэгдсэн профайл JSON

---

### 2.5 DELETE /users/{id} — Профайл устгах

**Request:**
```
DELETE http://localhost:8081/users/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (204 No Content):** Амжилттай устгасан

---

### REST Entity: UserProfile

| Талбар  | Төрөл   | Тайлбар                    |
|---------|---------|---------------------------|
| id      | Long    | Auto-generated primary key |
| userId  | String  | Оюутны код (unique)        |
| name    | String  | Нэр                       |
| email   | String  | Имэйл                     |
| bio     | String  | Танилцуулга               |
| phone   | String  | Утасны дугаар             |

---

## 3. Authentication Flow — Баталгаажуулалтын урсгал

```
Хэрэглэгч (Browser/Postman)
    │
    ├─ 1. Register ──► SOAP Service (8080) ──► H2:authdb ──► "Амжилттай"
    │
    ├─ 2. Login ──────► SOAP Service (8080) ──► JWT token үүсгэнэ ──► Token буцаана
    │
    ├─ 3. GET /users ──► JSON Service (8081)
    │                      │
    │                      ├─ Interceptor: Authorization header-аас token авна
    │                      │
    │                      ├─ SOAP XML call ──► SOAP Service (8080) ValidateToken
    │                      │                      │
    │                      │                      └─ valid: true/false буцаана
    │                      │
    │                      ├─ valid=true  → REST handler руу дамжуулна → JSON data буцаана
    │                      └─ valid=false → 401 Unauthorized буцаана
    │
    └─ Token хугацаа дуусвал → Дахин Login хийх шаардлагатай (1 цаг)
```

---

## 4. JWT Token — Техникийн мэдээлэл (Bonus 3)

| Параметр       | Утга                    |
|----------------|------------------------|
| Algorithm      | HMAC-SHA256 (HS256)    |
| Хүчинтэй хугацаа | 1 цаг (3600000ms)    |
| Library        | JJWT 0.12.6            |
| Token бүтэц    | Header.Payload.Signature |

**JWT Payload жишээ:**
```json
{
  "sub": "khangarid",
  "iat": 1773348102,
  "exp": 1773351702
}
```

---

*МУИС — Үйлчилгээнд суурилсан архитектур — Лаб 06*
