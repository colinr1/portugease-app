import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  SentenceTransformationDefinition
} from '../../../core/models/activity.model';
import { isSentenceTransformationDefinition } from '../../../core/utils/activity-definition.util';

@Component({
  selector: 'app-transformation-activity',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './transformation-activity.component.html',
  styleUrl: './transformation-activity.component.scss'
})
export class TransformationActivityComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answerText = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.answerText = '';
    }
  }

  get prompt(): string {
    return this.definition?.prompt ?? '';
  }

  get sourceSentence(): string {
    return this.definition?.sourceSentence ?? '';
  }

  submit(): void {
    if (this.disabled || !this.answerText.trim()) {
      return;
    }

    this.answerSubmitted.emit({
      submittedAnswer: {
        text: this.answerText.trim()
      }
    });
  }

  private get definition(): SentenceTransformationDefinition | null {
    return isSentenceTransformationDefinition(this.activity.definition)
      ? this.activity.definition
      : null;
  }
}
