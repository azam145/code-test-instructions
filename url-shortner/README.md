# URL Shortener Coding Task


## Task

Build a simple **URL shortener** in a ** preferably JVM-based language** (e.g. Java, Kotlin).

## Architectural Assumptions & Decisions:
Concurrency: I chose a "Database-First" approach for uniqueness. In a high-concurrency environment, I would consider a Distributed Lock (Redis) to reduce DB load during collision checks.
Base62 Encoding: I opted for Base62 ([a-zA-Z0-9]) to ensure URLs are URL-safe and visually distinct, avoiding the ambiguity of characters like 0 vs O or 1 vs l where possible.
Observability: I have included Spring Boot Actuator to provide /health and /info endpoints, which are essential for health checks in a containerized (Kubernetes) environment.
Separation of Concerns: The API interfaces are generated from the OpenAPI spec, ensuring the controller layer is strictly decoupled from the business logic in the Service layer.

## HLD - High level design decisions

1. Storage: Why Relational (Postgres) over NoSQL?
   Decision: Use a Relational Database (PostgreSQL).
   The Reasoning: A URL shortener requires Strong Consistency for custom aliases. If two users try to claim my-brand at the exact same millisecond, a NoSQL store with eventual consistency might allow both, leading to a collision. 
   Postgres’s ACID compliance and UNIQUE constraints prevent this.
   Trade-off: NoSQL (like DynamoDB) scales better horizontally, but for this requirement, the integrity of custom aliases is paramount.

2. Scaling Strategy: Read vs. Write
   Decision: Architect for a "Read-Heavy" workload.
   The Reasoning: In a real-world scenario, a URL is created once but read millions of times.
   Future Proofing: I would eventually add a Caching Layer (Redis). Because the mapping of alias -> original_url is immutable (once created, it rarely changes), the cache hit rate would be nearly 99%, taking the load off the database.

3. API Contract Enforcement
   Decision: "Contract-First" development using the provided openapi.yaml.
   The Reasoning: By generating interfaces, we ensure the backend and frontend stay in sync. It also allows for Parallel Development—the frontend team can build against a mock server based on the same YAML while the backend is being built.

### Future Considerations

Caching: "For production traffic, I would implement Redis to cache alias lookups."
Observability: "I would integrate OpenTelemetry for distributed tracing across the redirect flow."

## Low-Level Design (LLD) Decisions

1. Alias Generation: Random vs. Sequential
    Decision: Secure Random Base62 strings.
    The Reasoning: Sequential (ID-based): If I just convert Database ID 1, 2, 3 to Base62, My URLs are predictable (e.g., myapp.com/1, myapp.com/2). A competitor could scrape all my data by just incrementing the URL.
    Random: Using a SecureRandom Base62 string makes URLs unguessable.
    Trade-off: Random generation can lead to collisions. Must implement a "check-and-retry" loop or rely on a DB constraint to handle the rare case where the same random string is generated twice.

2. Redirect Status Codes: 301 vs. 302
   Decision: Use 302 (Found) or 307 (Temporary Redirect).
   The Reasoning:
   A 301 (Permanent) redirect is cached by browsers. If you ever want to change the destination URL or collect analytics for every click, a 301 will prevent you from seeing subsequent clicks from the same user.
   A 302/307 ensures the browser hits your server every time, allowing for Real-time Analytics.

3. Handling "Custom Alias" Conflicts
   Decision: Transparent Error Mapping.
   The Reasoning: When a user provides a custom alias that exists, the system shouldn't just throw a 500 error. It should return a 409 Conflict. 
   This is an LLD decision that improves the User Experience by allowing the frontend to say, "Sorry, that name is taken."
4. Validation Strategy
   Decision: Multilayered Validation.
   The Reasoning:
   - Syntax: Use @URL validation to ensure the input is a valid web address.
   - Security: Sanitize the "Custom Alias" to prevent XSS or Path Traversal (e.g., a user trying to set an alias as ../../etc/passwd).
   - Length: Limit the original URL length (e.g., 2048 chars) to prevent "Database Bloat" attacks. 


## Implimnetation decsiion
For this assessment, I've used hibernate.ddl-auto=update for rapid prototyping. However, in a production-grade system, I would implement Flyway or Liquibase. 
This allows us to have version-controlled SQL migration scripts, ensuring that database changes are predictable, auditable, and safe across different environments (Dev, QA, Prod).

## High Availability and Fault Tolerance

Multiple Availability Zones on AWS (Europe/America)

  - Load Balancer (ALB): Sits at the front. It receives the request and sends it to a healthy instance of your app in any AZ.
  - App Servers (Spring Boot): You run at least two instances of your container—one in AZ-A and one in AZ-B.
  - CDN (content delivery network)  caching closest to the user.
  - Database (RDS Multi-AZ): Synchronization: Data is synchronously replicated at the storage layer for Multi-AZ (High Availability) and asynchronously replicated for Cross-Region 
    (Disaster Recovery). To comply with GDPR, I would implement Regional Data Sharding to ensure European citizen data remains within the EU jurisdiction while US data is stored in North America.


