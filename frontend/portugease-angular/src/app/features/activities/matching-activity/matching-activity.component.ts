import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  WordMatchPair
} from '../../../core/models/activity.model';

@Component({
  selector: 'app-matching-activity',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './matching-activity.component.html',
  styleUrl: './matching-activity.component.scss'
})
export class MatchingActivityComponent {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answers: Record<string, string> = {};

  get pairs(): WordMatchPair[] {
    return (this.activity.definition['pairs'] as WordMatchPair[]) ?? [];
  }

  get rightOptions(): string[] {
    return [...this.pairs.map(pair => pair.right)].sort();
  }

  get allAnswered(): boolean {
    return this.pairs.every(pair => Boolean(this.answers[pair.left]));
  }

  submit(): void {
    if (this.disabled || !this.allAnswered) {
      return;
    }

    const matches = Object.entries(this.answers).map(([left, right]) => ({
      left,
      right
    }));

    this.answerSubmitted.emit({
      submittedAnswer: {
        matches
      }
    });
  }
}
