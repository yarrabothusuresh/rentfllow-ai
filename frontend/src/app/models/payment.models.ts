export type PaymentMethod =
  | 'CASH'
  | 'BANK_TRANSFER'
  | 'CREDIT_CARD'
  | 'DEBIT_CARD'
  | 'CHECK'
  | 'OTHER';

export type PaymentStatus =
  | 'PENDING'
  | 'COMPLETED'
  | 'FAILED'
  | 'REFUNDED'
  | 'VOID';

export interface Payment {
  id: string;
  tenantId: string;
  bookingId: string;
  customerId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  paymentDate: string;
  transactionReference?: string;
  notes?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RecordPaymentRequest {
  amount: number;
  paymentMethod: PaymentMethod;
  paymentDate: string;
  transactionReference?: string;
  notes?: string;
}

export interface BookingFinancialSummary {
  bookingId: string;
  bookingTotal: number;
  depositRequired: number;
  amountPaid: number;
  outstandingBalance: number;
  paymentStatus: string;
}
