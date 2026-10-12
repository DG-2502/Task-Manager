import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function TasksPage() {
    const [tasks, setTasks] = useState([]);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [filter, setFilter] = useState('ALL');
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [pageFirst, setPageFirst] = useState(true);
    const [pageLast, setPageLast] = useState(true);
    const [error, setError] = useState(null);
    const [title, setTitle] = useState('');
    const navigate = useNavigate();
    const { user, clearUser } = useAuth();

    function loadTasks() {
        apiFetch(`/api/tasks?page=${page}&size=${size}&filter=${filter}`)
            .then(data => {
                setTasks(data.content);
                setPage(data.page);
                setSize(data.size)
                setTotalPages(data.totalPages);
                setTotalElements(data.totalElements);
                setPageFirst(data.first);
                setPageLast(data.last);
            })
            .catch(err => setError(err.message));
    }

    useEffect(() => {
        loadTasks();
    }, [page, size, filter]);

    async function handleLogout() {
        try {
            await apiFetch('/api/users/logout', { method: 'POST' });
        } catch {}
        clearUser();
        navigate('/login');
    }

    async function handleClose(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}/close`, { method: 'POST' });
            if (tasks.length === 1 && page > 0) {
                setPage(page - 1);
                return;
            }
            loadTasks();
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleReopen(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}/open`, { method: 'POST' });
            loadTasks();
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleDelete(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
            if (tasks.length === 1 && page > 0) {
                setPage(page - 1);
                return;
            }
            loadTasks();
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleCreate(e) {
        e.preventDefault();
        setError(null);
        try {
            await apiFetch('/api/tasks', {
                method: 'POST',
                body: JSON.stringify({ title }),
            });
            setTitle('');
            loadTasks();
        } catch (err) {
            setError(err.message);
        }
    }

    function handleFilterChange(newFilter) {
        setFilter(newFilter);
        setPage(0);
    }

    function handleSizeChange(newSize) {
        setSize(newSize);
        setPage(0);
    }

    return (
        <div className="tasks-container">
            <div className="tasks-header">
                <div className="app-header">Task Manager</div>
                <div className="app-header">
                    <p className="account-name">Signed in as <b>{user?.username}</b></p>
                    <button onClick={handleLogout} className="button-logout">Log out</button>
                </div>
            </div>

            {error && <p className="field-error">{error}</p>}

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
                            className={`filter-button ${filter === f ? 'active' : ''}`}
                            onClick={() => handleFilterChange(f)}
                        >
                            {f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>

                {totalPages > 1 && (
                    <div className="pagination">
                        <button onClick={() => setPage(p => p - 1)} disabled={pageFirst}>
                            ← Prev
                        </button>
                        <span className="page-info">
                        Page {page + 1} of {totalPages}
                            <span className="total-info"> ({totalElements} total)</span>
                    </span>
                        <button onClick={() => setPage(p => p + 1)} disabled={pageLast}>
                            Next →
                        </button>
                    </div>
                )}

                <label className="size-selector">
                    Per page:
                    <select value={size} onChange={e => handleSizeChange(e.target.value)}>
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={20}>20</option>
                        <option value={50}>50</option>
                    </select>
                </label>
            </div>

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
                                {task.state === 'ACTIVE' ? (
                                    <button className="action-button" onClick={() => handleClose(task.id)}>Close</button>
                                ) : (
                                    <button className="action-button" onClick={() => handleReopen(task.id)}>Open</button>
                                )}
                                <button className="action-button danger" onClick={() => handleDelete(task.id)}>Delete</button>
                            </div>
                        </div>
                    ))}
                </div>}

        </div>
    );
}