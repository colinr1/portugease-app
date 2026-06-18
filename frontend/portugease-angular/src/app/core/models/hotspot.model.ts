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

export type HotspotStyle =
  | 'INTRO_DIALOGUE'
  | 'VOCAB_TOOLTIP'
  | 'ACTIVITY'
  | 'MATCHING'
  | 'VOCABULARY'
  | 'DIALOGUE'
  | 'AUDIO'
  | string;

export interface VocabularyTooltipContent {
  itemKey: string;
  portugueseText: string;
  englishTranslation: string;
  audioPath?: string | null;
}

export interface Hotspot {
  id: string;
  label: string;
  xPercent: number;
  yPercent: number;

  iconAssetKey?: string | null;
  iconUrl?: string | null;

  visible: boolean;

  activityKey?: string | null;
  activityId?: string | null;
  activityType?: ActivityType | null;

  hotspotType?: HotspotType | null;
  style?: HotspotStyle | null;
  dialogueId?: string | null;
  ariaLabel?: string | null;

  vocabulary?: VocabularyTooltipContent | null;

  raw?: Record<string, unknown>;
}
