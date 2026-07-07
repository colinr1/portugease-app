import { Component, Input } from '@angular/core';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';

@Component({
  selector: 'app-feedback-panel',
  standalone: true,
  imports: [],
  templateUrl: './feedback-panel.component.html',
  styleUrl: './feedback-panel.component.scss'
})
export class FeedbackPanelComponent {
  @Input() feedback?: ActivityAttemptResponse;
}
