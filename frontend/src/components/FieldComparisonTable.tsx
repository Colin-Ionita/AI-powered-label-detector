import type { FieldMatchResult } from '../types/api';
import { StatusBadge } from './StatusBadge';

type Props = {
  fields: FieldMatchResult[];
};

export function FieldComparisonTable({ fields }: Props) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Field</th>
            <th>Expected</th>
            <th>Extracted</th>
            <th>Status</th>
            <th>Confidence</th>
            <th>Reason</th>
          </tr>
        </thead>
        <tbody>
          {fields.map((field) => (
            <tr key={field.fieldKey}>
              <th scope="row">{field.displayName}</th>
              <td>{field.expectedValue || 'Not supplied'}</td>
              <td>{field.extractedValue || 'Not found'}</td>
              <td><StatusBadge status={field.status} /></td>
              <td>{field.confidence}%</td>
              <td>{field.reason}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
