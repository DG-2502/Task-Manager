import { createContext, useContext, useEffect, useState } from 'react';
import { apiFetch } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    console.log("Auth provider method is called");
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        console.log("Effects on AuthProvider are being run");
        apiFetch('/api/users/me')
            .then(setUser)
            .catch(() => setUser(null))
            .finally(() => {setLoading(false); console.log("Effects on AuthProvider finished");});
    }, []);

    const value = {
        user,
        loading,
        setUser,
        clearUser: () => setUser(null),
    };
    console.log("Auth provider method is returning the AuthContext.provider");
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
    return ctx;
}