import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgIf } from '@angular/common';
import { LessonApiService } from '../../core/services/lesson-api.service';
import { LessonDetail } from '../../core/models/lesson.model';
import { LocationScenarioComponent } from '../../features/scenario/location-scenario/location-scenario.component';

@Component({
  selector: 'app-location-page',
  standalone: true,
  imports: [NgIf, LocationScenarioComponent],
  templateUrl: './location-page.component.html',
  styleUrl: './location-page.component.scss'
})
export class LocationPageComponent implements OnInit {
  lesson?: LessonDetail;
  loading = true;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly lessonApi: LessonApiService
  ) {}

  ngOnInit(): void {
    const locationId = this.route.snapshot.paramMap.get('locationId');

    if (!locationId) {
      this.errorMessage = 'Location not found.';
      this.loading = false;
      return;
    }

    this.lessonApi.getLesson(locationId).subscribe({
      next: lesson => {
        this.lesson = lesson;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load scenario.';
        this.loading = false;
      }
    });
  }
}
