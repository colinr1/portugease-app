import { Hotspot } from '../models/hotspot.model';

type HotspotMarkerType = 'ACTIVITY' | 'INTRO_DIALOGUE' | 'VOCAB_TOOLTIP';

export function isActivityHotspot(hotspot: Hotspot): boolean {
  return (
    hasHotspotMarkerType(hotspot, 'ACTIVITY') ||
    Boolean(hotspot.activityId) ||
    Boolean(hotspot.activityKey)
  );
}

export function isIntroDialogueHotspot(
  hotspot: Hotspot,
  dialogueId?: string | null
): boolean {
  return (
    hasHotspotMarkerType(hotspot, 'INTRO_DIALOGUE') ||
    Boolean(dialogueId && hotspot.dialogueId === dialogueId)
  );
}

export function isVocabularyTooltipHotspot(hotspot: Hotspot): boolean {
  return hasHotspotMarkerType(hotspot, 'VOCAB_TOOLTIP');
}

function hasHotspotMarkerType(
  hotspot: Hotspot,
  markerType: HotspotMarkerType
): boolean {
  return (
    hotspot.hotspotType === markerType ||
    hotspot.style === markerType ||
    hotspot.raw?.['hotspotType'] === markerType ||
    hotspot.raw?.['style'] === markerType
  );
}
