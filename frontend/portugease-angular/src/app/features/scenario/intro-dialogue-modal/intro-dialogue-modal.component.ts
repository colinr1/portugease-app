import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild
} from '@angular/core';
import { NgIf } from '@angular/common';
import { IntroDialogue, IntroDialogueLine } from '../../../core/models/lesson.model';

@Component({
  selector: 'app-intro-dialogue-modal',
  standalone: true,
  imports: [NgIf],
  templateUrl: './intro-dialogue-modal.component.html',
  styleUrl: './intro-dialogue-modal.component.scss'
})
export class IntroDialogueModalComponent implements AfterViewInit {
  @Input({ required: true }) dialogue!: IntroDialogue;

  @Output() closed = new EventEmitter<void>();
  @Output() finished = new EventEmitter<void>();

  @ViewChild('closeButton') closeButton?: ElementRef<HTMLButtonElement>;
  @ViewChild('lineAudio') lineAudio?: ElementRef<HTMLAudioElement>;

  currentLineIndex = 0;
  audioAutoplayBlocked = false;

  ngAfterViewInit(): void {
    queueMicrotask(() => {
      this.closeButton?.nativeElement.focus();
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
    const audio = this.lineAudio?.nativeElement;

    if (!audio || !this.currentLine?.audioPath) {
      return;
    }

    this.audioAutoplayBlocked = false;

    audio.pause();
    audio.currentTime = 0;

    audio.play().catch(() => {
      this.audioAutoplayBlocked = true;
    });
  }

  goNext(): void {
    if (this.isFinalLine) {
      this.finished.emit();
      return;
    }

    this.currentLineIndex++;
    this.audioAutoplayBlocked = false;

    setTimeout(() => {
      this.playCurrentAudio();
    });
  }

  close(): void {
    this.closed.emit();
  }

  stopBackdropClick(event: MouseEvent): void {
    event.stopPropagation();
  }

  @HostListener('document:keydown.escape')
  onEscapePressed(): void {
    this.close();
  }
}
