// Reusable Doctor card component
// Usage: import { createDoctorCard } from "./components/doctorCard.js"
// and then container.appendChild(createDoctorCard(doctor))

import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";
import { openModal, showBookingOverlay } from "./modals.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  // Info
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.textContent = `Specialty: ${doctor.specialty ?? doctor.specialization ?? "—"}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email ?? "—"}`;

  const availability = document.createElement("p");
  const times = Array.isArray(doctor.availableTimes) ? doctor.availableTimes.join(", ") : (doctor.availableTimes || "—");
  availability.textContent = `Available: ${times}`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // Actions
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", async () => {
      if (!confirm(`Delete ${doctor.name}?`)) return;
      const token = localStorage.getItem("token");
      try {
        await deleteDoctor(doctor.id, token);
        card.remove();
        alert("Doctor removed.");
      } catch (err) {
        console.error(err);
        alert("Failed to delete the doctor.");
      }
    });
    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
      // Open login modal instead of silent no-op
      openModal("patientLogin");
    });
    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (e) => {
      try {
        const token = localStorage.getItem("token");
        const patientData = await getPatientData(token);
        showBookingOverlay(e, doctor, patientData);
      } catch (err) {
        console.error(err);
        alert("Unable to load patient data. Please re-login.");
      }
    });
    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);
  return card;
}
