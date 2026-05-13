import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgIf } from '@angular/common';
import { CityApiService } from '../../core/services/city-api.service';
import { CityDetail } from '../../core/models/city.model';
import { CityLocationMenuComponent } from '../../features/cities/city-location-menu/city-location-menu.component';

@Component({
  selector: 'app-city-page',
  standalone: true,
  imports: [NgIf, CityLocationMenuComponent],
  templateUrl: './city-page.component.html',
  styleUrl: './city-page.component.scss'
})
export class CityPageComponent implements OnInit {
  city?: CityDetail;
  loading = true;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly cityApi: CityApiService
  ) {}

  ngOnInit(): void {
    const cityId = this.route.snapshot.paramMap.get('cityId');

    if (!cityId) {
      this.errorMessage = 'City not found.';
      this.loading = false;
      return;
    }

    this.cityApi.getCity(cityId).subscribe({
      next: city => {
        this.city = city;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load city.';
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
