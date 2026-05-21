import { Component, Input } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ActivityHint } from '../../../core/models/activity.model';

@Component({
  selector: 'app-hint-panel',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './hint-panel.component.html',
  styleUrl: './hint-panel.component.scss'
})
export class HintPanelComponent {
  @Input() hints: ActivityHint[] = [];
  @Input() visibleHintLevel = 0;

  get visibleHints(): ActivityHint[] {
    return this.hints
      .filter(hint => hint.level <= this.visibleHintLevel)
      .sort((a, b) => a.level - b.level);
  }
}
