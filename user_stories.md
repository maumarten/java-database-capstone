# Smart Clinic Management System — User Stories

## 🧑‍💼 Admin User Stories
As an **Admin**, I want to:
1. **Create and manage user accounts** so that doctors and patients can securely access the system.
2. **Assign roles (Admin, Doctor, Patient)** to control access and permissions across the platform.
3. **View system usage statistics** to monitor appointment volumes and system activity.
4. **Update or remove inactive accounts** to keep the system clean and secure.
5. **Configure clinic-wide settings** (e.g., working hours, holiday schedules) to standardize appointment availability.

---

## 🧍‍♂️ Patient User Stories
As a **Patient**, I want to:
1. **Register for an account** using my personal and contact information so I can book appointments.
2. **Search for available doctors** by specialization, date, or time slot.
3. **Book appointments online** and receive confirmation instantly.
4. **View my appointment history** so I can track previous consultations.
5. **Cancel or reschedule appointments** in case of conflicts.
6. **View prescriptions and visit summaries** provided by my doctor after each appointment.

---

## 🩺 Doctor User Stories
As a **Doctor**, I want to:
1. **View my daily and weekly appointment schedule** to prepare for upcoming consultations.
2. **Manage my availability** by setting working hours and time slots.
3. **Approve, reject, or modify patient appointment requests** to maintain an accurate calendar.
4. **Access patient medical history** to provide better and informed care.
5. **Create and store digital prescriptions** using a flexible interface.
6. **Update visit notes** and attach them to the patient’s record for continuity of care.

---

## ✅ Acceptance Criteria
- Each user role (Admin, Doctor, Patient) must log in and only see relevant features for their role.
- The system must validate input and prevent unauthorized access.
- Users should receive clear feedback messages for all actions (success, failure, errors).
- Appointment conflicts should be prevented when booking overlapping slots.
- Prescriptions should be securely stored in MongoDB and retrievable by authorized users.
