import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

let selectedDate = new Date().toISOString().slice(0, 10); // yyyy-mm-dd
let token = null;
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
  token = localStorage.getItem("token") || null;

  // Search bar
  const search = document.getElementById("searchBar");
  if (search) {
    search.addEventListener("input", () => {
      const v = search.value.trim();
      patientName = v.length ? v : "null";
      loadAppointments();
    });
  }

  // Today's Appointments button
  const btnToday = document.getElementById("btnToday");
  if (btnToday) {
    btnToday.addEventListener("click", () => {
      selectedDate = new Date().toISOString().slice(0, 10);
      const dateEl = document.getElementById("dateFilter");
      if (dateEl) dateEl.value = selectedDate;
      loadAppointments();
    });
  }

  // Date picker
  const datePicker = document.getElementById("dateFilter");
  if (datePicker) {
    datePicker.value = selectedDate;
    datePicker.addEventListener("change", () => {
      selectedDate = datePicker.value || selectedDate;
      loadAppointments();
    });
  }

  // Initial load
  if (typeof window.renderContent === "function") window.renderContent();
  loadAppointments();
});

async function loadAppointments() {
  const tbody = document.getElementById("patientTbody") || document.getElementById("patientTableBody");
  if (!tbody) return;

  tbody.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;

  try {
    const appts = await getAllAppointments(selectedDate, patientName || "null", token);

    if (!appts || appts.length === 0) {
      tbody.innerHTML = `<tr class="noPatientRecord"><td colspan="5">No Appointments found for today</td></tr>`;
      return;
    }

    tbody.innerHTML = "";
    for (const appt of appts) {
      // Expected structure: { patient: { id, name, phone, email }, prescription: boolean/string, ... }
      const tr = createPatientRow(appt);
      tbody.appendChild(tr);
    }
  } catch (e) {
    console.error(e);
    tbody.innerHTML = `<tr class="noPatientRecord"><td colspan="5">Error loading appointments.</td></tr>`;
  }
}
