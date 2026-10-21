import {Navigate} from 'react-router-dom';
import {useAuth} from './AuthContext';

export default function AdminRoute({children}) {
    const {user, loading} = useAuth();
    if (loading) return <div className="loading">Loading...</div>;
    if (!user) return <Navigate to="/login" replace/>;
    if (user.status !== 'ADMIN') return <Navigate to="/tasks" replace/>;
    return children;
}