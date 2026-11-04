import {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {apiFetch} from '../api/client';
import {useAuth} from '../auth/AuthContext';

export default function ProfilePage() {
    const {user, setUser} = useAuth();

    useEffect(() => {
        document.title = 'Profile · Task Manager';
    }, []);

    const [username, setUsername] = useState(user?.username ?? '');
    const [usernameError, setUsernameError] = useState(null);
    const [usernameSuccess, setUsernameSuccess] = useState(null);
    const [savingUsername, setSavingUsername] = useState(false);

    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState(null);
    const [passwordErrorField, setPasswordErrorField] = useState(null);
    const [passwordSuccess, setPasswordSuccess] = useState(null);
    const [savingPassword, setSavingPassword] = useState(false);

    async function handleUsernameSubmit(e) {
        e.preventDefault();
        setUsernameError(null);
        setUsernameSuccess(null);

        if (username === user.username) {
            setUsernameError('New username is the same as current');
            return;
        }

        setSavingUsername(true);
        try {
            await apiFetch('/api/users/me', {
                method: 'PATCH',
                body: JSON.stringify({username}),
            });
            const fresh = await apiFetch('/api/users/me');
            setUser(fresh);
            setUsernameSuccess('Username updated');
        } catch (err) {
            setUsernameError(err.message);
        } finally {
            setSavingUsername(false);
        }
    }

    async function handlePasswordSubmit(e) {
        e.preventDefault();
        setPasswordError(null);
        setPasswordSuccess(null);
        setPasswordErrorField(null);

        if (newPassword !== confirmPassword) {
            setPasswordError('Passwords do not match');
            setPasswordErrorField("confirmPassword")
            return;
        }

        setSavingPassword(true);
        try {
            await apiFetch('/api/users/password', {
                method: 'PATCH',
                body: JSON.stringify({currentPassword, newPassword}),
            });
            setCurrentPassword('');
            setNewPassword('');
            setConfirmPassword('');
            setPasswordSuccess('Password changed');
        } catch (err) {
            setPasswordError(err.message);
            setPasswordErrorField(err.field);
        } finally {
            setSavingPassword(false);
        }
    }

    return (
        <div className="page">
            <Link to="/tasks" className="page-nav">← Back to my tasks</Link>

            <h1 className="section-title">Your profile</h1>

            <div className="card">
                <h2 className="card-heading">Account</h2>
                <form onSubmit={handleUsernameSubmit}>
                    <div className="inputs">
                        <label className="form-label" htmlFor="username">Username</label>
                        <input
                            id="username"
                            className="input"
                            type="text"
                            value={username}
                            onChange={e => {
                                setUsername(e.target.value);
                                setUsernameError(null);
                                setUsernameSuccess(null);
                            }}
                            maxLength={255}
                            required
                        />
                    </div>
                    {usernameError && <p className="field-error">{usernameError}</p>}
                    {usernameSuccess && <p className="success-banner">{usernameSuccess}</p>}
                    <button type="submit" disabled={savingUsername}>
                        {savingUsername ? 'Saving...' : 'Update username'}
                    </button>
                </form>
            </div>

            <div className="card">
                <h2 className="card-heading">Change password</h2>
                <form onSubmit={handlePasswordSubmit}>
                    <div className="inputs">
                        <label className="form-label" htmlFor="currentPassword">Current password</label>
                        <input
                            id="currentPassword"
                            className={`input ${passwordErrorField === 'currentPassword' ? 'input-error' : ''}`}
                            type="password"
                            autoComplete="current-password"
                            value={currentPassword}
                            onChange={e => {
                                setCurrentPassword(e.target.value);
                                setPasswordError(null);
                                setPasswordErrorField(null);
                                setPasswordSuccess(null);
                            }}
                            required
                        />

                        <label className="form-label" htmlFor="newPassword">New password</label>
                        <input
                            id="newPassword"
                            className={`input ${passwordErrorField === 'newPassword' ? 'input-error' : ''}`}
                            type="password"
                            autoComplete="new-password"
                            value={newPassword}
                            onChange={e => {
                                setNewPassword(e.target.value);
                                setPasswordError(null);
                                setPasswordErrorField(null);
                                setPasswordSuccess(null);
                            }}
                            minLength={3}
                            required
                        />

                        <label className="form-label" htmlFor="confirmPassword">Confirm new password</label>
                        <input
                            id="confirmPassword"
                            className={`input ${passwordErrorField === 'confirmPassword' ? 'input-error' : ''}`}
                            type="password"
                            autoComplete="new-password"
                            value={confirmPassword}
                            onChange={e => {
                                setConfirmPassword(e.target.value);
                                setPasswordError(null);
                                setPasswordErrorField(null);
                                setPasswordSuccess(null);
                            }}
                            minLength={3}
                            required
                        />
                    </div>
                    {passwordError && <p className="field-error">{passwordError}</p>}
                    {passwordSuccess && <p className="success-banner">{passwordSuccess}</p>}
                    <button type="submit" disabled={savingPassword}>
                        {savingPassword ? 'Updating...' : 'Change password'}
                    </button>
                </form>
            </div>
        </div>
    );
}