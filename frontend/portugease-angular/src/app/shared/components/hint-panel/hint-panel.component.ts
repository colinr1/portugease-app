import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-hint-panel',
  standalone: true,
  imports: [NgIf],
  templateUrl: './hint-panel.component.html',
  styleUrl: './hint-panel.component.scss'
})
export class HintPanelComponent {
  @Input() hint?: string | null;
  @Input() hintsUsed = 0;

  @Output() hintUsed = new EventEmitter<void>();

  showHint = false;

  revealHint(): void {
    this.showHint = true;
    this.hintUsed.emit();
  }
}
