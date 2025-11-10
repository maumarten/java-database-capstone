import { createDoctorCard } from "./components/doctorCard.js";
import { openModal, closeModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

// Load all doctors on ready
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  // Bind modal triggers (if present)
  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) signupBtn.addEventListener("click", () => openModal("patientSignup"));

  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) loginBtn.addEventListener("click", () => openModal("patientLogin"));

  // Filters
  const sb = document.getElementById("searchBar");
  const ft = document.getElementById("filterTime");
  const fs = document.getElementById("filterSpecialty");

  const hook = () => filterDoctorsOnChange();
  if (sb) sb.addEventListener("input", hook);
  if (ft) ft.addEventListener("change", hook);
  if (fs) fs.addEventListener("change", hook);
});

async function loadDoctorCards() {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;
  contentDiv.innerHTML = "Loading…";

  const doctors = await getDoctors();
  renderDoctorCards(doctors);
}

function renderDoctorCards(doctors = []) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";
  if (!doctors.length) {
    contentDiv.innerHTML = `<p>No doctors available at the moment.</p>`;
    return;
  }
  for (const d of doctors) {
    contentDiv.appendChild(createDoctorCard(d));
  }
}

async function filterDoctorsOnChange() {
  const nameEl = document.getElementById("searchBar");
  const timeEl = document.getElementById("filterTime");
  const specEl = document.getElementById("filterSpecialty");

  const name = nameEl?.value?.trim() || "";
  const time = timeEl?.value || "";
  const specialty = specEl?.value || "";

  const doctors = await filterDoctors(name, time, specialty);
  renderDoctorCards(doctors);
}

// === Auth (Patients) ===
window.signupPatient = async function signupPatient() {
  try {
    const name     = document.getElementById("signupName")?.value?.trim();
    const email    = document.getElementById("signupEmail")?.value?.trim();
    const password = document.getElementById("signupPassword")?.value?.trim();
    const phone    = document.getElementById("signupPhone")?.value?.trim();
    const address  = document.getElementById("signupAddress")?.value?.trim();

    const res = await patientSignup({ name, email, password, phone, address });
    if (res.success) {
      alert(res.message || "Signup successful.");
      closeModal?.();
      window.location.reload();
    } else {
      alert(res.message || "Signup failed.");
    }
  } catch (e) {
    console.error(e);
    alert("Unexpected error during signup.");
  }
};

window.loginPatient = async function loginPatient() {
  try {
    const email    = document.getElementById("email")?.value?.trim();
    const password = document.getElementById("password")?.value?.trim();

    const resp = await patientLogin({ email, password });
    if (resp.ok) {
      const data = await resp.json();
      if (data?.token) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userRole", "loggedPatient");
        window.location.href = "/pages/loggedPatientDashboard.html";
        return;
      }
    }
    alert("Invalid credentials.");
  } catch (e) {
    console.error(e);
    alert("Unexpected error during login.");
  }
};
