import {Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import { NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import {
  ActivityAnswerSubmitted,
  ActivityContent, ActivityHint,
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

@Component({
  selector: 'app-activity-renderer',
  standalone: true,
  imports: [
    NgIf,
    NgSwitch,
    NgSwitchCase,
    NgSwitchDefault,
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
export class ActivityRendererComponent {
  @Input({ required: true }) activity!: ActivityContent;

  @Output() finished = new EventEmitter<void>();

  submitting = false;
  feedback?: ActivityAttemptResponse;
  errorMessage = '';

  incorrectSubmissionCount = 0;
  visibleHintLevel = 0;

  constructor(private readonly activityApi: ActivityApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.resetActivitySession();
    }
  }

  get activityType(): NormalizedActivityType {
    return this.normalizeActivityType(this.activity.activityType);
  }

  get hints(): ActivityHint[] {
    const definition = this.activity.definition ?? {};
    const rawHints = definition['hints'];

    if (Array.isArray(rawHints)) {
      return rawHints
        .map((hint, index) => {
          if (typeof hint === 'string') {
            return {
              level: index + 1,
              text: hint
            };
          }

          if (hint && typeof hint === 'object') {
            const hintObject = hint as Record<string, unknown>;

            return {
              level: Number(hintObject['level'] ?? index + 1),
              text: String(hintObject['text'] ?? '')
            };
          }

          return null;
        })
        .filter((hint): hint is ActivityHint => !!hint && hint.text.trim().length > 0)
        .sort((a, b) => a.level - b.level);
    }

    const fallbackHints: ActivityHint[] = [];

    const hint1 = definition['hint'];
    const hint2 = definition['hint2'] ?? definition['secondHint'];

    if (hint1 != null && String(hint1).trim().length > 0) {
      fallbackHints.push({
        level: 1,
        text: String(hint1)
      });
    }

    if (hint2 != null && String(hint2).trim().length > 0) {
      fallbackHints.push({
        level: 2,
        text: String(hint2)
      });
    }

    return fallbackHints;
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
      selectedDifficulty: this.activity.selectedDifficulty ?? 'NORMAL',
      incorrectSubmissionCount: this.incorrectSubmissionCount
    }).subscribe({
      next: response => {
        this.feedback = response;

        if (!response.isCorrect) {
          this.registerIncorrectSubmission()
        }
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
  }

  returnToScenario(): void {
    this.finished.emit();
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
