import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import usePagedResource from '../hooks/usePagedResource';
import PriorityBadge from './PriorityBadge';

const PRIORITY_OPTIONS = ['LOW', 'MEDIUM', 'HIGH'];

export default function TaskList({userId, isAdmin = false}) {
    const [title, setTitle] = useState('');
    const [priority, setPriority] = useState('MEDIUM');
    const [priorityFilter, setPriorityFilter] = useState('ALL');
    const [mutationError, setMutationError] = useState(null);

    const [editingId, setEditingId] = useState(null);
    const [draft, setDraft] = useState(null);
    const [saving, setSaving] = useState(false);

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const base = isAdmin ? `/api/admin/users/${userId}/tasks` : '/api/tasks';
        const params = new URLSearchParams({page, size, state: filter});
        if (search) params.set('search', search);
        if (priorityFilter !== 'ALL') params.set('priority', priorityFilter);
        return `${base}?${params}`;
    }, [userId, isAdmin, priorityFilter]);

    const {
        content: tasks, fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        totalPages, totalElements, first, last,
        search, setSearch,
        applySearch, clearSearch,
        reload,
    } = usePagedResource(buildUrl);

    async function runMutation(fn) {
        setMutationError(null);
        try {
            await fn();
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    const handleClose = id => runMutation(() =>
        apiFetch(`/api/tasks/${id}/close`, {method: 'POST'}));

    const handleReopen = id => runMutation(() =>
        apiFetch(`/api/tasks/${id}/open`, {method: 'POST'}));

    const handleDelete = id => runMutation(async () => {
        await apiFetch(`/api/tasks/${id}`, {method: 'DELETE'});
        if (totalElements % size === 1 && !first) setPage(p => p - 1);
    });

    function startEdit(task) {
        setMutationError(null);
        setEditingId(task.id);
        setDraft({
            title: task.title,
            priority: task.priority || 'MEDIUM',
        });
    }

    function cancelEdit() {
        setEditingId(null);
        setDraft(null);
        setSaving(false);
    }

    async function handleSaveEdit(e) {
        e.preventDefault();
        if (editingId == null || !draft) return;

        const original = tasks.find(t => t.id === editingId);
        if (!original) return;

        const patch = {};
        if (draft.title !== original.title) patch.title = draft.title;
        const originalPriority = original.priority || 'MEDIUM';
        if (draft.priority !== originalPriority) patch.priority = draft.priority;

        if (Object.keys(patch).length === 0) {
            cancelEdit();
            return;
        }

        setSaving(true);
        setMutationError(null);
        try {
            await apiFetch(`/api/tasks/${editingId}`, {
                method: 'PATCH',
                body: JSON.stringify(patch),
            });
            cancelEdit();
            reload();
        } catch (err) {
            setMutationError(err.message);
            setSaving(false);
        }
    }

    function handleEditKeyDown(e) {
        if (e.key === 'Escape') {
            e.preventDefault();
            cancelEdit();
        }
    }

    const handleCreate = e => {
        e.preventDefault();
        setMutationError(null);
        const url = isAdmin ? `/api/admin/users/${userId}/tasks` : '/api/tasks';
        apiFetch(url, {method: 'POST', body: JSON.stringify({title, priority})})
            .then(() => {
                setTitle('');
                setPriority('MEDIUM');
                reload();
            })
            .catch(err => setMutationError(err.message));
    };

    function handleSearchSubmit(e) {
        e.preventDefault();
        applySearch();
    }

    return (
        <>
            {fetchError && <p className="field-error">{fetchError}</p>}
            {mutationError && <p className="field-error">{mutationError}</p>}

            <form onSubmit={handleCreate} className="create-form">
                <input
                    className="create-task"
                    type="text"
                    value={title}
                    placeholder="Create a new task..."
                    required
                    maxLength={255}
                    autoComplete="off"
                    onChange={e => setTitle(e.target.value)}
                />
                <select
                    className="priority-select"
                    value={priority}
                    onChange={e => setPriority(e.target.value)}
                    aria-label="Priority of new task"
                >
                    {PRIORITY_OPTIONS.map(p => (
                        <option key={p} value={p}>
                            {p.charAt(0) + p.slice(1).toLowerCase()}
                        </option>
                    ))}
                </select>
            </form>

            <div className="controls">
                <div className="filter-group">
                    {['ALL', 'ACTIVE', 'CLOSED'].map(f => (
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

                <label className="filter-select">
                    Priority:
                    <select
                        value={priorityFilter}
                        onChange={e => {
                            setPriorityFilter(e.target.value);
                            setPage(0);
                        }}
                    >
                        <option value="ALL">All</option>
                        {PRIORITY_OPTIONS.map(p => (
                            <option key={p} value={p}>
                                {p.charAt(0) + p.slice(1).toLowerCase()}
                            </option>
                        ))}
                    </select>
                </label>

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
                    placeholder="Search by title..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                {search && (
                    <button type="button" onClick={clearSearch}>Clear</button>
                )}
            </form>

            {tasks.length === 0 ? (
                <p className="empty">No tasks yet.</p>
            ) : (
                <div className="list">
                    {tasks.map(task => (
                        editingId === task.id ? (
                            <form
                                key={task.id}
                                className="list-item list-item-editing"
                                onSubmit={handleSaveEdit}
                                onKeyDown={handleEditKeyDown}
                            >
                                <div className="list-item-info">
                                    <input
                                        type="text"
                                        className="edit-input edit-title"
                                        value={draft.title}
                                        onChange={e => setDraft(d => ({...d, title: e.target.value}))}
                                        autoFocus
                                        required
                                        maxLength={255}
                                        autoComplete="off"
                                    />
                                    <div className="list-item-meta">
                                        <select
                                            className="edit-input edit-priority"
                                            value={draft.priority}
                                            onChange={e => setDraft(d => ({...d, priority: e.target.value}))}
                                            aria-label="Priority"
                                        >
                                            {PRIORITY_OPTIONS.map(p => (
                                                <option key={p} value={p}>
                                                    {p.charAt(0) + p.slice(1).toLowerCase()}
                                                </option>
                                            ))}
                                        </select>
                                        <span className="edit-hint">
                                            Enter to save · Esc to cancel
                                        </span>
                                    </div>
                                </div>
                                <div className="list-item-actions">
                                    <button
                                        type="submit"
                                        className="action-button"
                                        disabled={saving}
                                    >
                                        {saving ? 'Saving…' : 'Save'}
                                    </button>
                                    <button
                                        type="button"
                                        className="action-button secondary"
                                        onClick={cancelEdit}
                                        disabled={saving}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div
                                key={task.id}
                                className={`list-item ${task.state === 'CLOSED' ? 'closed' : ''}`}
                            >
                                <div className="list-item-info">
                                    <div className="list-item-title">{task.title}</div>
                                    <div className="list-item-meta">
                                        <PriorityBadge priority={task.priority}/>
                                        <span>{task.startDate}</span>
                                        <span>—</span>
                                        <span>{task.state}</span>
                                    </div>
                                </div>
                                <div className="list-item-actions">
                                    <button
                                        type="button"
                                        className="action-button secondary"
                                        onClick={() => startEdit(task)}
                                    >
                                        Edit
                                    </button>
                                    {task.state === 'ACTIVE' ? (
                                        <button
                                            type="button"
                                            className="action-button"
                                            onClick={() => handleClose(task.id)}
                                        >
                                            Close
                                        </button>
                                    ) : (
                                        <button
                                            type="button"
                                            className="action-button"
                                            onClick={() => handleReopen(task.id)}
                                        >
                                            Open
                                        </button>
                                    )}
                                    <button
                                        type="button"
                                        className="action-button danger"
                                        onClick={() => handleDelete(task.id)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        )
                    ))}
                </div>
            )}
        </>
    );
}