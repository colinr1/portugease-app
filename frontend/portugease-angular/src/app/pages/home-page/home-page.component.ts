import { Component, OnInit } from '@angular/core';
import { BrazilMapComponent } from '../../features/map/brazil-map/brazil-map.component';
import { AppFooterComponent } from '../../layout/app-footer/app-footer.component';
import { CityApiService } from '../../core/services/city-api.service';
import { CityListItem } from '../../core/models/city.model';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [BrazilMapComponent, AppFooterComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent implements OnInit {
  cities: CityListItem[] = [];
  loading = true;
  errorMessage = '';

  constructor(private readonly cityApi: CityApiService) {}

  ngOnInit(): void {
    this.cityApi.getCities().subscribe({
      next: cities => {
        this.cities = cities;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load cities.';
        this.loading = false;
      }
    });
  }
}
