export async function apiFetch(path, options = {}) {
    const response = await fetch(path, {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
        ...options,
    });

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
        throw error;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}