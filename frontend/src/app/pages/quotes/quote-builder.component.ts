import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuoteService } from '../../services/quote.service';
import { CrmService } from '../../services/crm.service';
import { CatalogService } from '../../services/catalog.service';
import {
  Quote,
  QuoteItem,
  QuoteStatus,
  PricingStrategy,
  QuoteCalculationResponse
} from '../../models/quote.models';
import { Customer, EventItem } from '../../models/crm.models';
import { Product } from '../../models/catalog.models';

@Component({
  selector: 'app-quote-builder',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './quote-builder.component.html',
  styleUrls: ['./quote-builder.component.css']
})
export class QuoteBuilderComponent implements OnInit {
  isEditMode = false;
  quoteId: string | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  availabilityWarning = '';

  // Options
  customers: Customer[] = [];
  events: EventItem[] = [];
  products: Product[] = [];

  // Quote Model
  quote: Quote = {
    customerId: '',
    eventId: '',
    status: 'DRAFT',
    quoteDate: new Date().toISOString().split('T')[0],
    validUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    rentalStartDateTime: '2026-09-20T08:00',
    rentalEndDateTime: '2026-09-22T18:00',
    deliveryFee: 250,
    pickupFee: 100,
    setupFee: 150,
    breakdownFee: 0,
    serviceFee: 0,
    taxRate: 8.25,
    depositPercentage: 30,
    discountAmount: 0,
    notes: '',
    internalNotes: '',
    items: []
  };

  // Calculation Results
  calculation: QuoteCalculationResponse = {
    grossSubtotal: 0,
    discountAmount: 0,
    subtotal: 0,
    totalFees: 500,
    taxableAmount: 500,
    taxAmount: 41.25,
    totalAmount: 541.25,
    depositAmount: 162.38,
    remainingBalance: 378.87,
    itemSubtotals: []
  };

