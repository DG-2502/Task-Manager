import {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import UserList from '../components/UserList';
import TaskList from '../components/TaskList';
import {apiFetch} from "../api/client.js";
import {useAuth} from "../auth/AuthContext.jsx";

export default function AdminPage() {
    const {user, clearUser} = useAuth();
    const navigate = useNavigate();
    const [selectedUser, setSelectedUser] = useState(null);

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
            <Link to="/tasks">Back to my tasks</Link>

            <div className="tasks-header">
                <div className="app-header">Task Manager</div>
                <div className="app-header">
                    <p className="account-name">Signed in as <b>{user?.username}</b></p>
                    <button onClick={handleLogout} className="button-logout">Log out</button>
                </div>
            </div>

            <UserList onSelect={setSelectedUser} selectedId={selectedUser?.id}/>

            {selectedUser && (
                <>
                    <h2>Tasks of {selectedUser.username}</h2>
                    <TaskList userId={selectedUser.id} isAdmin={true}/>
                </>
            )}
        </div>
    );
}