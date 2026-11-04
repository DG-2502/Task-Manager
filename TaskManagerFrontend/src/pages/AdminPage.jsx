import {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import PageHeader from '../components/PageHeader';
import UserList from '../components/UserList';
import TaskList from '../components/TaskList';
import ExpandableTitle from '../components/ExpandableTitle';
import GroupsSidebar from '../components/GroupsSidebar';
import useGroups from '../hooks/useGroups';

export default function AdminPage() {
    const [selectedUser, setSelectedUser] = useState(null);

    useEffect(() => {
        document.title = 'Admin · Task Manager';
    }, []);

    return (
        <div className="page">
            <Link to="/tasks" className="page-nav">← Back to my tasks</Link>
            <PageHeader/>

            <UserList
                onSelect={setSelectedUser}
                selectedId={selectedUser?.id}
                urlKey="users"
            />

            {selectedUser && (
                <AdminUserTasks key={selectedUser.id} user={selectedUser}/>
            )}
        </div>
    );
}

function AdminUserTasks({user}) {
    const [activeGroup, setActiveGroup] = useState('all');
    const [refreshKey, setRefreshKey] = useState(0);

    const {groups, error: groupsError, reload: reloadGroups} = useGroups(user.id);

    function handleGroupMutated(event) {
        if (event.type === 'delete') {
            if (activeGroup === String(event.groupId)) {
                setActiveGroup('all');
            } else {
                setRefreshKey(k => k + 1);
            }
        }
    }

    const groupIdProp = activeGroup === 'all' ? null : activeGroup;

    return (
        <>
            <ExpandableTitle
                text={`Tasks of ${user.username}`}
                className="section-title"
            />
            <div className="tasks-layout">
                <GroupsSidebar
                    userId={user.id}
                    activeGroup={activeGroup}
                    onSelect={setActiveGroup}
                    groups={groups}
                    groupsError={groupsError}
                    reloadGroups={reloadGroups}
                    onGroupMutated={handleGroupMutated}
                />

                <main className="tasks-main">
                    <TaskList
                        userId={user.id}
                        isAdmin
                        groupId={groupIdProp}
                        groups={groups}
                        externalRefreshKey={refreshKey}
                    />
                </main>
            </div>
        </>
    );
}