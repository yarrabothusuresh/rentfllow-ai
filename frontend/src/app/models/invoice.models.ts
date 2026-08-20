import { Payment } from './payment.models';

export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'VOID';

export interface InvoiceItem {
  id?: string;
  invoiceId?: string;
  productId?: string;
  productName?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  discount?: number;
  tax?: number;
  lineTotal: number;
  createdAt?: string;
}

export interface Invoice {
  id: string;
  tenantId: string;
  bookingId: string;
  bookingNumber?: string;
  customerId: string;
  customerName?: string;
  companyName?: string;
  email?: string;
  phone?: string;
  billingAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  eventName?: string;

  invoiceNumber: string;
  issueDate: string;
  dueDate: string;

  subtotal: number;
  discount: number;
  fees: number;
  tax: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;

  status: InvoiceStatus;
  notes?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;

  items?: InvoiceItem[];
  payments?: Payment[];
}

export interface CreateInvoiceRequest {
  notes?: string;
  dueDate?: string;
}