  // New Item Line Input State
  selectedProductId = '';
  newQuantity = 1;
  newUnitPrice = 0;
  newStrategy: PricingStrategy = 'PER_EVENT';
  newRentalDays = 2;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quoteService: QuoteService,
    private crmService: CrmService,
    private catalogService: CatalogService
  ) {}

  ngOnInit(): void {
    this.loadDropdownData();
    this.quoteId = this.route.snapshot.paramMap.get('id');
    if (this.quoteId) {
      this.isEditMode = true;
      this.loadQuote(this.quoteId);
    } else {
      this.seedInitialItems();
    }
  }

  loadDropdownData(): void {
    this.crmService.getCustomers().subscribe({
      next: (data: Customer[]) => {
        this.customers = data;
        if (data.length > 0 && !this.quote.customerId) {
          this.quote.customerId = data[0].id || '';
        }
      }
    });

    this.crmService.getEvents().subscribe({
      next: (data: EventItem[]) => {
        this.events = data;
        if (data.length > 0 && !this.quote.eventId) {
          this.quote.eventId = data[0].id || '';
        }
      }
    });

    this.catalogService.getProducts().subscribe({
      next: (data: Product[]) => {
        this.products = data;
        if (data.length > 0) {
          this.selectedProductId = data[0].id || '';
          this.newUnitPrice = data[0].rentalPrice || 0;
        }
      }
    });
  }

  seedInitialItems(): void {
    this.quote.items = [
      {
        description: 'Chiavari Chair (Gold)',
        quantity: 250,
        unitPrice: 8.00,
        standardUnitPrice: 8.50,
        pricingStrategy: 'PER_EVENT',
        rentalDays: 2,
        lineSubtotal: 2000.00
      },
      {
        description: 'Round Banquet Table 60"',
        quantity: 25,
        unitPrice: 15.00,
        standardUnitPrice: 15.00,
        pricingStrategy: 'PER_EVENT',
        rentalDays: 2,
        lineSubtotal: 375.00
      },
      {
        description: 'White Table Linen 120" Round',
        quantity: 25,
        unitPrice: 12.00,
        standardUnitPrice: 12.00,
        pricingStrategy: 'PER_EVENT',
        rentalDays: 2,
        lineSubtotal: 300.00
      },
      {
        description: 'Wireless LED Uplight (RGBAW)',
        quantity: 10,
        unitPrice: 25.00,
        standardUnitPrice: 25.00,
        pricingStrategy: 'PER_EVENT',
        rentalDays: 2,
        lineSubtotal: 250.00
      }
    ];
    this.quote.discountAmount = 292.50; // 10% demo discount
    this.calculateTotals();
  }

  loadQuote(id: string): void {
    this.isLoading = true;
    this.quoteService.getQuoteById(id).subscribe({
      next: (data: Quote) => {
        this.quote = data;
        this.calculateTotals();
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Failed to load quote: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  onProductSelect(): void {
    const prod = this.products.find(p => p.id === this.selectedProductId);
    if (prod) {
      this.newUnitPrice = prod.rentalPrice;
    }
  }

  addItem(): void {
    const prod = this.products.find(p => p.id === this.selectedProductId);
    const desc = prod ? prod.name : 'Custom Item';
    const stdPrice = prod ? prod.rentalPrice : this.newUnitPrice;

    const item: QuoteItem = {
      productId: this.selectedProductId,
      description: desc,
      quantity: this.newQuantity,
      unitPrice: this.newUnitPrice,
      standardUnitPrice: stdPrice,
      pricingStrategy: this.newStrategy,
      rentalDays: this.newRentalDays,
      lineSubtotal: this.newQuantity * this.newUnitPrice
    };

    this.quote.items.push(item);
    this.calculateTotals();
  }

  removeItem(index: number): void {
    this.quote.items.splice(index, 1);
    this.calculateTotals();
  }

  calculateTotals(): void {
    let grossSubtotal = 0;
    this.quote.items.forEach((item) => {
      let multiplier = 1;
      if (item.pricingStrategy === 'PER_DAY') {
        multiplier = item.rentalDays || 1;
      }
      item.lineSubtotal = (item.quantity || 0) * (item.unitPrice || 0) * multiplier;
      grossSubtotal += item.lineSubtotal;
    });

    const disc = this.quote.discountAmount || 0;
    const subtotal = Math.max(0, grossSubtotal - disc);
    const fees = (this.quote.deliveryFee || 0) + (this.quote.pickupFee || 0) +
                 (this.quote.setupFee || 0) + (this.quote.breakdownFee || 0) + (this.quote.serviceFee || 0);

    const taxable = subtotal + fees;
    const taxRate = (this.quote.taxRate || 8.25) / 100;
    const tax = taxable * taxRate;
    const total = taxable + tax;

    const depositPct = (this.quote.depositPercentage || 30) / 100;
    const deposit = total * depositPct;
    const balance = total - deposit;

    this.calculation = {
      grossSubtotal: Number(grossSubtotal.toFixed(2)),
      discountAmount: Number(disc.toFixed(2)),
      subtotal: Number(subtotal.toFixed(2)),
      totalFees: Number(fees.toFixed(2)),
      taxableAmount: Number(taxable.toFixed(2)),
      taxAmount: Number(tax.toFixed(2)),
      totalAmount: Number(total.toFixed(2)),
      depositAmount: Number(deposit.toFixed(2)),
      remainingBalance: Number(balance.toFixed(2)),
      itemSubtotals: this.quote.items.map(i => i.lineSubtotal || 0)
    };
  }

  saveQuote(roleHeader: string = 'OWNER'): void {
    this.isSaving = true;
    this.errorMessage = '';

    if (this.isEditMode && this.quoteId) {
      this.quoteService.updateQuote(this.quoteId, this.quote, roleHeader).subscribe({
        next: (saved: Quote) => {
          this.isSaving = false;
          this.router.navigate(['/quotes', saved.id]);
        },
        error: (err: any) => {
          this.errorMessage = err.error?.error || err.message;
          this.isSaving = false;
        }
      });
    } else {
      this.quoteService.createQuote(this.quote, roleHeader).subscribe({
        next: (created: Quote) => {
          this.isSaving = false;
          this.router.navigate(['/quotes', created.id]);
        },
        error: (err: any) => {
          this.errorMessage = err.error?.error || err.message;
          this.isSaving = false;
        }
      });
    }
  }
}
