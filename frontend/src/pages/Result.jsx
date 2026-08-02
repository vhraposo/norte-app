import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { RotateCcw, History, ThumbsUp, ThumbsDown, Send, Home, Sun, Moon } from "lucide-react";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import CompassDial from "../components/CompassDial.jsx";

export default function Result() {
  const { C, theme, toggleTheme } = useTheme();
  const { id } = useParams();
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const [showCommentBox, setShowCommentBox] = useState(false);
  const [comment, setComment] = useState("");
  const [sending, setSending] = useState(false);

  useEffect(() => {
    setResult(null);
    setShowCommentBox(false);
    setComment("");
    setSending(false);
    setError(null);
    api.getRecommendation(auth.token, id).then(setResult).catch((e) => setError(e.message));
  }, [id, auth.token]);

  async function handleAgree() {
    try {
      const updated = await api.sendFeedback(auth.token, id, true, null);
      if (updated.planId) {
        navigate(`/diario/${updated.planId}`);
      } else {
        setResult(updated);
      }
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleDisagreeSubmit() {
    if (!comment.trim()) return;
    setSending(true);
    try {
      const refined = await api.sendFeedback(auth.token, id, false, comment.trim());
      navigate(`/result/${refined.id}`);
    } catch (e) {
      setError(e.message);
      setSending(false);
    }
  }

  if (error) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: C.bg, color: C.text }}>
        <p className="text-sm">{error}</p>
      </div>
    );
  }

  if (!result) {
    return <div className="min-h-screen w-full flex items-center justify-center" style={{ background: C.bg, color: C.textMuted }}>Carregando...</div>;
  }

  if (!result.titulo) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: C.bg, color: C.text }}>
        <div className="rounded-2xl p-8 text-center max-w-sm" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <p className="text-sm mb-4">Essa recomendacao nao foi gerada corretamente (resposta incompleta da IA). Gera um novo Norte que agora ha uma protecao contra isso.</p>
          <Link to="/goals" className="inline-block rounded-lg px-4 py-2 text-sm font-medium" style={{ background: C.gold, color: C.bg }}>
            Gerar novo Norte
          </Link>
        </div>
      </div>
    );
  }

  const feedbackGiven = result.userAgrees !== null && result.userAgrees !== undefined;

  return (
    <div className="min-h-screen w-full flex items-center justify-center px-4 py-10" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-xl font-body">
        <div className="flex items-center justify-between mb-8">
          <Link to="/" className="flex items-center gap-1 text-xs" style={{ color: C.textMuted }}><History size={14} /> Historico</Link>
          <span className="font-mono text-xs tracking-widest uppercase" style={{ color: C.textMuted }}>Norte v{result.version}</span>
          <div className="flex items-center gap-2">
            <Link to="/" title="Voltar ao inicio" className="flex items-center justify-center rounded-lg w-8 h-8"
              style={{ border: `1px solid ${C.surfaceLine}`, color: C.textMuted }}>
              <Home size={14} />
            </Link>
            <button onClick={toggleTheme} title="Trocar tema" className="flex items-center justify-center rounded-lg w-8 h-8"
              style={{ border: `1px solid ${C.surfaceLine}`, color: C.textMuted, background: "transparent" }}>
              {theme === "dark" ? <Sun size={14} /> : <Moon size={14} />}
            </button>
          </div>
        </div>

        <div className="rounded-2xl p-8" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <CompassDial resultAngle={37} />

          <p className="font-mono text-xs text-center uppercase tracking-widest mt-4 mb-1" style={{ color: C.gold }}>seu norte agora</p>
          <h2 className="font-display text-2xl text-center mb-4">{result.titulo}</h2>
          <p className="text-sm text-center mb-6" style={{ color: C.textMuted }}>{result.porque}</p>

          {(result.pros?.length > 0 || result.contras?.length > 0) && (
            <div className="grid grid-cols-2 gap-3 mb-4">
              <div className="rounded-lg p-3" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
                <p className="font-mono text-xs mb-2" style={{ color: C.teal }}>pros</p>
                <ul className="text-xs flex flex-col gap-1.5">
                  {result.pros?.map((p, i) => <li key={i} style={{ color: C.text }}>+ {p}</li>)}
                </ul>
              </div>
              <div className="rounded-lg p-3" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
                <p className="font-mono text-xs mb-2" style={{ color: C.red }}>contras</p>
                <ul className="text-xs flex flex-col gap-1.5">
                  {result.contras?.map((c, i) => <li key={i} style={{ color: C.text }}>- {c}</li>)}
                </ul>
              </div>
            </div>
          )}

          <div className="rounded-lg p-4 mb-4" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
            <p className="font-mono text-xs mb-1" style={{ color: C.teal }}>primeiro passo</p>
            <p className="text-sm">{result.primeiroPasso}</p>
          </div>

          {result.alternativas?.length > 0 && (
            <div className="mb-4">
              <p className="font-mono text-xs mb-2" style={{ color: C.textMuted }}>por que nao os outros caminhos agora</p>
              <div className="flex flex-col gap-2">
                {result.alternativas.map((alt, i) => (
                  <div key={i} className="rounded-lg px-3 py-2" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
                    <p className="text-sm font-medium">{alt.titulo}</p>
                    <p className="text-xs mt-0.5" style={{ color: C.red }}>{alt.porqueNaoAgora}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {result.reflexao && (
            <p className="text-xs italic text-center mb-6" style={{ color: C.textMuted }}>"{result.reflexao}"</p>
          )}

          <div className="rounded-lg p-4 mb-6" style={{ background: C.bg, border: `1px solid ${C.surfaceLine}` }}>
            {!feedbackGiven && !showCommentBox && (
              <>
                <p className="text-sm mb-3 text-center">Voce concorda com esse resultado?</p>
                <div className="flex gap-2">
                  <button onClick={handleAgree} className="flex-1 flex items-center justify-center gap-2 rounded-lg py-2.5 text-sm font-medium" style={{ background: C.teal, color: C.bg }}>
                    <ThumbsUp size={14} /> Sim
                  </button>
                  <button onClick={() => setShowCommentBox(true)} className="flex-1 flex items-center justify-center gap-2 rounded-lg py-2.5 text-sm font-medium" style={{ background: "transparent", border: `1px solid ${C.surfaceLine}`, color: C.text }}>
                    <ThumbsDown size={14} /> Nao
                  </button>
                </div>
              </>
            )}

            {!feedbackGiven && showCommentBox && (
              <>
                <p className="text-sm mb-2">O que nao fez sentido pra voce?</p>
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="ex: acho que voces nao levaram em conta que eu ja tenho experiencia internacional..."
                  rows={3}
                  className="w-full rounded-lg px-3 py-2 text-sm outline-none mb-3 resize-none"
                  style={{ background: C.surface, border: `1px solid ${C.surfaceLine}`, color: C.text }}
                />
                <button onClick={handleDisagreeSubmit} disabled={sending || !comment.trim()}
                  className="w-full flex items-center justify-center gap-2 rounded-lg py-2.5 text-sm font-medium disabled:opacity-40"
                  style={{ background: C.gold, color: C.bg }}>
                  <Send size={14} /> {sending ? "Gerando novo Norte..." : "Enviar e gerar novo Norte"}
                </button>
              </>
            )}

            {feedbackGiven && result.userAgrees && (
              <div className="text-center">
                <p className="text-sm mb-2" style={{ color: C.teal }}>Voce confirmou que concorda com esse resultado.</p>
                {result.planId && (
                  <Link to={`/diario/${result.planId}`} className="inline-block rounded-lg px-4 py-2 text-sm font-medium"
                    style={{ background: C.gold, color: C.bg }}>
                    Ir para o Diario de Norte
                  </Link>
                )}
              </div>
            )}

            {feedbackGiven && !result.userAgrees && (
              <div>
                <p className="text-sm text-center mb-1" style={{ color: C.red }}>Voce discordou desse resultado.</p>
                {result.userComment && <p className="text-xs text-center italic" style={{ color: C.textMuted }}>"{result.userComment}"</p>}
              </div>
            )}
          </div>

          <Link to="/goals" className="w-full flex items-center justify-center gap-2 rounded-lg py-3 text-sm font-medium"
            style={{ background: "transparent", border: `1px solid ${C.surfaceLine}`, color: C.text }}>
            <RotateCcw size={14} /> Atualizar vontades e gerar novo Norte
          </Link>
        </div>
      </div>
    </div>
  );
}
