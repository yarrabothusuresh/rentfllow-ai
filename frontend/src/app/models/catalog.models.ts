export type ProductType = 'RENTAL_ITEM' | 'PACKAGE' | 'SERVICE' | 'CONSUMABLE';
export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'DRAFT' | 'DISCONTINUED';
export type TransactionType = 'PURCHASE' | 'ADJUSTMENT' | 'RESERVATION' | 'RELEASE' | 'ALLOCATE' | 'CHECKOUT' | 'RETURN' | 'DAMAGE' | 'LOSS' | 'MAINTENANCE' | 'RESTORED';
export type ReservationStatus = 'PENDING' | 'RESERVED' | 'RELEASED' | 'CANCELLED';

export interface ProductCategory {
  id: string;
  tenantId?: string;
  name: string;
  description?: string;
  parentCategoryId?: string;
  parentCategoryName?: string;
  subCategories?: ProductCategory[];
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id: string;
  tenantId?: string;
  sku: string;
  name: string;
  description?: string;
  categoryId?: string;
  categoryName?: string;
  productType: ProductType;
  status: ProductStatus;
  rentalPrice: number;
  replacementCost?: number | null;
  quantityOwned: number;
  quantityInMaintenance: number;
  quantityDamaged: number;
  quantityLost: number;
  availableQuantity: number;
  quantityReserved?: number;
  health?: 'GOOD' | 'WARNING' | 'CRITICAL';
  imageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryTransaction {
  id: string;
  tenantId?: string;
  productId: string;
  productName?: string;
  transactionType: TransactionType;
  quantity: number;
  referenceType?: string;
  referenceId?: string;
  createdBy?: string;
  notes?: string;
  createdAt: string;
}

export interface InventoryReservation {
  id: string;
  tenantId?: string;
  productId: string;
  productName?: string;
  eventId?: string;
  bookingId?: string;
  quantity: number;
  startDateTime: string;
  endDateTime: string;
  status: ReservationStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface AvailabilityResult {
  productId: string;
  productName: string;
  requestedQuantity: number;
  startDateTime: string;
  endDateTime: string;
  totalOwned: number;
  inMaintenance: number;
  damaged: number;
  lost: number;
  reservedForPeriod: number;
  availableQuantity: number;
  available: boolean;
  shortageQuantity: number;
  conflictingReservations: InventoryReservation[];
}

export interface InventorySummary {
  totalProducts: number;
  totalQuantityOwned: number;
  totalQuantityInMaintenance: number;
  totalQuantityDamaged: number;
  totalQuantityLost: number;
  totalAvailableNow: number;
  lowStockCount: number;
  criticalStockCount: number;
  totalAssetValue: number;
}

export interface InventoryAdjustmentRequest {
  quantity: number;
  type: TransactionType;
  reason?: string;
}
