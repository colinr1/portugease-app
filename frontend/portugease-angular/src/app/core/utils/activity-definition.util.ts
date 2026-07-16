import {
  ActivityHint,
  ListeningDefinition,
  MultipleChoiceDefinition,
  MultipleChoiceOption,
  NormalizedActivityType,
  SentenceBuildingDefinition,
  SentenceTransformationDefinition,
  WordMatchingDefinition
} from '../models/activity.model';

export function normalizeActivityType(type: string): NormalizedActivityType {
  const normalized = type.trim().toUpperCase();

  switch (normalized) {
    case 'MULTIPLE_CHOICE':
      return 'MULTIPLE_CHOICE';

    case 'WORD_MATCHING':
      return 'WORD_MATCHING';

    case 'SENTENCE_BUILDING':
      return 'SENTENCE_BUILDING';

    case 'LISTENING':
      return 'LISTENING';

    case 'TRANSFORMATION':
    case 'SENTENCE_TRANSFORMATION':
      return 'SENTENCE_TRANSFORMATION';

    case 'SCENARIO_CHALLENGE':
      return 'SCENARIO_CHALLENGE';

    default:
      return 'MULTIPLE_CHOICE';
  }
}

export function extractActivityHints(
  definition: unknown
): ActivityHint[] {
  if (!isRecord(definition)) {
    return [];
  }

  const rawHints = definition['hints'];

  if (!Array.isArray(rawHints)) {
    return [];
  }

  return rawHints
    .map(toActivityHint)
    .filter((hint): hint is ActivityHint => Boolean(hint));
}

function toActivityHint(hint: unknown, index: number): ActivityHint | null {
  if (!isRecord(hint) || typeof hint['text'] !== 'string') {
    return null;
  }

  const text = hint['text'].trim();
  if (!text) {
    return null;
  }

  return {
    level: index + 1,
    text
  };
}

export function isSentenceBuildingDefinition(
  definition: unknown
): definition is SentenceBuildingDefinition {
  return isDefinitionRecord(definition) && isStringArray(definition['tokens']);
}

export function isListeningDefinition(
  definition: unknown
): definition is ListeningDefinition {
  return isDefinitionRecord(definition) && typeof definition['audioUrl'] === 'string';
}

export function isWordMatchingDefinition(
  definition: unknown
): definition is WordMatchingDefinition {
  return isDefinitionRecord(definition) &&
    isStringArray(definition['leftItems']) &&
    isStringArray(definition['rightItems']);
}

export function isMultipleChoiceDefinition(
  definition: unknown
): definition is MultipleChoiceDefinition {
  return isDefinitionRecord(definition) &&
    typeof definition['question'] === 'string' &&
    isMultipleChoiceOptionArray(definition['options']);
}

export function isSentenceTransformationDefinition(
  definition: unknown
): definition is SentenceTransformationDefinition {
  return isDefinitionRecord(definition) &&
    typeof definition['prompt'] === 'string' &&
    typeof definition['sourceSentence'] === 'string';
}

function isDefinitionRecord(definition: unknown): definition is Record<string, unknown> {
  if (!isRecord(definition)) {
    return false;
  }

  const hints = definition['hints'];
  return hints === undefined || isHintArray(hints);
}

function isHintArray(value: unknown): boolean {
  return Array.isArray(value) && value.every(hint =>
    isRecord(hint) && typeof hint['text'] === 'string'
  );
}

function isMultipleChoiceOptionArray(value: unknown): value is MultipleChoiceOption[] {
  return Array.isArray(value) && value.every(option =>
    isRecord(option) &&
    typeof option['id'] === 'string' &&
    typeof option['text'] === 'string'
  );
}

function isStringArray(value: unknown): value is string[] {
  return Array.isArray(value) && value.every(item => typeof item === 'string');
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}
