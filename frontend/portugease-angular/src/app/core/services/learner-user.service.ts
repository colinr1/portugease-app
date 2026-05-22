import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { LearnerUser } from '../models/learner-user.model';

@Injectable({
  providedIn: 'root'
})
export class LearnerUserService {
  private readonly storageKey = 'portugease.selectedUser';

  private readonly selectedUserSubject = new BehaviorSubject<LearnerUser | null>(
    this.loadStoredUser()
  );

  readonly selectedUser$ = this.selectedUserSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  get selectedUser(): LearnerUser | null {
    return this.selectedUserSubject.value;
  }

  get selectedUserId(): string | null {
    return this.selectedUser?.id ?? null;
  }

  lookupUsername(username: string): Observable<LearnerUser> {
    const params = new HttpParams().set('username', username.trim());

    return this.http.get<LearnerUser>(`${API_BASE_URL}/users/lookup`, { params }).pipe(
      tap(user => this.setSelectedUser(user))
    );
  }

  setSelectedUser(user: LearnerUser): void {
    localStorage.setItem(this.storageKey, JSON.stringify(user));
    this.selectedUserSubject.next(user);
  }

  clearSelectedUser(): void {
    localStorage.removeItem(this.storageKey);
    this.selectedUserSubject.next(null);
  }

  private loadStoredUser(): LearnerUser | null {
    const raw = localStorage.getItem(this.storageKey);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as LearnerUser;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
