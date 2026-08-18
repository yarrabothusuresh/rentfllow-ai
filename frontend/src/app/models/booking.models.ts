export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'DEPOSIT_PENDING'
  | 'PARTIALLY_PAID'
  | 'PAID'
  | 'READY_FOR_FULFILLMENT'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export interface BookingItem {
  id?: string;
  bookingId?: string;
  productId?: string;
  productName?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  rentalStartDateTime?: string;
  rentalEndDateTime?: string;
  lineSubtotal: number;
  reservationStatus?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryReservation {
  id?: string;
  tenantId?: string;
  productId?: string;
  productName?: string;
  eventId?: string;
  eventName?: string;
  bookingId?: string;
  quantity: number;
  startDateTime?: string;
  endDateTime?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Booking {
  id?: string;
  tenantId?: string;
  bookingNumber?: string;
  quoteId?: string;
  quoteNumber?: string;
  customerId?: string;
  customerName?: string;
  eventId?: string;
  eventName?: string;
  status: BookingStatus;
  bookingDate?: string;
  rentalStartDateTime?: string;
  rentalEndDateTime?: string;
  subtotal: number;
  discountAmount?: number;
  deliveryFee?: number;
  pickupFee?: number;
  setupFee?: number;
  breakdownFee?: number;
  serviceFee?: number;
  taxAmount?: number;
  totalAmount: number;
  depositRequired: number;
  depositPaid: number;
  balanceDue: number;
  notes?: string;
  internalNotes?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
  items?: BookingItem[];
  reservations?: InventoryReservation[];
}

export interface ShortageItem {
  productId?: string;
  productName?: string;
  requestedQuantity: number;
  availableQuantity: number;
  shortage: number;
  startDateTime?: string;
  endDateTime?: string;
}

export interface BookingUnavailableError {
  error: string;
  message: string;
  items: ShortageItem[];
}
