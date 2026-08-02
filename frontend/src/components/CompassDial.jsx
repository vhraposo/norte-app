import React from "react";
import { useTheme } from "../context/ThemeContext.jsx";

export default function CompassDial({ spinning, resultAngle }) {
  const { C } = useTheme();
  const angle = spinning ? undefined : resultAngle ?? 0;
  return (
    <div className="relative w-24 h-24 mx-auto">
      <svg viewBox="0 0 100 100" className="w-full h-full">
        <circle cx="50" cy="50" r="46" fill="none" stroke={C.surfaceLine} strokeWidth="1.5" />
        <circle cx="50" cy="50" r="3" fill={C.textMuted} />
        {[0, 90, 180, 270].map((a) => (
          <line key={a} x1="50" y1="6" x2="50" y2="12" stroke={C.textMuted} strokeWidth="1.5" transform={`rotate(${a} 50 50)`} />
        ))}
        <g
          style={{
            transformOrigin: "50px 50px",
            transform: `rotate(${angle}deg)`,
            transition: spinning ? "none" : "transform 1.4s cubic-bezier(.22,1,.36,1)",
            animation: spinning ? "spin 1.1s linear infinite" : "none",
          }}
        >
          <polygon points="50,14 55,50 50,50" fill={C.gold} />
          <polygon points="50,86 45,50 50,50" fill={C.textMuted} />
        </g>
        <circle cx="50" cy="50" r="4" fill={C.gold} />
      </svg>
      <style>{`@keyframes spin { from { transform: rotate(0deg);} to { transform: rotate(360deg);} }`}</style>
    </div>
  );
}
