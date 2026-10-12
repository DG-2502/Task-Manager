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
        const text = await response.text();
        const error = new Error(text || `Request failed: ${response.status}`);
        error.status = response.status;
        throw error;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        console.log("fetched some json")
        return response.json();
    }
    console.log("fetched some text")
    return response.text();
}