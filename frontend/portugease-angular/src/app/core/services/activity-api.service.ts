import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  ActivityAttemptRequest,
  ActivityAttemptResponse
} from '../models/attempt.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class ActivityApiService {
  constructor(private readonly http: HttpClient) {}


  submitAttempt(
    activityId: string,
    request: ActivityAttemptRequest
  ): Observable<ActivityAttemptResponse> {
    return this.http.post<ActivityAttemptResponse>(
      `${API_BASE_URL}/activities/${activityId}/attempts`,
      request
    );
  }
}
