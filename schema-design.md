# Smart Clinic — Schema Design

This document defines the **relational schema (MySQL)** and the **document schema (MongoDB)** for the Smart Clinic system.  
Goals: integrity, performance, and evolvability (API- and data-model friendly).

### What data a Smart Clinic must manage
- **Identity & Access**: patients, doctors, admins; roles, contact info.
- **Scheduling**: doctor availability rules; appointments (status, timestamps, reason).
- **Clinical**: prescriptions (nested meds, diagnosis, follow-up), attachments, audit.
- **Operations & Audit**: created/updated timestamps, creator references, versioning.

---

## 1) MySQL Database Design

**Engine / Collation**
- Use `InnoDB` for FK support and transactions.
- Use `utf8mb4` for full Unicode.
- Timestamps in UTC (`TIMESTAMP`/`DATETIME`), managed by the app.

### 1.1. `patients`
| Column            | Type            | Constraints                                  | Notes |
|-------------------|-----------------|-----------------------------------------------|------|
| `id`              | BIGINT          | PK, AUTO_INCREMENT                            | surrogate key |
| `full_name`       | VARCHAR(120)    | NOT NULL                                      |       |
| `email`           | VARCHAR(191)    | NOT NULL, UNIQUE                              | 191 to fit common index limits |
| `phone`           | VARCHAR(30)     | NULL                                          | normalized by app |
| `date_of_birth`   | DATE            | NULL                                          |       |
| `gender`          | ENUM('M','F','X','U') | NOT NULL DEFAULT 'U'                    | U=Unknown |
| `created_at`      | DATETIME        | NOT NULL                                      | set by app |
| `updated_at`      | DATETIME        | NOT NULL                                      | set by app |

**Indexes**
- `UNIQUE(email)`
- `INDEX(created_at)`

---

### 1.2. `doctors`
| Column            | Type            | Constraints                                  | Notes |
|-------------------|-----------------|-----------------------------------------------|------|
| `id`              | BIGINT          | PK, AUTO_INCREMENT                            |       |
| `full_name`       | VARCHAR(120)    | NOT NULL                                      |       |
| `email`           | VARCHAR(191)    | NOT NULL, UNIQUE                              |       |
| `phone`           | VARCHAR(30)     | NULL                                          |       |
| `specialization`  | VARCHAR(120)    | NOT NULL                                      | e.g., cardiology |
| `license_number`  | VARCHAR(64)     | NOT NULL, UNIQUE                              | regulatory |
| `timezone`        | VARCHAR(64)     | NOT NULL DEFAULT 'UTC'                        | IANA TZ |
| `created_at`      | DATETIME        | NOT NULL                                      |       |
| `updated_at`      | DATETIME        | NOT NULL                                      |       |

**Indexes**
- `UNIQUE(email)`, `UNIQUE(license_number)`
- `INDEX(specialization)`

---

### 1.3. `admins`
| Column            | Type            | Constraints                                  |
|-------------------|-----------------|-----------------------------------------------|
| `id`              | BIGINT          | PK, AUTO_INCREMENT                            |
| `full_name`       | VARCHAR(120)    | NOT NULL                                      |
| `email`           | VARCHAR(191)    | NOT NULL, UNIQUE                              |
| `phone`           | VARCHAR(30)     | NULL                                          |
| `created_at`      | DATETIME        | NOT NULL                                      |
| `updated_at`      | DATETIME        | NOT NULL                                      |

**Rationale**
- Kept separate from doctors/patients to simplify role-specific data and reduce NULLs.

---

### 1.4. `appointments`
| Column               | Type             | Constraints                                                        | Notes |
|----------------------|------------------|---------------------------------------------------------------------|------|
| `id`                 | BIGINT           | PK, AUTO_INCREMENT                                                  |      |
| `patient_id`         | BIGINT           | NOT NULL, FK → `patients(id)` ON UPDATE CASCADE ON DELETE RESTRICT  |      |
| `doctor_id`          | BIGINT           | NOT NULL, FK → `doctors(id)` ON UPDATE CASCADE ON DELETE RESTRICT   |      |
| `created_by_admin_id`| BIGINT           | NULL, FK → `admins(id)` ON UPDATE CASCADE ON DELETE SET NULL        | optional |
| `starts_at`          | DATETIME         | NOT NULL                                                            | UTC |
| `ends_at`            | DATETIME         | NOT NULL                                                            | UTC |
| `status`             | ENUM('SCHEDULED','COMPLETED','CANCELED','NO_SHOW','RESCHEDULED') | NOT NULL DEFAULT 'SCHEDULED' | |
| `reason`             | VARCHAR(255)     | NULL                                                                | short free text |
| `created_at`         | DATETIME         | NOT NULL                                                            | |
| `updated_at`         | DATETIME         | NOT NULL                                                            | |

