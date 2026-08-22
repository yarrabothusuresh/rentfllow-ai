import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDTO, NotificationType, NotificationChannel, NotificationStatus } from '../../../models/notification.models';
import { NotificationTemplatesComponent } from '../notification-templates/notification-templates.component';
import { NotificationPreferencesComponent } from '../notification-preferences/notification-preferences.component';

@Component({
  selector: 'app-notifications-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationTemplatesComponent, NotificationPreferencesComponent],
  templateUrl: './notifications-list.component.html',
  styleUrls: ['./notifications-list.component.scss']
})
export class NotificationsListComponent implements OnInit {
  activeTab: 'all' | 'templates' | 'preferences' = 'all';

  notifications: NotificationDTO[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 15;

  // Filters
  unreadOnly = false;
  selectedType: NotificationType | '' = '';
  selectedChannel: NotificationChannel | '' = '';
  selectedStatus: NotificationStatus | '' = '';

  // Selected Notification for Detail Drawer
  selectedNotification: NotificationDTO | null = null;

  // AI Draft Modal
  aiDraftModalOpen = false;
  aiDraftType: NotificationType = 'INVOICE_OVERDUE';
  aiDraftChannel: NotificationChannel = 'EMAIL';
  aiDraftLoading = false;
  aiDraftResult: { subject: string; body: string } | null = null;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getStaffNotifications(
      this.unreadOnly,
      this.selectedType || undefined,
      this.selectedChannel || undefined,
      this.selectedStatus || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (res: any) => {
        this.notifications = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Failed to load notifications', err);
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadNotifications();
  }

  markAsRead(n: NotificationDTO, event?: MouseEvent): void {
    if (event) event.stopPropagation();
    this.notificationService.markStaffNotificationAsRead(n.id).subscribe(() => {
      n.readAt = new Date().toISOString();
      n.status = 'READ';
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllStaffNotificationsAsRead().subscribe(() => {
      this.notifications.forEach(n => {
        n.readAt = new Date().toISOString();
        n.status = 'READ';
      });
    });
  }

  retry(n: NotificationDTO, event: MouseEvent): void {
    event.stopPropagation();
    this.notificationService.retryNotification(n.id).subscribe({
      next: (updated: NotificationDTO) => {
        n.retryCount = updated.retryCount;
        n.status = updated.status;
        n.failureReason = updated.failureReason;
      },
      error: (err: any) => alert(err.error?.message || 'Failed to retry notification.')
    });
  }

  selectNotification(n: NotificationDTO): void {
    this.selectedNotification = n;
    if (!n.readAt) {
      this.markAsRead(n);
    }
  }

  openAIDraftModal(): void {
    this.aiDraftModalOpen = true;
    this.aiDraftResult = null;
  }

  generateAIDraft(): void {
    this.aiDraftLoading = true;
    this.notificationService.generateAIDraft(this.aiDraftType, this.aiDraftChannel).subscribe({
      next: (draft: any) => {
        this.aiDraftResult = { subject: draft.subject, body: draft.body };
        this.aiDraftLoading = false;
      },
      error: () => this.aiDraftLoading = false
    });
  }

  changePage(newPage: number): void {
    if (newPage >= 0 && newPage < this.totalPages) {
      this.currentPage = newPage;
      this.loadNotifications();
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SENT':
      case 'DELIVERED': return 'status-success';
      case 'READ': return 'status-read';
      case 'FAILED': return 'status-danger';
      case 'PROCESSING': return 'status-warning';
      default: return 'status-neutral';
    }
  }
}
