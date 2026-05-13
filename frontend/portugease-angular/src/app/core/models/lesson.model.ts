import { AssetMetadata } from './asset.model';
import { Hotspot } from './hotspot.model';
import { ActivityContent } from './activity.model';

export interface IntroDialogueLine {
  id: string;
  speaker?: string;
  portugueseText: string;
  englishTranslation: string;
  audioPath?: string;
  targetLearningItemKeys?: string[];
}

export interface IntroDialogue {
  id: string;
  title: string;
  description?: string;
  autoOpenOnFirstVisit: boolean;
  alreadySeen: boolean;
  hotspotId: string;
  targetLearningItemKeys: string[];
  lines: IntroDialogueLine[];
}

export interface LessonSummary {
  id: string;
  locationId: string;
  title: string;
  slug: string;
  description?: string | null;
  estimatedMinutes?: number | null;
}

export interface LessonDetail {
  id: string;
  locationId: string;
  cityId: string;
  title: string;
  slug: string;
  description?: string | null;
  estimatedMinutes?: number | null;
  backgroundImage?: AssetMetadata | null;
  content: Record<string, unknown>;
  hotspots: Hotspot[];
  activities: ActivityContent[];
  introDialogue?: IntroDialogue | null;
}
