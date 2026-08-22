import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { NotificationPreferenceDTO } from '../../../models/notification.models';

@Component({
  selector: 'app-notification-preferences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notification-preferences.component.html',
  styleUrls: ['./notification-preferences.component.scss']
})
export class NotificationPreferencesComponent implements OnInit {
  preferences: NotificationPreferenceDTO[] = [];
  loading = false;
  saving = false;
  savedSuccess = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadPreferences();
  }

  loadPreferences(): void {
    this.loading = true;
    this.notificationService.getStaffPreferences().subscribe({
      next: (res: NotificationPreferenceDTO[]) => {
        this.preferences = res;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  savePreferences(): void {
    this.saving = true;
    this.notificationService.updateStaffPreferences(this.preferences).subscribe({
      next: (res: NotificationPreferenceDTO[]) => {
        this.preferences = res;
        this.saving = false;
        this.savedSuccess = true;
        setTimeout(() => this.savedSuccess = false, 3000);
      },
      error: () => this.saving = false
    });
  }
}
