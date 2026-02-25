# Bayrak Bank — Proje Dokümanı

## Genel Bakis

**Bayrak Bank**, gercek bir banka uygulamasinin temel fonksiyonlarini kapsayan, full-stack bir web uygulamasidir.  
Kullanicilarin hesap actirabilecegi, para transferi yapabilecegi ve bildirim alabildigi guvenli bir dijital bankacilik platformu hedeflenmektedir.

---

## Teknoloji Yigini

| Katman      | Teknoloji                         |
|-------------|-----------------------------------|
| Backend     | Java 21 + Spring Boot 3.x         |
| Frontend    | React 19 + Vite + TypeScript      |
| Veritabani  | PostgreSQL 16                     |
| Cache/Queue | Redis (notification queue)        |
| Guvenlik    | Spring Security + JWT (Access + Refresh Token) |
| Build       | Maven (backend) / npm (frontend)  |

---

## Proje Yapisi

```
bayrak-bank/
├── BayrakBackend/
│   └── src/main/java/com/bayraktolga/BayrakBackend/
│       │
│       ├── entity/
│       │   ├── User.java
│       │   ├── Account.java
│       │   ├── Transaction.java
│       │   ├── Notification.java
│       │   └── RefreshToken.java
│       │
│       ├── enums/
│       │   ├── Role.java
│       │   ├── AccountStatus.java
│       │   ├── AccountType.java
│       │   ├── TransactionType.java
│       │   ├── TransactionStatus.java
│       │   └── NotificationType.java
│       │
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── AccountRepository.java
│       │   ├── TransactionRepository.java
│       │   ├── NotificationRepository.java
│       │   └── RefreshTokenRepository.java
│       │
│       ├── dto/
│       │   ├── request/
│       │   │   ├── RegisterRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── RefreshTokenRequest.java
│       │   │   ├── UpdateProfileRequest.java
│       │   │   ├── ChangePasswordRequest.java
│       │   │   ├── CreateAccountRequest.java
│       │   │   └── TransferRequest.java
│       │   └── response/
│       │       ├── AuthResponse.java
│       │       ├── UserResponse.java
│       │       ├── AccountResponse.java
│       │       ├── TransactionResponse.java
│       │       ├── NotificationResponse.java
│       │       └── PageResponse.java
│       │
│       ├── service/
│       │   ├── AuthService.java
│       │   ├── UserService.java
│       │   ├── AccountService.java
│       │   ├── TransactionService.java
│       │   ├── NotificationService.java
│       │   ├── EmailService.java
│       │   └── RefreshTokenService.java
│       │
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   ├── AccountController.java
│       │   ├── TransactionController.java
│       │   └── NotificationController.java
│       │
│       ├── security/
│       │   ├── JwtUtil.java
│       │   ├── JwtAuthenticationFilter.java
│       │   └── CustomUserDetailsService.java
│       │
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── CorsConfig.java
│       │   └── MailConfig.java
│       │
│       ├── event/
│       │   ├── AppEvent.java
│       │   ├── EventPublisher.java
│       │   ├── EventConsumer.java
│       │   ├── EventHandler.java
│       │   ├── NotificationEvent.java
│       │   └── NotificationHandler.java
│       │
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── ResourceNotFoundException.java
│       │   ├── InsufficientBalanceException.java
│       │   ├── AccountBlockedException.java
│       │   └── ErrorResponse.java
│       │
│       └── util/
│           ├── IbanGenerator.java
│           ├── AccountNumberGenerator.java
│           └── ReferenceNumberGenerator.java
│
└── web/                    # React + Vite frontend
    └── src/
        ├── pages/          # Sayfa bilesenler (Login, Dashboard, Transfer...)
        ├── components/     # Yeniden kullanilabilir UI bilesenleri
        ├── services/       # API cagrilari (axios)
        ├── store/          # State yonetimi
        ├── hooks/          # Custom React hooks
        ├── types/          # TypeScript tip tanimlari
        └── router/         # React Router tanimlari
```

---

## Moduller & Ozellikler

### 1. Kullanici Yonetimi
- Kayit (TC, ad-soyad, email, sifre, dogum tarihi)
- Giris (email + sifre)
- JWT Access Token (15 dk) + Refresh Token (7 gun) mekanizmasi
- Kullanici profil goruntuleme ve guncelleme
- Sifre degistirme
- Rol sistemi: `ROLE_USER`, `ROLE_ADMIN`

