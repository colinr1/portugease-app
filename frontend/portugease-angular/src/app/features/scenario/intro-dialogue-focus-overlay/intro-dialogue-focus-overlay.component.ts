import { NgFor, NgIf } from '@angular/common';
import { Component, Input } from '@angular/core';
import { IntroDialogueFocusMarker } from '../../../core/models/lesson.model';

@Component({
  selector: 'app-intro-dialogue-focus-overlay',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './intro-dialogue-focus-overlay.component.html',
  styleUrl: './intro-dialogue-focus-overlay.component.scss'
})
export class IntroDialogueFocusOverlayComponent {
  @Input() markers: IntroDialogueFocusMarker[] = [];

  markerLabel(marker: IntroDialogueFocusMarker): string {
    return marker.ariaLabel || 'Dialogue focus marker';
  }
}
