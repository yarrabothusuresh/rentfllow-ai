import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../../services/catalog.service';
import { RoleStateService } from '../../../services/role-state.service';
import { Product, InventorySummary } from '../../../models/catalog.models';

@Component({
  selector: 'app-inventory-overview',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './inventory-overview.component.html'
})
export class InventoryOverviewComponent implements OnInit {
  summary: InventorySummary | null = null;
  products: Product[] = [];
  loading = true;
  error = '';
  searchQuery = '';

  constructor(
    private catalogService: CatalogService,
    public roleState: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.catalogService.getInventorySummary().subscribe({
      next: (sum) => (this.summary = sum),
      error: (err) => console.error('Failed to load summary', err)
    });

    this.catalogService.getProducts().subscribe({
      next: (prods) => {
        this.products = prods;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load inventory assets.';
        this.loading = false;
      }
    });
  }

  get lowStockProducts(): Product[] {
    return this.products.filter((p) => p.health === 'CRITICAL' || p.health === 'WARNING');
  }

  get filteredProducts(): Product[] {
    if (!this.searchQuery.trim()) return this.products;
    const q = this.searchQuery.toLowerCase();
    return this.products.filter(
      (p) => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)
    );
  }
}
