import { useId, useRef, useState } from 'react';

type Props = {
  files: File[];
  multiple: boolean;
  onFilesChange: (files: File[]) => void;
};

export function FileDropzone({ files, multiple, onFilesChange }: Props) {
  const inputId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);

  function acceptFiles(fileList: FileList | null) {
    if (!fileList) return;
    const next = Array.from(fileList).filter((file) => ['image/jpeg', 'image/png', 'image/webp'].includes(file.type));
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
      {files.length > 0 && (
        <div className="file-list" aria-live="polite">
          {files.map((file) => (
            <span key={`${file.name}-${file.size}`}>{file.name}</span>
          ))}
          <button type="button" className="secondary small" onClick={() => {
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
