export type AssetStatus = 
  | 'AVAILABLE'
  | 'RESERVED'
  | 'PICKED'
  | 'OUT_ON_RENT'
  | 'RETURNED'
  | 'INSPECTION'
  | 'DAMAGED'
  | 'MAINTENANCE'
  | 'LOST'
  | 'RETIRED'
  | 'TRANSFER_PENDING';

export type AssetCondition = 
  | 'NEW'
  | 'EXCELLENT'
  | 'GOOD'
  | 'FAIR'
  | 'DAMAGED'
  | 'UNUSABLE';

export type MovementType = 
  | 'RECEIPT'
  | 'ADJUSTMENT_IN'
  | 'ADJUSTMENT_OUT'
  | 'TRANSFER_OUT'
  | 'TRANSFER_IN'
  | 'RESERVATION'
  | 'RELEASE'
  | 'CHECKOUT'
  | 'RETURN'
  | 'DAMAGE'
  | 'MAINTENANCE'
  | 'MAINTENANCE_RETURN'
  | 'LOSS'
  | 'RECOVERY'
  | 'RETIREMENT';

export type TransferStatus = 
  | 'DRAFT'
  | 'REQUESTED'
  | 'APPROVED'
  | 'IN_TRANSIT'
  | 'PARTIALLY_RECEIVED'
  | 'RECEIVED'
  | 'CANCELLED';

export type CycleCountStatus = 
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'APPROVED'
  | 'CANCELLED';

export interface InventoryItem {
  id: string;
  tenantId?: string;
  productId: string;
  warehouseId: string;
  assetCode: string;
  serialNumber?: string;
  barcode?: string;
  qrCode?: string;
  status: AssetStatus;
  condition: AssetCondition;
  acquisitionDate?: string;
  purchaseCost?: number;
  currentLocation?: string;
  currentBookingId?: string;
  lastCheckedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WarehouseStock {
  id: string;
  tenantId?: string;
  productId: string;
  warehouseId: string;
  quantityOnHand: number;
  quantityAvailable: number;
  quantityReserved: number;
  quantityInMaintenance: number;
  quantityDamaged: number;
  quantityLost: number;
  minimumStockLevel: number;
}

export interface StockMovement {
  id: string;
  tenantId?: string;
  productId: string;
  inventoryItemId?: string;
  warehouseId: string;
  movementType: MovementType;
  quantity: number;
  fromWarehouseId?: string;
  toWarehouseId?: string;
  referenceType?: string;
  referenceId?: string;
  reason?: string;
  performedBy?: string;
  createdAt: string;
}

export interface InventoryTransferItem {
  id?: string;
  transferId?: string;
  productId: string;
  productName?: string;
  productSku?: string;
  inventoryItemId?: string;
  assetCode?: string;
  quantity: number;
  receivedQuantity?: number;
  condition?: AssetCondition;
  notes?: string;
}

export interface InventoryTransfer {
  id: string;
  transferNumber: string;
  fromWarehouseId: string;
  fromWarehouseName?: string;
  toWarehouseId: string;
  toWarehouseName?: string;
  status: TransferStatus;
  requestedBy?: string;
  approvedBy?: string;
  shippedAt?: string;
  receivedAt?: string;
  notes?: string;
  items?: InventoryTransferItem[];
  createdAt?: string;
}

export interface InventorySummary2 {
  totalAssets: number;
  availableAssets: number;
  reservedAssets: number;
  outOnRentAssets: number;
  maintenanceAssets: number;
  damagedAssets: number;
  lostAssets: number;
  retiredAssets: number;
  totalAssetValue: number;
  lowStockCount: number;
  itemsInTransit: number;
  stockAdjustmentsToday: number;
}

export interface AssetScanResult {
  found: boolean;
  assetId?: string;
  assetCode?: string;
  serialNumber?: string;
  barcode?: string;
  productId?: string;
  productName?: string;
  productSku?: string;
  warehouseId?: string;
  warehouseName?: string;
  status?: AssetStatus;
  condition?: AssetCondition;
  currentBookingId?: string;
  currentBookingNumber?: string;
  customerName?: string;
  message?: string;
}

export interface AssetDetail {
  id: string;
  productId: string;
  productName?: string;
  productSku?: string;
  warehouseId: string;
  warehouseName?: string;
  assetCode: string;
  serialNumber?: string;
  barcode?: string;
  qrCode?: string;
  status: AssetStatus;
  condition: AssetCondition;
  acquisitionDate?: string;
  purchaseCost?: number;
  currentLocation?: string;
  currentBookingId?: string;
  currentBookingNumber?: string;
  customerName?: string;
  lastCheckedAt?: string;
  history?: Array<{
    id: string;
    movementType: string;
    quantity: number;
    fromWarehouseName?: string;
    toWarehouseName?: string;
    referenceType?: string;
    referenceId?: string;
    reason?: string;
    performedBy?: string;
    timestamp: string;
  }>;
}

export interface InventoryCycleCountItem {
  id: string;
  cycleCountId: string;
  productId: string;
  inventoryItemId?: string;
  expectedQuantity: number;
  countedQuantity: number;
  variance: number;
  reason?: string;
  notes?: string;
}

export interface InventoryCycleCount {
  id: string;
  countNumber: string;
  warehouseId: string;
  categoryId?: string;
  countDate: string;
  assignedTo?: string;
  status: CycleCountStatus;
  approvedBy?: string;
  notes?: string;
  createdAt?: string;
}
