import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import {
  ActivityAnswerSubmitted,
  ActivityContent
} from '../../../core/models/activity.model';

@Component({
  selector: 'app-sentence-builder-activity',
  standalone: true,
  imports: [],
  templateUrl: './sentence-builder-activity.component.html',
  styleUrl: './sentence-builder-activity.component.scss'
})
export class SentenceBuilderActivityComponent implements OnChanges {
  @Input({ required: true }) activity!: ActivityContent;
  @Input() disabled = false;

  @Output() answerSubmitted = new EventEmitter<ActivityAnswerSubmitted>();

  selectedTokens: string[] = [];
  shuffledTokens: string[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity'] && this.activity) {
      this.selectedTokens = [];
      this.shuffledTokens = this.shuffleTokens(this.originalTokens);
    }
  }

  get originalTokens(): string[] {
    return (this.activity.definition['tokens'] as string[]) ?? [];
  }

  addToken(token: string): void {
    if (this.disabled) {
      return;
    }

    this.selectedTokens.push(token);
  }

  removeLastToken(): void {
    if (this.disabled) {
      return;
    }

    this.selectedTokens = this.selectedTokens.slice(0, -1);
  }

  clear(): void {
    if (this.disabled) {
      return;
    }

    this.selectedTokens = [];
  }

  submit(): void {
    if (this.disabled || this.selectedTokens.length === 0) {
      return;
    }

    this.answerSubmitted.emit({
      submittedAnswer: {
        tokens: this.selectedTokens,
        sentence: this.selectedTokens.join(' ')
      }
    });
  }

  private shuffleTokens(tokens: string[]): string[] {
    const shuffled = [...tokens];

    for (let index = shuffled.length - 1; index > 0; index--) {
      const randomIndex = Math.floor(Math.random() * (index + 1));
      [shuffled[index], shuffled[randomIndex]] = [shuffled[randomIndex], shuffled[index]];
    }

    if (shuffled.length > 1 && this.arraysEqual(shuffled, tokens)) {
      return this.rotateTokens(shuffled);
    }

    return shuffled;
  }

  private rotateTokens(tokens: string[]): string[] {
    if (tokens.length <= 1) {
      return tokens;
    }

    return [...tokens.slice(1), tokens[0]];
  }

  private arraysEqual(first: string[], second: string[]): boolean {
    return first.length === second.length &&
      first.every((value, index) => value === second[index]);
  }
}
