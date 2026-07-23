import { NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ActivityContent } from '../../../core/models/activity.model';

@Component({
  selector: 'app-beach-task-tray',
  standalone: true,
  imports: [NgClass],
  templateUrl: './beach-task-tray.component.html',
  styleUrl: './beach-task-tray.component.scss'
})
export class BeachTaskTrayComponent {
  @Input() activities: ActivityContent[] = [];
  @Input() locationName = 'Location';
  @Input() open = false;
  @Input() completedActivityIds: ReadonlySet<string> = new Set<string>();
  @Output() activitySelected = new EventEmitter<ActivityContent>();
  @Output() openChange = new EventEmitter<boolean>();

  get sortedActivities(): ActivityContent[] {
    return [...this.activities].sort((a, b) => {
      const aOrder = a.displayOrder ?? 0;
      const bOrder = b.displayOrder ?? 0;
      return aOrder - bOrder;
    });
  }

  get taskCount(): number {
    return this.sortedActivities.length;
  }

  get trayTitle(): string {
    return `${this.locationName} Tasks`;
  }

  isCompleted(activity: ActivityContent): boolean {
    return this.completedActivityIds.has(activity.id);
  }

  taskAriaLabel(activity: ActivityContent): string {
    const completionLabel = this.isCompleted(activity) ? ', completed' : '';
    return `${this.activityTypeLabel(activity)} task${completionLabel}`;
  }

  toggleOpen(): void {
    this.open = !this.open;
    this.openChange.emit(this.open);
  }

  closeTray(): void {
    if (!this.open) {
      return;
    }

    this.open = false;
    this.openChange.emit(false);
  }

  selectActivity(activity: ActivityContent): void {
    this.activitySelected.emit(activity);
    this.closeTray();
  }

  activityTypeLabel(activity: ActivityContent): string {
    switch (activity.activityType) {
      case 'MULTIPLE_CHOICE':
        return 'Multiple choice';
      case 'WORD_MATCHING':
        return 'Word matching';
      case 'SENTENCE_BUILDING':
        return 'Sentence building';
      case 'LISTENING':
        return 'Listening';
      case 'SENTENCE_TRANSFORMATION':
        return 'Transformation';
      case 'SCENARIO_CHALLENGE':
        return 'Challenge';
      default:
        return String(activity.activityType || 'Activity')
          .replace(/_/g, ' ')
          .toLowerCase();
    }
  }

  activityTypeClass(activity: ActivityContent): string {
    switch (activity.activityType) {
      case 'MULTIPLE_CHOICE':
        return 'type-multiple-choice';
      case 'WORD_MATCHING':
        return 'type-word-matching';
      case 'SENTENCE_BUILDING':
        return 'type-sentence-building';
      case 'LISTENING':
        return 'type-listening';
      case 'SENTENCE_TRANSFORMATION':
        return 'type-transformation';
      case 'SCENARIO_CHALLENGE':
        return 'type-challenge';
      default:
        return 'type-default';
    }
  }
}
