import React from "react";
import { Link } from "react-router-dom";
import { Compass, Sun, Moon, Home } from "lucide-react";
import { useTheme } from "../context/ThemeContext.jsx";

// Cabecalho padrao: logo Norte + (opcional) botao de inicio + botao de trocar tema.
// showHome=false na propria Dashboard, ja que ali "inicio" e a propria pagina.
export default function PageHeader({ showHome = true }) {
  const { theme, toggleTheme, C } = useTheme();

  return (
    <div className="flex items-center justify-between mb-8">
      <div className="flex items-center gap-2">
        <Compass size={20} color={C.gold} />
        <span className="font-mono text-xs tracking-widest uppercase" style={{ color: C.textMuted }}>Norte</span>
      </div>
      <div className="flex items-center gap-3">
        {showHome && (
          <Link to="/" title="Voltar ao inicio" className="flex items-center justify-center rounded-lg w-8 h-8"
            style={{ border: `1px solid ${C.surfaceLine}`, color: C.textMuted }}>
            <Home size={14} />
          </Link>
        )}
        <button onClick={toggleTheme} title="Trocar tema" className="flex items-center justify-center rounded-lg w-8 h-8"
          style={{ border: `1px solid ${C.surfaceLine}`, color: C.textMuted, background: "transparent" }}>
          {theme === "dark" ? <Sun size={14} /> : <Moon size={14} />}
        </button>
      </div>
    </div>
  );
}
