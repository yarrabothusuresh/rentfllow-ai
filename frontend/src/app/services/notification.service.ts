import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import {
  NotificationDTO,
  NotificationTemplateDTO,
  NotificationPreferenceDTO,
  NotificationUnreadCountDTO,
  AIDraftDTO,
  NotificationType,
  NotificationChannel,
  NotificationStatus
} from '../models/notification.models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private staffUnreadSubject = new BehaviorSubject<number>(0);
  public staffUnread$ = this.staffUnreadSubject.asObservable();

  private portalUnreadSubject = new BehaviorSubject<number>(0);
  public portalUnread$ = this.portalUnreadSubject.asObservable();

  constructor(private http: HttpClient) {}

  // --- STAFF NOTIFICATIONS ---

  getStaffNotifications(
    unreadOnly = false,
    type?: NotificationType,
    channel?: NotificationChannel,
    status?: NotificationStatus,
    page = 0,
    size = 20
  ): Observable<{ content: NotificationDTO[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams()
      .set('unreadOnly', unreadOnly.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    if (type) params = params.set('type', type);
    if (channel) params = params.set('channel', channel);
    if (status) params = params.set('status', status);

    return this.http.get<{ content: NotificationDTO[]; totalElements: number; totalPages: number }>(
      '/api/notifications',
      { params }
    );
  }

  fetchStaffUnreadCount(): Observable<NotificationUnreadCountDTO> {
    return this.http.get<NotificationUnreadCountDTO>('/api/notifications/unread-count').pipe(
      tap(res => this.staffUnreadSubject.next(res.unreadCount))
    );
  }

  markStaffNotificationAsRead(id: string): Observable<NotificationDTO> {
    return this.http.patch<NotificationDTO>(`/api/notifications/${id}/read`, {}).pipe(
      tap(() => this.fetchStaffUnreadCount().subscribe())
    );
  }

  markAllStaffNotificationsAsRead(): Observable<void> {
    return this.http.patch<void>('/api/notifications/read-all', {}).pipe(
      tap(() => this.staffUnreadSubject.next(0))
    );
  }

  retryNotification(id: string): Observable<NotificationDTO> {
    return this.http.post<NotificationDTO>(`/api/notifications/${id}/retry`, {});
  }

  // --- STAFF PREFERENCES ---

  getStaffPreferences(): Observable<NotificationPreferenceDTO[]> {
    return this.http.get<NotificationPreferenceDTO[]>('/api/notification-preferences');
  }

  updateStaffPreferences(dtos: NotificationPreferenceDTO[]): Observable<NotificationPreferenceDTO[]> {
    return this.http.put<NotificationPreferenceDTO[]>('/api/notification-preferences', dtos);
  }

  // --- TEMPLATES ---

  getTemplates(): Observable<NotificationTemplateDTO[]> {
    return this.http.get<NotificationTemplateDTO[]>('/api/notification-templates');
  }

  getTemplateById(id: string): Observable<NotificationTemplateDTO> {
    return this.http.get<NotificationTemplateDTO>(`/api/notification-templates/${id}`);
  }

  createTemplate(template: NotificationTemplateDTO): Observable<NotificationTemplateDTO> {
    return this.http.post<NotificationTemplateDTO>('/api/notification-templates', template);
  }

  updateTemplate(id: string, template: NotificationTemplateDTO): Observable<NotificationTemplateDTO> {
    return this.http.put<NotificationTemplateDTO>(`/api/notification-templates/${id}`, template);
  }

  previewTemplate(id: string, variables?: Record<string, any>): Observable<{ subject: string; body: string }> {
    return this.http.post<{ subject: string; body: string }>(`/api/notification-templates/${id}/preview`, variables || {});
  }

  // --- AI DRAFTING ---

  generateAIDraft(type: NotificationType, channel: NotificationChannel, context?: Record<string, any>): Observable<AIDraftDTO> {
    return this.http.post<AIDraftDTO>('/api/notifications/ai-draft', {
      notificationType: type,
      channel,
      ...context
    });
  }

  // --- CUSTOMER PORTAL NOTIFICATIONS ---

  getCustomerNotifications(
    unreadOnly = false,
    page = 0,
    size = 20
  ): Observable<{ content: NotificationDTO[]; totalElements: number; totalPages: number }> {
    const params = new HttpParams()
      .set('unreadOnly', unreadOnly.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<{ content: NotificationDTO[]; totalElements: number; totalPages: number }>(
      '/api/portal/notifications',
      { params }
    );
  }

  fetchPortalUnreadCount(): Observable<NotificationUnreadCountDTO> {
    return this.http.get<NotificationUnreadCountDTO>('/api/portal/notifications/unread-count').pipe(
      tap(res => this.portalUnreadSubject.next(res.unreadCount))
    );
  }

  markCustomerNotificationAsRead(id: string): Observable<NotificationDTO> {
    return this.http.patch<NotificationDTO>(`/api/portal/notifications/${id}/read`, {}).pipe(
      tap(() => this.fetchPortalUnreadCount().subscribe())
    );
  }

  markAllCustomerNotificationsAsRead(): Observable<void> {
    return this.http.patch<void>('/api/portal/notifications/read-all', {}).pipe(
      tap(() => this.portalUnreadSubject.next(0))
    );
  }

  getCustomerPreferences(): Observable<NotificationPreferenceDTO[]> {
    return this.http.get<NotificationPreferenceDTO[]>('/api/portal/notification-preferences');
  }

  updateCustomerPreferences(dtos: NotificationPreferenceDTO[]): Observable<NotificationPreferenceDTO[]> {
    return this.http.put<NotificationPreferenceDTO[]>('/api/portal/notification-preferences', dtos);
  }
}
