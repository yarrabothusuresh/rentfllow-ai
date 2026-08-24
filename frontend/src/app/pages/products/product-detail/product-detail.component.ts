import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../../services/catalog.service';
import { RoleStateService } from '../../../services/role-state.service';
import {
  Product,
  InventoryTransaction,
  AvailabilityResult,
  InventoryAdjustmentRequest,
  TransactionType
} from '../../../models/catalog.models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-detail.component.html'
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  transactions: InventoryTransaction[] = [];
  loading = true;
  error = '';
  productId: string = '';

  // Availability Checker Widget state
  checkQuantity: number = 50;
  checkStartDate: string = '2026-09-20T08:00';
  checkEndDate: string = '2026-09-22T18:00';
  checkingAvailability = false;
  availabilityResult: AvailabilityResult | null = null;

  // Direct Inventory Adjustment Modal / State
  showAdjustModal = false;
  adjustQuantity: number = 10;
  adjustType: TransactionType = 'PURCHASE';
  adjustReason: string = 'Stock replenishment';
  adjusting = false;

  adjustmentTypes: TransactionType[] = [
    'PURCHASE',
    'ADJUSTMENT',
    'MAINTENANCE',
    'DAMAGE',
    'LOSS',
    'RESTORED'
  ];

  constructor(
    private catalogService: CatalogService,
    public roleState: RoleStateService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('id') || '';
    if (this.productId) {
      this.loadProduct();
      this.loadTransactions();
    }
  }

  loadProduct(): void {
    this.loading = true;
    this.catalogService.getProductById(this.productId).subscribe({
      next: (p) => {
        this.product = p;
        this.loading = false;
        // Perform default availability check
        this.onCheckAvailability();
      },
      error: (err) => {
        this.error = 'Product not found.';
        this.loading = false;
      }
    });
  }

  loadTransactions(): void {
    this.catalogService.getTransactions(this.productId).subscribe({
      next: (txs) => (this.transactions = txs),
      error: (err) => console.error('Failed to load transaction history', err)
    });
  }

  onCheckAvailability(): void {
    if (!this.productId || !this.checkStartDate || !this.checkEndDate) return;
    this.checkingAvailability = true;
    this.catalogService
      .checkAvailability(this.productId, this.checkQuantity, this.checkStartDate, this.checkEndDate)
      .subscribe({
        next: (res) => {
          this.availabilityResult = res;
          this.checkingAvailability = false;
        },
        error: (err) => {
          this.checkingAvailability = false;
        }
      });
  }

  openAdjustModal(): void {
    this.showAdjustModal = true;
  }

  closeAdjustModal(): void {
    this.showAdjustModal = false;
  }

  submitAdjustment(): void {
    if (!this.productId || this.adjustQuantity <= 0) return;
    this.adjusting = true;
    const req: InventoryAdjustmentRequest = {
      quantity: this.adjustQuantity,
      type: this.adjustType,
      reason: this.adjustReason
    };

    this.catalogService.adjustInventory(this.productId, req).subscribe({
      next: (updatedProduct) => {
        this.product = updatedProduct;
        this.adjusting = false;
        this.showAdjustModal = false;
        this.loadTransactions();
        this.onCheckAvailability();
      },
      error: (err) => {
        this.adjusting = false;
        alert('Failed to adjust inventory: ' + (err.error?.error || err.message));
      }
    });
  }

  get canAdjustInventory(): boolean {
    return this.roleState.hasAnyRole(['OWNER', 'ADMIN', 'WAREHOUSE']);
  }

  getProductImage(p: Product | null): string {
    if (!p) return 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=500&q=80';
    if (p.imageUrl && p.imageUrl.trim().length > 0) {
      return p.imageUrl;
    }
    const sku = (p.sku || '').toUpperCase();
    const name = (p.name || '').toLowerCase();
    if (sku.includes('CHI') || name.includes('chiavari')) return 'https://images.unsplash.com/photo-1503602642458-232111445657?w=500&q=80';
    if (sku.includes('WFC') || name.includes('folding')) return 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=500&q=80';
    if (sku.includes('TBL-060') || name.includes('round table')) return 'https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=500&q=80';
    if (sku.includes('TBL-CKT') || name.includes('cocktail')) return 'https://images.unsplash.com/photo-1530018607912-eff2daa1bac4?w=500&q=80';
    if (sku.includes('LIN') || name.includes('linen')) return 'https://images.unsplash.com/photo-1519741497674-611481863552?w=500&q=80';
    if (sku.includes('LGT') || name.includes('uplight') || name.includes('light')) return 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=500&q=80';
    if (sku.includes('CHR') || name.includes('bistro chair')) return 'https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=500&q=80';
    if (sku.includes('TNT') || name.includes('tent')) return 'https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=500&q=80';
    if (sku.includes('DNC') || name.includes('dance')) return 'https://images.unsplash.com/photo-1545128485-c400e7702796?w=500&q=80';
    if (sku.includes('STG') || name.includes('stage')) return 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80';
    return 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=500&q=80';
  }
}
