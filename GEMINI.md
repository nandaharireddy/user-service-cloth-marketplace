# CLAUDE.md — Build Instructions for Cloth Marketplace

This file governs how code is written across every repo in this project
(`eureka-server`, `api-gateway`, `user-service`, `catalog-service`, `cart-service`,
`order-service`, `payment-service`, `vendor-service`, `frontend`). Copy this file into
the root of each backend repo. Refer to `PROJECT_PLAN.md`, `schema.sql`,
`api-design.md`, and `auth-and-security.md` for what to build; this file governs *how*
to build it.

---

## 1. Architecture: 4-layer pattern (mandatory, every service)

Every backend service follows this exact layering — no layer may be skipped, and no
layer may call "downward" past its neighbor (e.g. a Controller must never call a DAO
or Repository directly):

```
Controller  →  Service  →  DAO  →  Repository
```

- **Controller** (`controller` package): HTTP layer only. Maps requests to DTOs,
  calls a Service method, returns a response. No business logic, no direct
  entity/database references.
- **Service** (`service` package): business logic and orchestration. Talks to DAOs
  (never to Repositories directly) and to other services (via REST client, resolved
  through Eureka). Transaction boundaries (`@Transactional`) belong here.
- **DAO** (`dao` package): **this is where repository query calls are actually made
  and composed.** Repository interfaces stay thin (Spring Data JPA method signatures
  only); the DAO class injects the Repository and is responsible for calling it,
  combining multiple repository calls when a single data-access operation needs more
  than one query, and translating results before handing them to the Service layer.
  The Service layer should never see a Spring Data `Repository` type directly — it
  only depends on the DAO interface.
- **Repository** (`repository` package): Spring Data JPA interfaces
  (`extends JpaRepository<Entity, Id>`), method-name or `@Query`-based queries only.
  No logic here beyond query declarations.

Each package should have an interface + implementation split for Service and DAO
(e.g. `ProductService` / `ProductServiceImpl`, `ProductDao` / `ProductDaoImpl`) so
components depend on abstractions, not concrete classes — this is what keeps the
codebase testable and swappable as it scales.

## 2. Scalability rules

- **Stateless services.** No in-memory session state, no local caching of
  request-scoped data that would break if a service ran as multiple instances behind
  a load balancer. Any caching (e.g. product catalog reads) must use an
  externalized cache (Redis, added when actually needed — not required for MVP).
- **Pagination on every list endpoint.** Never return an unbounded list — use
  `Pageable`/`Page<T>` from Spring Data on every `GET` collection endpoint
  (`/products`, `/orders`, etc.).
- **Avoid N+1 queries.** Use `@EntityGraph` or explicit `JOIN FETCH` in DAO query
  methods when a Service call is known to need related entities, instead of letting
  Hibernate lazy-load them one row at a time in a loop.
- **DTOs at every service boundary.** Controllers never accept or return JPA
  `@Entity` classes directly — always map to/from a dedicated request/response DTO.
  This decouples the API contract from the database schema, which matters a lot once
  multiple services and a frontend all depend on that contract.
- **Idempotency on write endpoints that matter.** `POST /orders` and
  `POST /payments/initiate` should accept an idempotency key so a retried request
  (network blip, double-click) doesn't create duplicate orders/charges.

## 3. Reducing redundant code

- **Shared code belongs in a common library, not copy-pasted.** Cross-cutting things
  every service needs — the JWT validation filter, standard error response DTO,
  common exception classes, logging config — should live in a small shared Maven
  artifact (e.g. `common-lib`) that each service depends on, rather than being
  re-written per service. Flag this to the user before creating it, since it adds a
  9th repo/artifact to publish and version.
- **One mapping layer.** Use MapStruct (or a single consistent manual mapper pattern)
  for Entity ↔ DTO conversion — don't hand-write ad hoc mapping code differently in
  every service.
- **Centralized exception handling.** One `@ControllerAdvice` /
  `@ExceptionHandler` class per service, returning a consistent error response shape
  (`{ "timestamp", "status", "error", "message", "path" }`) — don't scatter
  try/catch blocks with ad hoc error responses across controllers.
- **Constants and enums, not magic strings.** Role names, order statuses, payment
  statuses, etc. (already defined as Postgres ENUMs in `schema.sql`) should be Java
  enums, referenced everywhere — never hardcoded strings like `"CONFIRMED"`.

## 4. Comments and documentation (mandatory)

- **Every class** gets a Javadoc block above it explaining its responsibility —
  one or two sentences, e.g. `/** Handles product catalog queries: search, filter, and lookup by id or vendor. */`
- **Every public method** (Controller, Service, DAO, Repository) gets a Javadoc block
  describing what it does, its parameters, and its return value. Example:
  ```java
  /**
   * Retrieves a paginated list of active products for a given category.
   *
   * @param categoryId the category to filter by
   * @param pageable   pagination and sort parameters
   * @return a page of product summaries
   */
  Page<ProductSummaryDto> getProductsByCategory(Long categoryId, Pageable pageable);
  ```
- Inline comments (`//`) are for *why*, not *what* — only add them where logic isn't
  self-explanatory from the code itself (e.g. explaining a non-obvious business rule
  like "orders split by vendor because each vendor fulfills independently").
