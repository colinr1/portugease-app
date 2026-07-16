import { AssetMetadata } from './asset.model';
import { Hotspot } from './hotspot.model';
import { ActivityContent } from './activity.model';

export interface IntroDialogueLine {
  speaker?: string;
  portugueseText: string;
  englishTranslation: string;
  audioPath?: string;
  focusMarkers?: IntroDialogueFocusMarker[];
}

export interface IntroDialogue {
  id: string;
  autoOpenOnFirstVisit: boolean;
  alreadySeen: boolean;
  lines: IntroDialogueLine[];
}

export interface LessonSummary {
  id: string;
  locationId: string;
  title: string;
  slug: string;
}

export interface LessonDetail {
  id: string;
  locationId: string;
  cityId: string;
  title: string;
  slug: string;
  backgroundImage?: AssetMetadata | null;
  content: Record<string, unknown>;
  hotspots: Hotspot[];
  activities: ActivityContent[];
  introDialogue?: IntroDialogue | null;
}

export interface IntroDialogueFocusMarker {
  id: string;
  xPercent: number;
  yPercent: number;
  ariaLabel?: string;
}
