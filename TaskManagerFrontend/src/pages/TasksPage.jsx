import {useEffect, useState} from 'react';
import {Link, useSearchParams} from 'react-router-dom';
import {useAuth} from '../auth/AuthContext';
import PageHeader from '../components/PageHeader';
import TaskList from '../components/TaskList';
import GroupsSidebar from '../components/GroupsSidebar';
import useGroups from '../hooks/useGroups';

export default function TasksPage() {
    const {user} = useAuth();
    const [searchParams, setSearchParams] = useSearchParams();
    const activeGroup = searchParams.get('group') ?? 'all';

    const {groups, error: groupsError, reload: reloadGroups} = useGroups();

    const [taskRefreshKey, setTaskRefreshKey] = useState(0);

    useEffect(() => {
        document.title = 'My Tasks · Task Manager';
    }, []);

    function setActiveGroup(value) {
        const next = new URLSearchParams(searchParams);
        if (value === 'all') next.delete('group');
        else next.set('group', value);
        next.delete('tasks.page');
        setSearchParams(next);
    }

    function handleGroupMutated(event) {
        if (event.type === 'delete') {
            if (activeGroup === String(event.groupId)) {
                setActiveGroup('all');
            } else {
                setTaskRefreshKey(k => k + 1);
            }
        }
    }

    const groupIdProp = activeGroup === 'all' ? null : activeGroup;

    return (
        <div className="page">
            {user?.status === 'ADMIN' && (
                <Link to="/admin" className="page-nav">Admin panel →</Link>
            )}
            <PageHeader/>

            <div className="tasks-layout">
                <GroupsSidebar
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
                        urlKey="tasks"
                        groupId={groupIdProp}
                        groups={groups}
                        externalRefreshKey={taskRefreshKey}
                    />
                </main>
            </div>
        </div>
    );
}