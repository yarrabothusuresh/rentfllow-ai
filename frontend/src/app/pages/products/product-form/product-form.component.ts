import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../../services/catalog.service';
import { RoleStateService } from '../../../services/role-state.service';
import { Product, ProductCategory, ProductType, ProductStatus } from '../../../models/catalog.models';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-form.component.html'
})
export class ProductFormComponent implements OnInit {
  isEditMode = false;
  productId: string | null = null;
  categories: ProductCategory[] = [];
  saving = false;
  error = '';

  product: Partial<Product> = {
    sku: '',
    name: '',
    description: '',
    categoryId: '',
    productType: 'RENTAL_ITEM',
    status: 'ACTIVE',
    rentalPrice: 0,
    replacementCost: 0,
    quantityOwned: 10,
    imageUrl: ''
  };

  productTypes: ProductType[] = ['RENTAL_ITEM', 'PACKAGE', 'SERVICE', 'CONSUMABLE'];
  productStatuses: ProductStatus[] = ['ACTIVE', 'INACTIVE', 'DRAFT', 'DISCONTINUED'];

  constructor(
    private catalogService: CatalogService,
    public roleState: RoleStateService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.catalogService.getCategories().subscribe({
      next: (cats) => (this.categories = cats),
      error: (err) => console.error('Failed to load categories', err)
    });

    this.productId = this.route.snapshot.paramMap.get('id');
    if (this.productId) {
      this.isEditMode = true;
      this.catalogService.getProductById(this.productId).subscribe({
        next: (p) => (this.product = { ...p }),
        error: (err) => (this.error = 'Failed to load product details')
      });
    }
  }

  onSubmit(): void {
    if (!this.product.name || !this.product.sku) {
      this.error = 'Product Name and SKU are required.';
      return;
    }

    this.saving = true;
    this.error = '';

    if (this.isEditMode && this.productId) {
      this.catalogService.updateProduct(this.productId, this.product).subscribe({
        next: () => {
          this.saving = false;
          this.router.navigate(['/products', this.productId]);
        },
        error: (err) => {
          this.saving = false;
          this.error = 'Failed to update product.';
        }
      });
    } else {
      this.catalogService.createProduct(this.product).subscribe({
        next: (created) => {
          this.saving = false;
          this.router.navigate(['/products', created.id]);
        },
        error: (err) => {
          this.saving = false;
          this.error = 'Failed to create product.';
        }
      });
    }
  }
}
