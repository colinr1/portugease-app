import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';

export interface IntroDialogueSeenRequest {
  userId?: string | null;
  source?: IntroDialogueSeenSource | string | null;
}

export interface IntroDialogueSeenResponse {
  locationId: string;
  introDialogueSeen: boolean;
  introDialogueSeenAt: string | null;
  introDialogueReplayCount: number;
}

export type IntroDialogueSeenSource =
  | 'AUTO_OPEN_CLOSE'
  | 'AUTO_OPEN_FINISH'
  | 'HOTSPOT_CLOSE'
  | 'HOTSPOT_FINISH';

@Injectable({
  providedIn: 'root'
})
export class LocationApiService {
  constructor(private readonly http: HttpClient) {}




  markIntroDialogueSeen(
    locationId: string,
    source: IntroDialogueSeenSource | string
  ): Observable<IntroDialogueSeenResponse> {
    const request: IntroDialogueSeenRequest = {
      userId: null,
      source
    };

    return this.http.post<IntroDialogueSeenResponse>(
      `${API_BASE_URL}/locations/${locationId}/intro-dialogue/seen`,
      request
    );
  }
}
