# AI-Powered Alcohol Label Verification App

Standalone prototype for verifying alcohol label artwork against expected application data. It uses a React frontend, Spring Boot backend, deterministic mock OCR/extraction, optional Google Vision OCR, government warning validation, and in-memory batch processing.

Source repository: https://github.com/Colin-Ionita/AI-powered-label-detector

Deployed frontend: https://ai-powered-label-detector.vercel.app/

Deployed backend health check: https://ai-powered-label-detector.onrender.com/api/health

## What Works

- Single-label upload and verification.
- Batch upload with job ID, progress polling, summary counts, filters, and expandable results.
- Real bundled PNG sample labels plus a mock OCR provider that supports happy path, mismatch, warning failure, missing field, unreadable, imported, truncated-warning, and casing-difference scenarios based on filename.
- Optional Google Cloud Vision OCR provider for real image text extraction.
- Field comparison for brand, class/type, ABV/proof, net contents, responsible party, address, country of origin, and government warning.
- Dedicated government warning validator using `backend/src/main/resources/regulatory/government-warning.txt`.
- No external credentials required for local demo.

## Run Locally

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

On Windows PowerShell, use `npm.cmd` if script execution blocks `npm`:

```bash
npm.cmd install
npm.cmd run dev
```

## Demo Flow

1. Start backend and frontend.
2. Click the `Bourbon (pass)` scenario chip near the top.
3. Click `Verify label` - confirm the result is `Pass` and processing time is under 5 seconds.
4. Click another scenario chip to demo a failure mode (e.g. `Wrong ABV`, `Title-case warning`, `Stone's Throw (casing pass)`).
5. Click the `Batch (5 mixed)` chip to load five labels.
6. Click `Verify 5 labels`.
7. Watch progress, filter to `Fail`, and expand a row to inspect reasons.

### Sample scenarios

| Chip | Image | Expected result |
|---|---|---|
| Bourbon (pass) | `bourbon-label.png` | `PASS` - all fields match |
| Stone's Throw (casing pass) | `stone-label.png` | `PASS` - `STONE'S THROW` matches `Stone's Throw` after normalization |
| Imported (pass) | `import-label.png` | `PASS` - country of origin Canada, Imported by line extracted |
| Wrong ABV (fail) | `wrong-abv.png` | `FAIL` - 42% on label vs 45% expected |
| Title-case warning (fail) | `warning-titlecase.png` | `FAIL` - `Government Warning:` heading rejected |
| Missing net (fail) | `missing-net.png` | `FAIL` - 750 mL not present on label |
| Truncated warning (fail) | `truncated-warning.png` | `FAIL` - warning body cut off |
| Blurry (unreadable) | `blurry-label.png` | `UNREADABLE` - OCR confidence too low to verify |
| Batch (5 mixed) | five of the above | `1 PASS`, `3 FAIL`, `1 UNREADABLE` |

Sample assets live in `frontend/public/samples/`:

- `single/*.png` - eight label images.
- `batch/*.png` - five-image batch subset.
- `applications/*.json` - per-scenario application data (`bourbon`, `stone`, `import`).

The Java generator is in the repo root under `samples/SampleLabelGenerator.java`.

To regenerate the deterministic sample PNGs served by the app:

```bash
javac samples/SampleLabelGenerator.java
java -cp samples SampleLabelGenerator
```

The generator writes to `frontend/public/samples/single/` and refreshes the five-file subset in `frontend/public/samples/batch/`.

## API

- `GET /api/health`
- `POST /api/verify/single`
- `POST /api/batches`
- `GET /api/batches/{batchId}`
- `GET /api/batches/{batchId}/results`

Uploads use `multipart/form-data` with image part(s) plus an `applicationData` JSON part.

## Configuration

Backend environment variables:

- `ALLOWED_ORIGINS`: defaults to `http://localhost:5173`
- `MAX_BATCH_SIZE`: defaults to `300`
- `WORKER_CONCURRENCY`: defaults to `4`
- `BATCH_RETENTION_MS`: defaults to `3600000` (one hour)
- `BATCH_CLEANUP_RATE_MS`: defaults to `600000` (ten minutes)
- `OCR_PROVIDER`: defaults to `mock`
- `GOOGLE_VISION_API_KEY`: required only when `OCR_PROVIDER=google-vision`
- `GOOGLE_VISION_ENDPOINT`: defaults to `https://vision.googleapis.com/v1/images:annotate`
- `OCR_TIMEOUT_MS`: defaults to `10000`
- `REQUIRE_WARNING_BOLD_METADATA`: defaults to `false`; set to `true` if missing OCR bold metadata should force `NEEDS_REVIEW`

Frontend environment variables:

- `VITE_API_BASE_URL`: leave empty for local Vite proxying, or set to the deployed backend base URL for split deployments.

The default `mock` OCR provider is deterministic and credential-free for evaluators. Set `OCR_PROVIDER=google-vision` and provide `GOOGLE_VISION_API_KEY` to use Google Cloud Vision `DOCUMENT_TEXT_DETECTION` for real uploaded images. Google Vision does not expose reliable bold-type detection, so the hosted demo defaults `REQUIRE_WARNING_BOLD_METADATA=false`: canonical uppercase warning text can pass, while heading case and wording violations still fail. Set `REQUIRE_WARNING_BOLD_METADATA=true` for a stricter production posture.

Other real OCR/LLM integrations can be added behind `OcrProvider` and `FieldExtractionProvider`.

## Deployment

One low-friction deployment path:

Backend on Render:

- Root directory: `backend`
- Environment: Docker
- Blueprint file: `render.yaml` at the repository root can create the backend service.
- Environment variables:
  - `ALLOWED_ORIGINS`: deployed frontend URL; this must be set in Render for the deployed frontend to pass CORS
  - `OCR_PROVIDER`: `google-vision` for real OCR, or `mock` for credential-free demo mode
  - `GOOGLE_VISION_API_KEY`: required for `google-vision`
  - `REQUIRE_WARNING_BOLD_METADATA`: `false` for the hosted demo, `true` for stricter review behavior

Frontend on Vercel or Netlify:

- Root directory: `frontend`
- Build command: `npm run build`
- Publish directory: `dist`
- Config files: `frontend/vercel.json` and `frontend/netlify.toml`
- Environment variables:
  - `VITE_API_BASE_URL`: deployed backend base URL, for example `https://your-service.onrender.com`

Render free instances can cold-start after being idle, so the first request may exceed the 5-second target. Warm requests are the relevant latency target for the prototype.

## Tests

```bash
cd backend
mvn test
```

```bash
cd frontend
npm run build
```

## Assumptions And Limitations

- Mock OCR is filename-driven, so arbitrary uploaded images produce the default happy-path OCR text unless their filename matches a sample scenario. The bundled sample PNGs are real image files with exact rendered label text, ready for a future real OCR provider.
- Google Vision OCR is available as an optional provider, but it requires a Google Cloud API key and does not reliably expose text weight. The demo treats canonical uppercase warning text as passing unless `REQUIRE_WARNING_BOLD_METADATA=true`.
- Uploaded images are processed in memory and are not persisted.
- Batch state is in memory, expires after the configured retention window, and is lost on backend restart.
- Batch verification currently uses one application data record for all files in the batch. Mixed application records would need a manifest or per-file application payload.
- The API accepts up to 10 MB per file. The hosted service also has platform request-size limits, so very large 300-file batches should be compressed or split.
- Bold detection is represented by mock metadata. Real OCR providers often cannot reliably confirm text weight, so real integrations should return `NEEDS_REVIEW` when bold cannot be verified.
- This is a decision-support prototype, not a complete legal compliance engine.
