import { useEffect, useMemo, useRef, useState } from 'react';
import { getBatchResults, startBatch, verifySingleLabel } from './api/labelVerificationApi';
import { BatchResults } from './components/BatchResults';
import { FileDropzone } from './components/FileDropzone';
import { ResultPanel } from './components/ResultPanel';
import type { ApplicationDataRequest, BatchVerificationResponse, VerificationResponse } from './types/api';

const emptyApplication: ApplicationDataRequest = {
  applicationId: '',
  beverageType: 'DISTILLED_SPIRITS',
  brandName: '',
  classOrType: '',
  alcoholContent: '',
  netContents: '',
  responsiblePartyName: '',
  responsiblePartyAddress: '',
  countryOfOrigin: '',
  imported: false
};

type ApplicationKey = 'bourbon' | 'stone' | 'import';
type ScenarioKey =
  | 'bourbon'
  | 'stone'
  | 'import'
  | 'wrong-abv'
  | 'warning-titlecase'
  | 'missing-net'
  | 'truncated-warning'
  | 'blurry';

type Scenario = {
  key: ScenarioKey;
  label: string;
  image: string;
  appData: ApplicationKey;
};

const scenarios: Scenario[] = [
  { key: 'bourbon', label: 'Bourbon (pass)', image: 'bourbon-label.png', appData: 'bourbon' },
  { key: 'stone', label: "Stone's Throw (casing pass)", image: 'stone-label.png', appData: 'stone' },
  { key: 'import', label: 'Imported (pass)', image: 'import-label.png', appData: 'import' },
  { key: 'wrong-abv', label: 'Wrong ABV (fail)', image: 'wrong-abv.png', appData: 'bourbon' },
  { key: 'warning-titlecase', label: 'Title-case warning (fail)', image: 'warning-titlecase.png', appData: 'bourbon' },
  { key: 'missing-net', label: 'Missing net (fail)', image: 'missing-net.png', appData: 'bourbon' },
  { key: 'truncated-warning', label: 'Truncated warning (fail)', image: 'truncated-warning.png', appData: 'bourbon' },
  { key: 'blurry', label: 'Blurry (unreadable)', image: 'blurry-label.png', appData: 'bourbon' }
];

const batchScenarioFiles = [
  'bourbon-label.png',
  'wrong-abv.png',
  'warning-titlecase.png',
  'missing-net.png',
  'blurry-label.png'
];

