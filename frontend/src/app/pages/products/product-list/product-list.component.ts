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
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: ProductCategory[] = [];
  loading = true;
  error = '';

  // Filters & Search
  searchQuery = '';
  selectedCategory = '';
  selectedStatus: string = 'ALL';

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

  get filteredProducts(): Product[] {
    return this.products.filter((p) => {
      const matchesCat = !this.selectedCategory || p.categoryId === this.selectedCategory;
      const matchesStatus = this.selectedStatus === 'ALL' || p.status === this.selectedStatus;
      return matchesCat && matchesStatus;
    });
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
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'WARNING':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      case 'CRITICAL':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/20';
      default:
        return 'bg-slate-500/10 text-slate-400 border-slate-500/20';
    }
  }
}
