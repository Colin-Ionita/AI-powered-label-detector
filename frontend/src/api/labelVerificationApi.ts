import type {
  ApplicationDataRequest,
  BatchStartResponse,
  BatchStatusResponse,
  BatchVerificationResponse,
  VerificationResponse
} from '../types/api';

const timeoutMs = 30000;
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');

function apiUrl(path: string) {
  return `${apiBaseUrl}${path}`;
}

async function withTimeout<T>(input: RequestInfo | URL, init: RequestInit = {}, ms = timeoutMs): Promise<T> {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), ms);
  try {
    const requestInit = { ...init, signal: controller.signal };
    const response = await fetch(input, requestInit);
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message ?? 'Request failed.');
    }
    return response.json() as Promise<T>;
  } finally {
    window.clearTimeout(timeout);
  }
}

function applicationBlob(applicationData: ApplicationDataRequest) {
  return JSON.stringify(applicationData);
}

export function verifySingleLabel(image: File, applicationData: ApplicationDataRequest) {
  const formData = new FormData();
  formData.append('image', image);
  formData.append('applicationData', applicationBlob(applicationData));
  return withTimeout<VerificationResponse>(apiUrl('/api/verify/single'), { method: 'POST', body: formData }, 30000);
}

export function startBatch(images: File[], applicationData: ApplicationDataRequest) {
  const formData = new FormData();
  images.forEach((image) => formData.append('images', image));
  formData.append('applicationData', applicationBlob(applicationData));
  return withTimeout<BatchStartResponse>(apiUrl('/api/batches'), { method: 'POST', body: formData }, 30000);
}

export function getBatchStatus(batchId: string) {
  return withTimeout<BatchStatusResponse>(apiUrl(`/api/batches/${batchId}`), {}, 10000);
}

export function getBatchResults(batchId: string) {
  return withTimeout<BatchVerificationResponse>(apiUrl(`/api/batches/${batchId}/results`), {}, 10000);
}
