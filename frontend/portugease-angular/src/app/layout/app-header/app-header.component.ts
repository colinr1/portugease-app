import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  NavigationEnd,
  PRIMARY_OUTLET,
  Router,
  RouterLink
} from '@angular/router';
import { filter } from 'rxjs';
import { HeaderNavigationService } from '../../core/services/header-navigation.service';

type HeaderPage = 'map' | 'city' | 'location' | 'other';

interface HeaderRoute {
  page: HeaderPage;
  locationSlug: string | null;
}

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.scss'
})
export class AppHeaderComponent {
  private readonly router = inject(Router);
  private readonly headerNavigation = inject(HeaderNavigationService);
  private readonly currentRoute = signal<HeaderRoute>(
    this.routeFromUrl(this.router.url)
  );

  readonly cityLink = this.headerNavigation.cityLink;
  readonly showMapLink = computed(() => this.currentRoute().page !== 'map');
  readonly showCityLink = computed(() => {
    const route = this.currentRoute();
    const city = this.cityLink();

    return (
      route.page === 'location' &&
      route.locationSlug !== null &&
      city?.locationSlug === route.locationSlug
    );
  });

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed()
      )
      .subscribe(event => {
        this.currentRoute.set(this.routeFromUrl(event.urlAfterRedirects));
      });
  }

  private routeFromUrl(url: string): HeaderRoute {
    const segments =
      this.router.parseUrl(url).root.children[PRIMARY_OUTLET]?.segments ?? [];
    const firstSegment = segments[0]?.path;

    if (!firstSegment) {
      return { page: 'map', locationSlug: null };
    }

    if (firstSegment === 'cities') {
      return { page: 'city', locationSlug: null };
    }

    if (firstSegment === 'locations') {
      return {
        page: 'location',
        locationSlug: segments[1]?.path ?? null
      };
    }

    return { page: 'other', locationSlug: null };
  }
}
