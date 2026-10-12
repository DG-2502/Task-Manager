import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { useEffect } from "react";

export default function ProtectedRoute({ children }) {
    const { user, loading } = useAuth();
    console.log("Protected Route is is called");
    useEffect(() => {
        console.log("Effects on the protected Route are being run");
    });
    console.log("Protected Route displaying pages");
    if (loading) {
        return <div className="loading">Loading…</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
}