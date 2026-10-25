const AUTH_TOKEN_KEY = 'auth_token';
export const AUTH_DENIED_EVENT = 'auth:denied';

export function getToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function setToken(token) {
    if (token) {
        localStorage.setItem(AUTH_TOKEN_KEY, token);
    } else {
        localStorage.removeItem(AUTH_TOKEN_KEY);
    }
}

export function clearToken() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
}

export async function apiFetch(path, options = {}) {
    const {skipAuthRefresh = false, ...fetchOptions} = options;
    const token = getToken();

    const headers = {
        'Content-Type': 'application/json',
        ...(fetchOptions.headers || {}),
    };
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(path, {...fetchOptions, headers});

    if (!response.ok) {
        let message = `Request failed: ${response.status}`;
        let field = null;

        const text = await response.text();
        if (text) {
            try {
                const body = JSON.parse(text);
                if (body.message) message = body.message;
                if (body.field) field = body.field;
            } catch {
                message = text;
            }
        }

        const error = new Error(message);
        error.status = response.status;
        error.field = field;

        if (!skipAuthRefresh && (response.status === 401 || response.status === 403)) {
            window.dispatchEvent(
                new CustomEvent(AUTH_DENIED_EVENT, {detail: {status: response.status}})
            );
        }

        throw error;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}