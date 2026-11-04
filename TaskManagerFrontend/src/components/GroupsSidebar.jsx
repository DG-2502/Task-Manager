import {useState} from 'react';
import {apiFetch} from '../api/client';

export default function GroupsSidebar({
                                          userId = undefined,
                                          activeGroup,
                                          onSelect,
                                          groups,
                                          groupsError,
                                          reloadGroups,
                                          onGroupMutated,
                                      }) {
    const [showCreate, setShowCreate] = useState(false);
    const [newTitle, setNewTitle] = useState('');
    const [creating, setCreating] = useState(false);
    const [creatingError, setCreatingError] = useState(null);

    const [editingGroupId, setEditingGroupId] = useState(null);
    const [editingTitle, setEditingTitle] = useState('');
    const [savingEdit, setSavingEdit] = useState(false);
    const [editError, setEditError] = useState(null);

    const [mutationError, setMutationError] = useState(null);

    const createUrl = userId === undefined
        ? '/api/groups'
        : `/api/admin/users/${userId}/groups`;

    async function handleCreate(e) {
        e.preventDefault();
        setCreating(true);
        setCreatingError(null);
        try {
            await apiFetch(createUrl, {
                method: 'POST',
                body: JSON.stringify({title: newTitle}),
            });
            setNewTitle('');
            setShowCreate(false);
            reloadGroups();
        } catch (err) {
            setCreatingError(err.message);
        } finally {
            setCreating(false);
        }
    }

    function startEdit(g) {
        setMutationError(null);
        setEditingGroupId(g.id);
        setEditingTitle(g.title);
        setEditError(null);
    }

    function cancelEdit() {
        setEditingGroupId(null);
        setEditingTitle('');
        setEditError(null);
    }

    async function handleSaveEdit(e) {
        e.preventDefault();
        if (editingGroupId == null) return;
        setSavingEdit(true);
        setEditError(null);
        try {
            await apiFetch(`/api/groups/${editingGroupId}`, {
                method: 'PATCH',
                body: JSON.stringify({title: editingTitle}),
            });
            cancelEdit();
            reloadGroups();
        } catch (err) {
            setEditError(err.message);
        } finally {
            setSavingEdit(false);
        }
    }

    async function handleDelete(g) {
        if (!window.confirm(
            `Delete group "${g.title}"?\n\n` +
            `WARNING: this also permanently deletes ALL tasks in this group. ` +
            `This cannot be undone.`
        )) return;

        setMutationError(null);
        try {
            await apiFetch(`/api/groups/${g.id}`, {method: 'DELETE'});
            reloadGroups();
            onGroupMutated?.({type: 'delete', groupId: g.id});
        } catch (err) {
            setMutationError(err.message);
        }
    }

    return (
        <aside className="groups-sidebar">
            <h3 className="groups-heading">Groups</h3>

            {groupsError && <p className="field-error">{groupsError}</p>}
            {mutationError && <p className="field-error">{mutationError}</p>}

            <nav>
                <ul className="groups-list">
                    <li>
                        <button
                            type="button"
                            className={`group-link ${activeGroup === 'all' ? 'active' : ''}`}
                            onClick={() => onSelect('all')}
                        >
                            All tasks
                        </button>
                    </li>
                    <li>
                        <button
                            type="button"
                            className={`group-link ${activeGroup === 'ungrouped' ? 'active' : ''}`}
                            onClick={() => onSelect('ungrouped')}
                        >
                            No group
                        </button>
                    </li>
                    {groups.map(g =>
                        editingGroupId === g.id ? (
                            <li key={g.id}>
                                <form onSubmit={handleSaveEdit} className="group-edit-form">
                                    <input
                                        type="text"
                                        className="edit-input"
                                        value={editingTitle}
                                        onChange={e => setEditingTitle(e.target.value)}
                                        autoFocus
                                        required
                                        maxLength={255}
                                        onKeyDown={e => {
                                            if (e.key === 'Escape') {
                                                e.preventDefault();
                                                cancelEdit();
                                            }
                                        }}
                                    />
                                    <button type="submit" className="action-button" disabled={savingEdit}>
                                        {savingEdit ? '…' : 'Save'}
                                    </button>
                                    <button
                                        type="button"
                                        className="action-button secondary"
                                        onClick={cancelEdit}
                                        disabled={savingEdit}
                                    >
                                        ✕
                                    </button>
                                </form>
                                {editError && <p className="field-error">{editError}</p>}
                            </li>
                        ) : (
                            <li key={g.id} className="group-item">
                                <button
                                    type="button"
                                    className={`group-link ${activeGroup === String(g.id) ? 'active' : ''}`}
                                    onClick={() => onSelect(String(g.id))}
                                    title={g.title}
                                >
                                    {g.title}
                                </button>
                                <div className="group-actions">
                                    <button
                                        type="button"
                                        className="icon-button"
                                        onClick={() => startEdit(g)}
                                        title="Rename group"
                                    >
                                        ✎
                                    </button>
                                    <button
                                        type="button"
                                        className="icon-button danger"
                                        onClick={() => handleDelete(g)}
                                        title="Delete group"
                                    >
                                        🗑
                                    </button>
                                </div>
                            </li>
                        )
                    )}
                </ul>
            </nav>

            {showCreate ? (
                <form onSubmit={handleCreate} className="group-create-form">
                    <input
                        type="text"
                        className="input"
                        placeholder="Group name"
                        value={newTitle}
                        onChange={e => setNewTitle(e.target.value)}
                        autoFocus
                        required
                        maxLength={255}
                    />
                    <div className="group-create-actions">
                        <button type="submit" className="action-button" disabled={creating}>
                            {creating ? 'Creating...' : 'Create'}
                        </button>
                        <button
                            type="button"
                            className="action-button secondary"
                            onClick={() => {
                                setShowCreate(false);
                                setNewTitle('');
                                setCreatingError(null);
                            }}
                        >
                            Cancel
                        </button>
                    </div>
                    {creatingError && <p className="field-error">{creatingError}</p>}
                </form>
            ) : (
                <button
                    type="button"
                    className="toggle-form-button"
                    onClick={() => setShowCreate(true)}
                >
                    + New group
                </button>
            )}
        </aside>
    );
}