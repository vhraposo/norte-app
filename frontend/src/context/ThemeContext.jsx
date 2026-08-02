import React, { createContext, useContext, useState, useEffect } from "react";

const DARK = {
  bg: "#12162A", bgSoft: "#181D38", surface: "#1E2444", surfaceLine: "#2C3260",
  text: "#F2EFE6", textMuted: "#9498B8", gold: "#D4A24C", teal: "#4FB3A9", red: "#E27D6B",
};

const LIGHT = {
  bg: "#F6F1E4", bgSoft: "#EFE7D3", surface: "#FFFFFF", surfaceLine: "#DED2B0",
  text: "#26221A", textMuted: "#7A7057", gold: "#B0762C", teal: "#217A70", red: "#B54B34",
};

const ThemeContext = createContext(null);

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => window.localStorage?.getItem("norte_theme") || "dark");

  useEffect(() => {
    window.localStorage?.setItem("norte_theme", theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((t) => (t === "dark" ? "light" : "dark"));
  }

  const C = theme === "dark" ? DARK : LIGHT;

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, C }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
