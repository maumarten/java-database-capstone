# User Story Template
**Title:**
_As a [user role], I want [feature/goal], so that [reason]._
**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]
**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**
- [Additional information or edge cases]

---

# Smart Clinic Management System — User Stories

## Admin Stories

### 1) Admin Login
**Title:** Admin can log in  
_As an **Admin**, I want to log into the portal with my username and password, so that I can securely manage the platform._
**Acceptance Criteria:**
1. Given valid credentials, when I submit the login form, then I am authenticated and redirected to the admin dashboard.
2. Given invalid credentials, then I see an error without revealing which field is wrong.
3. Session expires after inactivity, requiring re-login.
**Priority:** High  
**Story Points:** 3  
**Notes:**  
- Enforce rate limiting and lockout after N failed attempts.

### 2) Admin Logout
**Title:** Admin can log out  
_As an **Admin**, I want to log out of the portal, so that I protect system access when I leave._
**Acceptance Criteria:**
1. Clicking “Logout” invalidates the session/token.
2. User is redirected to the public landing page.
3. Back button does not expose authenticated pages.
**Priority:** High  
**Story Points:** 2  
**Notes:**  
- Clear cookies and server session.

### 3) Add Doctor
**Title:** Admin can add doctors  
_As an **Admin**, I want to add doctors to the portal, so that they can receive bookings._
**Acceptance Criteria:**
1. Required fields: name, specialty, email, phone, license number.
2. Email and license number must be unique; show validation errors if duplicates.
3. On success, doctor appears in the doctors list and becomes available for scheduling.
**Priority:** High  
**Story Points:** 5  
**Notes:**  
- Send welcome email with set-password link.

### 4) Delete Doctor
**Title:** Admin can delete doctor profile  
_As an **Admin**, I want to delete a doctor’s profile, so that I can remove providers who no longer work at the clinic._
**Acceptance Criteria:**
1. Deletion requires confirmation.
2. Future appointments with this doctor are blocked; existing future appointments prompt rescheduling.
3. Historical appointments remain for audit (soft delete or status=INACTIVE).
**Priority:** Medium  
**Story Points:** 5  
**Notes:**  
- Prefer soft delete to preserve history.

### 5) Operational Reporting via Stored Procedure
**Title:** Admin can run monthly appointment count SP  
_As an **Admin**, I want to run a stored procedure in MySQL CLI to get appointments per month, so that I can track usage statistics._
**Acceptance Criteria:**
1. Stored procedure `sp_appointments_per_month` returns month, year, total_count.
2. Procedure can be run from CLI and application service.
3. Results are displayed or exported (CSV) from the admin dashboard.
**Priority:** Medium  
**Story Points:** 3  
**Notes:**  
- Add DB index on `appointments.starts_at`.

---

## Patient Stories

### 6) Browse Doctors (No Login)
**Title:** Patient can view doctors list publicly  
_As a **Patient**, I want to view a list of doctors without logging in, so that I can explore options before registering._
**Acceptance Criteria:**
1. Public endpoint/page lists doctors with name, specialty, and rating (if any).
2. Supports filtering by specialty and search by name.
3. No PII beyond public profile fields is shown.
**Priority:** High  
**Story Points:** 3  
**Notes:**  
- Cache results for performance.

### 7) Patient Sign Up
**Title:** Patient can register  
_As a **Patient**, I want to sign up using my email and password, so that I can book appointments._
**Acceptance Criteria:**
1. Validates email format and password strength.
2. Sends verification email to activate account.
3. After verification, user can log in.
**Priority:** High  
**Story Points:** 5  
**Notes:**  
- Enforce unique email.

### 8) Patient Login
**Title:** Patient can log in  
_As a **Patient**, I want to log into the portal, so that I can manage my bookings._
**Acceptance Criteria:**
1. Valid credentials grant access to the patient dashboard.
2. Invalid login shows generic error (no credential leakage).
3. Session timeout enforced.
**Priority:** High  
**Story Points:** 3  
**Notes:**  
- Consider MFA as an enhancement.

