import { Component, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AppHeaderComponent } from './layout/app-header/app-header.component';
import { LearnerUserService } from './core/services/learner-user.service';
import { UserSelectionModalComponent } from './shared/components/user-selection-modal/user-selection-modal.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    AsyncPipe,
    RouterOutlet,
    AppHeaderComponent,
    UserSelectionModalComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly learnerUserService = inject(LearnerUserService);

  readonly selectedUser$ = this.learnerUserService.selectedUser$;
}
