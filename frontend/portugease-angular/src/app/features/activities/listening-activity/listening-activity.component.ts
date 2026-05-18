import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import {
  ActivityAnswerSubmitted,
  ActivityContent
} from '../../../core/models/activity.model';
import { AudioPlayerComponent } from '../../../shared/components/audio-player/audio-player.component';

@Component({
  selector: 'app-listening-activity',
  standalone: true,
  imports: [FormsModule, NgIf, AudioPlayerComponent],
  templateUrl: './listening-activity.component.html',
  styleUrl: './listening-activity.component.scss'
})
export class ListeningActivityComponent {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answerText = '';

  get audioUrl(): string {
    return String(this.activity.definition['audioUrl'] ?? '');
  }

  get transcript(): string | null {
    const transcript = this.activity.definition['transcript'];
    return transcript == null ? null : String(transcript);
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
