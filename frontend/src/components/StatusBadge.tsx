import type { MatchStatus, VerificationStatus } from '../types/api';

type Props = {
  status: VerificationStatus | MatchStatus;
};

const labels: Record<string, string> = {
  PASS: 'Pass',
  FAIL: 'Fail',
  NEEDS_REVIEW: 'Needs review',
  UNREADABLE: 'Unreadable',
  MATCH: 'Match',
  MISMATCH: 'Mismatch',
  MISSING: 'Missing',
  LOW_CONFIDENCE: 'Needs review',
  NOT_APPLICABLE: 'Not applicable'
};

export function StatusBadge({ status }: Props) {
  return (
    <span className={`status-badge status-${status.toLowerCase()}`}>
      <span aria-hidden="true">{iconFor(status)}</span>
      {labels[status] ?? status}
    </span>
  );
}

function iconFor(status: string) {
  if (status === 'PASS' || status === 'MATCH') return 'OK';
  if (status === 'FAIL' || status === 'MISMATCH' || status === 'MISSING') return '!';
  if (status === 'NOT_APPLICABLE') return '-';
  return '?';
}
