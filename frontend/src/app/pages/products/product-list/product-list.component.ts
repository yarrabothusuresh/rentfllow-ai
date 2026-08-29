import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../../services/catalog.service';
import { RoleStateService } from '../../../services/role-state.service';
import { Product, ProductCategory, ProductStatus } from '../../../models/catalog.models';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: ProductCategory[] = [];
  loading = true;
  error = '';

  // Filters & Controls
  searchQuery = '';
  selectedCategory = '';
  selectedStatus: string = 'ALL';
  viewMode: 'GRID' | 'TABLE' = 'GRID';

  constructor(
    public catalogService: CatalogService,
    public roleState: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.catalogService.getCategories().subscribe({
      next: (cats) => (this.categories = cats),
      error: (err) => console.error('Failed to load categories', err)
    });

    this.catalogService.getProducts().subscribe({
      next: (prods) => {
        this.products = prods;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load product catalog.';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.loadData();
      return;
    }
    this.loading = true;
    this.catalogService.searchProducts(this.searchQuery).subscribe({
      next: (prods) => {
        this.products = prods;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
      }
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadData();
  }

  selectCategory(catId: string): void {
    this.selectedCategory = this.selectedCategory === catId ? '' : catId;
  }

  get filteredProducts(): Product[] {
    return this.products.filter((p) => {
      const matchesCat = !this.selectedCategory || p.categoryId === this.selectedCategory;
      const matchesStatus = this.selectedStatus === 'ALL' || p.status === this.selectedStatus;
      return matchesCat && matchesStatus;
    });
  }

  // Analytics KPI Computations
  get totalProductCount(): number {
    return this.products.length;
  }

  get totalUnitsOwned(): number {
    return this.products.reduce((acc, p) => acc + (p.quantityOwned || 0), 0);
  }

  get totalUnitsAvailable(): number {
    return this.products.reduce((acc, p) => acc + (p.availableQuantity || 0), 0);
  }

  get totalUnitsInIssue(): number {
    return this.products.reduce((acc, p) => acc + (p.quantityInMaintenance || 0) + (p.quantityDamaged || 0) + (p.quantityLost || 0), 0);
  }

  get totalFleetValue(): number {
    return this.products.reduce((acc, p) => {
      const cost = p.replacementCost || p.rentalPrice || 0;
      return acc + (cost * (p.quantityOwned || 0));
    }, 0);
  }

  updateStatus(product: Product, newStatus: ProductStatus): void {
    this.catalogService.updateStatus(product.id, newStatus).subscribe({
      next: (updated) => {
        product.status = updated.status;
      },
      error: (err) => alert('Failed to update status')
    });
  }

  deleteProduct(product: Product): void {
    if (confirm(`Are you sure you want to delete ${product.name}?`)) {
      this.catalogService.deleteProduct(product.id).subscribe({
        next: () => {
          this.products = this.products.filter((p) => p.id !== product.id);
        },
        error: (err) => alert('Failed to delete product')
      });
    }
  }

  getHealthBadgeClass(health?: string): string {
    switch (health) {
      case 'GOOD':
        return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
      case 'WARNING':
        return 'bg-amber-500/15 text-amber-300 border-amber-500/30';
      case 'CRITICAL':
        return 'bg-rose-500/15 text-rose-300 border-rose-500/30';
      default:
        return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
    }
  }

  getStatusBadgeClass(status?: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
      case 'INACTIVE':
        return 'bg-slate-800 text-slate-400 border-slate-700';
      case 'DRAFT':
        return 'bg-sky-500/15 text-sky-300 border-sky-500/30';
      case 'DISCONTINUED':
        return 'bg-rose-500/15 text-rose-300 border-rose-500/30';
      default:
        return 'bg-indigo-500/15 text-indigo-300 border-indigo-500/30';
    }
  }

  getAvailabilityPercentage(p: Product): number {
    if (!p.quantityOwned || p.quantityOwned === 0) return 0;
    const pct = Math.round((p.availableQuantity / p.quantityOwned) * 100);
    return Math.min(100, Math.max(0, pct));
  }

  getProductImage(p: Product): string {
    if (p.imageUrl && p.imageUrl.trim().length > 0) {
      return p.imageUrl;
    }
    const sku = (p.sku || '').toUpperCase();
    if (sku.includes('CHI')) return 'https://images.unsplash.com/photo-1503602642458-232111445657?w=600&q=80';
    if (sku.includes('WFC')) return 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=600&q=80';
    if (sku.includes('TBL-060')) return 'https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=600&q=80';
    if (sku.includes('TBL-CKT')) return 'https://images.unsplash.com/photo-1530018607912-eff2daa1bac4?w=600&q=80';
    if (sku.includes('LIN')) return 'https://images.unsplash.com/photo-1519741497674-611481863552?w=600&q=80';
    if (sku.includes('LGT')) return 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&q=80';
    if (sku.includes('CHR')) return 'https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=600&q=80';
    if (sku.includes('TNT')) return 'https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=600&q=80';
    if (sku.includes('DNC')) return 'https://images.unsplash.com/photo-1545128485-c400e7702796?w=600&q=80';
    if (sku.includes('STG')) return 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&q=80';
    return 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?w=600&q=80';
  }
}