### 9) Patient Logout
**Title:** Patient can log out  
_As a **Patient**, I want to log out of the portal, so that I can secure my account._
**Acceptance Criteria:**
1. Logout invalidates session/token.
2. Redirect to public landing page.
3. Back navigation does not reveal protected pages.
**Priority:** High  
**Story Points:** 2  
**Notes:**  
- Clear client storage.

### 10) Book One-Hour Appointment
**Title:** Patient books an appointment  
_As a **Patient**, I want to book an hour-long appointment with a doctor, so that I can receive care._
**Acceptance Criteria:**
1. Patient selects doctor and date/time; duration fixed to 60 minutes.
2. System validates doctor availability and prevents overlapping bookings.
3. Confirmation appears and an email notification is sent.
**Priority:** High  
**Story Points:** 8  
**Notes:**  
- Use server-side overlap checks with transaction/lock.

### 11) View Upcoming Appointments
**Title:** Patient views upcoming visits  
_As a **Patient**, I want to view my upcoming appointments, so that I can prepare accordingly._
**Acceptance Criteria:**
1. List shows future appointments with date, time, doctor, and location/telehealth link.
2. Sorted by soonest first; supports pagination if needed.
3. Allows cancellation window (e.g., ≥24h) where policy allows.
**Priority:** Medium  
**Story Points:** 3  
**Notes:**  
- Respect clinic cancellation policy.

---

## Doctor Stories

### 12) Doctor Login
**Title:** Doctor can log in  
_As a **Doctor**, I want to log into the portal, so that I can manage my appointments._
**Acceptance Criteria:**
1. Valid credentials grant access to the doctor dashboard.
2. Invalid login shows generic error.
3. Session timeout enforced.
**Priority:** High  
**Story Points:** 3  
**Notes:**  
- Optional MFA.

### 13) Doctor Logout
**Title:** Doctor can log out  
_As a **Doctor**, I want to log out of the portal, so that I protect my data._
**Acceptance Criteria:**
1. Session/token invalidated.
2. Redirect to public page.
3. Back navigation doesn’t expose protected pages.
**Priority:** High  
**Story Points:** 2  
**Notes:**  
- Same flow as other roles.

### 14) View Appointment Calendar
**Title:** Doctor sees calendar  
_As a **Doctor**, I want to view my appointment calendar, so that I stay organized._
**Acceptance Criteria:**
1. Calendar shows day/week views with scheduled appointments.
2. Supports filters (status) and jump to date.
3. Clicking an item reveals visit details (patient name, reason, notes if permitted).
**Priority:** High  
**Story Points:** 5  
**Notes:**  
- Consider ICS export.

### 15) Mark Unavailability
**Title:** Doctor marks unavailability  
_As a **Doctor**, I want to mark my unavailability, so that patients only see available slots._
**Acceptance Criteria:**
1. Doctor can add time ranges as unavailable.
2. Unavailable ranges immediately hide corresponding slots from booking UI.
3. Conflict detection prevents overlapping entries.
**Priority:** High  
**Story Points:** 5  
**Notes:**  
- Persist to `doctor_availability` or a blackout table.

### 16) Update Profile
**Title:** Doctor updates profile  
_As a **Doctor**, I want to update my specialization and contact info, so that patients have up-to-date information._
**Acceptance Criteria:**
1. Editable fields: name, specialty, email, phone, bio (optional).
2. Validation on email/phone formats; uniqueness on email.
3. Changes reflected on public doctor listing.
**Priority:** Medium  
**Story Points:** 3  
**Notes:**  
- Consider approval workflow for sensitive changes.

### 17) View Patient Details for Upcoming Appointments
**Title:** Doctor views patient details  
_As a **Doctor**, I want to view patient details for upcoming appointments, so that I can be prepared._
**Acceptance Criteria:**
1. Doctor can access read-only patient profile and visit history relevant to the appointment.
2. Access is limited to assigned appointments (RBAC enforced).
3. PHI is masked or minimized where not necessary.
**Priority:** High  
**Story Points:** 5  
**Notes:**  
- Log access for audit.
