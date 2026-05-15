import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  NormalizedActivityType
} from '../../../core/models/activity.model';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';
import { ActivityApiService } from '../../../core/services/activity-api.service';

import { MultipleChoiceActivityComponent } from '../multiple-choice-activity/multiple-choice-activity.component';
import { MatchingActivityComponent } from '../matching-activity/matching-activity.component';
import { FeedbackPanelComponent } from '../../../shared/components/feedback-panel/feedback-panel.component';
import { HintPanelComponent } from '../../../shared/components/hint-panel/hint-panel.component';

@Component({
  selector: 'app-activity-renderer',
  standalone: true,
  imports: [
    NgIf,
    NgSwitch,
    NgSwitchCase,
    NgSwitchDefault,
    MultipleChoiceActivityComponent,
    MatchingActivityComponent,
    FeedbackPanelComponent,
    HintPanelComponent
  ],
  templateUrl: './activity-renderer.component.html',
  styleUrl: './activity-renderer.component.scss'
})
export class ActivityRendererComponent {
  @Input({ required: true }) activity!: ActivityContent;

  @Output() finished = new EventEmitter<void>();

  hintsUsed = 0;
  submitting = false;
  feedback?: ActivityAttemptResponse;
  errorMessage = '';

  constructor(private readonly activityApi: ActivityApiService) {}

  get activityType(): NormalizedActivityType {
    return this.normalizeActivityType(this.activity.activityType);
  }

  get hint(): string | null {
    const hint = this.activity.definition?.['hint'];
    return hint == null ? null : String(hint);
  }

  get hasFeedback(): boolean {
    return this.feedback !== undefined;
  }

  onHintUsed(): void {
    this.hintsUsed++;
  }

  onAnswerSubmitted(event: ActivityAnswerSubmitted): void {
    this.submitAnswer(event.submittedAnswer);
  }

  submitAnswer(submittedAnswer: Record<string, unknown>): void {
    this.submitting = true;
    this.errorMessage = '';

    this.activityApi.submitAttempt(this.activity.id, {
      userId: null,
      learnerSessionId: null,
      submittedAnswer,
      hintsUsed: this.hintsUsed
    }).subscribe({
      next: response => {
        this.feedback = response;
        this.submitting = false;
      },
      error: () => {
        this.errorMessage = 'Could not submit answer. Please try again.';
        this.submitting = false;
      }
    });
  }

  resetActivity(): void {
    this.feedback = undefined;
    this.errorMessage = '';
    this.hintsUsed = 0;
  }

  returnToScenario(): void {
    this.finished.emit();
  }

  private normalizeActivityType(type: string): NormalizedActivityType {
    const normalized = type.trim().toUpperCase();

    switch (normalized) {
      case 'MULTIPLE_CHOICE':
        return 'MULTIPLE_CHOICE';

      case 'WORD_MATCHING':
        return 'WORD_MATCHING';

      case 'SENTENCE_BUILDING':
        return 'SENTENCE_BUILDING';

      case 'LISTENING':
        return 'LISTENING';

      case 'TRANSFORMATION':
      case 'SENTENCE_TRANSFORMATION':
        return 'SENTENCE_TRANSFORMATION';

      case 'SCENARIO_CHALLENGE':
        return 'SCENARIO_CHALLENGE';

      default:
        return 'MULTIPLE_CHOICE';
    }
  }
}
