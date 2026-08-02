import React from "react";
import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Goals from "./pages/Goals.jsx";
import Questionnaire from "./pages/Questionnaire.jsx";
import Result from "./pages/Result.jsx";
import DiarioNorte from "./pages/DiarioNorte.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/goals" element={<ProtectedRoute><Goals /></ProtectedRoute>} />
      <Route path="/questionnaire" element={<ProtectedRoute><Questionnaire /></ProtectedRoute>} />
      <Route path="/result/:id" element={<ProtectedRoute><Result /></ProtectedRoute>} />
      <Route path="/diario/:id" element={<ProtectedRoute><DiarioNorte /></ProtectedRoute>} />
    </Routes>
  );
}
