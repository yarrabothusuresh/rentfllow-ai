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
}
