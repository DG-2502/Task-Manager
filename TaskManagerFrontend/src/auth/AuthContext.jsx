import {createContext, useContext, useEffect, useState} from 'react';
import {apiFetch, getToken, clearToken, AUTH_DENIED_EVENT} from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({children}) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!getToken()) {
            setLoading(false);
            return;
        }

        apiFetch('/api/users/me', {skipAuthRefresh: true})
            .then(setUser)
            .catch(() => {
                clearToken();
                setUser(null);
            })
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        async function handleAuthDenied(event) {
            const status = event.detail?.status;

            if (status === 401) {
                clearToken();
                setUser(null);
                return;
            }

            try {
                const fresh = await apiFetch('/api/users/me', {skipAuthRefresh: true});
                setUser(fresh);
            } catch {
                clearToken();
                setUser(null);
            }
        }

        window.addEventListener(AUTH_DENIED_EVENT, handleAuthDenied);
        return () => window.removeEventListener(AUTH_DENIED_EVENT, handleAuthDenied);
    }, []);

    const value = {
        user,
        loading,
        setUser,
        clearUser: () => {
            clearToken();
            setUser(null);
        },
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used inside AuthContext');
    return ctx;
}