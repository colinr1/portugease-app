import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LessonApiService } from '../../core/services/lesson-api.service';
import { CityApiService } from '../../core/services/city-api.service';
import { LessonDetail } from '../../core/models/lesson.model';
import { ActivityAttemptResponse } from '../../core/models/attempt.model';
import { isLockedContentError } from '../../core/utils/http-error.util';
import { HeaderNavigationService } from '../../core/services/header-navigation.service';
import { LocationScenarioComponent } from '../../features/scenario/location-scenario/location-scenario.component';

interface NavigationCity {
  slug: string;
  name: string;
}

@Component({
  selector: 'app-location-page',
  standalone: true,
  imports: [LocationScenarioComponent, RouterLink],
  templateUrl: './location-page.component.html',
  styleUrl: './location-page.component.scss'
})
export class LocationPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private loadRequestId = 0;

  lesson?: LessonDetail;
  loading = true;
  errorMessage = '';
  locked = false;
  cityName = 'City';
  private returnCitySlug: string | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly lessonApi: LessonApiService,
    private readonly cityApi: CityApiService,
    private readonly router: Router,
    private readonly headerNavigation: HeaderNavigationService
  ) {}

  get lockedReturnRoute(): string[] {
    return this.returnCitySlug
      ? ['/cities', this.returnCitySlug]
      : ['/'];
  }

  get lockedReturnLabel(): string {
    return this.returnCitySlug ? 'Back to city' : 'Back to map';
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(paramMap => {
        this.loadLocation(paramMap.get('locationSlug'));
      });
  }

  private loadLocation(locationSlug: string | null): void {
    const requestId = ++this.loadRequestId;
    const navigationCity = this.resolveNavigationCity();

    this.lesson = undefined;
    this.loading = true;
    this.errorMessage = '';
    this.locked = false;
    this.returnCitySlug = navigationCity?.slug ?? null;
    this.cityName = navigationCity?.name ?? 'City';
    this.headerNavigation.clearCityLink();

    if (!locationSlug) {
      this.errorMessage = 'Location not found.';
      this.loading = false;
      return;
    }

    if (navigationCity) {
      this.headerNavigation.setCityLink(
        locationSlug,
        navigationCity.slug,
        navigationCity.name
      );
    }

    this.lessonApi.getLessonByLocationSlug(locationSlug)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: lesson => {
          if (requestId !== this.loadRequestId) {
            return;
          }

          this.lesson = lesson;
          this.locked = false;
          this.loading = false;
          this.returnCitySlug = lesson.citySlug;
          this.loadCityNavigation(
            locationSlug,
            lesson.cityId,
            lesson.citySlug,
            requestId
          );
        },
        error: (error: unknown) => {
          if (requestId !== this.loadRequestId) {
            return;
          }

          this.locked = isLockedContentError(error);
          this.errorMessage = this.locked
            ? 'Complete the required activities in the previous location to unlock this one.'
            : 'Could not load scenario.';
          this.loading = false;
        }
      });
  }

  private loadCityNavigation(
    locationSlug: string,
    cityId: string,
    citySlug: string,
    requestId: number
  ): void {
    const currentCity = this.headerNavigation.cityLink();

    if (
      currentCity?.locationSlug === locationSlug &&
      currentCity.citySlug === citySlug
    ) {
      return;
    }

    this.cityName = 'City';
    this.headerNavigation.clearCityLink();

    this.cityApi.getCity(cityId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: city => {
          if (requestId !== this.loadRequestId) {
            return;
          }

          this.cityName = city.name;
          this.headerNavigation.setCityLink(locationSlug, city.slug, city.name);
        },
        error: () => {
          if (requestId === this.loadRequestId) {
            this.cityName = 'City';
            this.headerNavigation.clearCityLink();
          }
        }
      });
  }

  private resolveNavigationCity(): NavigationCity | null {
    const navigationState = this.router.currentNavigation()?.extras.state;
    const historyState = history.state as Record<string, unknown>;
    const citySlug = navigationState?.['citySlug'] ?? historyState['citySlug'];
    const cityName = navigationState?.['cityName'] ?? historyState['cityName'];

    if (
      typeof citySlug !== 'string' ||
      !citySlug ||
      typeof cityName !== 'string' ||
      !cityName.trim() ||
      cityName.trim() === 'City'
    ) {
      return null;
    }

    return {
      slug: citySlug,
      name: cityName.trim()
    };
  }

  onActivityFinished(response: ActivityAttemptResponse): void {
    const progressionUpdate = response.progressionUpdate;

    if (progressionUpdate.unlockedCity || progressionUpdate.cityCompleted) {
      void this.router.navigate(['/']);
      return;
    }

    if (progressionUpdate.unlockedLocation || progressionUpdate.locationCompleted) {
      const citySlug = this.lesson?.citySlug;
      void this.router.navigate(citySlug ? ['/cities', citySlug] : ['/']);
    }
  }

  onContentLocked(): void {
    this.returnCitySlug = this.lesson?.citySlug ?? null;
    this.lesson = undefined;
    this.locked = true;
    this.loading = false;
    this.errorMessage = 'This location is locked. Return to your city to continue learning.';
  }
}
