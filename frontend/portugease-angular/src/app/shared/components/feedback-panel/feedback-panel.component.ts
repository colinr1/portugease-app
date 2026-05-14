import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';
import { ActivityAttemptResponse } from '../../../core/models/attempt.model';

@Component({
  selector: 'app-feedback-panel',
  standalone: true,
  imports: [NgIf],
  templateUrl: './feedback-panel.component.html',
  styleUrl: './feedback-panel.component.scss'
})
export class FeedbackPanelComponent {
  @Input() feedback?: ActivityAttemptResponse;
}
