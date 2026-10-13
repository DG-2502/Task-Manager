import {createContext, useContext, useEffect, useState} from 'react';
import {apiFetch} from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({children}) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        apiFetch('/api/users/me')
            .then(setUser)
            .catch(() => setUser(null))
            .finally(() => {
                setLoading(false);
            });
    });

    const value = {
        user,
        loading,
        setUser,
        clearUser: () => setUser(null),
    };
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
    return ctx;
}