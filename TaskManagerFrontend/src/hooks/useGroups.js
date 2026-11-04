import {useCallback, useEffect, useState} from 'react';
import {apiFetch} from '../api/client';

export default function useGroups(userId = undefined) {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const reload = useCallback(() => {
        setLoading(true);
        const base = userId === undefined
            ? '/api/groups'
            : `/api/admin/users/${userId}/groups`;
        apiFetch(`${base}?page=0&size=100&sortBy=title&direction=asc`)
            .then(data => {
                setGroups(data.content);
                setError(null);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [userId]);

    useEffect(() => {
        reload();
    }, [reload]);

    return {groups, loading, error, reload};
}