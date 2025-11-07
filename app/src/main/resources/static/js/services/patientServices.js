import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = API_BASE_URL + "/patient";

// POST /patient/signup
export async function patientSignup(data) {
  try {
    const res = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const json = await res.json().catch(() => ({}));
    return {
      success: res.ok,
      message: json?.message || (res.ok ? "Signup successful" : "Signup failed"),
      data: json || null,
    };
  } catch (e) {
    console.error("patientSignup error:", e);
    return { success: false, message: "Network or server error", data: null };
  }
}

// POST /patient/login  → return fetch Response (token extracted by caller)
export async function patientLogin(data) {
  // NOTE: log inputs only during development
  try {
    const res = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    return res;
  } catch (e) {
    console.error("patientLogin error:", e);
    return new Response(null, { status: 500, statusText: "Network error" });
  }
}

// GET /patient/me
export async function getPatientData(token) {
  try {
    const res = await fetch(`${PATIENT_API}/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (e) {
    console.error("getPatientData error:", e);
    return null;
  }
}

// GET /patient/{id}/appointments?user=patient|doctor
export async function getPatientAppointments(id, token, user = "patient") {
  try {
    const res = await fetch(`${PATIENT_API}/${encodeURIComponent(id)}/appointments?user=${encodeURIComponent(user)}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return null;
    const data = await res.json();
    return Array.isArray(data) ? data : (data?.appointments || []);
  } catch (e) {
    console.error("getPatientAppointments error:", e);
    return null;
  }
}

// GET /patient/appointments/filter?condition=&name=
export async function filterAppointments(condition = "", name = "", token) {
  try {
    const params = new URLSearchParams();
    if (condition) params.set("condition", condition);
    if (name) params.set("name", name);

    const res = await fetch(`${PATIENT_API}/appointments/filter?${params.toString()}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return [];
    const data = await res.json();
    return Array.isArray(data) ? data : (data?.appointments || []);
  } catch (e) {
    console.error("filterAppointments error:", e);
    alert("Unexpected error while filtering appointments.");
    return [];
  }
}
