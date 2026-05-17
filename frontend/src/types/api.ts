export type BeverageType = 'DISTILLED_SPIRITS' | 'WINE' | 'MALT_BEVERAGE';
export type VerificationStatus = 'PASS' | 'FAIL' | 'NEEDS_REVIEW' | 'UNREADABLE';
export type MatchStatus = 'MATCH' | 'MISMATCH' | 'MISSING' | 'LOW_CONFIDENCE' | 'NOT_APPLICABLE';

export interface ApplicationDataRequest {
  applicationId: string;
  beverageType: BeverageType;
  brandName: string;
  classOrType: string;
  alcoholContent: string;
  netContents: string;
  responsiblePartyName: string;
  responsiblePartyAddress: string;
  countryOfOrigin: string;
  imported: boolean;
}

export interface FieldMatchResult {
  fieldKey: string;
  displayName: string;
  expectedValue: string | null;
  extractedValue: string | null;
  status: MatchStatus;
  confidence: number;
  reason: string;
}

export interface GovernmentWarningResult {
  status: MatchStatus;
  confidence: number;
  extractedText: string | null;
  reason: string;
}

export interface OcrSummary {
  provider: string;
  fullText: string;
  confidence: number;
  wordCount: number;
  headingBoldDetected: boolean;
}

export interface VerificationResponse {
  labelId: string;
  filename: string;
  overallStatus: VerificationStatus;
  processingTimeMs: number;
  fields: FieldMatchResult[];
  governmentWarning: GovernmentWarningResult;
  ocr: OcrSummary;
  reviewReasons: string[];
}

export interface BatchStartResponse {
  batchId: string;
  total: number;
  status: string;
}

export interface BatchStatusResponse {
  batchId: string;
  total: number;
  processed: number;
  pass: number;
  fail: number;
  needsReview: number;
  unreadable: number;
  complete: boolean;
}

export interface BatchVerificationResponse {
  status: BatchStatusResponse;
  results: VerificationResponse[];
}
