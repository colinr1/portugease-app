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
import { ActivityContent } from '../../../core/models/activity.model';
import { Hotspot } from '../../../core/models/hotspot.model';
import { ActivityRendererComponent } from '../../activities/activity-renderer/activity-renderer.component';

@Component({
  selector: 'app-scenario-interaction-modal',
  standalone: true,
  imports: [
    NgIf,
    ActivityRendererComponent
  ],
  templateUrl: './scenario-interaction-modal.component.html',
  styleUrl: './scenario-interaction-modal.component.scss'
})
export class ScenarioInteractionModalComponent implements AfterViewInit {
  @Input() activity?: ActivityContent;
  @Input({ required: true }) hotspot!: Hotspot;
  @Input() locationName = 'Scenario';

  @Output() closed = new EventEmitter<void>();

  @ViewChild('closeButton') closeButton?: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    queueMicrotask(() => this.closeButton?.nativeElement.focus());
  }

  close(): void {
    this.closed.emit();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }
}
