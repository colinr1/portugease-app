import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import {
  ActivityAnswerSubmitted,
  ActivityContent
} from '../../../core/models/activity.model';

@Component({
  selector: 'app-transformation-activity',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './transformation-activity.component.html',
  styleUrl: './transformation-activity.component.scss'
})
export class TransformationActivityComponent {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answerText = '';

  get prompt(): string {
    return String(this.activity.definition['prompt'] ?? '');
  }

  get sourceSentence(): string {
    return String(this.activity.definition['sourceSentence'] ?? '');
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
}
