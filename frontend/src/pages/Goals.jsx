import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, X, ArrowRight } from "lucide-react";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import PageHeader from "../components/PageHeader.jsx";

export default function Goals() {
  const { C } = useTheme();
  const { auth } = useAuth();
  const [goals, setGoals] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  function load() {
    api.listGoals(auth.token).then(setGoals).catch((e) => setError(e.message)).finally(() => setLoading(false));
  }

  useEffect(load, [auth.token]);

  async function addGoal() {
    const v = input.trim();
    if (!v) return;
    try {
      await api.addGoal(auth.token, v);
      setInput("");
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  async function removeGoal(id) {
    try {
      await api.removeGoal(auth.token, id);
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="min-h-screen w-full flex items-center justify-center px-4 py-10" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-xl font-body">
        <PageHeader />

        <div className="rounded-2xl p-8" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <h1 className="font-display text-2xl mb-2">O que voce quer fazer?</h1>
          <p className="text-sm mb-6" style={{ color: C.textMuted }}>
            Sempre que voce adiciona ou remove uma vontade, seu Norte pode mudar. Atualize sempre que precisar.
          </p>

          {error && <p className="text-xs mb-4" style={{ color: C.red }}>{error}</p>}

          <div className="flex gap-2 mb-3">
            <input value={input} onChange={(e) => setInput(e.target.value)} onKeyDown={(e) => e.key === "Enter" && addGoal()}
              placeholder="ex: viajar pela Europa" className="flex-1 rounded-lg px-4 py-3 text-sm outline-none"
              style={{ background: C.bg, border: `1px solid ${C.surfaceLine}`, color: C.text }} />
            <button onClick={addGoal} className="rounded-lg px-4 flex items-center justify-center" style={{ background: C.gold, color: C.bg }}>
              <Plus size={18} />
            </button>
          </div>

          {loading ? (
            <p className="text-sm" style={{ color: C.textMuted }}>Carregando...</p>
          ) : (
            <div className="flex flex-wrap gap-2 mb-6">
              {goals.map((g) => (
                <span key={g.id} className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}`, color: C.text }}>
                  {g.description}
                  <X size={12} className="cursor-pointer" onClick={() => removeGoal(g.id)} style={{ color: C.textMuted }} />
                </span>
              ))}
            </div>
          )}

          <button onClick={() => navigate("/questionnaire")} disabled={goals.length === 0}
            className="w-full rounded-lg py-3 text-sm font-medium flex items-center justify-center gap-2 disabled:opacity-40" style={{ background: C.gold, color: C.bg }}>
            Gerar novo Norte <ArrowRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}
