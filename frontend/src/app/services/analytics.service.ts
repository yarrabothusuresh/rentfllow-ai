import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type DateRangePreset =
  | 'TODAY'
  | 'YESTERDAY'
  | 'THIS_WEEK'
  | 'LAST_WEEK'
  | 'THIS_MONTH'
  | 'LAST_MONTH'
  | 'THIS_QUARTER'
  | 'THIS_YEAR'
  | 'LAST_YEAR'
  | 'CUSTOM';

export interface KpiMetric {
  name: string;
  value: number;
  formattedValue: string;
  unit: string;
  previousValue: number;
  changePercentage: number;
  trend: 'UP' | 'DOWN' | 'FLAT';
  status: 'SUCCESS' | 'WARNING' | 'DANGER' | 'NORMAL';
}

export interface RevenueTrendItem {
  period: string;
  bookedRevenue: number;
  invoicedRevenue: number;
  collectedRevenue: number;
  grossProfit: number;
  bookingsCount: number;
}

export interface CategoryBreakdown {
  categoryName: string;
  revenue: number;
  grossProfit: number;
  marginPercent: number;
  rentedUnits: number;
  utilizationPercent: number;
}

export interface NamedMetric {
  name: string;
  value: number;
  count: number;
  percentage: number;
}

export interface ProductProfitability {
  productId: string;
  sku: string;
  name: string;
  categoryName: string;
  quantityOwned: number;
  rentalRevenue: number;
  rentalCount: number;
  totalQuantityRented: number;
  utilizationPercent: number;
  directCost: number;
  repairCost: number;
  damageCost: number;
  replacementLossCost: number;
  profit: number;
  marginPercent: number;
  marginFlag: 'HIGH_MARGIN' | 'HEALTHY_MARGIN' | 'LOW_MARGIN' | 'NEGATIVE_MARGIN';
}

export interface CostSourceExplanation {
  costCategory: string;
  amount: number;
  source: string;
  calculationType: 'ACTUAL' | 'ESTIMATED' | 'NOT_CONFIGURED';
}

export interface ProductProfitDetail {
  productId: string;
  sku: string;
  name: string;
  categoryName: string;
  trackingType: string;
  quantityOwned: number;
  quantityInMaintenance: number;
  quantityDamaged: number;
  quantityLost: number;
  rentalPrice: number;
  replacementCost: number;
  rentalRevenue: number;
  bookingCount: number;
  totalQuantityRented: number;
  utilizationPercent: number;
  averageRentalPrice: number;
  directProductCost: number;
  repairCost: number;
  damageCost: number;
  replacementLossCost: number;
  totalCost: number;
  profit: number;
  marginPercent: number;
  marginFlag: string;
  monthlyTrend: RevenueTrendItem[];
  costSources: CostSourceExplanation[];
}

export interface BookingProfitability {
  bookingId: string;
  bookingNumber: string;
  customerName: string;
  eventName: string;
  bookingDate: string;
  status: string;
  totalRevenue: number;
  directCost: number;
  grossProfit: number;
  grossMarginPercent: number;
  marginFlag: 'HIGH_MARGIN' | 'HEALTHY_MARGIN' | 'LOW_MARGIN' | 'NEGATIVE_MARGIN';
  warningFlag: boolean;
}

export interface BookingProfitDetail {
  bookingId: string;
  bookingNumber: string;
  customerName: string;
  eventName: string;
  bookingDate: string;
  status: string;
  rentalStart: string;
  rentalEnd: string;
  rentalRevenue: number;
  deliveryRevenue: number;
  setupRevenue: number;
  breakdownRevenue: number;
  serviceRevenue: number;
  discountAmount: number;
  taxAmount: number;
  totalRevenue: number;
  inventoryAllocationCost: number;
  deliveryVehicleCost: number;
  pickupVehicleCost: number;
  laborCost: number;
  repairAndDamageCost: number;
  otherDirectCost: number;
  totalDirectCost: number;
  grossProfit: number;
  grossMarginPercent: number;
  marginFlag: string;
  costSources: CostSourceExplanation[];
}

export interface UnderutilizedProduct {
  productId: string;
  sku: string;
  name: string;
  categoryName: string;
  quantityOwned: number;
  inventoryAssetValue: number;
  utilizationPercent: number;
  rentalRevenue: number;
  lastRentalDate: string;
  suggestedAction: string;
}

export interface ConflictDemandProduct {
  productId: string;
  sku: string;
  name: string;
  categoryName: string;
  conflictIncidentsCount: number;
  unmetQuantityRequested: number;
  potentialRevenueOpportunity: number;
  currentUtilizationPercent: number;
  statusTag: string;
}