function App() {
  const [mode, setMode] = useState<'single' | 'batch'>('single');
  const [applicationData, setApplicationData] = useState<ApplicationDataRequest>(emptyApplication);
  const [files, setFiles] = useState<File[]>([]);
  const [singleResult, setSingleResult] = useState<VerificationResponse | null>(null);
  const [batchId, setBatchId] = useState<string | null>(null);
  const [batchResult, setBatchResult] = useState<BatchVerificationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeSample, setActiveSample] = useState<ScenarioKey | 'batch' | null>(null);
  const resultsRef = useRef<HTMLElement>(null);

  const canSubmit = useMemo(() => {
    return files.length > 0 && applicationData.brandName.trim() !== '' && applicationData.alcoholContent.trim() !== '';
  }, [applicationData.alcoholContent, applicationData.brandName, files.length]);

  useEffect(() => {
    if (!batchId || batchResult?.status.complete) return;
    let consecutiveFailures = 0;
    const id = window.setInterval(async () => {
      try {
        const nextResult = await getBatchResults(batchId);
        consecutiveFailures = 0;
        setError(null);
        setBatchResult(nextResult);
      } catch (err) {
        consecutiveFailures += 1;
        if (consecutiveFailures >= 5) {
          window.clearInterval(id);
          setError('Lost connection to the verifier. Refresh or submit the batch again.');
        } else {
          setError(err instanceof Error ? err.message : 'Could not refresh batch results.');
        }
      }
    }, 1200);
    return () => window.clearInterval(id);
  }, [batchId, batchResult?.status.complete]);

  useEffect(() => {
    if (singleResult || batchResult?.status.complete || error) {
      resultsRef.current?.focus();
    }
  }, [singleResult, batchResult?.status.complete, error]);

  function updateField<K extends keyof ApplicationDataRequest>(key: K, value: ApplicationDataRequest[K]) {
    setApplicationData((current) => ({ ...current, [key]: value }));
  }

  function handleFilesChange(nextFiles: File[]) {
    setFiles(nextFiles);
    setActiveSample(null);
  }

  async function loadSampleFile(subdir: 'single' | 'batch', filename: string) {
    const response = await fetch(`/samples/${subdir}/${filename}`);
    if (!response.ok) {
      throw new Error(`Could not load sample file ${filename}.`);
    }
    const blob = await response.blob();
    return new File([blob], filename, { type: 'image/png' });
  }

  async function loadApplicationData(key: ApplicationKey): Promise<ApplicationDataRequest> {
    const response = await fetch(`/samples/applications/${key}.json`);
    if (!response.ok) {
      throw new Error(`Could not load application data for ${key}.`);
    }
    return response.json();
  }

  async function loadScenario(scenarioKey: ScenarioKey) {
    const scenario = scenarios.find((entry) => entry.key === scenarioKey);
    if (!scenario) return;
    setError(null);
    setSingleResult(null);
    setBatchResult(null);
    setBatchId(null);
    setActiveSample(null);
    try {
      const [appData, file] = await Promise.all([
        loadApplicationData(scenario.appData),
        loadSampleFile('single', scenario.image)
      ]);
      setApplicationData(appData);
      setFiles([file]);
      setMode('single');
      setActiveSample(scenario.key);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load sample scenario.');
    }
  }

  async function useBatchSamples() {
    setError(null);
    setSingleResult(null);
    setBatchResult(null);
    setBatchId(null);
    setActiveSample(null);
    try {
      const appData = await loadApplicationData('bourbon');
      const batchFiles = await Promise.all(batchScenarioFiles.map((name) => loadSampleFile('batch', name)));
      setApplicationData(appData);
      setFiles(batchFiles);
      setMode('batch');
      setActiveSample('batch');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load batch samples.');
    }
  }

  async function submit() {
    if (!canSubmit) return;
    setLoading(true);
    setError(null);
    setSingleResult(null);
    setBatchResult(null);
    try {
      if (mode === 'single') {
        setSingleResult(await verifySingleLabel(files[0], applicationData));
      } else {
        const started = await startBatch(files, applicationData);
        setBatchId(started.batchId);
        setBatchResult({
          status: {
            batchId: started.batchId,
            total: started.total,
            processed: 0,
            pass: 0,
            fail: 0,
            needsReview: 0,
            unreadable: 0,
            complete: false
          },
          results: []
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Verification failed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <section className="workspace">
        <header className="app-header">
          <div>
            <p className="eyebrow">TTB prototype</p>
            <h1>Alcohol label verifier</h1>
          </div>
        </header>

        <div className="scenario-chips" role="group" aria-label="Sample scenarios">
          <span className="scenario-label">Try a sample:</span>
          {scenarios.map((scenario) => (
            <button
              key={scenario.key}
              type="button"
              className={activeSample === scenario.key ? 'chip selected' : 'chip'}
              aria-pressed={activeSample === scenario.key}
              onClick={() => void loadScenario(scenario.key)}
            >
              {scenario.label}
            </button>
          ))}
          <button
            type="button"
            className={activeSample === 'batch' ? 'chip chip-batch selected' : 'chip chip-batch'}
            aria-pressed={activeSample === 'batch'}
            onClick={() => void useBatchSamples()}
          >
            Batch ({batchScenarioFiles.length} mixed)
          </button>
        </div>

        <section className="tool-grid">
          <form className="input-panel" onSubmit={(event) => {
            event.preventDefault();
            void submit();
          }}>
            <div className="segmented" role="group" aria-label="Verification mode">
              <button type="button" className={mode === 'single' ? 'selected' : ''} onClick={() => {
                setMode('single');
                setFiles(files.slice(0, 1));
                setActiveSample(null);
              }}>Single label</button>
              <button type="button" className={mode === 'batch' ? 'selected' : ''} onClick={() => {
                setMode('batch');
                setActiveSample(null);
              }}>Batch</button>
            </div>

            <FileDropzone files={files} multiple={mode === 'batch'} onFilesChange={handleFilesChange} />

            <div className="form-grid">
              <label>
                Application ID
                <input value={applicationData.applicationId} onChange={(event) => updateField('applicationId', event.target.value)} />
              </label>
              <label>
                Beverage type
                <select value={applicationData.beverageType} onChange={(event) => updateField('beverageType', event.target.value as ApplicationDataRequest['beverageType'])}>
                  <option value="DISTILLED_SPIRITS">Distilled spirits</option>
                  <option value="WINE">Wine</option>
                  <option value="MALT_BEVERAGE">Malt beverage</option>
                </select>
              </label>
              <label>
                Brand name
                <input required value={applicationData.brandName} onChange={(event) => updateField('brandName', event.target.value)} />
              </label>
              <label>
                Class/type
                <input value={applicationData.classOrType} onChange={(event) => updateField('classOrType', event.target.value)} />
              </label>
              <label>
                Alcohol content
                <input required value={applicationData.alcoholContent} onChange={(event) => updateField('alcoholContent', event.target.value)} />
              </label>
              <label>
                Net contents
                <input value={applicationData.netContents} onChange={(event) => updateField('netContents', event.target.value)} />
              </label>
              <label>
                Responsible party
                <input value={applicationData.responsiblePartyName} onChange={(event) => updateField('responsiblePartyName', event.target.value)} />
              </label>
              <label>
                Address
                <input value={applicationData.responsiblePartyAddress} onChange={(event) => updateField('responsiblePartyAddress', event.target.value)} />
              </label>
              <label className="checkbox-label">
                <input type="checkbox" checked={applicationData.imported} onChange={(event) => updateField('imported', event.target.checked)} />
                Imported product
              </label>
              <label>
                Country of origin
                <input value={applicationData.countryOfOrigin} onChange={(event) => updateField('countryOfOrigin', event.target.value)} />
              </label>
            </div>

            <button className="primary" type="submit" disabled={!canSubmit || loading}>
              {loading ? 'Verifying...' : mode === 'single' ? 'Verify label' : `Verify ${files.length || ''} labels`}
            </button>
            {!canSubmit && <p className="form-hint">Choose at least one image and fill in brand name and alcohol content.</p>}
          </form>

          <section className="output-panel" ref={resultsRef} tabIndex={-1} aria-live="polite">
            {error && <div className="error" role="alert">{error}</div>}
            {!error && !singleResult && !batchResult && (
              <div className="empty-state">
                <h2>Results will appear here</h2>
                <p>Click a sample chip above for a fast demo, or upload label images and verify them against the application fields.</p>
              </div>
            )}
            {singleResult && <ResultPanel result={singleResult} />}
            {batchResult && <BatchResults batch={batchResult} />}
          </section>
        </section>
      </section>
    </main>
  );
}

export default App;