### 2. Hesap Yonetimi
- Hesap acma (vadesiz TL hesabi)
- IBAN otomatik olusturma (TR standardi)
- Hesap listesi goruntuleme
- Bakiye goruntuleme
- Hesap detay sayfasi
- Hesap durumu: AKTIF / PASIF / BLOKE

### 3. Para Transferi
- Hesaplar arasi (ic transfer)
- IBAN ile transfer (EFT/havale)
- Transfer gecmisi (islem tablosu)
- Islem referans numarasi olusturma
- Yetersiz bakiye kontrolu
- Ayni hesap transferi engeli

### 4. Bildirim Sistemi (Event-Driven)
- In-app bildirimler (veritabani tabanli)
- Email bildirimleri (Spring Mail / JavaMailSender)
  - Kayit onay maili
  - Transfer bildirim maili
  - Sifre degisiklik maili
- Bildirim listesi ve okundu isaretleme
- **Redis Queue ile async notification**: Event-driven mimari

#### Event-Driven Notification Mimarisi (Generic Pattern)
```
┌─────────────┐    ┌────────────────┐    ┌───────────┐    ┌───────────────┐    ┌─────────────┐
│   Service   │ -> │ EventPublisher │ -> │ Redis     │ -> │ EventConsumer │ -> │   Handler   │
│             │    │                │    │ Queue     │    │ (type'e göre) │    │ (xxxHandler)│
└─────────────┘    └────────────────┘    └───────────┘    └───────────────┘    └─────────────┘
```

**Generic Event Sınıfları:**

| Sınıf | İşlev |
|-------|-------|
| `AppEvent` | Generic event record (type, payload) |
| `EventPublisher` | Redis'e event yayınlar |
| `EventConsumer` | Event'leri dinler, type'a göre handler'a yönlendirir |
| `EventHandler` | Handler interface'i - tüm handler'lar bunu implement eder |

**Event Handler'lar:**

| Handler | Event Type | İşlev |
|---------|------------|-------|
| `NotificationHandler` | `NOTIFICATION` | Bildirim oluşturur, DB'ye kaydeder |

> **Yeni Handler Ekleme:** Sadece `EventHandler` implement edip Spring bean olarak kaydet yeterli. Consumer otomatik bulur.

**Kullanım:**
```java
eventPublisher.publish("NOTIFICATION", notificationEvent);
// veya
notificationService.publishNotification(userId, "Başlık", "Mesaj", NotificationType.INFO);
```

**Redis Konfigürasyonu (application.properties):**
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

#### Email Gönderme Akışı (Spring @Async)

```
Service (@Async) → TaskExecutor Thread Pool → JavaMailSender → SMTP (Gmail) → Kullanıcı
```

**Adımlar:**
1. Service `emailService.sendWelcomeEmail()` çağırır
2. `@Async` annotation'ı ile ayrı thread'te çalışır
3. Spring'in `TaskExecutor` pool'undan thread alır (core=2, max=5)
4. `JavaMailSender` ile Gmail SMTP'ye bağlanır
5. Email gönderilir

**EmailService Metodları:**
- `sendWelcomeEmail(toEmail, firstName)` - Kayıt sonrası hoş geldin
- `sendTransferEmail(toEmail, firstName, referenceNo, amount)` - Transfer bildirimi
- `sendPasswordChangedEmail(toEmail, firstName)` - Şifre değişikliği

#### Notification Event-Driven Akışı (Redis Queue)

```
Service → NotificationEvent → Redis Queue (RPUSH) → Consumer → Database
```

**Neden Event-Driven?**
- **Decoupling**: Üretici service ile tüketici ayrıdır
- **Async Processing**: Kullanıcı işlemi beklemez, notification queue'ya yazılır
- **Scalability**: Consumer sayısı artırılabilir
- **Reliability**: Redis'te mesaj kalıcıdır (restart olsa bile)

**Akış Detayı:**
1. `UserService.changePassword()` çağrılır
2. `notificationService.publishNotification()` → `NotificationQueuePublisher`
3. Event JSON olarak Redis'e yazılır: `RPUSH notification:queue '{"userId":"...","title":"...","message":"..."}'`
4. `NotificationConsumer` sürekli dinler: `LPOP notification:queue`
5. Event deserialize edilir, user bilgisi alınır
6. `NotificationService.createNotification()` → DB'ye kaydedilir

