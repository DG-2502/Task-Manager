import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import usePagedResource from '../hooks/usePagedResource';

export default function TaskList({userId, isAdmin = false}) {
    const [title, setTitle] = useState('');
    const [mutationError, setMutationError] = useState(null);

    const buildUrl = useCallback(({page, size, filter, search}) => {
        const base = isAdmin ? `/api/admin/users/${userId}/tasks` : `/api/tasks`;
        const params = new URLSearchParams({ page, size, filter });
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

    const handleFilterChange = (f) => {
        setFilter(f);
        setPage(0);
    };
    const handleSizeChange = (s) => {
        setSize(s);
        setPage(0);
    };

    async function handleClose(id) {
        setMutationError(null)
        try {
            await apiFetch(`/api/tasks/${id}/close`, {method: 'POST'});
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    async function handleReopen(id) {
        setMutationError(null)
        try {
            await apiFetch(`/api/tasks/${id}/open`, {method: 'POST'});
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    async function handleDelete(id) {
        setMutationError(null)
        try {
            await apiFetch(`/api/tasks/${id}`, {method: 'DELETE'});
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    async function handleCreate(e) {
        e.preventDefault();
        setMutationError(null)
        try {
            const url = isAdmin ? `/api/admin/users/${userId}/tasks` : '/api/tasks';
            await apiFetch(url, {method: 'POST', body: JSON.stringify({title})});
            setTitle('');
            reload();
        } catch (err) {
            setMutationError(err.message);
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
                        <button key={f}
                                className={`filter-button ${filter === f ? 'active' : ''}`}
                                onClick={() => handleFilterChange(f)}>
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
                    <select value={size} onChange={e => handleSizeChange(Number(e.target.value))}>
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
                    placeholder="Search by title..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                {/*<button type="submit">Search</button>*/}
                {search && (
                    <button type="button" onClick={clearSearch}>Clear</button>
                )}
            </form>

            {tasks.length === 0
                ? <p className="empty">No tasks yet.</p>
                : <div className="tasks">
                    {tasks.map(task => (
                        <div key={task.id}
                             className={`task ${task.state === 'CLOSED' ? 'closed' : ''}`}>
                            <div className="task-info">
                                <div className="task-title">{task.title}</div>
                                <div className="task-meta">{task.startDate} — {task.state}</div>
                            </div>
                            <div className="task-actions">
                                {task.state === 'ACTIVE'
                                    ? <button className="action-button"
                                              onClick={() => handleClose(task.id)}>Close</button>
                                    : <button className="action-button"
                                              onClick={() => handleReopen(task.id)}>Open</button>}
                                <button className="action-button danger" onClick={() => handleDelete(task.id)}>Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>}
        </>
    );
}