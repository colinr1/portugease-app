import {
  ActivityHint,
  NormalizedActivityType
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
  definition: Record<string, unknown>
): ActivityHint[] {
  const rawHints = definition['hints'];

  if (Array.isArray(rawHints)) {
    return rawHints
      .map(toActivityHint)
      .filter((hint): hint is ActivityHint => Boolean(hint))
      .sort((first, second) => first.level - second.level);
  }

  return [
    toTextHint(1, definition['hint']),
    toTextHint(2, definition['hint2'] ?? definition['secondHint'])
  ].filter((hint): hint is ActivityHint => Boolean(hint));
}

function toActivityHint(hint: unknown, index: number): ActivityHint | null {
  if (typeof hint === 'string') {
    return toTextHint(index + 1, hint);
  }

  if (!hint || typeof hint !== 'object') {
    return null;
  }

  const hintObject = hint as Record<string, unknown>;

  return toTextHint(
    Number(hintObject['level'] ?? index + 1),
    hintObject['text']
  );
}

function toTextHint(level: number, text: unknown): ActivityHint | null {
  if (text == null || String(text).trim().length === 0) {
    return null;
  }

  return {
    level,
    text: String(text)
  };
}
