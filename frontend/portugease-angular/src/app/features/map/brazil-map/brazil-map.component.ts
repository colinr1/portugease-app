import { Component, Input } from '@angular/core';
import { CityListItem } from '../../../core/models/city.model';
import { CityMarkerComponent } from '../city-marker/city-marker.component';

@Component({
  selector: 'app-brazil-map',
  standalone: true,
  imports: [CityMarkerComponent],
  templateUrl: './brazil-map.component.html',
  styleUrl: './brazil-map.component.scss'
})
export class BrazilMapComponent {
  @Input({ required: true }) cities: CityListItem[] = [];

  readonly mapImagePath = '/assets/images/maps/brazil-map.jpg';
}
