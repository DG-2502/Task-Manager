import {useLayoutEffect, useRef, useState} from 'react';

export default function ExpandableTitle({text, className = ''}) {
    const ref = useRef(null);
    const [expanded, setExpanded] = useState(false);
    const [overflowing, setOverflowing] = useState(false);

    useLayoutEffect(() => {
        const el = ref.current;
        if (!el) return;

        const measure = () => {
            if (!expanded) {
                setOverflowing(el.scrollWidth > el.clientWidth + 1);
            }
        };

        measure();
        const ro = new ResizeObserver(measure);
        ro.observe(el);
        return () => ro.disconnect();
    }, [text, expanded]);

    const interactive = overflowing;

    function handleClick(e) {
        if (!interactive) return;
        e.stopPropagation();
        setExpanded(v => !v);
    }

    function handleKeyDown(e) {
        if (!interactive) return;
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            setExpanded(v => !v);
        }
    }

    return (
        <div
            ref={ref}
            className={
                `expandable-title ${className} ` +
                `${expanded ? 'expanded' : ''} ${interactive ? 'truncated' : ''}`
            }
            onClick={handleClick}
            onKeyDown={handleKeyDown}
            title={interactive && !expanded ? text : undefined}
            role={interactive ? 'button' : undefined}
            tabIndex={interactive ? 0 : undefined}
            aria-expanded={interactive ? expanded : undefined}
        >
            {text}
        </div>
    );
}