import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  WordMatchingDefinition,
  WordMatchSelection
} from '../../../core/models/activity.model';
import { isWordMatchingDefinition } from '../../../core/utils/activity-definition.util';

@Component({
  selector: 'app-matching-activity',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './matching-activity.component.html',
  styleUrl: './matching-activity.component.scss'
})
export class MatchingActivityComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answers: Record<string, string> = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.answers = {};
    }
  }

  get leftItems(): string[] {
    return this.definition?.leftItems ?? [];
  }

  get rightItems(): string[] {
    return this.definition?.rightItems ?? [];
  }

  get allAnswered(): boolean {
    return this.leftItems.length > 0 &&
      this.leftItems.every(left => Boolean(this.answers[left]));
  }

  submit(): void {
    if (this.disabled || !this.allAnswered) {
      return;
    }

    const matches: WordMatchSelection[] = this.leftItems.map(left => ({
      left,
      right: this.answers[left]
    }));

    this.answerSubmitted.emit({
      submittedAnswer: {
        matches
      }
    });
  }

  private get definition(): WordMatchingDefinition | null {
    return isWordMatchingDefinition(this.activity.definition)
      ? this.activity.definition
      : null;
  }
}
