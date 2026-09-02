export type AiSalesChannel = 'INTERNAL' | 'CUSTOMER_PORTAL' | 'STOREFRONT' | 'WEB_CHAT';
export type AiConversationStatus = 'ACTIVE' | 'WAITING_FOR_CUSTOMER' | 'WAITING_FOR_AGENT' | 'WAITING_FOR_HUMAN' | 'HUMAN_ACTIVE' | 'QUOTE_DRAFTED' | 'COMPLETED' | 'CLOSED' | 'ESCALATED';
export type AiSenderType = 'CUSTOMER' | 'AI' | 'SALES_USER' | 'SYSTEM';
export type AiMessageType = 'TEXT' | 'TOOL_CALL' | 'TOOL_RESULT' | 'SYSTEM_EVENT';
export type AiIntent = 'PRODUCT_SEARCH' | 'AVAILABILITY_CHECK' | 'RENTAL_RECOMMENDATION' | 'PRICE_ESTIMATE' | 'QUOTE_REQUEST' | 'QUOTE_STATUS' | 'BOOKING_QUESTION' | 'GENERAL_RENTAL_QUESTION' | 'HUMAN_ASSISTANCE' | 'UNKNOWN';
export type MarginStatus = 'HEALTHY' | 'ACCEPTABLE' | 'LOW_MARGIN' | 'LOSS_MAKING';
export type AiEscalationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type AiEscalationStatus = 'OPEN' | 'ASSIGNED' | 'RESOLVED' | 'CLOSED';

export interface RentalInquiry {
  id?: string;
  conversationId?: string;
  customerId?: string;
  eventType?: string;
  eventName?: string;
  eventDate?: string;
  rentalStart?: string;
  rentalEnd?: string;
  deliveryAddress?: string;
  deliveryCity?: string;
  deliveryTime?: string;
  deliveryRequired?: boolean;
  guestCount?: number;
  tablePreference?: string;
  chairPreference?: string;
  budget?: number;
  missingFields?: string[];
  complete?: boolean;
  notes?: string;
}

export interface AiSalesMessage {
  id?: string;
  conversationId?: string;
  senderType: AiSenderType;
  messageType?: AiMessageType;
  content: string;
  structuredData?: string;
  createdAt?: string;
}

export interface AiSalesConversation {
  id: string;
  publicId: string;
  customerId?: string;
  customerName?: string;
  customerEmail?: string;
  leadId?: string;
  quoteId?: string;
  quoteNumber?: string;
  channel: AiSalesChannel;
  status: AiConversationStatus;
  detectedIntent?: AiIntent;
  assignedSalesUserId?: string;
  internalNotes?: string;
  startedAt?: string;
  lastMessageAt?: string;
  createdAt?: string;
  inquiry?: RentalInquiry;
  messages?: AiSalesMessage[];
}

export interface ToolCallResult {
  toolName: string;
  success: boolean;
  result?: any;
  errorMessage?: string;
  internalOnly?: boolean;
}

export interface AiChatRequest {
  message: string;
  conversationId?: string;
  customerId?: string;
  customerName?: string;
  customerEmail?: string;
  channel?: string;
}

export interface AiChatResponse {
  conversationId: string;
  publicId: string;
  replyText: string;
  status: AiConversationStatus;
  detectedIntent?: AiIntent;
  inquiry?: RentalInquiry;
  executedTools?: ToolCallResult[];
  suggestedReplies?: string[];
  quoteDraftId?: string;
  quoteDraftNumber?: string;
  escalatedToHuman?: boolean;
  escalationReason?: string;
}

export interface AiEscalation {
  id: string;
  conversationId: string;
  conversationPublicId?: string;
  customerName?: string;
  reason: string;
  priority: AiEscalationPriority;
  status: AiEscalationStatus;
  assignedTo?: string;
  summary: string;
  resolutionNotes?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface AiQuoteReviewItem {
  productId?: string;
  sku?: string;
  name: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  available: boolean;
  availableQuantity: number;
}

export interface AiQuoteReview {
  quoteId: string;
  quoteNumber: string;
  status: string;
  conversationId?: string;
  conversationPublicId?: string;
  customerId?: string;
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  eventType?: string;
  eventName?: string;
  eventDate?: string;
  rentalStart?: string;
  rentalEnd?: string;
  deliveryAddress?: string;
  items: AiQuoteReviewItem[];
  subtotal: number;
  discountAmount: number;
  deliveryFee: number;
  setupFee: number;
  taxAmount: number;
  totalAmount: number;
  estimatedCost: number;
  estimatedProfit: number;
  estimatedMarginPct: number;
  targetMarginPct: number;
  marginStatus: MarginStatus;
  warnings: string[];
  aiRecommendationNotes?: string;
  approved?: boolean;
  approvedBy?: string;
  approvedAt?: string;
}

export interface AiSalesDashboard {
  activeConversations: number;
  newInquiriesToday: number;
  waitingForCustomer: number;
  waitingForHuman: number;
  quoteDraftsPendingReview: number;
  openEscalations: number;
  convertedConversations: number;
  conversionRate: number;
  tokensUsedToday: number;
  recentConversations: AiSalesConversation[];
  urgentEscalations: AiEscalation[];
}

export interface AiSettings {
  aiEnabled: boolean;
  aiProvider: string;
  aiModel: string;
  customerAiEnabled: boolean;
  internalSalesAssistantEnabled: boolean;
  humanQuoteApprovalRequired: boolean;
  dailyRequestLimit: number;
  maxConversationMessages: number;
  targetGrossMarginPct: number;
  lowMarginThresholdPct: number;
  apiKeyMasked?: string;
}
