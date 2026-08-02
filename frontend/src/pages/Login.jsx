import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import PageHeader from "../components/PageHeader.jsx";

export default function Login() {
  const { C } = useTheme();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const data = await api.login(email, password);
      login(data);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-sm font-body">
        <PageHeader showHome={false} />
        <form onSubmit={handleSubmit} className="rounded-2xl p-8" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <h1 className="font-display text-2xl mb-6">Entrar</h1>
          {error && <p className="text-xs mb-4" style={{ color: C.red }}>{error}</p>}
          <input type="email" required placeholder="e-mail" value={email} onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg px-4 py-3 text-sm outline-none mb-3" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}`, color: C.text }} />
          <input type="password" required placeholder="senha" value={password} onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg px-4 py-3 text-sm outline-none mb-5" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}`, color: C.text }} />
          <button disabled={loading} type="submit" className="w-full rounded-lg py-3 text-sm font-medium disabled:opacity-40" style={{ background: C.gold, color: C.bg }}>
            {loading ? "Entrando..." : "Entrar"}
          </button>
          <p className="text-xs text-center mt-5" style={{ color: C.textMuted }}>
            Ainda nao tem conta? <Link to="/register" style={{ color: C.gold }}>Cadastre-se</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
