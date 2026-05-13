import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LocationMenuItem } from '../../../core/models/location.model';

@Component({
  selector: 'app-city-location-menu',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink],
  templateUrl: './city-location-menu.component.html',
  styleUrl: './city-location-menu.component.scss'
})
export class CityLocationMenuComponent {
  @Input({ required: true }) locations: LocationMenuItem[] = [];
}
