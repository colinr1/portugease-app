import {DifficultyLevel} from "./activity.model";

export interface ActivityAttemptRequest {
  userId?: string | null;
  learnerSessionId?: string | null;
  submittedAnswer: Record<string, unknown>;
  selectedDifficulty?: DifficultyLevel;
  incorrectSubmissionCount?: number;
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
  adaptiveDifficulty?: AdaptiveDifficultyResponse;
}

export interface AdaptiveDifficultyResponse {
  selectedDifficulty: DifficultyLevel;
  difficultyChanged: boolean;
  newDifficulty?: DifficultyLevel;
  difficultyChangeMessage?: string | null;
}
