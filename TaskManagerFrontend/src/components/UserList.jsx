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

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const base = '/api/admin/users';
        const params = new URLSearchParams({page, size, filter});
        if (search) params.set('search', search);
        return `${base}?${params}`;
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
        setMutationError(null)
        try {
            await apiFetch(`/api/admin/users/${id}`, {method: 'DELETE'});
            if (id === selectedId) {
                onSelect(null);
            }
            if (totalElements % size === 1 && !first) {
                setPage(page - 1);
            }
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    async function handleCreate(e) {
        e.preventDefault()
        setError(null)
        setErrorField(null);
        try {
            await apiFetch('/api/admin/users', {method: 'POST', body: JSON.stringify({username, password, status})})
            setUsername('');
            setPassword('');
            setStatus('USER');
            setIsFormOpen(false);
            reload();
        } catch (err) {
            setError(err.message)
            setErrorField(err.value);
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
                    onClick={() => setIsFormOpen(!isFormOpen)}
                >
                    {isFormOpen ? '✕ Cancel' : '+ Create a new user'}
                </button>

                {isFormOpen && (
                    <form onSubmit={handleCreate} className="create-user-form">
                        <div className="form-row">
                            <input className={`input ${errorField === 'username' ? 'input-error' : ''}`}
                                   type="text"
                                   placeholder="Username"
                                   value={username}
                                   onChange={e => setUsername(e.target.value)}
                                   required
                            />
                            <input className={`input ${errorField === 'password' ? 'input-error' : ''}`}
                                   type="password"
                                   placeholder="Password"
                                   value={password}
                                   onChange={e => setPassword(e.target.value)}
                                   required
                            />
                        </div>

                        <div className="form-row align-center">
                            <span className="form-label">Role:</span>
                            <div className="filter-group">
                                {['USER', 'ADMIN'].map(f => (
                                    <button key={f}
                                            type="button"
                                            className={`filter-button ${status === f ? 'active' : ''}`}
                                            onClick={() => setStatus(f)}>
                                        {f.charAt(0) + f.slice(1).toLowerCase()}
                                    </button>
                                ))}
                            </div>
                            <button type="submit" className="submit-button">Create User</button>
                        </div>

                        {errorField && <p className="field-error">{errorField}</p>}
                    </form>
                )}
            </div>

            <div className="controls">
                <div className="filter-group">
                    {['ALL', 'USERS', 'ADMINS'].map(f => (
                        <button key={f}
                                className={`filter-button ${filter === f ? 'active' : ''}`}
                                onClick={() => {
                                    setFilter(f);
                                    setPage(0);
                                }}>
                            {f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>

                {totalPages > 1 && (
                    <div className="pagination">
                        <button onClick={() => setPage(p => p - 1)} disabled={first}>← Prev</button>
                        <span className="page-info">
                            Page {page + 1} of {totalPages}
                            <span className="total-info"> ({totalElements} total)</span>
                        </span>
                        <button onClick={() => setPage(p => p + 1)} disabled={last}>Next →</button>
                    </div>
                )}

                <label className="size-selector">
                    Per page:
                    <select value={size} onChange={e => {
                        setSize(Number(e.target.value));
                        setPage(0);
                    }}>
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={20}>20</option>
                        <option value={50}>50</option>
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

            {users.length === 0
                ? <p className="empty">No users.</p>
                : <div className="tasks">
                    {users.map(u => (
                        <div key={u.id}
                             className={`task ${selectedId === u.id ? 'selected' : ''}`}
                             onClick={() => onSelect(u)}>
                            <div className="task-info">
                                <div className="task-title">{u.username}</div>
                                <div className="task-meta">{u.id} — {u.status}</div>
                            </div>
                            <div className="task-actions">
                                <button className="action-button danger"
                                        onClick={e => {
                                            e.stopPropagation();
                                            handleDelete(u.id);
                                        }}>
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>}
        </>
    );
}