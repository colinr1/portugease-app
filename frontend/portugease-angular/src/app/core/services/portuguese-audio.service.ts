import { Injectable, OnDestroy } from '@angular/core';
import { VocabularyTooltipContent } from '../models/hotspot.model';

@Injectable({
  providedIn: 'root'
})
export class PortugueseAudioService implements OnDestroy {
  private currentAudio?: HTMLAudioElement;
  private readonly unavailableAudioSources = new Set<string>();

  playVocabulary(
    vocabulary: VocabularyTooltipContent,
    explicitAudioSource?: string | null
  ): void {
    this.playText(
      vocabulary.portugueseText,
      explicitAudioSource ?? vocabulary.audioPath
    );
  }

  playText(text: string, audioSource?: string | null): void {
    const normalizedAudioSource = this.normalizeAudioSource(audioSource);

    this.stop();

    if (normalizedAudioSource) {
      if (this.unavailableAudioSources.has(normalizedAudioSource)) {
        this.speak(text);
        return;
      }

      const audio = new Audio(normalizedAudioSource);
      let fallbackStarted = false;
      this.currentAudio = audio;

      const fallbackToSpeech = (markAudioUnavailable: boolean): void => {
        if (fallbackStarted || this.currentAudio !== audio) {
          return;
        }

        fallbackStarted = true;
        this.currentAudio = undefined;

        if (markAudioUnavailable) {
          this.unavailableAudioSources.add(normalizedAudioSource);
        }

        this.speak(text);
      };

      audio.addEventListener('error', () => {
        fallbackToSpeech(true);
      }, { once: true });

      audio.play().catch(error => {
        if (this.isPlaybackBlocked(error)) {
          if (this.currentAudio === audio) {
            this.currentAudio = undefined;
          }

          return;
        }

        window.setTimeout(() => {
          if (!audio.paused || audio.currentTime > 0) {
            return;
          }

          fallbackToSpeech(false);
        }, 0);
      });

      audio.addEventListener('ended', () => {
        if (this.currentAudio === audio) {
          this.currentAudio = undefined;
        }
      });

      return;
    }

    this.speak(text);
  }

  stop(): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio.currentTime = 0;
      this.currentAudio = undefined;
    }

    if (this.canUseSpeechSynthesis()) {
      window.speechSynthesis.cancel();
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }

  private speak(text: string): void {
    const spokenText = text.trim();

    if (!spokenText || !this.canUseSpeechSynthesis()) {
      return;
    }

    const utterance = new SpeechSynthesisUtterance(spokenText);
    utterance.lang = 'pt-BR';
    utterance.rate = 0.9;

    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  }

  private canUseSpeechSynthesis(): boolean {
    return (
      typeof window !== 'undefined' &&
      'speechSynthesis' in window &&
      typeof SpeechSynthesisUtterance !== 'undefined'
    );
  }

  private normalizeAudioSource(value: string | null | undefined): string | null {
    if (!value) {
      return null;
    }

    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private isPlaybackBlocked(error: unknown): boolean {
    return error instanceof DOMException && error.name === 'NotAllowedError';
  }
}
