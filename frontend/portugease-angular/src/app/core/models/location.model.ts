import { AssetMetadata } from './asset.model';

export type LocationStatus = 'LOCKED' | 'UNLOCKED' | 'IN_PROGRESS' | 'COMPLETED';

export interface LocationMenuItem {
  id: string;
  cityId: string;
  name: string;
  slug: string;
  displayOrder: number;
  backgroundImage?: AssetMetadata | null;
  status: LocationStatus;
}
