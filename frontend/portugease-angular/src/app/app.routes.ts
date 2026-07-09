import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { CityPageComponent } from './pages/city-page/city-page.component';
import { LocationPageComponent } from './pages/location-page/location-page.component';
import { CreateLocationComponent } from './pages/create-location/create-location.component';

export const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
    title: 'PortugEase | Brazil Map'
  },
  {
    path: 'cities/:cityId',
    component: CityPageComponent,
    title: 'PortugEase | City'
  },
  {
    path: 'locations/:locationId',
    component: LocationPageComponent,
    title: 'PortugEase | Location'
  },
  {
    path: 'create-location',
    component: CreateLocationComponent,
    title: 'PortugEase | Create Location'
  },
  {
    path: '**',
    redirectTo: ''
  }
];
