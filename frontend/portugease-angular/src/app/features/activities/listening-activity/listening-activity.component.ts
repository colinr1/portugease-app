import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActivityAnswerSubmitted,
  ActivityContent,
  ListeningDefinition
} from '../../../core/models/activity.model';
import { PortugueseAudioService } from '../../../core/services/portuguese-audio.service';
import { isListeningDefinition } from '../../../core/utils/activity-definition.util';

@Component({
  selector: 'app-listening-activity',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './listening-activity.component.html',
  styleUrl: './listening-activity.component.scss'
})
export class ListeningActivityComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  answerText = '';

  constructor(private readonly portugueseAudio: PortugueseAudioService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']) {
      this.answerText = '';
      this.portugueseAudio.stop();
    }
  }

  get hasPlayableAudio(): boolean {
    const definition = this.definition;

    if (!definition) {
      return false;
    }

    return Boolean(
      definition.audioUrl?.trim() || definition.correctAnswer?.trim()
    );
  }

  playAudio(): void {
    const definition = this.definition;

    if (!definition || !this.hasPlayableAudio) {
      return;
    }

    this.portugueseAudio.playText(
      definition.correctAnswer ?? '',
      definition.audioUrl
    );
  }

  ngOnDestroy(): void {
    this.portugueseAudio.stop();
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
