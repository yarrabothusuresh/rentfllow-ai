import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService, CustomerAddress } from '../services/customer-portal.service';

@Component({
  selector: 'app-portal-addresses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="addresses-container p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-light mb-1">Saved Delivery Addresses</h2>
          <p class="text-muted mb-0">Manage venue & delivery locations for seamless quote & checkout requests</p>
        </div>
        <button class="btn btn-info" (click)="openAddModal()">+ Add New Address</button>
      </div>

      <div *ngIf="loading" class="text-center py-5">
        <div class="spinner-border text-info"></div>
      </div>

      <div *ngIf="!loading && addresses.length === 0" class="card-glass p-5 text-center text-muted">
        <p class="mb-0">No saved addresses on file. Add a delivery address to speed up bookings.</p>
      </div>

      <div *ngIf="!loading && addresses.length > 0" class="row g-4">
        <div class="col-md-6" *ngFor="let addr of addresses">
          <div class="card-glass p-4 h-100 position-relative">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="badge bg-info text-dark">{{ addr.addressType }}</span>
              <span *ngIf="addr.isDefault" class="badge bg-success">Default Delivery</span>
            </div>

            <h5 class="text-light mb-1">{{ addr.addressLine1 }}</h5>
            <p class="text-muted mb-1" *ngIf="addr.addressLine2">{{ addr.addressLine2 }}</p>
            <p class="text-muted mb-2">{{ addr.city }}, {{ addr.state }} {{ addr.zipCode }}</p>

            <div class="small text-muted mb-3" *ngIf="addr.deliveryInstructions">
              <strong>Instructions:</strong> {{ addr.deliveryInstructions }}
            </div>

            <div class="d-flex gap-2 mt-auto">
              <button class="btn btn-sm btn-outline-info" (click)="openEditModal(addr)">Edit</button>
              <button class="btn btn-sm btn-outline-danger" (click)="deleteAddress(addr.id!)">Delete</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Add/Edit Modal -->
      <div class="modal-backdrop-custom" *ngIf="showModal">
        <div class="card-glass modal-box p-4">
          <h4 class="text-light mb-3">{{ editingId ? 'Edit Address' : 'Add New Address' }}</h4>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="text-muted small">Address Type</label>
              <select [(ngModel)]="formAddress.addressType" class="form-control bg-dark text-light border-secondary">
                <option value="HOME">HOME</option>
                <option value="OFFICE">OFFICE</option>
                <option value="VENUE">VENUE</option>
                <option value="OTHER">OTHER</option>
              </select>
            </div>
            <div class="col-6">
              <label class="text-muted small">Contact Person</label>
              <input type="text" [(ngModel)]="formAddress.contactPerson" class="form-control bg-dark text-light border-secondary" />
            </div>
          </div>

          <div class="mb-3">
            <label class="text-muted small">Address Line 1 *</label>
            <input type="text" [(ngModel)]="formAddress.addressLine1" placeholder="742 Evergreen Terrace" class="form-control bg-dark text-light border-secondary" />
          </div>

          <div class="mb-3">
            <label class="text-muted small">Address Line 2</label>
            <input type="text" [(ngModel)]="formAddress.addressLine2" placeholder="Suite / Hall / Dock #" class="form-control bg-dark text-light border-secondary" />
          </div>

          <div class="row g-2 mb-3">
            <div class="col-5">
              <label class="text-muted small">City *</label>
              <input type="text" [(ngModel)]="formAddress.city" class="form-control bg-dark text-light border-secondary" />
            </div>
            <div class="col-3">
              <label class="text-muted small">State *</label>
              <input type="text" [(ngModel)]="formAddress.state" class="form-control bg-dark text-light border-secondary" />
            </div>
            <div class="col-4">
              <label class="text-muted small">ZIP Code *</label>
              <input type="text" [(ngModel)]="formAddress.zipCode" class="form-control bg-dark text-light border-secondary" />
            </div>
          </div>

          <div class="mb-3">
            <label class="text-muted small">Delivery Instructions</label>
            <input type="text" [(ngModel)]="formAddress.deliveryInstructions" placeholder="e.g. Ring bell at gate" class="form-control bg-dark text-light border-secondary" />
          </div>

          <div class="form-check mb-4">
            <input type="checkbox" [(ngModel)]="formAddress.isDefault" class="form-check-input" id="defCheck" />
            <label class="form-check-label text-muted" for="defCheck">Set as default delivery address</label>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button class="btn btn-outline-secondary" (click)="showModal = false">Cancel</button>
            <button class="btn btn-info" (click)="saveAddress()">Save Address</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .addresses-container { color: #f8fafc; }
    .card-glass {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
    }
    .modal-backdrop-custom {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.75);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1050;
    }
    .modal-box { width: 500px; }
  `]
})
export class PortalAddressesComponent implements OnInit {
  addresses: CustomerAddress[] = [];
  loading: boolean = true;

  showModal: boolean = false;
  editingId: string | null = null;

  formAddress: CustomerAddress = {
    addressType: 'VENUE',
    addressLine1: '',
    addressLine2: '',
    city: 'Springfield',
    state: 'IL',
    zipCode: '62701',
    isDefault: false
  };

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadAddresses();
  }

  loadAddresses(): void {
    this.loading = true;
    this.portalService.getAddresses().subscribe({
      next: (data) => {
        this.addresses = data;
        this.loading = false;
      },
      error: () => {
        // Fallback demo data
        this.addresses = [{
          id: 'addr-1',
          addressType: 'VENUE',
          addressLine1: '742 Evergreen Terrace',
          addressLine2: 'Grand Ballroom',
          city: 'Springfield',
          state: 'IL',
          zipCode: '62701',
          isDefault: true,
          deliveryInstructions: 'Deliver to loading dock 2'
        }];
        this.loading = false;
      }
    });
  }

  openAddModal(): void {
    this.editingId = null;
    this.formAddress = {
      addressType: 'VENUE',
      addressLine1: '',
      addressLine2: '',
      city: 'Springfield',
      state: 'IL',
      zipCode: '62701',
      isDefault: false
    };
    this.showModal = true;
  }

  openEditModal(addr: CustomerAddress): void {
    this.editingId = addr.id || null;
    this.formAddress = { ...addr };
    this.showModal = true;
  }

  saveAddress(): void {
    if (!this.formAddress.addressLine1 || !this.formAddress.city) return;

    if (this.editingId) {
      this.portalService.updateAddress(this.editingId, this.formAddress).subscribe({
        next: () => {
          this.showModal = false;
          this.loadAddresses();
        }
      });
    } else {
      this.portalService.createAddress(this.formAddress).subscribe({
        next: () => {
          this.showModal = false;
          this.loadAddresses();
        }
      });
    }
  }

  deleteAddress(id: string): void {
    if (!confirm('Are you sure you want to delete this address?')) return;
    this.portalService.deleteAddress(id).subscribe({
      next: () => this.loadAddresses()
    });
  }
}
