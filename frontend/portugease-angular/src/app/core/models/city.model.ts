import { AssetMetadata } from './asset.model';
import { LocationMenuItem } from './location.model';

export type CityStatus = 'LOCKED' | 'UNLOCKED' | 'IN_PROGRESS' | 'COMPLETED';

export interface CityMarker {
  xPercent: number;
  yPercent: number;
  iconAssetKey?: string | null;
  raw?: Record<string, unknown>;
}

export interface CityListItem {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  displayOrder: number;
  marker: CityMarker;
  backgroundImage?: AssetMetadata | null;
  status: CityStatus;
}

export interface CityDetail {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  displayOrder: number;
  marker: CityMarker;
  backgroundImage?: AssetMetadata | null;
  status: CityStatus;
  unlockRule?: Record<string, unknown>;
  locations: LocationMenuItem[];
}
