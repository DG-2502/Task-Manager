import {useState} from 'react';
import {Link} from 'react-router-dom';
import PageHeader from '../components/PageHeader';
import UserList from '../components/UserList';
import TaskList from '../components/TaskList';
import ExpandableTitle from '../components/ExpandableTitle';

export default function AdminPage() {
    const [selectedUser, setSelectedUser] = useState(null);

    return (
        <div className="page">
            <Link to="/tasks" className="page-nav">← Back to my tasks</Link>
            <PageHeader />

            <UserList onSelect={setSelectedUser} selectedId={selectedUser?.id} />

            {selectedUser && (
                <>
                    <ExpandableTitle
                        text={`Tasks of ${selectedUser.username}`}
                        className="section-title"
                    />
                    <TaskList key={selectedUser.id} userId={selectedUser.id} isAdmin />
                </>
            )}
        </div>
    );
}