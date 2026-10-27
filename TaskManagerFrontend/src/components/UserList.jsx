import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import {useAuth} from '../auth/AuthContext';
import usePagedResource from '../hooks/usePagedResource';
import ExpandableTitle from './ExpandableTitle';

export default function UserList({onSelect, selectedId, urlKey = null}) {
    const {user: currentUser, setUser} = useAuth();

    const [mutationError, setMutationError] = useState(null);
    const [error, setError] = useState(null);
    const [errorField, setErrorField] = useState(null);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [status, setStatus] = useState('USER');
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [creating, setCreating] = useState(false);

    const [editingUserId, setEditingUserId] = useState(null);
    const [userDraft, setUserDraft] = useState(null);
    const [savingUser, setSavingUser] = useState(false);
    const [editError, setEditError] = useState(null);
    const [editErrorField, setEditErrorField] = useState(null);

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const params = new URLSearchParams({page, size, status: filter});
        if (search) params.set('search', search);
        return `/api/admin/users?${params}`;
    }, []);

    const {
        content: users, loading, fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        totalPages, totalElements, first, last,
        search, setSearch,
        applySearch, clearSearch,
        reload,
    } = usePagedResource(buildUrl, {urlKey});

    async function handleDelete(id) {
        if (!window.confirm('Delete this user? This cannot be undone.')) return;
        setMutationError(null);
        try {
            await apiFetch(`/api/admin/users/${id}`, {method: 'DELETE'});
            if (id === selectedId) onSelect(null);
            if (id === editingUserId) cancelEditUser();
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

    function startEditUser(u) {
        setMutationError(null);
        setEditError(null);
        setEditErrorField(null);
        setEditingUserId(u.id);
        setUserDraft({username: u.username, status: u.status, password: ''});
    }

    function cancelEditUser() {
        setEditingUserId(null);
        setUserDraft(null);
        setSavingUser(false);
        setEditError(null);
        setEditErrorField(null);
    }

    function updateDraft(patch) {
        setUserDraft(d => ({...d, ...patch}));
        if (editError) setEditError(null);
        if (editErrorField) setEditErrorField(null);
    }

    async function handleSaveEdit(e) {
        e.preventDefault();
        if (editingUserId == null || !userDraft) return;

        const original = users.find(u => u.id === editingUserId);
        if (!original) return;

        const patch = {};
        const editingSelf = editingUserId === currentUser?.id;
        if (userDraft.username !== original.username) patch.username = userDraft.username;
        if (userDraft.status !== original.status) patch.status = userDraft.status;
        if (userDraft.password && !editingSelf) patch.password = userDraft.password;

        if (Object.keys(patch).length === 0) {
            cancelEditUser();
            return;
        }

        setSavingUser(true);
        setEditError(null);
        setEditErrorField(null);
        try {
            await apiFetch(`/api/admin/users/${editingUserId}`, {
                method: 'PATCH',
                body: JSON.stringify(patch),
            });

            if (editingUserId === currentUser?.id) {
                const fresh = await apiFetch('/api/users/me');
                setUser(fresh);
            }

            cancelEditUser();
            reload();
        } catch (err) {
            setEditError(err.message);
            setEditErrorField(err.field);
            setSavingUser(false);
        }
    }

    function handleEditKeyDown(e) {
        if (e.key === 'Escape') {
            e.preventDefault();
            cancelEditUser();
        }
    }

    function handleSearchSubmit(e) {
        e.preventDefault();
        applySearch();
    }

    const isSelf = u => u.id === currentUser?.id;
    const showEmpty = !loading && users.length === 0;

    return (
        <>
            {fetchError && <p className="field-error">{fetchError}</p>}
            {mutationError && <p className="field-error">{mutationError}</p>}

            <div className="toolbar">
                <div className="toolbar-row">
                    <div className="filter-group">
                        {['ALL', 'USERS', 'ADMINS'].map(f => (
                            <button
                                key={f}
                                type="button"
                                className={`filter-button ${filter === f ? 'active' : ''}`}
                                onClick={() => setFilter(f)}
                            >
                                {f.charAt(0) + f.slice(1).toLowerCase()}
                            </button>
                        ))}
                    </div>
                    <button
                        type="button"
                        className="toggle-form-button"
                        onClick={() => setIsFormOpen(open => !open)}
                    >
                        {isFormOpen ? '✕ Cancel' : '+ Create user'}
                    </button>
                </div>

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
                                    onChange={e => {
                                        setError(null);
                                        setErrorField(null);
                                        setUsername(e.target.value);
                                    }}
                                    maxLength={255}
                                    required
                                />
                                <input
                                    className={`input ${errorField === 'password' ? 'input-error' : ''}`}
                                    type="password"
                                    name="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={e => {
                                        setError(null);
                                        setErrorField(null);
                                        setPassword(e.target.value);
                                    }}
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

                <div className="toolbar-row">
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
                </div>
            </div>

            {loading && users.length === 0 ? (
                <p className="empty">Loading...</p>
            ) : showEmpty ? (
                <p className="empty">No users.</p>
            ) : (
                <div className={`list ${loading ? 'list-loading' : ''}`}>
                    {users.map(u => (
                        editingUserId === u.id ? (
                            <form
                                key={u.id}
                                className="list-item list-item-editing"
                                onSubmit={handleSaveEdit}
                                onKeyDown={handleEditKeyDown}
                            >
                                <div className="list-item-info">
                                    <input
                                        type="text"
                                        className={`edit-input edit-title ${editErrorField === 'username' ? 'input-error' : ''}`}
                                        value={userDraft.username}
                                        onChange={e => updateDraft({username: e.target.value})}
                                        autoFocus
                                        required
                                        maxLength={255}
                                        autoComplete="username"
                                        placeholder="Username"
                                    />
                                    <div className="list-item-meta">
                                        {!isSelf(u) && (
                                            <input
                                                type="password"
                                                className={`edit-input edit-password ${editErrorField === 'password' ? 'input-error' : ''}`}
                                                value={userDraft.password}
                                                onChange={e => updateDraft({password: e.target.value})}
                                                placeholder="New password (blank to keep)"
                                                autoComplete="new-password"
                                            />
                                        )}
                                        <div className="filter-group">
                                            {['USER', 'ADMIN'].map(role => (
                                                <button
                                                    key={role}
                                                    type="button"
                                                    className={`filter-button ${userDraft.status === role ? 'active' : ''}`}
                                                    onClick={() => updateDraft({status: role})}
                                                    disabled={isSelf(u)}
                                                >
                                                    {role.charAt(0) + role.slice(1).toLowerCase()}
                                                </button>
                                            ))}
                                        </div>
                                        {isSelf(u) && (
                                            <span className="edit-hint">
                                                You can't change your own role or password here
                                            </span>
                                        )}
                                    </div>
                                    {editError && <p className="field-error">{editError}</p>}
                                </div>
                                <div className="list-item-actions">
                                    <button type="submit" className="action-button" disabled={savingUser}>
                                        {savingUser ? 'Saving...' : 'Save'}
                                    </button>
                                    <button
                                        type="button"
                                        className="action-button secondary"
                                        onClick={cancelEditUser}
                                        disabled={savingUser}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div
                                key={u.id}
                                className={`list-item clickable ${selectedId === u.id ? 'selected' : ''}`}
                                onClick={() => onSelect(u)}
                            >
                                <div className="list-item-info">
                                    <ExpandableTitle text={u.username} className="list-item-title"/>
                                    <div className="list-item-meta">
                                        <span>{u.id}</span>
                                        <span>—</span>
                                        <span>{u.status}</span>
                                        {isSelf(u) && (
                                            <>
                                                <span>—</span>
                                                <span className="edit-hint">you</span>
                                            </>
                                        )}
                                    </div>
                                </div>
                                <div className="list-item-actions">
                                    <button
                                        type="button"
                                        className="action-button secondary"
                                        onClick={e => {
                                            e.stopPropagation();
                                            startEditUser(u);
                                        }}
                                    >
                                        Edit
                                    </button>
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
                        )
                    ))}
                </div>
            )}

            {!showEmpty && (
                <div className="pagination-footer">
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
                            onChange={e => setSize(Number(e.target.value))}
                        >
                            {[5, 10, 20, 50].map(n => <option key={n} value={n}>{n}</option>)}
                        </select>
                    </label>
                </div>
            )}
        </>
    );
}