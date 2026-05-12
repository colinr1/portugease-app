export type AssetType =
  | 'MAP_IMAGE'
  | 'CITY_IMAGE'
  | 'LOCATION_IMAGE'
  | 'MARKER_ICON'
  | 'HOTSPOT_ICON'
  | 'AUDIO'
  | 'UI_IMAGE';

export interface AssetMetadata {
  id: string;
  assetKey: string;
  assetType: AssetType;
  filePath: string;
  altText?: string | null;
  description?: string | null;
  mimeType?: string | null;
}
