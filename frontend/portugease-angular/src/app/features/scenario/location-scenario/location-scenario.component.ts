import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { IntroDialogueFocusMarker, IntroDialogueLine, LessonDetail } from '../../../core/models/lesson.model';
import { ActivityContent } from '../../../core/models/activity.model';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';
import { Hotspot } from '../../../core/models/hotspot.model';
import {
  IntroDialogueSeenSource,
  LocationApiService
} from '../../../core/services/location-api.service';
import { ImageSceneComponent } from '../image-scene/image-scene.component';
import { ScenarioInteractionModalComponent } from '../scenario-interaction-modal/scenario-interaction-modal.component';
import { IntroDialogueModalComponent } from '../intro-dialogue-modal/intro-dialogue-modal.component';
import { BeachTaskTrayComponent } from '../beach-task-tray/beach-task-tray.component';
import {
  isActivityHotspot,
  isIntroDialogueHotspot,
  isVocabularyTooltipHotspot
} from '../../../core/utils/hotspot.util';
import { isLockedContentError } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-location-scenario',
  standalone: true,
  imports: [
    ImageSceneComponent,
    ScenarioInteractionModalComponent,
    IntroDialogueModalComponent,
    BeachTaskTrayComponent
  ],
  templateUrl: './location-scenario.component.html',
  styleUrl: './location-scenario.component.scss'
})
export class LocationScenarioComponent implements OnChanges {
  @Input({ required: true }) lesson!: LessonDetail;
  @Input() cityName = 'City';

  @Output() activityFinished = new EventEmitter<ActivityAttemptResponse>();
  @Output() contentLocked = new EventEmitter<void>();

  selectedActivity?: ActivityContent;
  selectedHotspot?: Hotspot;

  introDialogueOpen = false;
  introDialogueOpenedFrom: 'AUTO_OPEN' | 'HOTSPOT' | null = null;
  introFocusMarkers: IntroDialogueFocusMarker[] = [];
  taskTrayOpen = false;
  completedActivityIds = new Set<string>();

  get modalOpen(): boolean {
    return this.introDialogueOpen || Boolean(this.selectedActivity && this.selectedHotspot);
  }

  get sceneHotspots(): Hotspot[] {
    return this.lesson.hotspots.filter(hotspot => !isActivityHotspot(hotspot));
  }

  get visibleSceneHotspots(): Hotspot[] {
    return this.modalOpen || this.taskTrayOpen ? [] : this.sceneHotspots;
  }

  onTaskTrayOpenChange(open: boolean): void {
    this.taskTrayOpen = open;
  }

  private autoOpenCheckCompleted = false;
  private markSeenInProgress = false;
  private markSeenCompletedForCurrentOpening = false;

  constructor(private readonly locationApi: LocationApiService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lesson'] && this.lesson) {
      this.completedActivityIds = new Set<string>();
      this.resetIntroDialogueOpeningState();
      this.maybeAutoOpenIntroDialogue();
    }
  }

  onHotspotSelected(hotspot: Hotspot): void {
    if (isVocabularyTooltipHotspot(hotspot)) {
      return;
    }

    if (isIntroDialogueHotspot(hotspot, this.lesson.introDialogue?.id)) {
      this.openIntroDialogueFromHotspot();
      return;
    }

    this.selectedHotspot = hotspot;
    this.selectedActivity = this.lesson.activities.find(activity =>
      activity.id === hotspot.activityId ||
      activity.activityKey === hotspot.activityKey
    );
  }

  onIntroDialogueLineChanged(line: IntroDialogueLine | null): void {
    this.introFocusMarkers = line?.focusMarkers ?? [];
  }

  onActivityTaskSelected(activity: ActivityContent): void {
    this.taskTrayOpen = false;

    this.introDialogueOpen = false;
    this.introDialogueOpenedFrom = null;

    this.selectedActivity = activity;
    this.selectedHotspot = this.createActivityTaskHotspot(activity);
  }

  closeNormalModal(): void {
    this.selectedHotspot = undefined;
    this.selectedActivity = undefined;
  }

  returnToTaskTray(): void {
    this.closeNormalModal();
    this.taskTrayOpen = true;
  }

  onAttemptResolved(response: ActivityAttemptResponse): void {
    if (!response.isCorrect || !response.progressUpdate.activityCompleted) {
      return;
    }

    this.completedActivityIds = new Set(this.completedActivityIds).add(response.activityId);
  }

  onActivityFinished(response: ActivityAttemptResponse): void {
    this.closeNormalModal();
    this.activityFinished.emit(response);
  }

  onContentLocked(): void {
    this.closeNormalModal();
    this.contentLocked.emit();
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
    this.introFocusMarkers = [];

    this.markSeenInProgress = false;
    this.markSeenCompletedForCurrentOpening = false;
  }

  private closeIntroDialogueAndMarkSeen(source: IntroDialogueSeenSource): void {
    this.introDialogueOpen = false;
    this.introFocusMarkers = [];

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
      error: (error: unknown) => {
        this.markSeenInProgress = false;
        this.resetCurrentIntroOpening();

        if (isLockedContentError(error)) {
          this.contentLocked.emit();
        }
      }
    });
  }

  private resetIntroDialogueOpeningState(): void {
    this.introDialogueOpen = false;
    this.introDialogueOpenedFrom = null;
    this.introFocusMarkers = [];
    this.autoOpenCheckCompleted = false;
    this.markSeenInProgress = false;
    this.markSeenCompletedForCurrentOpening = false;
  }

  private resetCurrentIntroOpening(): void {
    this.introDialogueOpenedFrom = null;
    this.markSeenInProgress = false;
  }

  private createActivityTaskHotspot(activity: ActivityContent): Hotspot {
    return {
      id: `task-${activity.id}`,
      label: activity.title,
      xPercent: 0,
      yPercent: 0,
      visible: false,
      activityId: activity.id,
      activityKey: activity.activityKey,
      activityType: activity.activityType,
      hotspotType: 'ACTIVITY',
      ariaLabel: `Open ${activity.title} activity`
    };
  }
}