**Not:** Email için Redis kullanılmıyor, Spring'in `@Async` yeterli. Redis sadece notification için kullanılıyor.

---

## Veritabani Semalari

### users
| Sutun          | Tip           | Aciklama                    |
|----------------|---------------|-----------------------------|
| id             | UUID (PK)     | Benzersiz kullanici kimligi |
| tc_no          | VARCHAR(11)   | TC Kimlik No (unique)       |
| first_name     | VARCHAR(50)   |                             |
| last_name      | VARCHAR(50)   |                             |
| email          | VARCHAR(100)  | Unique, giris icin          |
| password_hash  | VARCHAR(255)  | BCrypt hash                 |
| phone          | VARCHAR(15)   |                             |
| birth_date     | DATE          |                             |
| role           | ENUM          | USER / ADMIN                |
| is_active      | BOOLEAN       | Hesap aktiflik durumu       |
| created_at     | TIMESTAMP     |                             |
| updated_at     | TIMESTAMP     |                             |

### accounts
| Sutun          | Tip           | Aciklama                    |
|----------------|---------------|-----------------------------|
| id             | UUID (PK)     |                             |
| user_id        | UUID (FK)     | users tablosuna referans    |
| iban           | VARCHAR(26)   | Unique, TR standardi        |
| account_no     | VARCHAR(16)   | Unique, dahili numara       |
| balance        | DECIMAL(15,2) | Bakiye                      |
| currency       | VARCHAR(3)    | TRY (varsayilan)            |
| status         | ENUM          | ACTIVE / PASSIVE / BLOCKED  |
| account_type   | ENUM          | CHECKING / SAVINGS          |
| created_at     | TIMESTAMP     |                             |

### transactions
| Sutun              | Tip           | Aciklama                       |
|--------------------|---------------|--------------------------------|
| id                 | UUID (PK)     |                                |
| reference_no       | VARCHAR(20)   | Unique referans no             |
| sender_account_id  | UUID (FK)     | Gonderen hesap                 |
| receiver_account_id| UUID (FK)     | Alici hesap                    |
| amount             | DECIMAL(15,2) | Transfer tutari                |
| currency           | VARCHAR(3)    | TRY                            |
| type               | ENUM          | TRANSFER / EFT / DEPOSIT       |
| status             | ENUM          | PENDING / COMPLETED / FAILED   |
| description        | VARCHAR(255)  | Aciklama                       |
| created_at         | TIMESTAMP     |                                |

### notifications
| Sutun        | Tip          | Aciklama                         |
|--------------|--------------|----------------------------------|
| id           | UUID (PK)    |                                  |
| user_id      | UUID (FK)    |                                  |
| title        | VARCHAR(100) | Bildirim basligi                 |
| message      | TEXT         | Bildirim icerigi                 |
| type         | ENUM         | TRANSFER / SECURITY / SYSTEM     |
| is_read      | BOOLEAN      | Okundu mu?                       |
| created_at   | TIMESTAMP    |                                  |

### refresh_tokens
| Sutun        | Tip          | Aciklama                         |
|--------------|--------------|----------------------------------|
| id           | UUID (PK)    |                                  |
| user_id      | UUID (FK)    |                                  |
| token        | TEXT         | Refresh token degeri             |
| expires_at   | TIMESTAMP    | Son kullanma tarihi              |
| is_revoked   | BOOLEAN      | Iptal edildi mi?                 |
| created_at   | TIMESTAMP    |                                  |

---

## API Endpoint'leri

### Auth
```
POST   /api/auth/register         # Kayit
POST   /api/auth/login            # Giris -> access + refresh token
POST   /api/auth/refresh          # Access token yenile
POST   /api/auth/logout           # Refresh token iptal
```

### Kullanici
```
GET    /api/users/me              # Profil bilgisi (auth gerekli)
PUT    /api/users/me              # Profil guncelleme
PUT    /api/users/me/password     # Sifre degistirme
```

### Hesap
```
GET    /api/accounts              # Kullanicinin hesaplari
POST   /api/accounts              # Yeni hesap ac
GET    /api/accounts/{id}         # Hesap detayi
GET    /api/accounts/{id}/balance # Bakiye
```

