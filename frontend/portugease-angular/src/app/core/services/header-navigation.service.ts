import { Injectable, signal } from '@angular/core';

export interface HeaderCityLink {
  locationSlug: string;
  citySlug: string;
  cityName: string;
}

@Injectable({
  providedIn: 'root'
})
export class HeaderNavigationService {
  private readonly cityLinkState = signal<HeaderCityLink | null>(null);

  readonly cityLink = this.cityLinkState.asReadonly();

  setCityLink(locationSlug: string, citySlug: string, cityName: string): void {
    const trimmedName = cityName.trim();

    if (!locationSlug || !citySlug || !trimmedName) {
      this.clearCityLink();
      return;
    }

    this.cityLinkState.set({
      locationSlug,
      citySlug,
      cityName: trimmedName
    });
  }

  clearCityLink(): void {
    this.cityLinkState.set(null);
  }
}