export interface InventoryUtilization {
  overallUtilizationPercent: number;
  targetUtilizationPercent: number;
  totalTrackedProducts: number;
  totalFleetUnits: number;
  unitsOnRent: number;
  unitsInMaintenance: number;
  unitsDamaged: number;
  utilizationByCategory: CategoryBreakdown[];
  topUtilizedProducts: ProductProfitability[];
  underutilizedProducts: UnderutilizedProduct[];
  highDemandConflictProducts: ConflictDemandProduct[];
}

export interface CustomerAnalytics {
  customerId: string;
  customerNumber: string;
  customerName: string;
  companyName: string;
  customerType: string;
  totalBookingsCount: number;
  lifetimeRevenue: number;
  lifetimeCollected: number;
  outstandingBalance: number;
  lifetimeProfit: number;
  lifetimeMarginPercent: number;
  averageBookingValue: number;
  quoteConversionRate: number;
  damageClaimsCount: number;
  lastBookingDate: string;
}

export interface CustomerAnalyticsDetail {
  customerId: string;
  customerNumber: string;
  customerName: string;
  companyName: string;
  email: string;
  phone: string;
  customerType: string;
  lifetimeRevenue: number;
  lifetimeCollected: number;
  outstandingBalance: number;
  lifetimeProfit: number;
  lifetimeMarginPercent: number;
  totalBookingsCount: number;
  averageBookingValue: number;
  quoteConversionRate: number;
  totalQuotesCount: number;
  approvedQuotesCount: number;
  damageClaimsCount: number;
  totalDamageCost: number;
  lastBookingDate: string;
  topRentedProducts: NamedMetric[];
  revenueTrend: RevenueTrendItem[];
  recentBookings: BookingProfitability[];
}

export interface SalesFunnelStage {
  stageName: string;
  count: number;
  value: number;
  conversionFromPrevious: number;
  overallConversion: number;
}

export interface QuoteAnalytics {
  totalQuotesCreated: number;
  quotesSent: number;
  quotesApproved: number;
  quotesDeclined: number;
  quotesExpired: number;
  overallConversionRate: number;
  totalQuotedValue: number;
  totalApprovedValue: number;
  averageQuoteValue: number;
  averageApprovalHours: number;
  potentialLostRevenue: number;
  salesFunnel: SalesFunnelStage[];
  quotesByStatus: NamedMetric[];
}

export interface WarehouseAnalytics {
  totalOrdersProcessed: number;
  ordersReadyOnTime: number;
  onTimeReadinessRate: number;
  averagePickDurationMinutes: number;
  averagePackDurationMinutes: number;
  averageLoadDurationMinutes: number;
  averageTotalFulfillmentMinutes: number;
  totalShortPicksCount: number;
  totalDamageFoundInPickCount: number;
  totalExceptionsReported: number;
  resolvedExceptionsCount: number;
  exceptionsByType: NamedMetric[];
  ordersByStatus: NamedMetric[];
}

export interface DeliveryAnalytics {
  totalDeliveries: number;
  onTimeDeliveries: number;
  lateDeliveries: number;
  failedDeliveries: number;
  rescheduledDeliveries: number;
  onTimeDeliveryRate: number;
  averageDeliveryDurationMinutes: number;
  fleetVehicleUtilizationPercent: number;
  deliveriesByStatus: NamedMetric[];
  deliveriesByVehicle: NamedMetric[];
  deliveriesByDriver: NamedMetric[];
}

export interface ReturnAndDamageAnalytics {
  totalReturnsProcessed: number;
  totalReturnedUnits: number;
  damagedUnitsCount: number;
  damageRatePercent: number;
  damageClaimsCount: number;
  estimatedDamageCost: number;
  approvedDamageCost: number;
  actualRepairCost: number;
  replacementCost: number;
  lostInventoryCost: number;
  customerApprovedClaimsCount: number;
  disputedClaimsCount: number;
  waivedClaimsCount: number;
  averageInspectionMinutes: number;
  claimsByStatus: NamedMetric[];
  damageByProductCategory: NamedMetric[];
}

export interface ArAgingBucket {
  bucketName: string;
  amount: number;
  invoiceCount: number;
  percentageOfTotal: number;
}

export interface PaymentAndArAnalytics {
  totalCollectedAmount: number;
  totalOutstandingAmount: number;
  totalOverdueAmount: number;
  collectionRatePercent: number;
  averageDaysToPay: number;
  completedPaymentsCount: number;
  failedPaymentsCount: number;
  arAgingBuckets: ArAgingBucket[];
  paymentsByMethod: NamedMetric[];
}