**Constraints**
- `CHECK (ends_at > starts_at)`  <!-- MySQL 8.0+ -->
- `UNIQUE(doctor_id, starts_at)`  <!-- quick guard vs exact duplicates -->
- `INDEX(patient_id, starts_at)`
- `INDEX(doctor_id, starts_at)`

> **Note on overlaps:** preventing *time-range* overlaps requires app/service logic (or exclusion constraints, which MySQL lacks). We add fast indexes and enforce overlaps in the Service layer.

---

### 1.5. `doctor_availability` (for scheduling)
| Column         | Type        | Constraints                                                  | Notes |
|----------------|-------------|---------------------------------------------------------------|------|
| `id`           | BIGINT      | PK, AUTO_INCREMENT                                           |      |
| `doctor_id`    | BIGINT      | NOT NULL, FK → `doctors(id)` ON UPDATE CASCADE ON DELETE CASCADE |      |
| `day_of_week`  | TINYINT     | NOT NULL, CHECK (day_of_week BETWEEN 0 AND 6)                | 0=Sun |
| `start_time`   | TIME        | NOT NULL                                                     |      |
| `end_time`     | TIME        | NOT NULL                                                     |      |
| `effective_from`| DATE       | NOT NULL                                                     |      |
| `effective_to` | DATE        | NULL                                                         | open-ended |

**Constraints**
- `CHECK (end_time > start_time)`
- `UNIQUE(doctor_id, day_of_week, start_time, effective_from)`  <!-- prevents dup slots -->
- `INDEX(doctor_id, day_of_week)`

---

### 1.6. Sample DDL (MySQL 8+)

