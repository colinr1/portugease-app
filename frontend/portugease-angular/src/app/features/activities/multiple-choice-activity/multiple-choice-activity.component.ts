import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  MultipleChoiceOption
} from '../../../core/models/activity.model';

@Component({
  selector: 'app-multiple-choice-activity',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './multiple-choice-activity.component.html',
  styleUrl: './multiple-choice-activity.component.scss'
})
export class MultipleChoiceActivityComponent {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  selectedOptionId = '';

  get question(): string {
    return String(this.activity.definition['question'] ?? '');
  }

  get options(): MultipleChoiceOption[] {
    return (this.activity.definition['options'] as MultipleChoiceOption[]) ?? [];
  }

  submit(): void {
    if (!this.selectedOptionId || this.disabled) {
      return;
    }

    this.answerSubmitted.emit({
      submittedAnswer: {
        selectedOptionId: this.selectedOptionId
      }
    });
  }
}
