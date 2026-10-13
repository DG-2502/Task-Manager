import {useEffect, useState} from 'react';
import {apiFetch} from '../api/client';

export default function usePagedResource(buildUrl) {
    const [content, setContent] = useState([]);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [filter, setFilter] = useState('ALL');
    const [meta, setMeta] = useState({
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });
    const [fetchError, setFetchError] = useState(null);
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        apiFetch(buildUrl({page, size, filter}))
            .then(data => {
                setContent(data.content);
                setMeta({
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    first: data.first,
                    last: data.last,
                });
                setFetchError(null);
            })
            .catch(err => setFetchError(err.message));
    }, [page, size, filter, reloadKey, buildUrl]);

    return {
        content,
        fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        ...meta,
        reload: () => setReloadKey(k => k + 1),
    };
}