export interface BusinessInsight {
  insightKey: string;
  title: string;
  summary: string;
  category: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL' | 'OPPORTUNITY';
  financialImpact: number;
  contextData: Record<string, any>;
}

export interface RevenueAnalytics {
  totalBookedRevenue: number;
  totalInvoicedRevenue: number;
  totalCollectedRevenue: number;
  totalOutstandingRevenue: number;
  revenueGrowthPercent: number;
  averageBookingValue: number;
  currency: string;
  revenueTrend: RevenueTrendItem[];
  revenueByEventType: NamedMetric[];
  revenueByBookingStatus: NamedMetric[];
}

export interface ExecutiveDashboard {
  periodName: string;
  startDate: string;
  endDate: string;
  currency: string;
  lastUpdated: string;
  dataFreshness: string;
  bookedRevenue: KpiMetric;
  collectedRevenue: KpiMetric;
  outstandingRevenue: KpiMetric;
  grossProfit: KpiMetric;
  grossMargin: KpiMetric;
  bookingsCount: KpiMetric;
  averageBookingValue: KpiMetric;
  inventoryUtilization: KpiMetric;
  quoteConversionRate: KpiMetric;
  openDamageExposure: KpiMetric;
  revenueTrend: RevenueTrendItem[];
  revenueByCategory: CategoryBreakdown[];
  topProfitableProducts: ProductProfitability[];
  topCustomers: CustomerAnalytics[];
  dataQualityWarnings: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private baseUrl = '/api/analytics';

  constructor(private http: HttpClient) {}

  private createParams(range?: DateRangePreset, startDate?: string, endDate?: string): HttpParams {
    let params = new HttpParams();
    if (range) params = params.set('range', range);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return params;
  }

  getDashboard(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<ExecutiveDashboard> {
    return this.http.get<ExecutiveDashboard>(`${this.baseUrl}/dashboard`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getRevenueAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<RevenueAnalytics> {
    return this.http.get<RevenueAnalytics>(`${this.baseUrl}/revenue`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getBookingProfitability(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<BookingProfitability[]> {
    return this.http.get<BookingProfitability[]>(`${this.baseUrl}/bookings`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getBookingProfitDetail(bookingId: string): Observable<BookingProfitDetail> {
    return this.http.get<BookingProfitDetail>(`${this.baseUrl}/bookings/${bookingId}`);
  }

  getProductProfitability(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<ProductProfitability[]> {
    return this.http.get<ProductProfitability[]>(`${this.baseUrl}/products`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getProductProfitDetail(productId: string): Observable<ProductProfitDetail> {
    return this.http.get<ProductProfitDetail>(`${this.baseUrl}/products/${productId}`);
  }

  getInventoryUtilization(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<InventoryUtilization> {
    return this.http.get<InventoryUtilization>(`${this.baseUrl}/utilization`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getCustomerAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<CustomerAnalytics[]> {
    return this.http.get<CustomerAnalytics[]>(`${this.baseUrl}/customers`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getCustomerAnalyticsDetail(customerId: string): Observable<CustomerAnalyticsDetail> {
    return this.http.get<CustomerAnalyticsDetail>(`${this.baseUrl}/customers/${customerId}`);
  }

  getQuoteAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<QuoteAnalytics> {
    return this.http.get<QuoteAnalytics>(`${this.baseUrl}/quotes`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getWarehouseAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<WarehouseAnalytics> {
    return this.http.get<WarehouseAnalytics>(`${this.baseUrl}/warehouse`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getDeliveryAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<DeliveryAnalytics> {
    return this.http.get<DeliveryAnalytics>(`${this.baseUrl}/delivery`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getReturnAndDamageAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<ReturnAndDamageAnalytics> {
    return this.http.get<ReturnAndDamageAnalytics>(`${this.baseUrl}/returns`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getPaymentAndArAnalytics(range?: DateRangePreset, startDate?: string, endDate?: string): Observable<PaymentAndArAnalytics> {
    return this.http.get<PaymentAndArAnalytics>(`${this.baseUrl}/payments`, {
      params: this.createParams(range, startDate, endDate)
    });
  }

  getBusinessInsights(): Observable<BusinessInsight[]> {
    return this.http.get<BusinessInsight[]>(`${this.baseUrl}/insights`);
  }

  exportReportCsv(reportType: string, range?: DateRangePreset, startDate?: string, endDate?: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/${reportType}`, {
      params: this.createParams(range, startDate, endDate),
      responseType: 'blob'
    });
  }

  downloadCsvBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
