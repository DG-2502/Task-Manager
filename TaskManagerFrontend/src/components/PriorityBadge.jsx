const LABELS = {
    HIGH: 'High',
    MEDIUM: 'Medium',
    LOW: 'Low',
};

export default function PriorityBadge({priority}) {
    if (!priority) return null;

    const key = String(priority).toUpperCase();
    if (!LABELS[key]) return null;

    return (
        <span
            className={`priority-badge priority-${key.toLowerCase()}`}
            title={`Priority: ${LABELS[key]}`}
        >
            {LABELS[key]}
        </span>
    );
}