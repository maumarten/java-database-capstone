// modals.js
import { patientLogin } from "../services/patientServices.js";

export function openModal(type) {
  let modalContent = '';
  if (type === 'addDoctor') {
    modalContent = `
         <h2>Add Doctor</h2>
         <input type="text" id="doctorName" placeholder="Doctor Name" class="input-field">
         <select id="specialization" class="input-field select-dropdown">
             <option value="">Specialization</option>
                        <option value="cardiologist">Cardiologist</option>
                        <option value="dermatologist">Dermatologist</option>
                        <option value="neurologist">Neurologist</option>
                        <option value="pediatrician">Pediatrician</option>
                        <option value="orthopedic">Orthopedic</option>
                        <option value="gynecologist">Gynecologist</option>
                        <option value="psychiatrist">Psychiatrist</option>
                        <option value="dentist">Dentist</option>
                        <option value="ophthalmologist">Ophthalmologist</option>
                        <option value="ent">ENT Specialist</option>
                        <option value="urologist">Urologist</option>
                        <option value="oncologist">Oncologist</option>
                        <option value="gastroenterologist">Gastroenterologist</option>
                        <option value="general">General Physician</option>

        </select>
        <input type="email" id="doctorEmail" placeholder="Email" class="input-field">
        <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
        <input type="text" id="doctorPhone" placeholder="Mobile No." class="input-field">
        <div class="availability-container">
        <label class="availabilityLabel">Select Availability:</label>
          <div class="checkbox-group">
              <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00 AM - 10:00 AM</label>
              <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00 AM - 11:00 AM</label>
              <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00 AM - 12:00 PM</label>
              <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00 PM - 1:00 PM</label>
          </div>
        </div>
        <button class="dashboard-btn" id="saveDoctorBtn">Save</button>
      `;
  } else if (type === 'patientLogin') {
    modalContent = `
        <h2>Patient Login</h2>
        <input type="text" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="patientLoginSubmit">Login</button>
      `;
  }
  else if (type === "patientSignup") {
    modalContent = `
      <h2>Patient Signup</h2>
      <input type="text" id="name" placeholder="Name" class="input-field">
      <input type="email" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <input type="text" id="phone" placeholder="Phone" class="input-field">
      <input type="text" id="address" placeholder="Address" class="input-field">
      <button class="dashboard-btn" id="patientSignupSubmit">Signup</button>
    `;

  } else if (type === 'adminLogin') {
    modalContent = `
        <h2>Admin Login</h2>
        <input type="text" id="adminUsername" name="username" placeholder="Username" class="input-field">
        <input type="password" id="adminPassword" name="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="adminLoginBtn" >Login</button>
      `;
  } else if (type === 'doctorLogin') {
    modalContent = `
        <h2>Doctor Login</h2>
        <input type="text" id="doctorEmail" placeholder="Email" class="input-field">
        <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="doctorLoginBtn" >Login</button>
      `;
  }

  document.getElementById('modal-body').innerHTML = modalContent;
  document.getElementById('modal').style.display = 'block';

  document.getElementById('closeModal').onclick = () => {
    document.getElementById('modal').style.display = 'none';
  };

  // Note: patient signup handler may be provided elsewhere; keep binding if available
  if (type === "patientSignup") {
    const el = document.getElementById("patientSignupSubmit");
    if (el && typeof window.signupPatient === "function") {
      el.addEventListener("click", window.signupPatient);
    }
  }

  if (type === "patientLogin") {
    // Handle login directly here so it works across pages
    document.getElementById("patientLoginSubmit").addEventListener("click", async () => {
      const email = document.getElementById("email")?.value?.trim();
      const password = document.getElementById("password")?.value?.trim();
      if (!email || !password) { alert("Enter email and password."); return; }
      try {
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
        alert("Login failed. Please try again.");
      }
    });
  }

  if (type === 'addDoctor') {
    document.getElementById('saveDoctorBtn').addEventListener('click', adminAddDoctor);
  }

  if (type === 'adminLogin') {
    document.getElementById('adminLoginBtn').addEventListener('click', adminLoginHandler);
  }

  if (type === 'doctorLogin') {
    document.getElementById('doctorLoginBtn').addEventListener('click', doctorLoginHandler);
  }
}

