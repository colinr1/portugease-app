export interface ActivityAttemptRequest {
  userId?: string | null;
  learnerSessionId?: string | null;
  submittedAnswer: Record<string, unknown>;
  hintsUsed: number;
}

export interface AdaptiveSupportDecision {
  scaffoldingLevel: 'LOW' | 'NORMAL' | 'HIGH' | string;
  addToReview: boolean;
  offerTranscript: boolean;
  offerSlowerAudio: boolean;
  reduceScaffolding: boolean;
  messages: string[];
}

export interface ProgressUpdateSummary {
  activityCompleted: boolean;
  activityMastered: boolean;
  attemptsCount: number;
  incorrectAttemptsCount: number;
  bestScore: number;
  maxScore: number;
  itemMarkedForReview: boolean;
}

export interface ActivityAttemptResponse {
  attemptId: string;
  activityId: string;
  isCorrect: boolean;
  score: number;
  maxScore: number;
  feedbackMessage: string;
  explanation: string;
  adaptiveSupport: AdaptiveSupportDecision;
  progressUpdate: ProgressUpdateSummary;
}
