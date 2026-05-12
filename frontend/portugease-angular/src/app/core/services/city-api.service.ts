import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CityDetail, CityListItem } from '../models/city.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class CityApiService {
  constructor(private readonly http: HttpClient) {}

  getCities(): Observable<CityListItem[]> {
    return this.http.get<CityListItem[]>(`${API_BASE_URL}/cities`);
  }

  getCity(cityId: string): Observable<CityDetail> {
    return this.http.get<CityDetail>(`${API_BASE_URL}/cities/${cityId}`);
  }

}
