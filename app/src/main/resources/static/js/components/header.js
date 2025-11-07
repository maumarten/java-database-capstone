(function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  // If at root ("/"), clear any old session artifacts
  if (window.location.pathname.endsWith("/")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
  }

  const role  = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // If a privileged page is opened without token, kick back home
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  let headerContent = `
    <header class="header">
      <div class="brand">
        <img src="/assets/images/logo/logo.png" alt="Logo" class="logo">
        <span class="app-title">Smart Clinic</span>
      </div>
      <nav class="nav">`;

  if (role === "admin") {
    headerContent += `
      <button id="addDocBtn" class="adminBtn" onclick="openModal && openModal('addDoctor')">Add Doctor</button>
      <a href="#" id="logoutLink">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
      <a href="/" id="homeLink">Home</a>
      <a href="#" id="logoutLink">Logout</a>`;
  } else if (role === "loggedPatient") {
    headerContent += `
      <a href="/pages/patientDashboard.html" id="homeLink">Home</a>
      <a href="/pages/patientAppointments.html" id="apptLink">Appointments</a>
      <a href="#" id="logoutLink">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
      <a href="/pages/patientDashboard.html">Home</a>
      <a href="/pages/loggedPatientDashboard.html" id="loginBtn">Login</a>
      <a href="/pages/patientDashboard.html#signup" id="signupBtn">Sign Up</a>`;
  } else {
    // unknown / empty role → default to simple home
    headerContent += `<a href="/">Home</a>`;
  }

  headerContent += `
      </nav>
    </header>`;

  headerDiv.innerHTML = headerContent;

  // Attach listeners after injection
  const logoutLink = document.getElementById("logoutLink");
  if (logoutLink) {
    logoutLink.addEventListener("click", (e) => { e.preventDefault(); logout(); });
  }

  // Example: if you want a separate "patient logout" behavior
  // const logoutPatientLink = document.getElementById("logoutPatientLink");
  // if (logoutPatientLink) logoutPatientLink.addEventListener("click", (e)=>{ e.preventDefault(); logoutPatient(); });

  // optional: other header buttons
  const homeLink = document.getElementById("homeLink");
  if (homeLink) homeLink.addEventListener("click", () => {/* could track navigation */});
})();

// Session helpers
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}
