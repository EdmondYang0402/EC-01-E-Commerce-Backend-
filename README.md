# EC-01

This repository contains a Spring Boot backend and a Vue/Vite frontend.

## Project layout

- `backend/` — Spring Boot API and the database schema.
- `frontend/` — Vue storefront.

## Run locally

Start the backend from `backend`:

```powershell
.\mvnw.cmd spring-boot:run
```

Start the frontend from `frontend`:

```powershell
npm run dev
```

## Import demo catalog data

The catalog importer reads up to 100 development-only products from
[DummyJSON](https://dummyjson.com/), copies each cover image to OSS, and inserts one
`product` plus one `sku` row for every source product. Existing rows are never
deleted, and a deterministic SKU code makes reruns idempotent.

Set these environment variables before running it:

```powershell
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = '<your-local-password>'
$env:OSS_ACCESS_KEY_ID = '<your-access-key-id>'
$env:OSS_ACCESS_KEY_SECRET = '<your-access-key-secret>'
```

Run the one-off importer from `backend`:

```powershell
.\mvnw.cmd -DskipTests compile exec:java "-Dexec.mainClass=com.ec01.catalogimport.CatalogImportApplication"
```

Optional variables are `CATALOG_IMPORT_LIMIT` (1–100),
`CATALOG_IMPORT_OBJECT_PREFIX`, `CATALOG_IMPORT_SOURCE_URL`, and
`CATALOG_IMPORT_CURRENCY_MULTIPLIER`.
