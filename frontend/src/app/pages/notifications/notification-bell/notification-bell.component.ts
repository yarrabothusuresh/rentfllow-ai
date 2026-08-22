import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { NotificationDTO } from '../../../models/notification.models';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.scss']
})
export class NotificationBellComponent implements OnInit {
  @Input() isPortal = false;

  unreadCount = 0;
  dropdownOpen = false;
  recentNotifications: NotificationDTO[] = [];
  loading = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    if (this.isPortal) {
      this.notificationService.portalUnread$.subscribe((count: number) => this.unreadCount = count);
      this.notificationService.fetchPortalUnreadCount().subscribe();
    } else {
      this.notificationService.staffUnread$.subscribe((count: number) => this.unreadCount = count);
      this.notificationService.fetchStaffUnreadCount().subscribe();
    }
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
    if (this.dropdownOpen) {
      this.loadRecent();
    }
  }

  loadRecent(): void {
    this.loading = true;
    if (this.isPortal) {
      this.notificationService.getCustomerNotifications(false, 0, 5).subscribe({
        next: (res: any) => {
          this.recentNotifications = res.content;
          this.loading = false;
        },
        error: () => this.loading = false
      });
    } else {
      this.notificationService.getStaffNotifications(false, undefined, undefined, undefined, 0, 5).subscribe({
        next: (res: any) => {
          this.recentNotifications = res.content;
          this.loading = false;
        },
        error: () => this.loading = false
      });
    }
  }

  markAsRead(n: NotificationDTO, event: MouseEvent): void {
    event.stopPropagation();
    if (this.isPortal) {
      this.notificationService.markCustomerNotificationAsRead(n.id).subscribe(() => {
        n.readAt = new Date().toISOString();
        n.status = 'READ';
      });
    } else {
      this.notificationService.markStaffNotificationAsRead(n.id).subscribe(() => {
        n.readAt = new Date().toISOString();
        n.status = 'READ';
      });
    }
  }

  markAllRead(event: MouseEvent): void {
    event.stopPropagation();
    if (this.isPortal) {
      this.notificationService.markAllCustomerNotificationsAsRead().subscribe(() => {
        this.recentNotifications.forEach(n => {
          n.readAt = new Date().toISOString();
          n.status = 'READ';
        });
      });
    } else {
      this.notificationService.markAllStaffNotificationsAsRead().subscribe(() => {
        this.recentNotifications.forEach(n => {
          n.readAt = new Date().toISOString();
          n.status = 'READ';
        });
      });
    }
  }

  getChannelIcon(channel: string): string {
    switch (channel) {
      case 'EMAIL': return 'ph-envelope';
      case 'SMS': return 'ph-chat-circle-text';
      default: return 'ph-bell-simple';
    }
  }
}
