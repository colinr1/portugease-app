import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CityListItem } from '../../../core/models/city.model';

@Component({
  selector: 'app-city-marker',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './city-marker.component.html',
  styleUrl: './city-marker.component.scss'
})
export class CityMarkerComponent {
  @Input({ required: true }) city!: CityListItem;

  get locked(): boolean {
    return this.city.status === 'LOCKED';
  }

  get markerStyle(): Record<string, string> {
    return {
      left: `${this.city.marker.xPercent}%`,
      top: `${this.city.marker.yPercent}%`
    };
  }
}
