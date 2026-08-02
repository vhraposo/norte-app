import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { api } from "../api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { useTheme } from "../context/ThemeContext.jsx";
import PageHeader from "../components/PageHeader.jsx";
import CompassDial from "../components/CompassDial.jsx";

export default function Questionnaire() {
  const { C } = useTheme();
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [questions, setQuestions] = useState(null);
  const [qIndex, setQIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.getQuestionnaire(auth.token).then(setQuestions).catch((e) => setError(e.message));
  }, [auth.token]);

  async function answer(optionLabel) {
    const q = questions[qIndex];
    const newAnswers = { ...answers, [q.text]: optionLabel };
    setAnswers(newAnswers);

    if (qIndex + 1 < questions.length) {
      setQIndex(qIndex + 1);
    } else {
      setSubmitting(true);
      try {
        const rec = await api.submitAnswers(auth.token, newAnswers);
        navigate(`/result/${rec.id}`);
      } catch (e) {
        setError(e.message);
        setSubmitting(false);
      }
    }
  }

  function goBack() {
    if (qIndex > 0) setQIndex(qIndex - 1);
  }

  if (error) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: C.bg, color: C.text }}>
        <div className="rounded-2xl p-8 text-center max-w-sm" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <p className="text-sm">{error}</p>
        </div>
      </div>
    );
  }

  if (submitting) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center px-4" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
        <div className="rounded-2xl p-10 text-center max-w-sm w-full" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <CompassDial spinning />
          <p className="font-mono text-xs mt-6" style={{ color: C.textMuted }}>calculando seu norte...</p>
        </div>
      </div>
    );
  }

  if (!questions) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center" style={{ background: C.bg, color: C.textMuted }}>
        Gerando perguntas personalizadas pras suas vontades...
      </div>
    );
  }

  const q = questions[qIndex];

  return (
    <div className="min-h-screen w-full flex items-center justify-center px-4 py-10" style={{ background: `radial-gradient(circle at 50% -10%, ${C.bgSoft}, ${C.bg} 60%)`, color: C.text }}>
      <div className="w-full max-w-xl font-body">
        <PageHeader />

        <div className="rounded-2xl p-8" style={{ background: C.surface, border: `1px solid ${C.surfaceLine}` }}>
          <div className="flex justify-between items-center mb-6">
            <button onClick={goBack} disabled={qIndex === 0} className="flex items-center gap-1 text-xs disabled:opacity-30"
              style={{ color: C.textMuted }}>
              <ArrowLeft size={13} /> Voltar
            </button>
            <span className="font-mono text-xs" style={{ color: C.textMuted }}>{qIndex + 1} / {questions.length}</span>
          </div>
          <div className="w-full h-1 rounded-full overflow-hidden mb-6" style={{ background: C.bg }}>
            <div className="h-full rounded-full" style={{ width: `${((qIndex + 1) / questions.length) * 100}%`, background: C.gold, transition: "width .3s ease" }} />
          </div>

          <h2 className="font-display text-xl mb-6">{q.text}</h2>

          <div className="flex flex-col gap-2.5">
            {q.options.map((opt) => {
              const selected = answers[q.text] === opt;
              return (
                <button key={opt} onClick={() => answer(opt)} className="text-left rounded-lg px-4 py-3 text-sm transition-colors"
                  style={{ background: selected ? C.gold : C.bg, border: `1px solid ${selected ? C.gold : C.surfaceLine}`, color: selected ? C.bg : C.text }}
                  onMouseEnter={(e) => { if (!selected) e.currentTarget.style.borderColor = C.gold; }}
                  onMouseLeave={(e) => { if (!selected) e.currentTarget.style.borderColor = C.surfaceLine; }}>
                  {opt}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
