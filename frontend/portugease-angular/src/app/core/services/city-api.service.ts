import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CityDetail, CityListItem } from '../models/city.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { LearnerUserService } from './learner-user.service';

@Injectable({
  providedIn: 'root'
})
export class CityApiService {
  constructor(
    private readonly http: HttpClient,
    private readonly learnerUserService: LearnerUserService
  ) {}

  getCities(): Observable<CityListItem[]> {
    return this.http.get<CityListItem[]>(`${API_BASE_URL}/cities`, {
      params: this.userParams()
    });
  }

  getCity(cityId: string): Observable<CityDetail> {
    return this.http.get<CityDetail>(`${API_BASE_URL}/cities/${cityId}`, {
      params: this.userParams()
    });
  }

  private userParams(): HttpParams {
    const userId = this.learnerUserService.selectedUserId;

    return userId
      ? new HttpParams().set('userId', userId)
      : new HttpParams();
  }
}
