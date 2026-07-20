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

export type DifficultyLevel = 'EASY' | 'NORMAL' | 'HARD';

export interface ActivityDefinitionHint {
  text: string;
}

export interface ActivityHint {
  level: number;
  text: string;
}

export interface ActivityDefinitionBase {
  hints?: ActivityDefinitionHint[];
}

export interface SentenceBuildingDefinition extends ActivityDefinitionBase {
  tokens: string[];
}

export interface ListeningDefinition extends ActivityDefinitionBase {
  audioUrl?: string;
  correctAnswer?: string;
}

export interface WordMatchingDefinition extends ActivityDefinitionBase {
  leftItems: string[];
  rightItems: string[];
}

export interface MultipleChoiceOption {
  id: string;
  text: string;
}

export interface MultipleChoiceDefinition extends ActivityDefinitionBase {
  question: string;
  options: MultipleChoiceOption[];
}

export interface SentenceTransformationDefinition extends ActivityDefinitionBase {
  prompt: string;
  sourceSentence: string;
}

export type ActivityDefinition =
  | SentenceBuildingDefinition
  | ListeningDefinition
  | WordMatchingDefinition
  | MultipleChoiceDefinition
  | SentenceTransformationDefinition;

export interface ActivityContent {
  id: string;
  activityKey: string;
  activityType: ActivityType;
  title: string;
  instructions?: string | null;
  definition: ActivityDefinition;
  displayOrder: number;
  selectedDifficulty?: DifficultyLevel;
}

export interface WordMatchSelection {
  left: string;
  right: string;
}

export interface ActivityAnswerSubmitted {
  submittedAnswer: Record<string, unknown>;
}
