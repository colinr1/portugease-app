import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  MultipleChoiceDefinition,
  MultipleChoiceOption
} from '../../../core/models/activity.model';
import { isMultipleChoiceDefinition } from '../../../core/utils/activity-definition.util';

@Component({
  selector: 'app-multiple-choice-activity',
  standalone: true,
  imports: [],
  templateUrl: './multiple-choice-activity.component.html',
  styleUrl: './multiple-choice-activity.component.scss'
})
export class MultipleChoiceActivityComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  selectedOptionId = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.selectedOptionId = '';
    }
  }

  get question(): string {
    return this.definition?.question ?? '';
  }

  get options(): MultipleChoiceOption[] {
    return this.definition?.options ?? [];
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

  private get definition(): MultipleChoiceDefinition | null {
    return isMultipleChoiceDefinition(this.activity.definition)
      ? this.activity.definition
      : null;
  }
}
