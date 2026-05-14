import {Component, EventEmitter, Input, Output} from '@angular/core';
import {
  ActivityContent,
} from '../../../core/models/activity.model';

@Component({
  selector: 'app-activity-renderer',
  standalone: true,
  imports: [],
  templateUrl: './activity-renderer.component.html',
  styleUrl: './activity-renderer.component.scss'
})
export class ActivityRendererComponent {
  @Input({required: true}) activity!: ActivityContent;

  @Output() finished = new EventEmitter<void>();
}
