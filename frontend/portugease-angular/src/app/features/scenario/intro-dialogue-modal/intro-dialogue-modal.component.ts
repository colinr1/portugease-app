import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  Output,
  ViewChild
} from '@angular/core';
import { IntroDialogue, IntroDialogueLine } from '../../../core/models/lesson.model';
import { PortugueseAudioService } from '../../../core/services/portuguese-audio.service';

@Component({
  selector: 'app-intro-dialogue-modal',
  standalone: true,
  imports: [],
  templateUrl: './intro-dialogue-modal.component.html',
  styleUrl: './intro-dialogue-modal.component.scss'
})
export class IntroDialogueModalComponent implements AfterViewInit, OnDestroy {
  @Input({ required: true }) dialogue!: IntroDialogue;

  @Output() closed = new EventEmitter<void>();
  @Output() finished = new EventEmitter<void>();
  @Output() lineChanged = new EventEmitter<IntroDialogueLine | null>();

  @ViewChild('closeButton') closeButton?: ElementRef<HTMLButtonElement>;

  currentLineIndex = 0;

  constructor(private readonly portugueseAudio: PortugueseAudioService) {}

  ngAfterViewInit(): void {
    queueMicrotask(() => {
      this.closeButton?.nativeElement.focus();
      this.emitCurrentLine();
      this.playCurrentAudio();
    });
  }

  get currentLine(): IntroDialogueLine | null {
    if (!this.dialogue?.lines?.length) {
      return null;
    }

    return this.dialogue.lines[this.currentLineIndex] ?? null;
  }

  get isFinalLine(): boolean {
    if (!this.dialogue?.lines?.length) {
      return true;
    }

    return this.currentLineIndex === this.dialogue.lines.length - 1;
  }

  get progressText(): string {
    const total = this.dialogue?.lines?.length ?? 0;

    if (total === 0) {
      return '0 of 0';
    }

    return `${this.currentLineIndex + 1} of ${total}`;
  }

  get nextButtonLabel(): string {
    return this.isFinalLine ? 'Finish' : 'Next';
  }

  get playAudioLabel(): string {
    const lineNumber = this.currentLineIndex + 1;
    const speaker = this.currentLine?.speaker;

    if (speaker) {
      return `Play audio for ${speaker}, line ${lineNumber}`;
    }

    return `Play audio for dialogue line ${lineNumber}`;
  }

  playCurrentAudio(): void {
    const currentLine = this.currentLine;

    if (!currentLine) {
      return;
    }

    this.portugueseAudio.playText(currentLine.portugueseText, currentLine.audioPath);
  }

  goNext(): void {
    this.stopCurrentPlayback();

    if (this.isFinalLine) {
      this.lineChanged.emit(null)
      this.finished.emit();
      return;
    }

    this.currentLineIndex++;
    this.emitCurrentLine();

    setTimeout(() => {
      this.playCurrentAudio();
    });
  }

  close(): void {
    this.stopCurrentPlayback();
    this.lineChanged.emit(null);
    this.closed.emit();
  }

  ngOnDestroy(): void {
    this.stopCurrentPlayback();
  }

  stopBackdropClick(event: MouseEvent): void {
    event.stopPropagation();
  }

  private emitCurrentLine(): void {
    this.lineChanged.emit(this.currentLine);
  }

  private stopCurrentPlayback(): void {
    this.portugueseAudio.stop();
  }

  @HostListener('document:keydown.escape')
  onEscapePressed(): void {
    this.close();
  }
}
