import { ActivityType } from './activity.model';

export type HotspotType =
  | 'INTRO_DIALOGUE'
  | 'VOCAB_TOOLTIP'
  | 'ACTIVITY'
  | 'VOCABULARY'
  | 'DIALOGUE'
  | 'AUDIO'
  | 'HINT'
  | 'EXPLANATION'
  | 'OBJECT'
  | 'CHARACTER'
  | string;

export interface VocabularyTooltipContent {
  portugueseText: string;
  englishTranslation: string;
  audioPath?: string | null;
}

export interface Hotspot {
  id: string;
  label: string;
  xPercent: number;
  yPercent: number;

  iconUrl?: string | null;

  visible: boolean;

  activityKey?: string | null;
  activityId?: string | null;
  activityType?: ActivityType | null;

  hotspotType?: HotspotType | null;
  dialogueId?: string | null;
  ariaLabel?: string | null;

  vocabulary?: VocabularyTooltipContent | null;

  raw?: Record<string, unknown>;
}
