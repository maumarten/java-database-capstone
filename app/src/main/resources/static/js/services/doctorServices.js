import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

// GET /doctor → list all doctors
export async function getDoctors() {
  try {
    const res = await fetch(DOCTOR_API, { method: "GET" });
    if (!res.ok) return [];
    const data = await res.json();
    return Array.isArray(data) ? data : (data?.doctors || []);
  } catch (e) {
    console.error("getDoctors error:", e);
    return [];
  }
}

// DELETE /doctor/{id}
export async function deleteDoctor(id, token) {
  try {
    const res = await fetch(`${DOCTOR_API}/${encodeURIComponent(id)}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });
    const json = await res.json().catch(() => ({}));
    return {
      success: res.ok,
      message: json?.message || (res.ok ? "Deleted" : "Delete failed"),
    };
  } catch (e) {
    console.error("deleteDoctor error:", e);
    return { success: false, message: "Network or server error" };
  }
}

// POST /doctor
export async function saveDoctor(doctor, token) {
  try {
    const res = await fetch(DOCTOR_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(doctor),
    });
    const json = await res.json().catch(() => ({}));
    return {
      success: res.ok,
      message: json?.message || (res.ok ? "Doctor saved" : "Save failed"),
      data: json || null,
    };
  } catch (e) {
    console.error("saveDoctor error:", e);
    return { success: false, message: "Network or server error", data: null };
  }
}

// GET /doctor/filter/{name}/{time}/{speciality}
// Backend expects literal "null" when a segment is not provided.
export async function filterDoctors(name = "", time = "", specialty = "") {
  try {
    const seg = (v) => (v && v.trim().length > 0 ? encodeURIComponent(v.trim()) : "null");
    const url = `${DOCTOR_API}/filter/${seg(name)}/${seg(time)}/${seg(studyToSpecialityParam(specialty))}`;
    const res = await fetch(url, { method: "GET" });
    if (!res.ok) return [];
    const data = await res.json();
    return Array.isArray(data) ? data : (data?.doctors || []);
  } catch (e) {
    console.error("filterDoctors error:", e);
    return [];
  }
}

// Helper to keep variable naming consistent with backend path var 'speciality'
function studyToSpecialityParam(value) {
  return value ?? "";
}
