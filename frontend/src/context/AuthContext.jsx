import React, { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const stored = window.localStorage?.getItem("norte_auth");
    return stored ? JSON.parse(stored) : null;
  });

  useEffect(() => {
    if (auth) window.localStorage?.setItem("norte_auth", JSON.stringify(auth));
    else window.localStorage?.removeItem("norte_auth");
  }, [auth]);

  function login(data) {
    setAuth(data);
  }

  function logout() {
    setAuth(null);
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
