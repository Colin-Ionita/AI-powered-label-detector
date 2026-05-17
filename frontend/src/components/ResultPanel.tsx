import type { VerificationResponse } from '../types/api';
import { FieldComparisonTable } from './FieldComparisonTable';
import { StatusBadge } from './StatusBadge';

type Props = {
  result: VerificationResponse;
};

export function ResultPanel({ result }: Props) {
  return (
    <section className="result-panel" aria-live="polite">
      <div className={`result-banner banner-${result.overallStatus.toLowerCase()}`}>
        <div>
          <p className="eyebrow">Verification result</p>
          <h2><StatusBadge status={result.overallStatus} /></h2>
        </div>
        <div className="metric">
          <span>{result.processingTimeMs} ms</span>
          <small>Processing time</small>
        </div>
      </div>

      {result.reviewReasons.length > 0 && (
        <div className="notice">
          <strong>Review reasons</strong>
          <ul>
            {result.reviewReasons.map((reason) => <li key={reason}>{reason}</li>)}
          </ul>
        </div>
      )}

      <section className="panel-section">
        <h3>Field comparison</h3>
        <FieldComparisonTable fields={result.fields} />
      </section>

      <section className="panel-section warning-panel">
        <div>
          <h3>Government warning</h3>
          <p>{result.governmentWarning.reason}</p>
        </div>
        <StatusBadge status={result.governmentWarning.status} />
      </section>

      <details className="panel-section">
        <summary>Raw OCR text ({result.ocr.provider}, {result.ocr.confidence}% confidence)</summary>
        <pre>{result.ocr.fullText || 'No OCR text returned.'}</pre>
      </details>
    </section>
  );
}
