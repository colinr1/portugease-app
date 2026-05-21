export interface ActivityAttemptRequest {
  userId?: string | null;
  learnerSessionId?: string | null;
  submittedAnswer: Record<string, unknown>;
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
  progressUpdate: ProgressUpdateSummary;
  adaptiveDifficulty?: AdaptiveDifficultyResult;
}

export interface AdaptiveDifficultyResult {
  selectedDifficulty: 'EASY' | 'NORMAL' | 'HARD';
  difficultyChanged: boolean;
  newDifficulty?: 'EASY' | 'NORMAL' | 'HARD';
  difficultyChangeMessage?: string | null;
}
