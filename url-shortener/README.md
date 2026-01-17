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
   Observation: The provided openapi.yaml lacked formal validation constraints (patterns, min/max lengths).
   Decision: Multilayered Validation.

   - Controller - using spring-boot-starter-validation - this would be done with additions in open api - validates format/syntax (cheap, fail-fast)
   - Service Layer: validates business rules (already exists, permissions, etc.) and protect against "Database Bloat" by capping input lengths.
     As it is not possible to validate in controller as I dont want to modify open-api contract I will validation in service layer.
   - Frontend: Implemented regex-based sanitization for custom aliases to prevent path-traversal attempts and improve UX.
   - Outcome: This ensures the system remains robust and secure even if the API contract is loosely defined.
   - Syntax: Use @URL validation to ensure the input is a valid web address.
   - Security: Sanitize the "Custom Alias" to prevent XSS or Path Traversal (e.g., a user trying to set an alias as ../../etc/passwd).
   - Length: Limit the original URL length (e.g., 2048 chars) to prevent "Database Bloat" attacks. 


## Implimnetation decsion
For this assessment, I've used hibernate.ddl-auto=update for rapid prototyping. However, in a production-grade system, I would implement Flyway or Liquibase. 
This allows us to have version-controlled SQL migration scripts, ensuring that database changes are predictable, auditable, and safe across different environments (Dev, QA, Prod).

## How to Run Backend
1. git clone
2. docker compose up
3. Profit.

## How to Run the Whole Stack
docker compose up --build
Open http://localhost:5173 for the UI.
Open http://localhost:8080/swagger-ui/index.html for the API docs.

## Example usage
Once the stack is running via docker compose up, you can interact with the system via the UI or the REST API.
1. Web Interface (End-User Flow)
   The frontend is designed following GOV.UK Design System standards for accessibility and clarity.
   Navigate to: http://localhost:5173
   Shorten a URL:
   Enter a long URL (e.g., https://www.tpximpact.com/careers).
   (Optional) Enter a custom alias (e.g., tpx-jobs).
   Click "Shorten URL".
   Use the Link:
   The shortened link will appear. Click "Copy link" to save it to your clipboard.
   Manage Links:
   Navigate to the "Manage" tab.
   Enter an existing alias and click "Delete Link" to remove it from the system.
2. REST API (Technical Integration)
   For programmatic access, the API follows RESTful standards. Below are examples using curl.
   A. Create a Shortened URL
   Endpoint: POST /shorten
   code
   Bash
   curl -X POST http://localhost:8080/shorten \
   -H "Content-Type: application/json" \
   -d '{
   "fullUrl": "https://www.tpximpact.com",
   "customAlias": "impact-link"
   }'
   Expected Response (201 Created):
   code
   JSON
   {
   "alias": "impact-link",
   "shortUrl": "http://localhost:8080/impact-link"
   }
   B. Access a Redirect
   Endpoint: GET /{alias}
   code
   Bash
3. 
# Use -I to see the HTTP headers
curl -I http://localhost:8080/impact-link
Expected Response (302 Found):
HTTP/1.1 302 Found
Location: https://www.tpximpact.com
C. Delete an Alias
Endpoint: DELETE /{alias}
code
Bash
curl -X DELETE http://localhost:8080/impact-link
Expected Response (204 No Content):
HTTP/1.1 204 No Content


## High Availability and Fault Tolerance

Multiple Availability Zones on AWS (Europe/America)

  - Load Balancer (ALB): Sits at the front. It receives the request and sends it to a healthy instance of your app in any AZ.
  - App Servers (Spring Boot): You run at least two instances of your container—one in AZ-A and one in AZ-B.
  - CDN (content delivery network)  caching closest to the user.
  - Database (RDS Multi-AZ): Synchronization: Data is synchronously replicated at the storage layer for Multi-AZ (High Availability) and asynchronously replicated for Cross-Region 
    (Disaster Recovery). To comply with GDPR, I would implement Regional Data Sharding to ensure European citizen data remains within the EU jurisdiction while US data is stored in North America.


