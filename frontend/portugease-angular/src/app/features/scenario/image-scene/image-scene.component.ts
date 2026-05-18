import { Component, EventEmitter, Input, Output } from '@angular/core';
import {NgFor, NgIf} from '@angular/common';
import { Hotspot } from '../../../core/models/hotspot.model';
import { HotspotOverlayComponent } from '../hotspot-overlay/hotspot-overlay.component';
import {IntroDialogueFocusMarker} from "../../../core/models/lesson.model";

@Component({
  selector: 'app-image-scene',
  standalone: true,
  imports: [NgIf, NgFor, HotspotOverlayComponent],
  templateUrl: './image-scene.component.html',
  styleUrl: './image-scene.component.scss'
})
export class ImageSceneComponent {
  @Input({ required: true }) imagePath = '';
  @Input() altText = 'Interactive scene';
  @Input({ required: true }) hotspots: Hotspot[] = [];
  @Input() introFocusMarkers: IntroDialogueFocusMarker[] = [];

  @Output() hotspotSelected = new EventEmitter<Hotspot>();

  imageLoaded = false;
  imageFailed = false;

  onImageLoad(): void {
    this.imageLoaded = true;
    this.imageFailed = false;
  }

  onImageError(): void {
    this.imageLoaded = false;
    this.imageFailed = true;
  }

  onHotspotSelected(hotspot: Hotspot): void {
    this.hotspotSelected.emit(hotspot);
  }
}
