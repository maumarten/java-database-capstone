// Role-Based Login Handling

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API  = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor/login";

// Ensure buttons are bound after DOM is ready
window.onload = function () {
  const adminBtn  = document.getElementById("adminLogin");
  const doctorBtn = document.getElementById("doctorLogin");

  if (adminBtn) {
    adminBtn.addEventListener("click", () => openModal("adminLogin"));
  }
  if (doctorBtn) {
    doctorBtn.addEventListener("click", () => openModal("doctorLogin"));
  }
};

// Helper: set role (provided in render.js)
function selectRole(role) {
  if (typeof window.selectRole === "function") {
    window.selectRole(role);
  } else {
    // fallback
    localStorage.setItem("userRole", role);
  }
}

// Admin login
window.adminLoginHandler = async function adminLoginHandler() {
  try {
    const username = document.getElementById("adminUsername")?.value?.trim();
    const password = document.getElementById("adminPassword")?.value?.trim();

    const admin = { username, password };

    const res = await fetch(ADMIN_API + "/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(admin),
    });

    if (res.ok) {
      const data = await res.json();
      if (data?.token) {
        localStorage.setItem("token", data.token);
        selectRole("admin");
        window.location.href = "/admin/adminDashboard.html"; // Thymeleaf route
        return;
      }
    }
    alert("Invalid credentials!");
  } catch (err) {
    console.error(err);
    alert("Unexpected error during admin login.");
  }
};

// Doctor login
window.doctorLoginHandler = async function doctorLoginHandler() {
  try {
    const email    = document.getElementById("doctorEmail")?.value?.trim();
    const password = document.getElementById("doctorPassword")?.value?.trim();

    const doctor = { email, password };

    const res = await fetch(DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    if (res.ok) {
      const data = await res.json();
      if (data?.token) {
        localStorage.setItem("token", data.token);
        selectRole("doctor");
        window.location.href = "/doctor/doctorDashboard.html";
        return;
      }
    }
    alert("Invalid credentials!");
  } catch (err) {
    console.error(err);
    alert("Unexpected error during doctor login.");
  }
};
