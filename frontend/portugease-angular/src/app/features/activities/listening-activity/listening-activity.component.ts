import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  ListeningDefinition
} from '../../../core/models/activity.model';
import { isListeningDefinition } from '../../../core/utils/activity-definition.util';
import { AudioPlayerComponent } from '../../../shared/components/audio-player/audio-player.component';

@Component({
  selector: 'app-listening-activity',
  standalone: true,
  imports: [FormsModule, AudioPlayerComponent],
  templateUrl: './listening-activity.component.html',
  styleUrl: './listening-activity.component.scss'
})
export class ListeningActivityComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answerText = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.answerText = '';
    }
  }

  get audioUrl(): string {
    return this.definition?.audioUrl ?? '';
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

  private get definition(): ListeningDefinition | null {
    return isListeningDefinition(this.activity.definition)
      ? this.activity.definition
      : null;
  }
}
