export type QuoteStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'SENT'
  | 'VIEWED'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'CANCELLED';

export type PricingStrategy =
  | 'PER_EVENT'
  | 'PER_DAY'
  | 'PER_WEEK'
  | 'FLAT_RATE';

export type DiscountType = 'PERCENTAGE' | 'FIXED';

export type FeeType =
  | 'DELIVERY'
  | 'PICKUP'
  | 'SETUP'
  | 'BREAKDOWN'
  | 'SERVICE'
  | 'OTHER';

export interface QuoteItem {
  id?: string;
  quoteId?: string;
  productId?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  standardUnitPrice?: number;
  pricingStrategy: PricingStrategy;
  rentalDays?: number;
  lineSubtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  lineTotal?: number;
  notes?: string;

  // UI availability check fields
  availableQuantity?: number;
  isAvailable?: boolean;
  shortageQuantity?: number;
  priceOverrideDifference?: number;
}

export interface QuoteFee {
  id?: string;
  quoteId?: string;
  feeType: FeeType;
  description: string;
  amount: number;
}

export interface QuoteDiscount {
  id?: string;
  quoteId?: string;
  type: DiscountType;
  value: number;
  amount?: number;
  reason?: string;
  approvedBy?: string;
}

export interface Quote {
  id?: string;
  tenantId?: string;
  quoteNumber?: string;
  customerId: string;
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  eventId: string;
  eventName?: string;
  eventDate?: string;
  status: QuoteStatus;
  quoteDate?: string;
  validUntil?: string;
  rentalStartDateTime?: string;
  rentalEndDateTime?: string;
  subtotal?: number;
  discountAmount?: number;
  deliveryFee?: number;
  pickupFee?: number;
  setupFee?: number;
  breakdownFee?: number;
  serviceFee?: number;
  taxRate?: number;
  taxAmount?: number;
  totalAmount?: number;
  totalFees?: number;
  depositPercentage?: number;
  depositAmount?: number;
  remainingBalance?: number;
  notes?: string;
  internalNotes?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;

  items: QuoteItem[];
  fees?: QuoteFee[];
  discounts?: QuoteDiscount[];

  hasAvailabilityShortage?: boolean;
  shortageWarnings?: string[];
}

export interface QuoteCalculationRequest {
  items: QuoteItem[];
  discountType?: DiscountType;
  discountValue?: number;
  deliveryFee?: number;
  pickupFee?: number;
  setupFee?: number;
  breakdownFee?: number;
  serviceFee?: number;
  taxRate?: number;
  depositPercentage?: number;
  discountAmount?: number;
}

export interface QuoteCalculationResponse {
  grossSubtotal: number;
  discountAmount: number;
  subtotal: number;
  totalFees: number;
  taxableAmount: number;
  taxAmount: number;
  totalAmount: number;
  depositAmount: number;
  remainingBalance: number;
  itemSubtotals: number[];
}
