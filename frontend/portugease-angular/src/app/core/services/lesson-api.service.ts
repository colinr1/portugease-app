import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LessonDetail } from '../models/lesson.model';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class LessonApiService {
  constructor(private readonly http: HttpClient) {}

  getLesson(lessonId: string): Observable<LessonDetail> {
    return this.http.get<LessonDetail>(`${API_BASE_URL}/lessons/${lessonId}`);
  }
}
