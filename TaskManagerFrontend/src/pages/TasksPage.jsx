import {useEffect} from 'react';
import {Link} from 'react-router-dom';
import {useAuth} from '../auth/AuthContext';
import PageHeader from '../components/PageHeader';
import TaskList from '../components/TaskList';

export default function TasksPage() {
    const {user} = useAuth();

    useEffect(() => {
        document.title = 'My Tasks · Task Manager';
    }, []);

    return (
        <div className="page">
            {user?.status === 'ADMIN' && (
                <Link to="/admin" className="page-nav">Admin panel →</Link>
            )}
            <PageHeader/>
            <TaskList userId={user.id} urlKey="tasks"/>
        </div>
    );
}