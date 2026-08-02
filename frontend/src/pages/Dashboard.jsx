import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, LogOut, BookOpen } from "lucide-react";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import PageHeader from "../components/PageHeader.jsx";

export default function Dashboard() {
  const { C } = useTheme();
  const { auth, logout } = useAuth();
  const [history, setHistory] = useState([]);
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([api.getHistory(auth.token), api.listPlans(auth.token)])
      .then(([h, p]) => { setHistory(h); setPlans(p); })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [auth.token]);

  return (
    <div className="min-h-screen w-full px-4 py-10" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-xl mx-auto font-body">
        <PageHeader showHome={false} />

        <div className="flex items-center justify-between mb-1">
          <h1 className="font-display text-2xl">Ola, {auth.name.split(" ")[0]}</h1>
          <button onClick={logout} className="flex items-center gap-1 text-xs" style={{ color: C.textMuted }}>
            <LogOut size={14} /> Sair
          </button>
        </div>
        <p className="text-sm mb-6" style={{ color: C.textMuted }}>Seu historico de Nortes ao longo do tempo.</p>

        <Link to="/goals" className="w-full flex items-center justify-center gap-2 rounded-lg py-3 text-sm font-medium mb-8" style={{ background: C.gold, color: C.bg }}>
          <Plus size={16} /> Atualizar vontades e gerar novo Norte
        </Link>

        {error && <p className="text-sm mb-4" style={{ color: C.red }}>{error}</p>}
        {loading && <p className="text-sm" style={{ color: C.textMuted }}>Carregando...</p>}

        {!loading && plans.length > 0 && (
          <div className="mb-8">
            <p className="font-mono text-xs mb-2 uppercase tracking-widest" style={{ color: C.textMuted }}>Seus Diarios de Norte</p>
            <div className="flex flex-col gap-2">
              {plans.map((p) => {
                const done = p.passos.filter((s) => s.feito).length;
                return (
                  <Link key={p.id} to={`/diario/${p.id}`} className="flex items-center justify-between rounded-xl px-4 py-3" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
                    <div className="flex items-center gap-2">
                      <BookOpen size={14} color={C.gold} />
                      <div>
                        <p className="text-sm font-medium">Diario de Norte {p.number}</p>
                        <p className="text-xs" style={{ color: C.textMuted }}>{p.titulo}</p>
                      </div>
                    </div>
                    <span className="font-mono text-xs" style={{ color: C.textMuted }}>{done}/{p.passos.length}</span>
                  </Link>
                );
              })}
            </div>
          </div>
        )}

        {!loading && history.length === 0 && (
          <div className="rounded-2xl p-8 text-center" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
            <p className="text-sm" style={{ color: C.textMuted }}>Voce ainda nao gerou nenhum Norte. Comece adicionando suas vontades.</p>
          </div>
        )}

        <div className="flex flex-col gap-3">
          {history.map((rec) => (
            <Link key={rec.id} to={`/result/${rec.id}`} className="block rounded-xl p-4" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
              <div className="flex justify-between items-center mb-1">
                <span className="font-mono text-xs" style={{ color: C.gold }}>Norte v{rec.version}</span>
                <span className="font-mono text-xs" style={{ color: C.textMuted }}>
                  {new Date(rec.createdAt).toLocaleDateString("pt-BR")}
                </span>
              </div>
              <p className="font-display text-lg">{rec.titulo}</p>
              <p className="text-xs mt-1" style={{ color: C.textMuted }}>
                {rec.goalsSnapshot?.join(" - ")}
              </p>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
