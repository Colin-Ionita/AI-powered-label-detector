import { useMemo, useState } from 'react';
import type { BatchVerificationResponse, VerificationStatus } from '../types/api';
import { ResultPanel } from './ResultPanel';
import { StatusBadge } from './StatusBadge';

type Props = {
  batch: BatchVerificationResponse;
};

type Filter = 'ALL' | VerificationStatus;

export function BatchResults({ batch }: Props) {
  const [filter, setFilter] = useState<Filter>('ALL');
  const [expanded, setExpanded] = useState<string | null>(null);
  const filtered = useMemo(() => {
    return batch.results.filter((result) => filter === 'ALL' || result.overallStatus === filter);
  }, [batch.results, filter]);

  return (
    <section className="batch-panel">
      <div className="batch-summary">
        <div>
          <p className="eyebrow">Batch progress</p>
          <h2>{batch.status.processed} of {batch.status.total} processed</h2>
        </div>
        <div className="summary-grid">
          <span><strong>{batch.status.pass}</strong> Pass</span>
          <span><strong>{batch.status.needsReview}</strong> Review</span>
          <span><strong>{batch.status.fail}</strong> Fail</span>
          <span><strong>{batch.status.unreadable}</strong> Unreadable</span>
        </div>
      </div>
      <progress value={batch.status.processed} max={batch.status.total || 1} />

      <div className="filter-row">
        {(['ALL', 'PASS', 'NEEDS_REVIEW', 'FAIL', 'UNREADABLE'] as Filter[]).map((value) => (
          <button
            key={value}
            className={filter === value ? 'selected secondary' : 'secondary'}
            type="button"
            onClick={() => setFilter(value)}
          >
            {value === 'ALL' ? 'All' : value.replace('_', ' ').toLowerCase()}
          </button>
        ))}
      </div>

      <div className="batch-list">
        {filtered.map((result) => (
          <article key={result.labelId} className="batch-row">
            <button type="button" onClick={() => setExpanded(expanded === result.labelId ? null : result.labelId)}>
              <span>{result.filename}</span>
              <StatusBadge status={result.overallStatus} />
            </button>
            {expanded === result.labelId && <ResultPanel result={result} />}
          </article>
        ))}
      </div>
    </section>
  );
}
