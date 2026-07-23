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
  completedPerfectly: boolean;
  incorrectBeforeSuccess: number;
}

export interface UnlockedLocation {
  locationId: string;
  cityId: string;
  locationSlug: string;
  locationName: string;
}

export interface UnlockedCity {
  cityId: string;
  citySlug: string;
  cityName: string;
  firstUnlockedLocation: UnlockedLocation | null;
}

export interface ProgressionUpdate {
  locationCompleted: boolean;
  unlockedLocation: UnlockedLocation | null;
  cityCompleted: boolean;
  unlockedCity: UnlockedCity | null;
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
  progressionUpdate: ProgressionUpdate;
  adaptiveDifficulty?: AdaptiveDifficultyResponse;
}

export interface AdaptiveDifficultyResponse {
  selectedDifficulty: DifficultyLevel;
  difficultyChanged: boolean;
  newDifficulty?: DifficultyLevel;
  difficultyChangeMessage?: string | null;
}
