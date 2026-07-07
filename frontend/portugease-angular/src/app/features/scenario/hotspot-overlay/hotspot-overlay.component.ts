import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Hotspot } from '../../../core/models/hotspot.model';
import { HotspotMarkerComponent } from '../hotspot-marker/hotspot-marker.component';

@Component({
  selector: 'app-hotspot-overlay',
  standalone: true,
  imports: [HotspotMarkerComponent],
  templateUrl: './hotspot-overlay.component.html',
  styleUrl: './hotspot-overlay.component.scss'
})
export class HotspotOverlayComponent {
  @Input({ required: true }) hotspots: Hotspot[] = [];

  @Output() hotspotSelected = new EventEmitter<Hotspot>();

  get visibleHotspots(): Hotspot[] {
    return this.hotspots.filter(hotspot => hotspot.visible !== false);
  }

  trackByHotspotId(_index: number, hotspot: Hotspot): string {
    return hotspot.id;
  }
}
