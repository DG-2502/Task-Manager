import {apiFetch} from "../api/client.js";
import {useState} from "react";
import {useNavigate, Link} from "react-router-dom";

export default function RegisterPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [error, setError] = useState(null);
    const [errorField, setErrorField] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        setErrorField(null)

        if (password !== passwordConfirm) {
            setError("Passwords are different!");
            setErrorField("passwordConfirm")
            return;
        }

        setLoading(true);
        try {
            await apiFetch('/api/users/register', {
                method: 'POST',
                body: JSON.stringify({username, password, passwordConfirm}),
            });
            navigate('/login', {
                state: {
                    registeredUsername: username,
                    successMessage: "Account created successfully. Please log in.",
                }
            });
        } catch (err) {
            setError(err.message || 'Login failed');
            setErrorField(err.field);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="card">
            <h1>Register</h1>
            <form onSubmit={handleSubmit}>
                <div className="inputs">
                    <input className={`input ${errorField === 'username' ? 'input-error' : ''}`}
                           type="text"
                           name="username"
                           autoComplete="username"
                           placeholder="Enter your username"
                           value={username}
                           onChange={e => setUsername(e.target.value)}
                           required
                    />

                    <input className={`input ${errorField === 'password' ? 'input-error' : ''}`}
                           type="password"
                           name="password"
                           autoComplete="new-password"
                           placeholder="Enter your password"
                           value={password}
                           onChange={e => setPassword(e.target.value)}
                           required
                    />

                    <input className={`input ${errorField === 'passwordConfirm' ? 'input-error' : ''}`}
                           type="password"
                           name="passwordConfirm"
                           autoComplete="new-password"
                           placeholder="Repeat your password"
                           value={passwordConfirm}
                           onChange={e => setPasswordConfirm(e.target.value)}
                           required
                    />
                </div>
                {error && <p className="field-error">{error}</p>}
                <button type="submit" disabled={loading}>
                    {loading ? 'Registering…' : 'Register'}
                </button>
            </form>
            <p><i>Already have an account?</i> <Link to="/login">Log in</Link></p>
        </div>
    );
}