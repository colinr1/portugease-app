import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { LearnerUserService } from '../../../core/services/learner-user.service';

@Component({
  selector: 'app-user-selection-modal',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './user-selection-modal.component.html',
  styleUrl: './user-selection-modal.component.scss'
})
export class UserSelectionModalComponent {
  username = '';
  loading = false;
  errorMessage = '';

  constructor(private readonly learnerUserService: LearnerUserService) {}

  submit(): void {
    const trimmedUsername = this.username.trim();

    if (!trimmedUsername) {
      this.errorMessage = 'Please enter your username.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.learnerUserService.lookupUsername(trimmedUsername).subscribe({
      next: () => {
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Username not found. Please check the username you were given. Usernames are case sensitive';
        this.loading = false;
      }
    });
  }
}
