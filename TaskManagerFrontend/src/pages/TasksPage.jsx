import {Link, useNavigate} from 'react-router-dom';
import {apiFetch} from '../api/client';
import {useAuth} from '../auth/AuthContext';
import TaskList from "../components/TaskList.jsx";

export default function TasksPage() {
    const navigate = useNavigate();
    const {user, clearUser} = useAuth();

    async function handleLogout() {
        try {
            await apiFetch('/api/users/logout', {method: 'POST'});
        } catch {
        }
        clearUser();
        navigate('/login');
    }

    return (
        <div className="tasks-container">
            {user?.status === "ADMIN" && (
                <Link to="/admin">Admin</Link>
            )}
            <div className="tasks-header">
                <div className="app-header">Task Manager</div>
                <div className="app-header">
                    <p className="account-name">Signed in as <b>{user?.username}</b></p>
                    <button onClick={handleLogout} className="button-logout">Log out</button>
                </div>
            </div>
            <TaskList userId={user.id}/>

        </div>
    );
}