import {Link, useNavigate} from 'react-router-dom';
import {apiFetch} from '../api/client.js';
import {useAuth} from '../auth/AuthContext.jsx';

export default function PageHeader() {
    const {user, clearUser} = useAuth();
    const navigate = useNavigate();

    async function handleLogout() {
        try {
            await apiFetch('/api/users/logout', {method: 'POST'});
        } catch {
        }
        clearUser();
        navigate('/login');
    }

    return (
        <header className="page-header">
            <div className="page-header-title">Task Manager</div>
            <div className="page-header-account">
                <Link to="/profile" className="account-link">
                    Signed in as <b>{user?.username}</b>
                </Link>
                <button type="button" onClick={handleLogout} className="button-logout">
                    Log out
                </button>
            </div>
        </header>
    );
}