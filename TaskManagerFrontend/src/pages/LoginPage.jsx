import {useState} from 'react';
import {useLocation, useNavigate, Link} from 'react-router-dom';
import {apiFetch} from '../api/client';
import {useAuth} from '../auth/AuthContext';

export default function LoginPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const {setUser} = useAuth();

    const prefillUsername = location.state?.registeredUsername ?? '';
    const successMessage = location.state?.successMessage ?? null;

    const [username, setUsername] = useState(prefillUsername);
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            const user = await apiFetch('/api/users/login', {
                method: 'POST',
                body: JSON.stringify({username, password}),
            });
            setUser(user);
            navigate('/tasks');
        } catch (err) {
            setError(err.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="card">
            <h1>Welcome back</h1>
            {successMessage && (
                <div className="success-banner">{successMessage}</div>
            )}
            <form onSubmit={handleSubmit}>
                <div className="inputs">
                    <input className="input"
                           type="text"
                           name="username"
                           autoComplete="username"
                           placeholder="Enter your username"
                           value={username}
                           onChange={e => {
                               setError(null);
                               setUsername(e.target.value)
                           }}
                           required
                    />

                    <input className="input"
                           type="password"
                           name="password"
                           autoComplete="current-password"
                           placeholder="Enter your password"
                           value={password}
                           onChange={e => {
                               setError(null);
                               setPassword(e.target.value)
                           }}
                           required
                    />
                </div>
                {error && <p className="field-error">{error}</p>}
                <button type="submit" disabled={loading}>
                    {loading ? 'Logging in...' : 'Log in'}
                </button>
            </form>
            <p><i>Don't have an account?</i> <Link to="/register">Create one</Link></p>
        </div>
    );
}