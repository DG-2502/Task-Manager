import {useCallback, useState} from 'react';
import {apiFetch} from '../api/client';
import usePagedResource from '../hooks/usePagedResource';

export default function UserList({onSelect, selectedId}) {
    const [mutationError, setMutationError] = useState(null);
    const buildUrl = useCallback(({page, size, filter}) =>
        `/api/admin/users?page=${page}&size=${size}&filter=${filter}`, []);

    const {
        content: users, fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        totalPages, totalElements, first, last,
        reload,
    } = usePagedResource(buildUrl);

    async function handleDelete(id) {
        setMutationError(null)
        try {
            await apiFetch(`/api/admin/users/${id}`, {method: 'DELETE'});
            if (id === selectedId) {
                onSelect(null);
            }
            reload();
        } catch (err) {
            setMutationError(err.message);
        }
    }

    return (
        <>
            {fetchError && <p className="field-error">{fetchError}</p>}
            {mutationError && <p className="field-error">{mutationError}</p>}

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