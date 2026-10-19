import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import usePagedResource from '../hooks/usePagedResource';

export default function UserList({onSelect, selectedId}) {
    const [mutationError, setMutationError] = useState(null);
    const [error, setError] = useState(null);
    const [errorField, setErrorField] = useState(null);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [status, setStatus] = useState('USER');
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [creating, setCreating] = useState(false);

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const params = new URLSearchParams({page, size, status: filter});
        if (search) params.set('search', search);
        return `/api/admin/users?${params}`;
    }, []);

    const {
        content: users, fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        totalPages, totalElements, first, last,
        search, setSearch,
        applySearch, clearSearch,
        reload,
    } = usePagedResource(buildUrl);

    async function handleDelete(id) {
        setMutationError(null);
        try {
            await apiFetch(`/api/admin/users/${id}`, {method: 'DELETE'});
            if (id === selectedId) onSelect(null);
            if (totalElements % size === 1 && !first) setPage(p => p - 1);
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    async function handleCreate(e) {
        e.preventDefault();
        setCreating(true);
        setError(null);
        setErrorField(null);
        try {
            await apiFetch('/api/admin/users', {
                method: 'POST',
                body: JSON.stringify({username, password, status}),
            });
            setUsername('');
            setPassword('');
            setStatus('USER');
            reload();
        } catch (err) {
            setError(err.message);
            setErrorField(err.field);
        } finally {
            setCreating(false);
        }
    }

    function handleSearchSubmit(e) {
        e.preventDefault();
        applySearch();
    }

    return (
        <>
            {fetchError && <p className="field-error">{fetchError}</p>}
            {mutationError && <p className="field-error">{mutationError}</p>}

            <div className="create-user-section">
                <button
                    type="button"
                    className="toggle-form-button"
                    onClick={() => setIsFormOpen(open => !open)}
                >
                    {isFormOpen ? '✕ Cancel' : '+ Create a new user'}
                </button>

                {isFormOpen && (
                    <div className="create-user-form">
                        <form onSubmit={handleCreate}>
                            <div className="inputs">
                                <input
                                    className={`input ${errorField === 'username' ? 'input-error' : ''}`}
                                    type="text"
                                    name="username"
                                    placeholder="Username"
                                    value={username}
                                    onChange={e => setUsername(e.target.value)}
                                    required
                                />
                                <input
                                    className={`input ${errorField === 'password' ? 'input-error' : ''}`}
                                    type="password"
                                    name="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="form-row align-center">
                                <span className="form-label">Role:</span>
                                <div className="filter-group">
                                    {['USER', 'ADMIN'].map(role => (
                                        <button
                                            key={role}
                                            type="button"
                                            className={`filter-button ${status === role ? 'active' : ''}`}
                                            onClick={() => setStatus(role)}
                                        >
                                            {role.charAt(0) + role.slice(1).toLowerCase()}
                                        </button>
                                    ))}
                                </div>
                                <button type="submit" className="submit-button" disabled={creating}>
                                    {creating ? 'Creating...' : 'Create user'}
                                </button>
                            </div>

                            {error && <p className="field-error">{error}</p>}
                        </form>
                    </div>
                )}
            </div>

            <div className="controls">
                <div className="filter-group">
                    {['ALL', 'USERS', 'ADMINS'].map(f => (
                        <button
                            key={f}
                            type="button"
                            className={`filter-button ${filter === f ? 'active' : ''}`}
                            onClick={() => {
                                setFilter(f);
                                setPage(0);
                            }}
                        >
                            {f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>

                {totalPages > 1 && (
                    <div className="pagination">
                        <button type="button" onClick={() => setPage(p => p - 1)} disabled={first}>
                            ← Prev
                        </button>
                        <span className="page-info">
                            Page {page + 1} of {totalPages}
                            <span className="total-info"> ({totalElements} total)</span>
                        </span>
                        <button type="button" onClick={() => setPage(p => p + 1)} disabled={last}>
                            Next →
                        </button>
                    </div>
                )}

                <label className="size-selector">
                    Per page:
                    <select
                        value={size}
                        onChange={e => {
                            setSize(Number(e.target.value));
                            setPage(0);
                        }}
                    >
                        {[5, 10, 20, 50].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                </label>
            </div>

            <form onSubmit={handleSearchSubmit} className="search-form">
                <input
                    type="text"
                    placeholder="Search by username..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                {search && (
                    <button type="button" onClick={clearSearch}>Clear</button>
                )}
            </form>

            {users.length === 0 ? (
                <p className="empty">No users.</p>
            ) : (
                <div className="list">
                    {users.map(u => (
                        <div
                            key={u.id}
                            className={`list-item clickable ${selectedId === u.id ? 'selected' : ''}`}
                            onClick={() => onSelect(u)}
                        >
                            <div className="list-item-info">
                                <div className="list-item-title">{u.username}</div>
                                <div className="list-item-meta">{u.id} — {u.status}</div>
                            </div>
                            <div className="list-item-actions">
                                <button
                                    type="button"
                                    className="action-button danger"
                                    onClick={e => {
                                        e.stopPropagation();
                                        handleDelete(u.id);
                                    }}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </>
    );
}