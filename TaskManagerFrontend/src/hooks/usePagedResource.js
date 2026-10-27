import {useEffect, useState} from 'react';
import {useSearchParams} from 'react-router-dom';
import {apiFetch} from '../api/client';

const DEFAULTS = {
    page: 0,
    size: 10,
    filter: 'ALL',
    search: '',
    q: '',
};

export default function usePagedResource(buildUrl, {urlKey = null} = {}) {
    const [searchParams, setSearchParams] = useSearchParams();
    const prefix = urlKey ? `${urlKey}.` : '';

    const [local, setLocal] = useState({
        page: DEFAULTS.page,
        size: DEFAULTS.size,
        filter: DEFAULTS.filter,
        search: DEFAULTS.search,
        appliedSearch: '',
    });
    const [content, setContent] = useState([]);
    const [meta, setMeta] = useState({totalPages: 0, totalElements: 0, first: true, last: true});
    const [fetchError, setFetchError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [reloadKey, setReloadKey] = useState(0);

    const read = (key) => {
        const urlValue = searchParams.get(`${prefix}${key}`);
        return urlValue !== null ? urlValue : null;
    };

    const page = urlKey ? Number(read('page') ?? DEFAULTS.page) : local.page;
    const size = urlKey ? Number(read('size') ?? DEFAULTS.size) : local.size;
    const filter = urlKey ? (read('filter') ?? DEFAULTS.filter) : local.filter;
    const search = urlKey ? (read('search') ?? DEFAULTS.search) : local.search;
    const appliedSearch = urlKey ? (read('q') ?? DEFAULTS.q) : local.appliedSearch;

    const update = (patchObj, {replace = false} = {}) => {
        if (urlKey) {
            setSearchParams(prev => {
                const next = new URLSearchParams(prev);
                for (const [k, v] of Object.entries(patchObj)) {
                    const isDefault = v == null || v === '' || v === DEFAULTS[k];
                    if (isDefault) next.delete(`${prefix}${k}`);
                    else next.set(`${prefix}${k}`, String(v));
                }
                return next;
            }, {replace});
        } else {
            setLocal(prev => {
                const next = {...prev, ...patchObj};
                if (patchObj.search !== undefined) next.search = patchObj.search;
                if (patchObj.q !== undefined) next.appliedSearch = patchObj.q;
                return next;
            });
        }
    };

    const setPage = (v) => {
        if (urlKey) {
            setSearchParams(prev => {
                const next = new URLSearchParams(prev);
                const current = Number(next.get(`${prefix}page`) ?? 0);
                const newValue = typeof v === 'function' ? v(current) : v;
                if (newValue === DEFAULTS.page) next.delete(`${prefix}page`);
                else next.set(`${prefix}page`, String(newValue));
                return next;
            });
        } else {
            setLocal(s => ({...s, page: typeof v === 'function' ? v(s.page) : v}));
        }
    };
    const setSize = (v) => update({size: v, page: DEFAULTS.page});
    const setFilter = (v) => update({filter: v, page: DEFAULTS.page});
    const setSearch = (v) => update({search: v}, {replace: true});
    const applySearch = () => update({q: search, page: DEFAULTS.page});
    const clearSearch = () => update({search: '', q: '', page: DEFAULTS.page});

    useEffect(() => {
        setLoading(true);
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
                if (data.content.length === 0 && page > 0) {
                    setPage(p => p - 1);
                }
            })
            .catch(err => setFetchError(err.message))
            .finally(() => setLoading(false));
    }, [page, size, filter, appliedSearch, reloadKey, buildUrl]);

    return {
        content, loading, fetchError,
        page, size, filter,
        setPage, setSize, setFilter,
        search, setSearch,
        applySearch, clearSearch,
        ...meta,
        reload: () => setReloadKey(k => k + 1),
    };
}