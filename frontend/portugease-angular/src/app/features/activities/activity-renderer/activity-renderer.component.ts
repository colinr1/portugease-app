import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  ActivityHint,
  NormalizedActivityType
} from '../../../core/models/activity.model';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';
import { ActivityApiService } from '../../../core/services/activity-api.service';

import { MultipleChoiceActivityComponent } from '../multiple-choice-activity/multiple-choice-activity.component';
import { SentenceBuilderActivityComponent } from '../sentence-builder-activity/sentence-builder-activity.component';
import { MatchingActivityComponent } from '../matching-activity/matching-activity.component';
import { ListeningActivityComponent } from '../listening-activity/listening-activity.component';
import { TransformationActivityComponent } from '../transformation-activity/transformation-activity.component';
import { FeedbackPanelComponent } from '../../../shared/components/feedback-panel/feedback-panel.component';
import { HintPanelComponent } from '../../../shared/components/hint-panel/hint-panel.component';
import { LearnerUserService } from '../../../core/services/learner-user.service';
import {
  extractActivityHints,
  normalizeActivityType
} from '../../../core/utils/activity-definition.util';
import { isLockedContentError } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-activity-renderer',
  standalone: true,
  imports: [
    MultipleChoiceActivityComponent,
    SentenceBuilderActivityComponent,
    MatchingActivityComponent,
    ListeningActivityComponent,
    TransformationActivityComponent,
    FeedbackPanelComponent,
    HintPanelComponent
  ],
  templateUrl: './activity-renderer.component.html',
  styleUrl: './activity-renderer.component.scss'
})
export class ActivityRendererComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() locationName = 'Scenario';
  @Input() cityName = 'City';

  @Output() finished = new EventEmitter<ActivityAttemptResponse>();
  @Output() returned = new EventEmitter<void>();
  @Output() attemptResolved = new EventEmitter<ActivityAttemptResponse>();
  @Output() contentLocked = new EventEmitter<void>();

  submitting = false;
  feedback?: ActivityAttemptResponse;
  errorMessage = '';

  incorrectSubmissionCount = 0;
  visibleHintLevel = 0;

  constructor(
    private readonly activityApi: ActivityApiService,
    private readonly learnerUserService: LearnerUserService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.resetActivitySession();
    }
  }

  get activityType(): NormalizedActivityType {
    return normalizeActivityType(this.activity.activityType);
  }

  get hints(): ActivityHint[] {
    return extractActivityHints(this.activity.definition ?? {});
  }

  get hasHints(): boolean {
    return this.hints.length > 0;
  }

  get hasVisibleHints(): boolean {
    return this.visibleHintLevel > 0 && this.hasHints;
  }

  get hasFeedback(): boolean {
    return this.feedback !== undefined;
  }

  get hasProgressionAction(): boolean {
    const progressionUpdate = this.feedback?.progressionUpdate;

    return Boolean(
      progressionUpdate?.locationCompleted ||
      progressionUpdate?.unlockedLocation ||
      progressionUpdate?.cityCompleted ||
      progressionUpdate?.unlockedCity
    );
  }

  get progressionActionLabel(): string {
    const progressionUpdate = this.feedback?.progressionUpdate;

    if (progressionUpdate?.unlockedCity || progressionUpdate?.cityCompleted) {
      return 'View map';
    }

    if (progressionUpdate?.unlockedLocation || progressionUpdate?.locationCompleted) {
      return `View ${this.cityName}`;
    }

    return '';
  }

  onAnswerSubmitted(event: ActivityAnswerSubmitted): void {
    this.submitAnswer(event.submittedAnswer);
  }

  submitAnswer(submittedAnswer: Record<string, unknown>): void {
    this.submitting = true;
    this.errorMessage = '';

    this.activityApi.submitAttempt(this.activity.id, {
      userId: this.learnerUserService.selectedUserId,
      learnerSessionId: null,
      submittedAnswer,
      selectedDifficulty: this.activity.selectedDifficulty ?? 'NORMAL',
      incorrectSubmissionCount: this.incorrectSubmissionCount
    }).subscribe({
      next: response => {
        this.feedback = response;
        this.attemptResolved.emit(response);

        if (!response.isCorrect) {
          this.registerIncorrectSubmission();
        }

        this.submitting = false;
      },
      error: (error: unknown) => {
        if (isLockedContentError(error)) {
          this.contentLocked.emit();
        } else {
          this.errorMessage = 'Could not submit answer. Please try again.';
        }

        this.submitting = false;
      }
    });
  }

  resetActivity(): void {
    this.feedback = undefined;
    this.errorMessage = '';
  }

  returnToScenario(): void {
    this.returned.emit();
  }

  viewProgression(): void {
    if (this.feedback) {
      this.finished.emit(this.feedback);
    }
  }

  private registerIncorrectSubmission(): void {
    this.incorrectSubmissionCount++;

    if (this.incorrectSubmissionCount >= 3) {
      this.visibleHintLevel = 2;
      return;
    }

    this.visibleHintLevel = 1;
  }

  private resetActivitySession(): void {
    this.feedback = undefined;
    this.errorMessage = '';
    this.submitting = false;
    this.incorrectSubmissionCount = 0;
    this.visibleHintLevel = 0;
  }
}