// Booking overlay for logged-in patients (used by doctorCard.js)
import { bookAppointment } from "../services/appointmentRecordService.js";
export async function showBookingOverlay(e, doctor, patient) {
  // Prefer the shared modal on the page for consistent styling
  const sharedModal = document.getElementById("modal");
  const sharedBody = document.getElementById("modal-body");
  const sharedClose = document.getElementById("closeModal");

  const times = Array.isArray(doctor && doctor.availableTimes) ? doctor.availableTimes : [];
  const optionsHtml = times.map(t => `<option value="${t}">${t}</option>`).join("");
  const patientName = patient && patient.name ? patient.name : "";
  const docName = doctor && doctor.name ? doctor.name : "";
  const docSpec = doctor && (doctor.specialty || doctor.specialization) ? (doctor.specialty || doctor.specialization) : "";
  const docEmail = doctor && doctor.email ? doctor.email : "";

  if (sharedModal && sharedBody) {
    sharedBody.innerHTML = `
      <h2>Book Appointment</h2>
      <input class="input-field" type="text" value="${patientName}" disabled name="patientName" />
      <input class="input-field" type="text" value="${docName}" disabled name="doctorName" />
      <input class="input-field" type="text" value="${docSpec}" disabled name="doctorSpec" />
      <input class="input-field" type="email" value="${docEmail}" disabled name="doctorEmail" />
      <input class="input-field" type="date" id="appointment-date" name="appointmentDate" />
      <select class="input-field" id="appointment-time" name="appointmentTime">
        <option value="">Select time</option>
        ${optionsHtml}
      </select>
      <button class="dashboard-btn" id="confirmBookingBtn">Confirm Booking</button>
    `;
    sharedModal.style.display = "block";

    const cleanup = () => {
      // no-op cleanup; modal close is handled elsewhere
    };
    if (sharedClose) {
      sharedClose.onclick = () => {
        sharedModal.style.display = "none";
        cleanup();
      };
    }

    document.getElementById("confirmBookingBtn").onclick = async () => {
      const date = document.getElementById("appointment-date").value;
      const time = document.getElementById("appointment-time").value;
      const token = localStorage.getItem("token");
      if (!date || !time || !token) { alert("Select date/time and ensure you are logged in."); return; }
      const startTime = time.split("-")[0];
      const appointment = {
        doctor: { id: doctor.id },
        patient: { id: patient.id },
        appointmentTime: `${date}T${startTime}:00`,
        status: 0
      };
      const { success, message } = await bookAppointment(appointment, token);
      if (success) {
        alert("Appointment booked.");
        sharedModal.style.display = "none";
      } else {
        alert("Failed to book: " + message);
      }
    };
    return;
  }

  // Fallback: create lightweight overlay/modal if shared modal not present
  document.querySelectorAll(".ripple-overlay, .modalApp").forEach(el => el.remove());
  const overlay = document.createElement("div");
  overlay.classList.add("ripple-overlay");
  document.body.appendChild(overlay);
  setTimeout(() => overlay.classList.add("active"), 20);

  const modalApp = document.createElement("div");
  modalApp.classList.add("modalApp");
  modalApp.innerHTML = `
    <button class="modal-close" aria-label="Close">&times;</button>
    <h2>Book Appointment</h2>
    <input class="input-field" type="text" value="${patientName}" disabled />
    <input class="input-field" type="text" value="${docName}" disabled />
    <input class="input-field" type="text" value="${docSpec}" disabled/>
    <input class="input-field" type="email" value="${docEmail}" disabled/>
    <input class="input-field" type="date" id="appointment-date" />
    <select class="input-field" id="appointment-time">
      <option value="">Select time</option>
      ${optionsHtml}
    </select>
    <button class="confirm-booking">Confirm Booking</button>
  `;
  overlay.appendChild(modalApp);

  const closeBooking = () => {
    window.removeEventListener("keydown", onEsc);
    overlay.remove();
  };
  const onEsc = (evt) => { if (evt.key === "Escape") closeBooking(); };
  window.addEventListener("keydown", onEsc);
  overlay.addEventListener("click", (evt) => { if (evt.target === overlay) closeBooking(); });
  const closeBtn = modalApp.querySelector(".modal-close");
  if (closeBtn) closeBtn.addEventListener("click", closeBooking);

  modalApp.querySelector(".confirm-booking").addEventListener("click", async () => {
    const date = modalApp.querySelector("#appointment-date").value;
    const time = modalApp.querySelector("#appointment-time").value;
    const token = localStorage.getItem("token");
    if (!date || !time || !token) { alert("Select date/time and ensure you are logged in."); return; }
    const startTime = time.split('-')[0]; // supports "09:00" or "09:00-10:00"
    const appointment = {
      doctor: { id: doctor.id },
      patient: { id: patient.id },
      appointmentTime: `${date}T${startTime}:00`,
      status: 0
    };
    const { success, message } = await bookAppointment(appointment, token);
    if (success) {
      alert("Appointment booked.");
      closeBooking();
    } else {
      alert("Failed to book: " + message);
    }
  });
}

// Expose helpers to non-module scripts (e.g., header.js)
if (typeof window !== "undefined") {
  window.openModal = openModal;
  window.showBookingOverlay = showBookingOverlay;
}