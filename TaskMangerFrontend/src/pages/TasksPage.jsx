import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {apiFetch} from '../api/client';
import {useAuth} from '../auth/AuthContext';

export default function TasksPage() {
    console.log("Task page method is being run");
    const [tasks, setTasks] = useState([]);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const {user, clearUser} = useAuth();
    const [title, setTitle] = useState('');

    useEffect(() => {
        console.log("Effects on TaskPage are being run");
        apiFetch('/api/tasks/')
            .then(setTasks)
            .catch(err => setError(err.message));
    }, []);

    async function handleLogout() {
        try {
            await apiFetch('/api/users/logout', {method: 'POST'});
        } catch {
        }
        clearUser();
        navigate('/login');
    }

    async function handleClose(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}/close`, {method: "POST"});
            setTasks(prev => prev.map(task =>
                task.id === taskId ? {...task, state: "CLOSED"} : task))
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleOpen(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}/open`, {method: "POST"});
            setTasks(prev => prev.map(task =>
                task.id === taskId ? {...task, state: "ACTIVE"} : task))
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleDelete(taskId) {
        setError(null);
        try {
            await apiFetch(`/api/tasks/${taskId}`, {method: "DELETE"});
            setTasks(prev => prev.filter(task => task.id !== taskId))
        } catch (err) {
            setError(err.message);
        }
    }

    async function handleCreate(e) {
        e.preventDefault()
        setError(null);
        try {
            const task = await apiFetch('/api/tasks/', {
                method: "POST",
                body: JSON.stringify({title}),
            });
            setTasks(prev => [...prev, task]);
            setTitle('')
        } catch (err) {
            setError(err.message);
        }
    }


    return (
        <div className="tasks-container">
            <div className="tasks-header">
                <div className="app-header">Task Manager</div>
                <div className="app-header">
                    <p className="account-name">Signed in as <b>{user?.username}</b></p>
                    <button onClick={handleLogout} className="button-logout">
                        Log out
                    </button>
                </div>
            </div>

            {error && <p className="field-error">{error}</p>}

            <div className="menu">My tassks
                Menu will be here
            </div>

            <form onSubmit={handleCreate}>
                <input className="input"
                       type="text"
                       name="title"
                       value={title}
                       placeholder="Create a new task..."
                       required
                       autoComplete="off"
                       onChange={e => setTitle(e.target.value)}
                />
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
                                {task.state === "ACTIVE" ? (
                                    <button className="action-button" onClick={() => {
                                        handleClose(task.id)
                                    }}>Close</button>
                                ) : (
                                    <button className="action-button" onClick={() => {
                                        handleOpen(task.id)
                                    }}>Open</button>)}
                                <button className="action-button danger" onClick={() => {
                                    handleDelete(task.id)
                                }}>Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>}
        </div>
    );
}