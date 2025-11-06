# s3-sync

A small Spring Boot service that detects changes in customer and order
data, exports changed rows as CSV files grouped by country, and uploads the
CSV files to an S3-compatible endpoint.

This repository contains the service implementation, a set of unit tests,
and integration tests that run against Testcontainers (Postgres + LocalStack).

## Features

- Detects changes in customer rows using a per-row SHA-256 row hash.
- Detects changes in orders using an order marker hash (lastchange field).
- Writes CSVs (UTF-8, LF) for customers (`kunde_<country>_YYYYMMDD_HH.csv`)
  and orders (`auftraege_<country>_YYYYMMDD_HH.csv`).
- Uploads CSV bytes to S3 and stores tracking entries to avoid re-uploading
  unchanged rows.

## Quickstart (development)

Prerequisites:

- Java 17+
- Maven 3.6+ (or use the included `./mvnw` wrapper)
- Docker (for running integration tests with Testcontainers / LocalStack)

Run unit tests:

```bash
./mvnw test
```

Run integration tests (requires Docker):

```bash
./mvnw -DskipUnitTests=false verify -DskipITs=false
```

If you prefer to run a single integration test class locally, use:

```bash
./mvnw -Dtest=com.example.s3sync.integration.OnlyCustomerIT test
```

## Configuration

Application configuration is in `src/main/resources/application.properties`.
The following properties are relevant for S3 and runtime behavior:

- `aws.region` — AWS region (default used for the AWS SDK)
- `aws.s3.endpoint` — Optional S3 endpoint override (used for LocalStack)
- `aws.bucket` — Target S3 bucket name (tests often use `test-bucket`)

## How it works (high level)

1. A scheduled job calls the sync job.
2. The service computes hashes for customers and marker hashes for orders.
3. It compares hashes against persisted tracking entries in the DB.
4. Unsynced rows are mapped to CSV DTOs and grouped by country.
5. CSVs are rendered and uploaded to S3; tracking entries are persisted
   transactionally so uploads run only after successful DB commit.

## Developer notes

- CSV rendering uses Apache Commons CSV and produces UTF-8 encoded bytes.
- The S3 client uses AWS SDK v2. Integration tests use LocalStack via
  Testcontainers to provide a fast, isolated S3-compatible endpoint.
- Tracking entities are `SyncedCustomerHash` and `SyncedOrderHash`.

## Running locally with Docker Compose

This project includes a `compose.yaml` that starts a PostgreSQL database
and a LocalStack container pre-populated with test data. The containers use
named volumes, so data persists across container restarts.

Note: the S3 bucket used by LocalStack in this repository is created by the
helper script `init.sh` and is named `s3sync`.

1. Start the infrastructure (Postgres + LocalStack):

```bash
docker compose up -d
```

2. Verify the containers are running:

```bash
docker compose ps
```

3. The compose setup persists data using Docker volumes. To inspect or
   remove the persisted volumes use `docker compose down -v` (this will delete
   the volumes and their data).

4. Run the Spring Boot application pointing it at the compose services.
   You can directly run it with Maven.

- Run with Maven (hot reload / dev):

```bash
./mvnw spring-boot:run
```

### Inspecting files stored in LocalStack S3

When running the compose stack the LocalStack container exposes a small
CLI wrapper `awslocal` that you can call from inside the container to
interact with the emulated AWS services. Example usage (replace
`<bucket>`, `<country>` and `<YYYYMMDD_HH>` with your values):

```bash
# list objects in the bucket
docker exec -it localstack-s3 awslocal s3 ls s3://<bucket>

# copy a CSV from S3 inside the container to a temporary path
docker exec -it localstack-s3 awslocal s3 cp s3://<bucket>/kunde_<country>_<YYYYMMDD_HH>.csv /tmp/customer.csv

# copy the file from the container to your host
docker cp localstack-s3:/tmp/customer.csv .
```

Notes:

- The compose service name for LocalStack may be `localstack-s3` (the
  container id used in the examples). Adjust the container name if your
  compose setup uses a different service/container name.
- `awslocal` is a convenience wrapper around the AWS CLI preinstalled in
  the LocalStack container. It forwards calls to the LocalStack endpoints.

If you want to connect the application to a real S3 bucket (production),
start the app with the `prod` profile so the `application-prod.properties`
values are used and set the profile and bucket name accordingly. The
production properties include `aws.profile` and `aws.s3.bucket` which you
should configure for your AWS account and target bucket.

Example (run with the prod profile and explicit bucket):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Also ensure your AWS credentials are available for the intended profile.

**VERY IMPORTANT: Using a real S3 Bucket is not tested!**

## Cleanup

- Stop containers without removing volumes:

```bash
docker compose down
```

- Stop containers and remove volumes (data will be lost):

```bash
docker compose down -v
```