- Do not comment trivial getters/setters/constructors generated by Lombok.

## 5. API documentation (mandatory, every service)

- Every service includes `springdoc-openapi-starter-webmvc-ui` and exposes Swagger UI
  at `/swagger-ui.html`.
- Every Controller class gets `@Tag(name = ..., description = ...)`.
- Every endpoint gets `@Operation(summary = ..., description = ...)` and documents
  its response codes with `@ApiResponse` (200, 400, 401, 403, 404, 500 as applicable).
- Every request/response DTO field gets a `@Schema(description = ...)` annotation.
- `api-gateway` should aggregate/link to each service's Swagger docs so there's one
  discoverable place to browse the whole API surface, not 6 separate URLs to know
  about.

## 6. Other required practices for an optimized, production-ready build

- **Input validation** via Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.) on
  every request DTO — reject bad input at the Controller boundary, don't let it reach
  the Service layer.
- **Logging, not `System.out.println`.** Use SLF4J (`private static final Logger log
  = LoggerFactory.getLogger(...)`). Log at appropriate levels — `info` for business
  events (order placed, payment confirmed), `debug` for detailed flow, `error` for
  failures with stack traces. Never log passwords, tokens, or full card details (see
  `auth-and-security.md`).
- **Configuration externalized**, never hardcoded. DB credentials, JWT secret,
  Razorpay keys, Eureka URL all come from `application.yml` + environment variables,
  with a `.env.example` documenting required variables per service.
- **Consistent naming.** REST resources plural and lowercase (`/products`, not
  `/Product` or `/getProduct`). Java classes PascalCase, methods/variables camelCase,
  DB columns snake_case (already reflected in `schema.sql`).
- **Unit tests for Service and DAO layers**, at minimum for core business logic (order
  splitting by vendor, price calculation, stock validation). Integration tests for
  Controllers using `@SpringBootTest` + Testcontainers (Postgres) where practical.
- **Health checks.** Every service exposes Spring Boot Actuator's `/actuator/health`
  — Eureka and Docker Compose both benefit from this for readiness checks.
- **Database migrations via Flyway or Liquibase**, not `hibernate.ddl-auto: update`.
  Each service's schema changes should be versioned SQL migration files, so the
  schema history is explicit and repeatable — this matters even more in microservices
  since each service owns its schema independently.
- **Correlation IDs for tracing.** Since a single user action can span multiple
  services (e.g. checkout touches cart-service, order-service, payment-service), pass
  a correlation/request ID through service-to-service calls and include it in every
  log line — without this, debugging a cross-service issue later becomes very hard.

## 7. SOLID principles — apply explicitly, not just by convention

The 4-layer structure above already leans on SOLID, but apply these deliberately in
every class you write, not just as a side effect of the layering:

- **Single Responsibility.** A class should have one reason to change. A Controller
  handles HTTP concerns only; a Service handles one area of business logic (don't let
  `OrderService` also contain payment-calculation logic — that belongs in
  `PaymentService` or a dedicated `PricingService`); a DAO handles data access for
  one aggregate/entity family, not a grab-bag of unrelated queries.
- **Open/Closed.** Classes should be open for extension, closed for modification.
  Favor adding a new implementation over editing an existing one to add a special
  case — e.g. if payment methods grow beyond Razorpay later, introduce a
  `PaymentProvider` interface with `RazorpayPaymentProvider` implementing it, rather
  than adding `if (provider == "razorpay")` branches inside one class.
- **Liskov Substitution.** Any implementation of an interface (`ProductDaoImpl`,
  a future `MockProductDao` in tests, a future `CachedProductDao`) must be fully
  substitutable for the interface without breaking callers — don't have an
  implementation silently narrow the contract (e.g. throwing on inputs the interface
  contract says are valid).
- **Interface Segregation.** Keep Service/DAO interfaces focused on what their
  callers actually need — don't force `CartService` to depend on a giant
  `CatalogOperations` interface with 20 methods when it only needs
  `getProductVariant(id)`. Split interfaces by consumer need, not by convenience.
- **Dependency Inversion.** Already structurally enforced by the interface+impl split
  in section 1 — Services depend on `XxxDao` interfaces, not `XxxDaoImpl` classes;
  Controllers depend on `XxxService` interfaces, not implementations. Always inject
  interfaces via constructor injection (never field injection with `@Autowired` on a
  field — use `@RequiredArgsConstructor` from Lombok with `final` fields), so
  dependencies are explicit and mockable in tests.

## 8. What NOT to do

- Don't let Controllers touch entities or repositories directly (see layering rule).
- Don't put business logic in DAO classes — DAOs compose and execute queries, they
  don't decide business rules.
- Don't hardcode other services' URLs — always resolve via Eureka
  (`http://catalog-service/...`, not `http://localhost:8082/...`).
- Don't skip the shared error-response format "just this once" — consistency across
  8 services is what makes the frontend integration manageable.
- Don't build features marked as deferred in `PROJECT_PLAN.md` (coupons, returns,
  B2B bulk ordering, RabbitMQ, Kubernetes) unless explicitly instructed — stay in
  scope for the current build phase.
- Don't violate SOLID for convenience — e.g. don't add a second responsibility to an
  existing Service class just to avoid creating a new one; don't use field injection
  as a shortcut past constructor injection.