### Transfer
```
POST   /api/transactions/transfer # Para transferi
GET    /api/transactions          # Islem gecmisi (pagination)
GET    /api/transactions/{id}     # Islem detayi
```

### Bildirim
```
GET    /api/notifications         # Bildirimler
PUT    /api/notifications/{id}/read  # Okundu isaretleme
PUT    /api/notifications/read-all   # Tumunu okundu isaretleme
GET    /api/notifications/unread-count # Okunmamis sayi
```

---

## Guvenlik Mimarisi

```
Client
  |
  v
React App
  |-- JWT Access Token (Authorization: Bearer <token>)
  |
  v
Spring Security Filter Chain
  |-- JwtAuthenticationFilter (her istek icin token dogrula)
  |-- UsernamePasswordAuthenticationFilter (sadece /auth/login)
  |
  v
Controller -> Service -> Repository -> PostgreSQL
```

**Token Stratejisi:**
- Access Token: 15 dakika omurlu, her API istegiyle gonderilir
- Refresh Token: 7 gun omurlu, HttpOnly cookie olarak saklanir
- Token yenileme: `/api/auth/refresh` endpoint'i ile otomatik
- Sifre: BCrypt ile hashlenir (strength: 12)

---

## Frontend Sayfa Yapisi

```
/                   -> Yonlendirme (giris yapildiysa dashboard, degilse login)
/login              -> Giris sayfasi
/register           -> Kayit sayfasi
/dashboard          -> Ana sayfa (bakiye ozeti, son islemler)
/accounts           -> Hesap listesi
/accounts/:id       -> Hesap detayi + islem gecmisi
/transfer           -> Para transferi formu
/notifications      -> Bildirimler
/profile            -> Profil ve sifre degistirme
```

---

## Gelistirme Adim Plani

### Backend Adimlar
1. [ ] `pom.xml` bagimlilik guncelleme (JWT, Lombok, Validation)
2. [ ] Entity siniflari (User, Account, Transaction, Notification, RefreshToken)
3. [ ] Repository'ler (JPA)
4. [ ] JWT utility & filter
5. [ ] Security konfigurasyonu (SecurityFilterChain, CORS)
6. [ ] Auth servisi ve controller (register, login, refresh, logout)
7. [ ] Kullanici servisi ve controller
8. [ ] Hesap servisi ve controller (IBAN uretimi dahil)
9. [ ] Transfer servisi ve controller (@Transactional, bakiye kontrolu)
10. [ ] Bildirim servisi ve controller
11. [ ] Email servisi (JavaMailSender)
12. [ ] Global exception handler
13. [ ] DTO validation (@Valid, @NotBlank vb.)

### Frontend Adimlar
1. [ ] Paket kurulumu (axios, react-router, zustand veya context)
2. [ ] Router yapisi ve layout bilesenler
3. [ ] Login / Register sayfalari
4. [ ] Auth context + token yonetimi (axios interceptor)
5. [ ] Dashboard sayfasi
6. [ ] Hesap listeleme ve detay sayfasi
7. [ ] Transfer formu ve onay ekrani
8. [ ] Bildirim paneli
9. [ ] Profil sayfasi
10. [ ] Genel UI bilesenleri (Button, Input, Card, Modal, Toast)

---

## Ortam Degiskenleri

### Backend Configuration

**Profile Yapısı:**
- `application.properties` - Base config (JPA gibi ortak ayarlar)
- `application-dev.properties` - Local geliştirme (sensitiveler dahil, GitHub'a pushlanmaz)
- `application-prod.properties` - Production (environment variable'lar)

**Kullanım:**
```bash
# Development (default)
./mvnw spring-boot:run

# Production
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://... \
JWT_SECRET=... \
./mvnw spring-boot:run
```

**Environment Variables (Production):**
```bash
DB_URL, DB_USERNAME, DB_PASSWORD
JWT_SECRET
MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
```

### Frontend (`.env`)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## Kurulum

### Gereksinimler
- Java 21+
- Maven 3.8+
- Node.js 20+
- PostgreSQL 16
- Redis (notification queue için)
- (Opsiyonel) Docker

### Baslangic

**Backend:**
```bash
cd BayrakBackend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd web
npm install
npm run dev
```

**PostgreSQL veritabani olusturma:**
```sql
CREATE DATABASE bayrakbank;
```

**Redis (Docker ile):**
```bash
docker run -d -p 6379:6379 --name redis redis:alpine
```
