# Famme Product Catalog — Implementation Plan

## Goal

Build a small Spring Boot application that imports up to 50 products from Famme into PostgreSQL and manages them through server-rendered Thymeleaf pages enhanced with HTMX and Web Awesome.

## Guiding decisions

- Keep one Spring Boot module and one `products` table.
- Use `JdbcClient` for all database access.
- Store the small variant projection as `jsonb`; variants are read together and manual variants can be saved or appended with their product.
- Treat the scheduled Famme job as an idempotent importer, so later runs do not overwrite local edits.
- Keep browser state minimal: controllers return Thymeleaf pages or fragments and HTMX swaps server-rendered HTML.
- Use Web Awesome components, utilities, and design tokens before adding custom CSS.
- Avoid unnecessary interfaces, custom exceptions, frontend frameworks, and abstractions.

## Delivery steps

1. Scaffold the current Spring Boot, Kotlin, Gradle, PostgreSQL, Flyway, Thymeleaf, and HTMX stack.
2. Add a Flyway migration and Docker Compose PostgreSQL service.
3. Fetch Famme products with `RestClient` from `https://famme.no/products.json`.
4. Import at startup with `@Scheduled(initialDelay = 0)` and save no more than 50 products.
5. Build an overview with an HTMX “Load products” action, plus a focused add-product page.
6. Add the AI-assisted features in small, reviewable changes:
   - active title search;
   - product editing;
   - deletion through a native `<dialog>` confirmation;
   - an HTMX variant viewer as the chosen extra feature;
   - product-type filtering as an additional small improvement.
7. Add IntelliJ HTTP Client requests and concise setup/AI-review notes.
8. Build, start, and verify the complete workflow against PostgreSQL.

## Data model

`products` contains an internal integer identity, nullable unique Famme ID, title, vendor, product type, and a JSONB array containing variant title, price, SKU, and the source ID when imported.

## Verification checklist

- Flyway creates the schema on an empty PostgreSQL database.
- Startup import saves at most 50 products and a restart creates no duplicates.
- Load, add, search, edit, delete, type-filter, variant-entry, and variant-view actions work.
- Overview, add, and search pages are available from the shared side navigation.
- Overview exposes an explicit “Load products” action; search loads current products immediately.
- HTMX actions update only the intended page region without a full reload.
- Delete requires a native dialog confirmation.
- The table is responsive and uses Web Awesome components/tokens.
- `products.http` exercises the server endpoints.
- The Gradle build succeeds on JDK 26 while targeting JVM 25 bytecode.

## Explicitly out of scope

Authentication, pagination, editing or deleting individual variants, a JavaScript framework, deployment infrastructure, and unrelated API integrations.
