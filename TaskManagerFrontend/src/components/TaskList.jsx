import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import usePagedResource from '../hooks/usePagedResource';

export default function TaskList({userId, isAdmin = false}) {
    const [title, setTitle] = useState('');
    const [mutationError, setMutationError] = useState(null);

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const base = isAdmin ? `/api/admin/users/${userId}/tasks` : '/api/tasks';
        const params = new URLSearchParams({page, size, state: filter});
        if (search) params.set('search', search);
        return `${base}?${params}`;
    }, [userId, isAdmin]);

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

    const handleCreate = e => {
        e.preventDefault();
        setMutationError(null);
        const url = isAdmin ? `/api/admin/users/${userId}/tasks` : '/api/tasks';
        apiFetch(url, {method: 'POST', body: JSON.stringify({title})})
            .then(() => {
                setTitle('');
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
                    autoComplete="off"
                    onChange={e => setTitle(e.target.value)}
                />
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
                        <div
                            key={task.id}
                            className={`list-item ${task.state === 'CLOSED' ? 'closed' : ''}`}
                        >
                            <div className="list-item-info">
                                <div className="list-item-title">{task.title}</div>
                                <div className="list-item-meta">{task.startDate} — {task.state}</div>
                            </div>
                            <div className="list-item-actions">
                                {task.state === 'ACTIVE' ? (
                                    <button type="button" className="action-button"
                                            onClick={() => handleClose(task.id)}>Close</button>
                                ) : (
                                    <button type="button" className="action-button"
                                            onClick={() => handleReopen(task.id)}>Open</button>
                                )}
                                <button type="button" className="action-button danger"
                                        onClick={() => handleDelete(task.id)}>Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </>
    );
}