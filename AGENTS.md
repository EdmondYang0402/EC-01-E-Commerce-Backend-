# EC-01 Project Guide

## Purpose and scope

EC-01 is a small, readable e-commerce project for learning and demonstrating Java backend engineering. Prefer correct business models, clear code, and conventional layering over breadth or elaborate abstractions.

The first-stage domain is limited to `User`, `Product`, `Sku`, `CartItem`, `Order`, and `OrderItem`. Do not add coupons, flash sales, payment, MQ, Elasticsearch, Spring Cloud, microservices, a separate inventory module, or other large infrastructure unless explicitly requested. Inventory belongs to `Sku.stock` in this stage.

## Repository layout

- `backend/` — Java 21 Spring Boot application (`com.ec01`), Maven, MyBatis, MySQL schema, Redis-backed login sessions, and JWT authentication.
- `backend/src/main/resources/db/schema.sql` — authoritative local schema for the six core tables. Inspect it before choosing entity fields or SQL.
- `backend/src/main/resources/mapper/` — MyBatis XML files when the established implementation style calls for XML.
- `frontend/` — Vue 3 + Vite storefront. Keep frontend work separate from backend changes unless the task crosses the API boundary.
- `README.md` — local startup commands.

## Domain rules

- `Product` is the product concept; `Sku` is the purchasable specification. Price and stock belong to SKU; do not merge the two.
- Relationships: Product–SKU is 1:N; User–CartItem is 1:N; SKU–CartItem is 1:N; User–Order is 1:N; Order–OrderItem is 1:N; SKU–OrderItem is 1:N.
- An `OrderItem` must retain purchase-time product/SKU snapshot data (name, specification, price, quantity, subtotal) so historical orders do not change when a product changes.
- When implementing an order, keep stock check, stock deduction, order creation, and order-item creation in one transaction. Address concurrency only when the order flow is being implemented; start with an appropriate database-level conditional update rather than distributed inventory design.

## Backend conventions

- Follow `Controller -> Service -> Mapper -> MySQL`. Controllers bind DTOs, validate input, invoke services, and return `Result`; they do not contain business logic.
- Services express business behavior, validate state/ownership, coordinate mappers, and own transactions. Mappers only handle persistence.
- Use DTOs for request input, entities for persistence, and VOs for responses when those boundaries differ. Do not expose entities merely for convenience, and do not create empty DTO/VO layers without a concrete boundary.
- Use Bean Validation on DTOs for basic format/range checks. Keep resource existence, ownership, purchaseability, and stock checks in services.
- Use clear names such as `selectById`, `selectPage`, `getById`, `addToCart`, and `submitOrder`.
- Use constructor injection. Do not use field `@Autowired`.
- Raise the existing business exception type for expected business failures; do not use plain `RuntimeException` for them.

## Authentication and security

- The current backend already has JWT utilities, a `JwtInterceptor`, `UserContext`, and Redis-backed login sessions. Reuse and extend the existing approach; do not assume a different project's security design.
- For Cart, Order, and user profile operations, derive identity from the authenticated server-side context. Never trust a client-supplied `userId` for authorization.
- Never store passwords in plaintext, return sensitive fields, log passwords/tokens, or concatenate SQL. Keep SQL parameterized.
- `application.yml` requires `JWT_SECRET` and configures Redis through environment-overridable values. Do not add fallback secrets.

## SQL and performance

- Follow the existing MyBatis style in the touched module. Keep SQL readable, parameterized, and indexed where the schema supports it.
- Avoid obvious N+1 access, load related records in batches where appropriate, and paginate list queries instead of loading everything.
- For database changes, modify `schema.sql` with explicit, reviewable SQL and align entities, mappers, and affected API contracts.

## Working rules

1. Read the real affected code, schema, and configuration before editing.
2. Make the smallest change that satisfies the task; do not refactor unrelated code or invent requirements.
3. Design in this order: entities and relationships, schema, minimum business flow, service boundary, then controller/API.
4. For cross-module or schema changes, state affected contracts and provide migration SQL when applicable.
5. Run the relevant build or tests after changes. Report verification failures honestly.

Useful commands:

```powershell
cd backend; .\mvnw.cmd test
cd frontend; npm run build
```

## Current development focus

Prioritize clarifying and implementing the Product + SKU model (product detail, SKU price, and stock) before expanding Cart or Order behavior. Keep the minimum commerce flow readable: authentication, browsing Product/SKU, cart updates, transactional order submission, and order viewing.

Keep this file limited to durable project conventions. Do not record one-off bugs, temporary API details, test data, or transient tasks here.
