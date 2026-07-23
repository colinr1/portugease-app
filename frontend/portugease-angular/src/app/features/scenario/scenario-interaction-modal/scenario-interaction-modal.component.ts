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
import { ActivityContent } from '../../../core/models/activity.model';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';
import { Hotspot } from '../../../core/models/hotspot.model';
import { ActivityRendererComponent } from '../../activities/activity-renderer/activity-renderer.component';

@Component({
  selector: 'app-scenario-interaction-modal',
  standalone: true,
  imports: [
    ActivityRendererComponent
  ],
  templateUrl: './scenario-interaction-modal.component.html',
  styleUrl: './scenario-interaction-modal.component.scss'
})
export class ScenarioInteractionModalComponent implements AfterViewInit {
  @Input() activity?: ActivityContent;
  @Input({ required: true }) hotspot!: Hotspot;
  @Input() locationName = 'Scenario';
  @Input() cityName = 'City';

  @Output() closed = new EventEmitter<void>();
  @Output() finished = new EventEmitter<ActivityAttemptResponse>();
  @Output() returned = new EventEmitter<void>();
  @Output() attemptResolved = new EventEmitter<ActivityAttemptResponse>();
  @Output() contentLocked = new EventEmitter<void>();

  @ViewChild('closeButton') closeButton?: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    queueMicrotask(() => this.closeButton?.nativeElement.focus());
  }

  close(): void {
    this.closed.emit();
  }

  finish(response: ActivityAttemptResponse): void {
    this.finished.emit(response);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }
}
