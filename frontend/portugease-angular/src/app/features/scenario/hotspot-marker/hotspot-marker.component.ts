import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output
} from '@angular/core';
import { NgIf } from '@angular/common';
import { Hotspot, VocabularyTooltipContent } from '../../../core/models/hotspot.model';
import {
  isIntroDialogueHotspot,
  isVocabularyTooltipHotspot
} from '../../../core/utils/hotspot.util';
import { PortugueseAudioService } from '../../../core/services/portuguese-audio.service';

@Component({
  selector: 'app-hotspot-marker',
  standalone: true,
  imports: [NgIf],
  templateUrl: './hotspot-marker.component.html',
  styleUrl: './hotspot-marker.component.scss'
})
export class HotspotMarkerComponent {
  @Input({ required: true }) hotspot!: Hotspot;

  @Output() selected = new EventEmitter<Hotspot>();

  tooltipOpen = false;
  private pointerActivating = false;

  constructor(
    private readonly elementRef: ElementRef<HTMLElement>,
    private readonly portugueseAudio: PortugueseAudioService
  ) {}

  get markerStyle(): Record<string, string> {
    return {
      left: `${this.clampPercent(this.hotspot.xPercent)}%`,
      top: `${this.clampPercent(this.hotspot.yPercent)}%`
    };
  }

  get isIntroDialogue(): boolean {
    return isIntroDialogueHotspot(this.hotspot);
  }

  get isVocabularyTooltip(): boolean {
    return isVocabularyTooltipHotspot(this.hotspot);
  }

  get vocabulary(): VocabularyTooltipContent | null {
    if (this.hotspot.vocabulary) {
      return this.hotspot.vocabulary;
    }

    const rawVocabulary = this.hotspot.raw?.['vocabulary'];

    if (this.isVocabularyShape(rawVocabulary)) {
      return rawVocabulary;
    }

    return null;
  }

  get tooltipId(): string {
    return `vocab-tooltip-${this.hotspot.id}`;
  }

  get ariaLabel(): string {
    if (this.hotspot.ariaLabel && this.hotspot.ariaLabel.trim().length > 0) {
      return this.hotspot.ariaLabel;
    }

    if (this.isVocabularyTooltip) {
      return `${this.hotspot.label}. Vocabulary tooltip.`;
    }

    if (this.isIntroDialogue) {
      return `${this.hotspot.label}. Opens introductory dialogue.`;
    }

    return `${this.hotspot.label}. Opens interaction.`;
  }

  onPointerDown(): void {
    this.pointerActivating = true;
  }

  onFocus(): void {
    if (this.isVocabularyTooltip && !this.pointerActivating) {
      this.tooltipOpen = true;
    }
  }

  onBlur(): void {
    if (this.isVocabularyTooltip) {
      this.tooltipOpen = false;
    }

    this.pointerActivating = false;
  }

  onMouseEnter(): void {
    if (this.isVocabularyTooltip) {
      this.tooltipOpen = true;
    }
  }

  onMouseLeave(): void {
    if (this.isVocabularyTooltip) {
      this.tooltipOpen = false;
    }
  }

  selectHotspot(event: MouseEvent): void {
    if (this.isVocabularyTooltip) {
      event.stopPropagation();
      this.tooltipOpen = !this.tooltipOpen;
      this.pointerActivating = false;
      this.playVocabularyAudio();
      return;
    }

    this.selected.emit(this.hotspot);
  }

  closeTooltip(): void {
    this.tooltipOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.tooltipOpen || !this.isVocabularyTooltip) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(event.target as Node);

    if (!clickedInside) {
      this.closeTooltip();
    }
  }

  @HostListener('document:keydown.escape')
  onEscapePressed(): void {
    if (this.tooltipOpen) {
      this.closeTooltip();
    }
  }

  private clampPercent(value: number): number {
    if (Number.isNaN(value)) {
      return 0;
    }

    return Math.min(100, Math.max(0, value));
  }

  private isVocabularyShape(value: unknown): value is VocabularyTooltipContent {
    if (!value || typeof value !== 'object') {
      return false;
    }

    const candidate = value as Partial<VocabularyTooltipContent>;

    return (
      typeof candidate.portugueseText === 'string' &&
      typeof candidate.englishTranslation === 'string'
    );
  }

  private playVocabularyAudio(): void {
    const vocabulary = this.vocabulary;

    if (!vocabulary) {
      return;
    }

    this.portugueseAudio.playVocabulary(vocabulary, this.hotspotAudioSource);
  }

  private get hotspotAudioSource(): string | null {
    const audioSource =
      this.hotspot.raw?.['audioPath'] ??
      null;

    return typeof audioSource === 'string' ? audioSource : null;
  }
}
