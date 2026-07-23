import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CityApiService } from '../../core/services/city-api.service';
import { CityDetail } from '../../core/models/city.model';
import { isLockedContentError } from '../../core/utils/http-error.util';
import { CityLocationMenuComponent } from '../../features/cities/city-location-menu/city-location-menu.component';

@Component({
  selector: 'app-city-page',
  standalone: true,
  imports: [CityLocationMenuComponent, RouterLink],
  templateUrl: './city-page.component.html',
  styleUrl: './city-page.component.scss'
})
export class CityPageComponent implements OnInit {
  city?: CityDetail;
  loading = true;
  errorMessage = '';
  locked = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly cityApi: CityApiService
  ) {}

  ngOnInit(): void {
    const citySlug = this.route.snapshot.paramMap.get('citySlug');

    if (!citySlug) {
      this.errorMessage = 'City not found.';
      this.loading = false;
      return;
    }

    this.cityApi.getCityBySlug(citySlug).subscribe({
      next: city => {
        this.city = city;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.locked = isLockedContentError(error);
        this.errorMessage = this.locked
          ? 'Complete the earlier city activities to unlock this city.'
          : 'Could not load city.';
        this.loading = false;
      }
    });
  }

  get backgroundImageStyle(): string {
    const imagePath = this.city?.backgroundImage?.filePath;

    if (!imagePath) {
      return 'linear-gradient(rgba(16,42,67,.25), rgba(16,42,67,.25))';
    }

    return `linear-gradient(rgba(16,42,67,.25), rgba(16,42,67,.25)), url(${imagePath})`;
  }
}
