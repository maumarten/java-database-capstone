import { openModal, closeModal, readModalForm } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

// Open Add Doctor modal (in case header button exists on page load)
document.addEventListener("DOMContentLoaded", () => {
  const addBtn = document.getElementById("addDocBtn");
  if (addBtn) addBtn.addEventListener("click", () => openModal("addDoctor"));
});

// Load list on ready
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  // Search & filters
  const sb  = document.getElementById("searchBar");
  const ft  = document.getElementById("filterTime");
  const fs  = document.getElementById("filterSpecialty");

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
    contentDiv.innerHTML = `<p>No doctors found.</p>`;
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

// Called by the Add Doctor modal's submit button
window.adminAddDoctor = async function adminAddDoctor() {
  try {
    // Example modal form reader (adjust if your modal fields differ)
    // Expected fields: name, specialty, email, password, mobile, available_times[] (checkboxes)
    const token = localStorage.getItem("token");
    if (!token) { alert("Admin session expired."); return; }

    const form = readModalForm?.() || {}; // optional helper; fallback to manual DOM lookup below
    const getVal = (id) => document.getElementById(id)?.value?.trim();

    const doctor = {
      name: form.name ?? getVal("docName"),
      specialty: form.specialty ?? getVal("docSpecialty"),
      email: form.email ?? getVal("docEmail"),
      password: form.password ?? getVal("docPassword"),
      mobile: form.mobile ?? getVal("docMobile"),
      available_times: form.available_times ?? Array.from(document.querySelectorAll('input[name="available_times"]:checked')).map(i => i.value),
    };

    const result = await saveDoctor(doctor, token);
    if (result.success) {
      closeModal?.();
      alert("Doctor added successfully.");
      await loadDoctorCards();
    } else {
      alert(result.message || "Failed to add doctor.");
    }
  } catch (e) {
    console.error(e);
    alert("Unexpected error while adding doctor.");
  }
};
