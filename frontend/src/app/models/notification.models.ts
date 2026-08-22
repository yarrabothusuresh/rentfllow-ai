export type NotificationType =
  | 'QUOTE_SENT'
  | 'QUOTE_ACCEPTED'
  | 'QUOTE_CHANGE_REQUESTED'
  | 'BOOKING_CONFIRMED'
  | 'BOOKING_CANCELLED'
  | 'PAYMENT_RECEIVED'
  | 'INVOICE_CREATED'
  | 'INVOICE_SENT'
  | 'INVOICE_OVERDUE'
  | 'CUSTOMER_REQUEST_CREATED'
  | 'SYSTEM';

export type NotificationChannel = 'IN_APP' | 'EMAIL' | 'SMS';

export type NotificationStatus = 'PENDING' | 'PROCESSING' | 'SENT' | 'DELIVERED' | 'FAILED' | 'READ';

export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export interface NotificationDTO {
  id: string;
  tenantId: string;
  recipientUserId?: string;
  recipientCustomerId?: string;
  recipientName?: string;
  type: NotificationType;
  channel: NotificationChannel;
  title: string;
  message: string;
  referenceType?: string;
  referenceId?: string;
  status: NotificationStatus;
  priority: NotificationPriority;
  readAt?: string;
  sentAt?: string;
  failedAt?: string;
  retryCount: number;
  failureReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface NotificationTemplateDTO {
  id?: string;
  tenantId?: string;
  templateKey: string;
  type: NotificationType;
  channel: NotificationChannel;
  subject: string;
  body: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface NotificationPreferenceDTO {
  id?: string;
  tenantId?: string;
  customerId?: string;
  userId?: string;
  notificationType: NotificationType;
  emailEnabled: boolean;
  smsEnabled: boolean;
  inAppEnabled: boolean;
}

export interface NotificationUnreadCountDTO {
  unreadCount: number;
}

export interface AIDraftDTO {
  subject: string;
  body: string;
  suggestedChannel: NotificationChannel;
}
