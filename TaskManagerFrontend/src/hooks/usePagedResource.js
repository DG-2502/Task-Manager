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
    const [search, setSearch] = useState('');
    const [appliedSearch, setAppliedSearch] = useState('');
    const [fetchError, setFetchError] = useState(null);
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        apiFetch(buildUrl({page, size, filter, search: appliedSearch}))
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
    }, [page, size, filter, reloadKey, buildUrl, appliedSearch]);

    function applySearch() {
        setAppliedSearch(search);
        setPage(0);
    }

    function clearSearch() {
        setSearch('');
        setAppliedSearch('');
        setPage(0);
    }

    return {
        content,
        fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        search, setSearch,
        applySearch, clearSearch,
        ...meta,
        reload: () => setReloadKey(k => k + 1),
    };
}