```sql
-- Global defaults
SET NAMES utf8mb4;                                     -- Full Unicode (names, intl emails)

-- Patients: core identity for appointments
CREATE TABLE patients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,                -- Surrogate PK for simple joins
  full_name VARCHAR(120) NOT NULL,                     -- Human-readable; generous length
  email VARCHAR(191) NOT NULL UNIQUE,                  -- UNIQUE to prevent duplicate accounts (191 fits idx limits)
  phone VARCHAR(30),                                   -- App enforces format; keep flexible here
  date_of_birth DATE,                                  -- Nullable for privacy/completeness
  gender ENUM('M','F','X','U') NOT NULL DEFAULT 'U',   -- Controlled values; U = unknown
  created_at DATETIME NOT NULL,                        -- Audit (UTC); set by app
  updated_at DATETIME NOT NULL                         -- Audit (UTC); set by app
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Doctors: includes specialization and license for traceability
CREATE TABLE doctors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(191) NOT NULL UNIQUE,                  -- Prevent duplicate identities
  phone VARCHAR(30),
  specialization VARCHAR(120) NOT NULL,                -- Common search filter → index below
  license_number VARCHAR(64) NOT NULL UNIQUE,          -- Regulatory uniqueness
  timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',         -- For correct slot rendering
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_doctors_specialization ON doctors(specialization);  -- Speeds specialty search

-- Admins: separate table keeps role data clean, avoids NULLs in other tables
CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(191) NOT NULL UNIQUE,
  phone VARCHAR(30),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Doctor availability rules (not every minute) for compact, versioned schedules
CREATE TABLE doctor_availability (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,                           -- FK to doctors
  day_of_week TINYINT NOT NULL,                        -- 0..6 (Sun..Sat)
  start_time TIME NOT NULL,                            -- Local to doctor's timezone
  end_time TIME NOT NULL,
  effective_from DATE NOT NULL,                        -- Versioning window start
  effective_to DATE NULL,                              -- NULL = open-ended

  CONSTRAINT fk_av_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
    ON UPDATE CASCADE ON DELETE CASCADE,               -- Remove rules if doctor removed

  CONSTRAINT chk_av_day CHECK (day_of_week BETWEEN 0 AND 6),  -- Input discipline
  CONSTRAINT chk_av_time CHECK (end_time > start_time),       -- Prevent inverted ranges

  UNIQUE KEY uq_slot (doctor_id, day_of_week, start_time, effective_from), -- No duplicate rule keys
  KEY ix_doc_day (doctor_id, day_of_week)                 -- Common lookup path
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Appointments: joins patient↔doctor; optional admin creator (front-desk bookings)
CREATE TABLE appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,                          -- FK to patients
  doctor_id BIGINT NOT NULL,                           -- FK to doctors
  created_by_admin_id BIGINT NULL,                     -- Nullable; keep history if admin removed
  starts_at DATETIME NOT NULL,                         -- UTC instant
  ends_at DATETIME NOT NULL,                           -- UTC instant
  status ENUM('SCHEDULED','COMPLETED','CANCELED','NO_SHOW','RESCHEDULED')
         NOT NULL DEFAULT 'SCHEDULED',                 -- Operational states
  reason VARCHAR(255),                                 -- Brief free text; detailed notes elsewhere
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,

  CONSTRAINT fk_appt_patient
    FOREIGN KEY (patient_id) REFERENCES patients(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,              -- Preserve patient history

  CONSTRAINT fk_appt_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,              -- Preserve doctor history

  CONSTRAINT fk_appt_admin
    FOREIGN KEY (created_by_admin_id) REFERENCES admins(id)
    ON UPDATE CASCADE ON DELETE SET NULL,              -- Keep appointment even if admin removed

  CONSTRAINT chk_time_range CHECK (ends_at > starts_at),      -- Basic time sanity

  UNIQUE KEY uq_doctor_start (doctor_id, starts_at),          -- Quick guard vs exact dup
  KEY ix_patient_time (patient_id, starts_at),                -- Patient timeline queries
  KEY ix_doctor_time (doctor_id, starts_at)                   -- Doctor schedule queries
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOTE: MySQL lacks exclusion constraints. Enforce time-range overlaps in the Service layer
-- with SELECT ... FOR UPDATE and business rules before INSERT/UPDATE.

---

## 2) MongoDB Collection Design

We store flexible clinical artifacts in MongoDB. Chosen primary collection: **`prescriptions`**.

### 2.1. Purpose
- Capture the **legal/clinical snapshot** of a visit’s prescription, including patient/doctor details **as they were at issue time** (denormalized for traceability).
- Support arrays (e.g., multiple medications) and nested objects (audit, follow-up, attachments).
- Link back to the relational world via `appointmentId`.

### 2.2. Example Document (JSON)
> Note: JSON doesn’t allow inline comments; explanations follow after the example.

```json
{
  "_id": "presc_0192f4d4c2e7",                // Stable doc id (could be ObjectId too)
  "appointmentId": 987654321,                 // Cross-link to MySQL appointments.id

  "patient": {                                // Snapshot at issue time (legal traceability)
    "id": 1001,                               // Denormalized RDBMS id for fast filters
    "fullName": "Ana Rodríguez",
    "dob": "1990-07-12",
    "email": "ana.rodriguez@example.com"
  },

  "doctor": {                                 // Snapshot prevents historical drift
    "id": 501,
    "fullName": "Dr. Marco Jiménez",
    "specialization": "Family Medicine",
    "licenseNumber": "CR-FAM-88231"           // Regulatory reference
  },

  "issuedAt": "2025-11-06T19:15:00Z",         // ISO instant; use Date type in driver

  "diagnosis": {
    "primary": "J06.9",                       // ICD-10 primary code
    "secondary": ["R50.9"],                   // Array allows multiple codes
    "notes": "Acute upper respiratory infection; monitor fever."
  },

  "medications": [                            // Natural one-to-many as embedded array
    {
      "drugName": "Paracetamol",
      "dosage": "500 mg",
      "frequency": "q8h",
      "durationDays": 5,
      "route": "PO",
      "instructions": "Take with water after meals.",
      "refills": 0
    },
    {
      "drugName": "Dextromethorphan",
      "dosage": "10 mg/5 mL",
      "frequency": "q12h",
      "durationDays": 3,
      "route": "PO",
      "instructions": "Do not exceed recommended dose.",
      "refills": 0
    }
  ],

  "allergiesChecked": true,                   // Indicates safety screening performed

  "followUp": {
    "recommendedDate": "2025-11-13",
    "instructions": "Return if symptoms persist or worsen."
  },

  "attachments": [                            // Supports PDFs/images with integrity hash
    {
      "type": "pdf",
      "url": "https://files.smartclinic.example/prescriptions/presc_0192f4d4c2e7.pdf",
      "sha256": "b0b5f0e4...redacted..."
    }
  ],

  "audit": {                                  // Compliance + optimistic concurrency
    "createdBy": "doctor:501",                // Encodes role and id
    "createdAt": "2025-11-06T19:15:05Z",
    "updatedAt": "2025-11-06T19:15:05Z",
    "version": 1
  }
}
