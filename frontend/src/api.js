const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

async function request(path, { method = "GET", body, token } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    if (res.status === 401) {
      window.localStorage?.removeItem("norte_auth");
      if (window.location.pathname !== "/login" && window.location.pathname !== "/register") {
        window.location.href = "/login";
      }
    }
    const message = data?.error || "Algo deu errado. Tenta de novo.";
    throw new Error(message);
  }
  return data;
}

export const api = {
  register: (name, email, password) => request("/auth/register", { method: "POST", body: { name, email, password } }),
  login: (email, password) => request("/auth/login", { method: "POST", body: { email, password } }),

  listGoals: (token) => request("/goals", { token }),
  addGoal: (token, description) => request("/goals", { method: "POST", body: { description }, token }),
  removeGoal: (token, id) => request(`/goals/${id}`, { method: "DELETE", token }),

  getQuestionnaire: (token) => request("/questionnaire", { token }),

  submitAnswers: (token, answers) => request("/recommendations", { method: "POST", body: { answers }, token }),
  getHistory: (token) => request("/recommendations", { token }),
  getRecommendation: (token, id) => request(`/recommendations/${id}`, { token }),
  sendFeedback: (token, id, agrees, comment) =>
    request(`/recommendations/${id}/feedback`, { method: "POST", body: { agrees, comment }, token }),

  listPlans: (token) => request("/plans", { token }),
  getPlan: (token, id) => request(`/plans/${id}`, { token }),
  togglePlanStep: (token, planId, stepId, feito) =>
    request(`/plans/${planId}/steps/${stepId}`, { method: "PATCH", body: { feito }, token }),
};
