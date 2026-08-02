import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { Check, Compass } from "lucide-react";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import PageHeader from "../components/PageHeader.jsx";

export default function DiarioNorte() {
  const { C } = useTheme();
  const { id } = useParams();
  const { auth } = useAuth();
  const [plan, setPlan] = useState(null);
  const [error, setError] = useState(null);
  const [togglingId, setTogglingId] = useState(null);

  useEffect(() => {
    setPlan(null);
    setError(null);
    api.getPlan(auth.token, id).then(setPlan).catch((e) => setError(e.message));
  }, [id, auth.token]);

  async function toggleStep(step) {
    setTogglingId(step.id);
    try {
      const updated = await api.togglePlanStep(auth.token, id, step.id, !step.feito);
      setPlan(updated);
    } catch (e) {
      setError(e.message);
    } finally {
      setTogglingId(null);
    }
  }

  if (error) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: C.bg, color: C.text }}>
        <p className="text-sm">{error}</p>
      </div>
    );
  }

  if (!plan) {
    return <div className="min-h-screen w-full flex items-center justify-center" style={{ background: C.bg, color: C.textMuted }}>Carregando...</div>;
  }

  const done = plan.passos.filter((p) => p.feito).length;
  const total = plan.passos.length;

  return (
    <div className="min-h-screen w-full flex items-center justify-center px-4 py-10" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-xl font-body">
        <PageHeader />

        <div className="rounded-2xl p-8" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <div className="flex items-center gap-2 justify-center mb-1">
            <Compass size={16} color={C.gold} />
            <p className="font-mono text-xs uppercase tracking-widest" style={{ color: C.gold }}>Diario de Norte {plan.number}</p>
          </div>
          <h1 className="font-display text-2xl text-center mb-1">{plan.titulo}</h1>
          <p className="text-xs text-center mb-6" style={{ color: C.textMuted }}>{done} de {total} passos concluidos</p>

          <div className="w-full h-1.5 rounded-full overflow-hidden mb-6" style={{ background: C.bg }}>
            <div className="h-full rounded-full" style={{ width: total ? `${(done / total) * 100}%` : "0%", background: C.teal, transition: "width .3s ease" }} />
          </div>

          <div className="flex flex-col gap-2 mb-6">
            {plan.passos.map((step) => (
              <button key={step.id} onClick={() => toggleStep(step)} disabled={togglingId === step.id}
                className="flex items-center gap-3 text-left rounded-lg px-4 py-3 text-sm transition-opacity disabled:opacity-50"
                style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
                <span className="flex items-center justify-center rounded-md w-5 h-5 shrink-0"
                  style={{ background: step.feito ? C.teal : "transparent", border: `1.5px solid ${step.feito ? C.teal : C.surfaceLine}` }}>
                  {step.feito && <Check size={13} color={C.bg} />}
                </span>
                <span style={{ color: step.feito ? C.textMuted : C.text, textDecoration: step.feito ? "line-through" : "none" }}>
                  {step.descricao}
                </span>
              </button>
            ))}
          </div>

          {plan.ordemSugerida && (
            <div className="rounded-lg p-4" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
              <p className="font-mono text-xs mb-1" style={{ color: C.gold }}>o norte diz: melhor ordem entre seus objetivos</p>
              <p className="text-sm">{plan.ordemSugerida}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
