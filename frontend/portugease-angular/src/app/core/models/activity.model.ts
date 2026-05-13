export type ActivityType =
  | 'MULTIPLE_CHOICE'
  | 'WORD_MATCHING'
  | 'SENTENCE_BUILDING'
  | 'SENTENCE_TRANSFORMATION'
  | 'SCENARIO_CHALLENGE'
  | 'LISTENING'
  | 'multiple_choice'
  | 'word_matching'
  | 'sentence_building'
  | 'transformation'
  | 'scenario_challenge'
  | 'listening';

export type NormalizedActivityType =
  | 'MULTIPLE_CHOICE'
  | 'WORD_MATCHING'
  | 'SENTENCE_BUILDING'
  | 'SENTENCE_TRANSFORMATION'
  | 'SCENARIO_CHALLENGE'
  | 'LISTENING';

export interface ActivityContent {
  id: string;
  locationId: string;
  hotspotId: string;
  activityKey: string;
  activityType: ActivityType;
  title: string;
  instructions?: string | null;
  definition: Record<string, unknown>;
  learningItems?: Record<string, unknown>;
  maxScore: number;
  requiredForCompletion: boolean;
  displayOrder: number;
}

export interface MultipleChoiceOption {
  id: string;
  text: string;
  isCorrect?: boolean;
}

export interface WordMatchPair {
  left: string;
  right: string;
}

export interface ActivityAnswerSubmitted {
  submittedAnswer: Record<string, unknown>;
}
