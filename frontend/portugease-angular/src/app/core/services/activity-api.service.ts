import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  ActivityAttemptRequest,
  ActivityAttemptResponse
} from '../../../../../../../../Documents/PortugEase-working-mvp/cleaned-up-mvp/frontend/portugease-angular/src/app/core/models/attempt.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../../../../../Documents/PortugEase-working-mvp/cleaned-up-mvp/frontend/portugease-angular/src/app/core/services/api.config';

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
