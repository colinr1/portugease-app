import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LessonDetail } from '../models/lesson.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { LearnerUserService } from './learner-user.service';

@Injectable({
  providedIn: 'root'
})
export class LessonApiService {
  constructor(
    private readonly http: HttpClient,
    private readonly learnerUserService: LearnerUserService
  ) {}

  getLesson(lessonId: string): Observable<LessonDetail> {
    const userId = this.learnerUserService.selectedUserId;

    const params = userId
      ? new HttpParams().set('userId', userId)
      : new HttpParams();

    return this.http.get<LessonDetail>(`${API_BASE_URL}/lessons/${lessonId}`, {
      params
    });
  }

  getLessonByLocationSlug(locationSlug: string): Observable<LessonDetail> {
    const userId = this.learnerUserService.selectedUserId;

    const params = userId
      ? new HttpParams().set('userId', userId)
      : new HttpParams();

    return this.http.get<LessonDetail>(
      `${API_BASE_URL}/lessons/by-location-slug/${encodeURIComponent(locationSlug)}`,
      { params }
    );
  }
}
