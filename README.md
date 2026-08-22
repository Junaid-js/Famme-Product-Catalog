# Famme Product Catalog

A small fullstack product catalog built for the RESP developer assignment. The application imports up to 50 products from Famme into PostgreSQL and manages them with server-rendered Thymeleaf, HTMX, and Web Awesome.

## Stack

- Spring Boot 4.1.0 and Kotlin 2.4.10
- Gradle 9.7.0
- JDK 26 runtime with JVM 25 compilation target
- PostgreSQL 18 and Flyway
- Spring `JdbcClient` and `RestClient`
- Thymeleaf, HTMX 4 beta, and Web Awesome 3.11.0

## Run locally

Requirements: JDK 26 and Docker.

```shell
docker compose up -d
./gradlew bootRun
```

Open [http://localhost:8080/product](http://localhost:8080/product).

The scheduled importer runs immediately at startup. It inserts missing Famme products and leaves existing or manually edited products unchanged.

## Tooling

This was built entirely in IntelliJ IDEA Ultimate (EAP channel), using the built-in tooling rather than external apps, as required by the assignment:

- **Database** — connect with IntelliJ's built-in database client (Database tool window → New Data Source → PostgreSQL): `localhost:55432`, database `famme_catalog`, user/password `famme`/`famme` (see `compose.yml` and `application.yml`). No pgAdmin or DBeaver involved.
- **Git** — all commits went through IntelliJ's Git tool window (Commit panel, Push, log/diff viewers), not a separate GUI client.
- **HTTP requests** — see [http/products.http](http/products.http), run directly from IntelliJ's HTTP Client and checked into the repo instead of a Postman collection.
- **JVM/Gradle** — project runs on JDK 26 with Kotlin/Java bytecode targeting JVM 25 (see `build.gradle.kts`), and the Gradle wrapper is pinned to 9.7.0. Project Structure settings show this if you want to confirm it directly.

## Structure

The application deliberately uses a small product feature package, a Famme integration package, and one database table:

- `FammeProductClient` reads the source with `RestClient`.
- `ProductImportJob` starts the importer with `@Scheduled(initialDelay = 0)`.
- `ProductService` owns product behavior and transaction boundaries.
- `ProductRepository` contains all PostgreSQL queries using `JdbcClient`.
- `ProductController` returns complete Thymeleaf pages or small HTMX fragments.
- Product variants are stored as a small JSONB projection, can be added with a manual product, and expand through an HTMX fragment when requested.
- A shared side navigation separates the overview, add-product, and active-search workflows without introducing a client-side router.

The detailed delivery checklist is in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). IntelliJ HTTP Client requests are in [http/products.http](http/products.http).

## AI-assisted feature review

The active search, edit flow, delete confirmation, variant viewer, and product-type filter were implemented with Codex. The variant viewer is the chosen “one more feature”; the type filter is an additional small improvement. Review them one at a time before recording the Loom:

| Feature | Generated approach | What to review manually |
| --- | --- | --- |
| Active search | Debounced HTMX request returning the table fragment | Confirm clearing the query restores all rows and stale requests cannot replace newer results |
| Product editing | Dedicated page with an HTMX `PUT` that can append new variants | Confirm validation keeps rejected values and existing variants remain unchanged |
| Product deletion | Native `<dialog>` with an HTMX `DELETE` | Confirm cancel makes no request and deletion refreshes only the table |
| Variant entry and viewer | Repeatable create-form fields save variants into JSONB; a chevron loads them inline through HTMX | Confirm add/remove fields, validation, collapse behavior, and manually entered variants |
| Product-type filter | Web Awesome select combined with title search | Confirm the selected type is retained after filtering and deletion |

Add a small follow-up commit from IntelliJ for any improvement you make after reviewing the generated diff. Do not present this table as completed manual review until you have performed it yourself.

## Loom checklist

- Project Structure showing JDK 26 / target JVM 25, and the Gradle JVM used to run the build.
- The `products` table in IntelliJ's built-in database client, not an external tool.
- The scheduled import log on startup (`initialDelay = 0`).
- Load products, add a product, search-as-you-type, edit, delete with the confirmation dialog, and the variant viewer, all without a full page reload.
- The `.http` requests running from IntelliJ's HTTP Client.
- Git history in IntelliJ's Git tool window, including the commits made while reviewing each AI-generated feature.
