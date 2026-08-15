import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  Product,
  ProductCategory,
  InventoryTransaction,
  InventoryReservation,
  AvailabilityResult,
  InventorySummary,
  InventoryAdjustmentRequest,
  ProductStatus
} from '../models/catalog.models';
import { RoleStateService } from './role-state.service';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private apiUrl = 'http://localhost:8080/api';
  private tenantId = '99999999-9999-9999-9999-999999999999';

  // Fallback in-memory catalog mock database if backend is unreachable
  private mockCategories: ProductCategory[] = [
    { id: 'cat-001', name: 'Chairs & Seating', description: 'Event chairs, banquet seating, bistro & cocktail chairs', active: true },
    { id: 'cat-002', name: 'Tables & Linens', description: 'Round banquet tables, cocktail high-boys, dining tables & linens', active: true },
    { id: 'cat-003', name: 'Lighting & Decor', description: 'Uplighting, bistro string lights, arches, and decorative elements', active: true },
    { id: 'cat-004', name: 'Tents & Structures', description: 'Frame tents, pole tents, clear-span structures, sidewalls', active: true },
    { id: 'cat-005', name: 'Staging & Flooring', description: 'Dance floors, staging platforms, carpet, sub-flooring', active: true }
  ];

  private mockProducts: Product[] = [
    {
      id: '11111111-1111-1111-1111-111111111111',
      sku: 'CHI-001',
      name: 'Chiavari Chair (Gold)',
      description: 'Elegant gold chiavari chair with comfortable ivory vinyl cushion.',
      categoryId: 'cat-001',
      categoryName: 'Chairs & Seating',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 8.50,
      replacementCost: 65.00,
      quantityOwned: 500,
      quantityInMaintenance: 20,
      quantityDamaged: 10,
      quantityLost: 5,
      availableQuantity: 465,
      quantityReserved: 300,
      health: 'GOOD',
      imageUrl: 'https://images.unsplash.com/photo-1541558869434-2840d308329a?w=400',
      createdAt: '2026-01-10T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    },
    {
      id: '22222222-2222-2222-2222-222222222222',
      sku: 'WFC-002',
      name: 'White Resin Folding Chair',
      description: 'Clean, durable white folding chair with padded seat for outdoor ceremonies.',
      categoryId: 'cat-001',
      categoryName: 'Chairs & Seating',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 3.75,
      replacementCost: 38.00,
      quantityOwned: 800,
      quantityInMaintenance: 15,
      quantityDamaged: 5,
      quantityLost: 0,
      availableQuantity: 780,
      quantityReserved: 150,
      health: 'GOOD',
      imageUrl: 'https://images.unsplash.com/photo-1503602642458-232111445657?w=400',
      createdAt: '2026-01-10T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    },
    {
      id: '33333333-3333-3333-3333-333333333333',
      sku: 'TBL-60R',
      name: 'Round Banquet Table 60"',
      description: '60-inch heavy duty plywood round banquet table (seats 8-10 guests).',
      categoryId: 'cat-002',
      categoryName: 'Tables & Linens',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 14.00,
      replacementCost: 120.00,
      quantityOwned: 60,
      quantityInMaintenance: 2,
      quantityDamaged: 1,
      quantityLost: 0,
      availableQuantity: 57,
      quantityReserved: 25,
      health: 'GOOD',
      imageUrl: 'https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=400',
      createdAt: '2026-01-15T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    },
    {
      id: '44444444-4444-4444-4444-444444444444',
      sku: 'TNT-2020',
      name: '20x20 High Peak Frame Tent',
      description: 'Commercial grade 20x20 high peak marquee tent for outdoor receptions.',
      categoryId: 'cat-004',
      categoryName: 'Tents & Structures',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 350.00,
      replacementCost: 2800.00,
      quantityOwned: 8,
      quantityInMaintenance: 1,
      quantityDamaged: 0,
      quantityLost: 0,
      availableQuantity: 7,
      quantityReserved: 2,
      health: 'WARNING',
      imageUrl: 'https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=400',
      createdAt: '2026-02-01T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    },
    {
      id: '55555555-5555-5555-5555-555555555555',
      sku: 'LGT-UPL',
      name: 'Wireless LED Uplight (RGBAW)',
      description: 'Battery powered wireless DMX LED uplight for wall washing and ambiance.',
      categoryId: 'cat-003',
      categoryName: 'Lighting & Decor',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 25.00,
      replacementCost: 195.00,
      quantityOwned: 48,
      quantityInMaintenance: 4,
      quantityDamaged: 2,
      quantityLost: 0,
      availableQuantity: 42,
      quantityReserved: 16,
      health: 'GOOD',
      imageUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400',
      createdAt: '2026-02-10T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    },
    {
      id: '66666666-6666-6666-6666-666666666666',
      sku: 'FLR-DNC16',
      name: 'Oak Wood Dance Floor 16x16 ft',
      description: 'Interlocking real oak wood dance floor tiles with gold aluminum edging.',
      categoryId: 'cat-005',
      categoryName: 'Staging & Flooring',
      productType: 'RENTAL_ITEM',
      status: 'ACTIVE',
      rentalPrice: 450.00,
      replacementCost: 3500.00,
      quantityOwned: 3,
      quantityInMaintenance: 1,
      quantityDamaged: 0,
      quantityLost: 0,
      availableQuantity: 2,
      quantityReserved: 1,
      health: 'WARNING',
      imageUrl: 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400',
      createdAt: '2026-03-01T10:00:00Z',
      updatedAt: '2026-08-15T10:00:00Z'
    }
  ];

  private mockTransactions: InventoryTransaction[] = [
    {
      id: 'tx-001',
      tenantId: this.tenantId,
      productId: '11111111-1111-1111-1111-111111111111',
      productName: 'Chiavari Chair (Gold)',
      transactionType: 'PURCHASE',
      quantity: 500,
      referenceType: 'PO',
      referenceId: 'PO-2026-001',
      createdBy: 'OWNER',
      notes: 'Initial inventory acquisition from manufacturer',
      createdAt: '2026-01-10T10:00:00Z'
    },
    {
      id: 'tx-002',
      tenantId: this.tenantId,
      productId: '11111111-1111-1111-1111-111111111111',
      productName: 'Chiavari Chair (Gold)',
      transactionType: 'RESERVATION',
      quantity: 300,
      referenceType: 'EVENT',
      referenceId: 'event-001',
      createdBy: 'SYSTEM',
      notes: 'Reserved for Emily\'s Wedding (Sep 20-22, 2026)',
      createdAt: '2026-08-15T10:00:00Z'
    }
  ];

  constructor(private http: HttpClient, private roleState: RoleStateService) {}

  private getHeaders(): HttpHeaders {
    const role = this.roleState.getCurrentRole();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Tenant-Id': this.tenantId,
      'X-User-Role': role
    });
  }

  // --- CATEGORIES ---

  getCategories(): Observable<ProductCategory[]> {
    return this.http.get<ProductCategory[]>(`${this.apiUrl}/product-categories`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockCategories))
    );
  }

  // --- PRODUCTS ---

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/products`, { headers: this.getHeaders() }).pipe(
      map(products => this.sanitizeProducts(products)),
      catchError(() => of(this.sanitizeProducts(this.mockProducts)))
    );
  }

  searchProducts(query: string): Observable<Product[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<Product[]>(`${this.apiUrl}/products/search`, { headers: this.getHeaders(), params }).pipe(
      map(products => this.sanitizeProducts(products)),
      catchError(() => {
        const filtered = this.mockProducts.filter(p =>
          p.name.toLowerCase().includes(query.toLowerCase()) ||
          p.sku.toLowerCase().includes(query.toLowerCase())
        );
        return of(this.sanitizeProducts(filtered));
      })
    );
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/products/${id}`, { headers: this.getHeaders() }).pipe(
      map(p => this.sanitizeProduct(p)),
      catchError(() => {
        const p = this.mockProducts.find(prod => prod.id === id);
        return p ? of(this.sanitizeProduct(p)) : throwError(() => new Error('Product not found'));
      })
    );
  }

  createProduct(product: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}/products`, product, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const newP: Product = {
          id: 'prod-' + Date.now(),
          sku: product.sku || 'SKU-' + Math.floor(Math.random() * 1000),
          name: product.name || 'New Product',
          description: product.description,
          categoryId: product.categoryId,
          categoryName: this.mockCategories.find(c => c.id === product.categoryId)?.name || 'General',
          productType: product.productType || 'RENTAL_ITEM',
          status: product.status || 'ACTIVE',
          rentalPrice: product.rentalPrice || 0,
          replacementCost: product.replacementCost || 0,
          quantityOwned: product.quantityOwned || 0,
          quantityInMaintenance: 0,
          quantityDamaged: 0,
          quantityLost: 0,
          availableQuantity: product.quantityOwned || 0,
          health: 'GOOD',
          imageUrl: product.imageUrl || 'https://images.unsplash.com/photo-1541558869434-2840d308329a?w=400',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        this.mockProducts.push(newP);
        return of(newP);
      })
    );
  }

  updateProduct(id: string, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/products/${id}`, product, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const index = this.mockProducts.findIndex(p => p.id === id);
        if (index !== -1) {
          this.mockProducts[index] = { ...this.mockProducts[index], ...product, updatedAt: new Date().toISOString() };
          return of(this.mockProducts[index]);
        }
        return throwError(() => new Error('Product not found'));
      })
    );
  }

  updateStatus(id: string, status: ProductStatus): Observable<Product> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<Product>(`${this.apiUrl}/products/${id}/status`, null, { headers: this.getHeaders(), params }).pipe(
      catchError(() => {
        const p = this.mockProducts.find(prod => prod.id === id);
        if (p) {
          p.status = status;
          return of(p);
        }
        return throwError(() => new Error('Product not found'));
      })
    );
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/products/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        this.mockProducts = this.mockProducts.filter(p => p.id !== id);
        return of(void 0);
      })
    );
  }

  // --- INVENTORY & AVAILABILITY ---

  getInventorySummary(): Observable<InventorySummary> {
    return this.http.get<InventorySummary>(`${this.apiUrl}/inventory/summary`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const totalOwned = this.mockProducts.reduce((sum, p) => sum + p.quantityOwned, 0);
        const totalMaint = this.mockProducts.reduce((sum, p) => sum + p.quantityInMaintenance, 0);
        const totalDmg = this.mockProducts.reduce((sum, p) => sum + p.quantityDamaged, 0);
        const totalLost = this.mockProducts.reduce((sum, p) => sum + p.quantityLost, 0);
        const totalAvail = this.mockProducts.reduce((sum, p) => sum + p.availableQuantity, 0);
        const totalValue = this.mockProducts.reduce((sum, p) => sum + (p.quantityOwned * (p.replacementCost || 0)), 0);

        return of({
          totalProducts: this.mockProducts.length,
          totalQuantityOwned: totalOwned,
          totalQuantityInMaintenance: totalMaint,
          totalQuantityDamaged: totalDmg,
          totalQuantityLost: totalLost,
          totalAvailableNow: totalAvail,
          lowStockCount: 2,
          criticalStockCount: 0,
          totalAssetValue: totalValue
        });
      })
    );
  }

  adjustInventory(productId: string, request: InventoryAdjustmentRequest): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}/inventory/products/${productId}/adjust`, request, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const p = this.mockProducts.find(prod => prod.id === productId);
        if (p) {
          if (request.type === 'PURCHASE' || request.type === 'RESTORED') {
            p.quantityOwned += request.quantity;
          } else if (request.type === 'MAINTENANCE') {
            p.quantityInMaintenance += request.quantity;
          } else if (request.type === 'DAMAGE') {
            p.quantityDamaged += request.quantity;
          } else if (request.type === 'LOSS') {
            p.quantityLost += request.quantity;
          }
          p.availableQuantity = Math.max(0, p.quantityOwned - p.quantityInMaintenance - p.quantityDamaged - p.quantityLost);
          this.mockTransactions.unshift({
            id: 'tx-' + Date.now(),
            tenantId: this.tenantId,
            productId: p.id,
            productName: p.name,
            transactionType: request.type,
            quantity: request.quantity,
            createdBy: this.roleState.getCurrentRole(),
            notes: request.reason || 'Direct inventory adjustment',
            createdAt: new Date().toISOString()
          });
          return of(p);
        }
        return throwError(() => new Error('Product not found'));
      })
    );
  }

  getTransactions(productId: string): Observable<InventoryTransaction[]> {
    return this.http.get<InventoryTransaction[]>(`${this.apiUrl}/inventory/products/${productId}/transactions`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockTransactions.filter(t => t.productId === productId)))
    );
  }

  checkAvailability(productId: string, quantity: number, startDateTime: string, endDateTime: string): Observable<AvailabilityResult> {
    const params = new HttpParams()
      .set('quantity', quantity.toString())
      .set('startDateTime', startDateTime)
      .set('endDateTime', endDateTime);

    return this.http.get<AvailabilityResult>(`${this.apiUrl}/inventory/products/${productId}/availability`, { headers: this.getHeaders(), params }).pipe(
      catchError(() => {
        const p = this.mockProducts.find(prod => prod.id === productId);
        const owned = p ? p.quantityOwned : 100;
        const avail = p ? p.availableQuantity : 80;
        const isAvail = avail >= quantity;
        const result: AvailabilityResult = {
          productId: productId,
          productName: p ? p.name : 'Rental Product',
          requestedQuantity: quantity,
          startDateTime: startDateTime,
          endDateTime: endDateTime,
          totalOwned: owned,
          inMaintenance: p ? p.quantityInMaintenance : 0,
          damaged: p ? p.quantityDamaged : 0,
          lost: p ? p.quantityLost : 0,
          reservedForPeriod: 0,
          availableQuantity: avail,
          available: isAvail,
          shortageQuantity: isAvail ? 0 : quantity - avail,
          conflictingReservations: []
        };
        return of(result);
      })
    );
  }

  createReservation(reservation: Partial<InventoryReservation>): Observable<InventoryReservation> {
    return this.http.post<InventoryReservation>(`${this.apiUrl}/inventory/reservations`, reservation, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const res: InventoryReservation = {
          id: 'res-' + Date.now(),
          tenantId: this.tenantId,
          productId: reservation.productId || '',
          quantity: reservation.quantity || 1,
          startDateTime: reservation.startDateTime || new Date().toISOString(),
          endDateTime: reservation.endDateTime || new Date().toISOString(),
          status: 'RESERVED',
          createdAt: new Date().toISOString()
        };
        return of(res);
      })
    );
  }

  // --- HELPERS ---

  private sanitizeProducts(products: Product[]): Product[] {
    const role = this.roleState.getCurrentRole();
    return products.map(p => this.sanitizeProduct(p, role));
  }

  private sanitizeProduct(p: Product, role?: string): Product {
    const currentRole = role || this.roleState.getCurrentRole();
    if (currentRole === 'CUSTOMER') {
      return { ...p, replacementCost: null };
    }
    return p;
  }
}
