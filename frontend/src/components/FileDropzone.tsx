import { useId, useRef, useState } from 'react';

const maxFileBytes = 10 * 1024 * 1024;
const acceptedTypes = ['image/jpeg', 'image/png', 'image/webp'];

type Props = {
  files: File[];
  multiple: boolean;
  onFilesChange: (files: File[]) => void;
};

export function FileDropzone({ files, multiple, onFilesChange }: Props) {
  const inputId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);
  const [rejectionMessage, setRejectionMessage] = useState<string | null>(null);

  function acceptFiles(fileList: FileList | null) {
    if (!fileList) return;
    const selected = Array.from(fileList);
    const next = selected.filter((file) => acceptedTypes.includes(file.type) && file.size <= maxFileBytes);
    const rejected = selected.length - next.length;
    setRejectionMessage(rejected > 0 ? `${rejected} file${rejected === 1 ? '' : 's'} skipped. Use JPEG, PNG, or WebP files up to 10 MB.` : null);
    onFilesChange(multiple ? next : next.slice(0, 1));
  }

  return (
    <section
      className={`dropzone ${dragging ? 'is-dragging' : ''}`}
      onDragOver={(event) => {
        event.preventDefault();
        setDragging(true);
      }}
      onDragLeave={() => setDragging(false)}
      onDrop={(event) => {
        event.preventDefault();
        setDragging(false);
        acceptFiles(event.dataTransfer.files);
      }}
    >
      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple={multiple}
        onChange={(event) => acceptFiles(event.target.files)}
      />
      <label htmlFor={inputId}>
        <strong>{multiple ? 'Choose label images' : 'Choose a label image'}</strong>
        <span>Drop JPEG, PNG, or WebP files here, or click to browse. Each file must be 10 MB or smaller.</span>
      </label>
      {rejectionMessage && <p className="dropzone-error" role="alert">{rejectionMessage}</p>}
      {files.length > 0 && (
        <div className="file-list" aria-live="polite">
          {files.map((file) => (
            <span key={`${file.name}-${file.size}`}>{file.name}</span>
          ))}
          <button type="button" className="secondary small" onClick={() => {
            setRejectionMessage(null);
            onFilesChange([]);
            if (inputRef.current) inputRef.current.value = '';
          }}>
            Clear
          </button>
        </div>
      )}
    </section>
  );
}
