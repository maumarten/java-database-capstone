# Smart Clinic — Schema & Architecture

## Section 1: Architecture summary
This Spring Boot–based Smart Clinic Management System follows a three-tier web architecture.

- **Presentation Tier**: 
  - Thymeleaf MVC for server-rendered admin and doctor dashboards.
  - JSON REST APIs for modules such as Appointments, Patient Dashboard, and Patient Records—so future web/mobile clients can integrate over HTTP/JSON.
- **Application Tier (Spring Boot)**:
  - Controllers (MVC + REST) delegate to Services that apply business rules, orchestration, and validation.
  - Services talk to Repositories for persistence.
- **Data Tier**:
  - **MySQL** via Spring Data JPA for structured entities (Patient, Doctor, Appointment, Admin, Roles).
  - **MongoDB** via Spring Data MongoDB for flexible, document-style data (Prescription).

Why Spring Boot:
- Rapid development with opinionated defaults, production-ready starters, built-in validation/testing support, and easy containerization (Docker/Kubernetes).
- Clean separation of concerns (Controller → Service → Repository), which improves maintainability, testability, and scalability.
- REST-first approach for interoperability with external clients and third-party integrations.

## Section 2: Numbered flow of data and control
1. **User Interaction (UI Layer)**  
   A user opens an Admin/Doctor dashboard (Thymeleaf) or a client app calls a REST endpoint (e.g., `/api/appointments`).
2. **Routing to Controllers**  
   Spring maps the HTTP method/path to either a Thymeleaf MVC controller (returns an `.html` view) or a REST controller (returns JSON).
3. **Delegation to Services**  
   Controllers validate input and delegate to Service methods that encapsulate business rules (e.g., checking doctor availability before scheduling).
4. **Repository Access**  
   Services call Spring Data repositories:
   - JPA repositories for MySQL (relational tables/entities).
   - Mongo repositories for MongoDB (document collections).
5. **Database Operations**  
   Repositories translate method calls to SQL (MySQL) or queries/aggregations (MongoDB) and persist/fetch data.
6. **Model Binding**  
   Results bind into domain models:
   - JPA `@Entity` classes for relational rows.
   - Mongo `@Document` classes for BSON/JSON documents.
7. **Response Construction**  
   - MVC path: models are added to the `Model` and rendered into Thymeleaf templates → HTML to the browser.
   - REST path: models/DTOs are serialized to JSON and returned with an HTTP status code.

### Notes on Deployability & CI/CD
- Package as a container image (Docker). Run on Kubernetes/VMs with horizontal scaling.
- Integrate CI/CD (GitHub Actions/Jenkins/GitLab CI) for automated build, test, and deploy pipelines.
