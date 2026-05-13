import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { NgIf } from '@angular/common';
import { LessonDetail } from '../../../core/models/lesson.model';
import { ActivityContent } from '../../../core/models/activity.model';
import { Hotspot } from '../../../core/models/hotspot.model';
import {
  IntroDialogueSeenSource,
  LocationApiService
} from '../../../core/services/location-api.service';
import { ImageSceneComponent } from '../image-scene/image-scene.component';
import { ScenarioInteractionModalComponent } from '../scenario-interaction-modal/scenario-interaction-modal.component';
import { IntroDialogueModalComponent } from '../intro-dialogue-modal/intro-dialogue-modal.component';

@Component({
  selector: 'app-location-scenario',
  standalone: true,
  imports: [
    NgIf,
    ImageSceneComponent,
    ScenarioInteractionModalComponent,
    IntroDialogueModalComponent
  ],
  templateUrl: './location-scenario.component.html',
  styleUrl: './location-scenario.component.scss'
})
export class LocationScenarioComponent implements OnChanges {
  @Input({ required: true }) lesson!: LessonDetail;

  selectedActivity?: ActivityContent;
  selectedHotspot?: Hotspot;

  introDialogueOpen = false;
  introDialogueOpenedFrom: 'AUTO_OPEN' | 'HOTSPOT' | null = null;

  get modalOpen(): boolean {
    return this.introDialogueOpen || Boolean(this.selectedActivity && this.selectedHotspot);
  }

  private autoOpenCheckCompleted = false;
  private markSeenInProgress = false;
  private markSeenCompletedForCurrentOpening = false;

  constructor(private readonly locationApi: LocationApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lesson'] && this.lesson) {
      this.resetIntroDialogueOpeningState();
      this.maybeAutoOpenIntroDialogue();
    }
  }

  onHotspotSelected(hotspot: Hotspot): void {
    if (this.isVocabularyTooltipHotspot(hotspot)) {
      return;
    }

    if (this.isIntroDialogueHotspot(hotspot)) {
      this.openIntroDialogueFromHotspot();
      return;
    }

    this.selectedHotspot = hotspot;
    this.selectedActivity = this.lesson.activities.find(activity =>
      activity.id === hotspot.activityId ||
      activity.activityKey === hotspot.activityKey
    );
  }

  closeNormalModal(): void {
    this.selectedHotspot = undefined;
    this.selectedActivity = undefined;
  }

  onIntroDialogueClosed(): void {
    const source: IntroDialogueSeenSource =
      this.introDialogueOpenedFrom === 'AUTO_OPEN'
        ? 'AUTO_OPEN_CLOSE'
        : 'HOTSPOT_CLOSE';

    this.closeIntroDialogueAndMarkSeen(source);
  }

  onIntroDialogueFinished(): void {
    const source: IntroDialogueSeenSource =
      this.introDialogueOpenedFrom === 'AUTO_OPEN'
        ? 'AUTO_OPEN_FINISH'
        : 'HOTSPOT_FINISH';

    this.closeIntroDialogueAndMarkSeen(source);
  }

  private maybeAutoOpenIntroDialogue(): void {
    if (this.autoOpenCheckCompleted) {
      return;
    }

    this.autoOpenCheckCompleted = true;

    const introDialogue = this.lesson.introDialogue;

    if (!introDialogue) {
      return;
    }

    if (
      introDialogue.autoOpenOnFirstVisit
    ) {
      this.openIntroDialogue('AUTO_OPEN');
    }
  }

  private openIntroDialogueFromHotspot(): void {
    this.openIntroDialogue('HOTSPOT');
  }

  private openIntroDialogue(openedFrom: 'AUTO_OPEN' | 'HOTSPOT'): void {
    if (!this.lesson.introDialogue) {
      return;
    }

    this.selectedHotspot = undefined;
    this.selectedActivity = undefined;

    this.introDialogueOpenedFrom = openedFrom;
    this.introDialogueOpen = true;

    this.markSeenInProgress = false;
    this.markSeenCompletedForCurrentOpening = false;
  }

  private closeIntroDialogueAndMarkSeen(source: IntroDialogueSeenSource): void {
    this.introDialogueOpen = false;

    if (!this.lesson?.locationId) {
      this.resetCurrentIntroOpening();
      return;
    }

    if (this.markSeenInProgress || this.markSeenCompletedForCurrentOpening) {
      this.resetCurrentIntroOpening();
      return;
    }

    this.markSeenInProgress = true;

    this.locationApi.markIntroDialogueSeen(this.lesson.locationId, source).subscribe({
      next: () => {
        if (this.lesson.introDialogue) {
          this.lesson.introDialogue.alreadySeen = true;
        }

        this.markSeenCompletedForCurrentOpening = true;
        this.markSeenInProgress = false;
        this.resetCurrentIntroOpening();
      },
      error: () => {
        this.markSeenInProgress = false;
        this.resetCurrentIntroOpening();
      }
    });
  }

  private resetIntroDialogueOpeningState(): void {
    this.introDialogueOpen = false;
    this.introDialogueOpenedFrom = null;
    this.autoOpenCheckCompleted = false;
    this.markSeenInProgress = false;
    this.markSeenCompletedForCurrentOpening = false;
  }

  private resetCurrentIntroOpening(): void {
    this.introDialogueOpenedFrom = null;
    this.markSeenInProgress = false;
  }

  private isIntroDialogueHotspot(hotspot: Hotspot): boolean {
    return (
      hotspot.hotspotType === 'INTRO_DIALOGUE' ||
      hotspot.style === 'INTRO_DIALOGUE' ||
      hotspot.dialogueId === this.lesson.introDialogue?.id
    );
  }

  private isVocabularyTooltipHotspot(hotspot: Hotspot): boolean {
    return (
      hotspot.hotspotType === 'VOCAB_TOOLTIP' ||
      hotspot.style === 'VOCAB_TOOLTIP' ||
      hotspot.raw?.['hotspotType'] === 'VOCAB_TOOLTIP' ||
      hotspot.raw?.['style'] === 'VOCAB_TOOLTIP'
    );
  }
}
