import { Routes } from '@angular/router';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { OverviewComponent } from './dashboard/overview/overview.component';
import { AiCopilotComponent } from './dashboard/ai-copilot/ai-copilot.component';
import { IdealCustomerComponent } from './ideal-customer/ideal-customer.component';
import { UserRolesComponent } from './user-roles/user-roles.component';
import { WorkflowDemoComponent } from './workflow-demo/workflow-demo.component';
import { LeadsListComponent } from './leads/leads-list/leads-list.component';
import { LeadDetailComponent } from './leads/lead-detail/lead-detail.component';
import { CustomersListComponent } from './customers/customers-list/customers-list.component';
import { CustomerDetailComponent } from './customers/customer-detail/customer-detail.component';
import { EventsListComponent } from './events/events-list/events-list.component';
import { EventDetailComponent } from './events/event-detail/event-detail.component';
import { ProductListComponent } from './pages/products/product-list/product-list.component';
import { ProductFormComponent } from './pages/products/product-form/product-form.component';
import { ProductDetailComponent } from './pages/products/product-detail/product-detail.component';
import { InventoryOverviewComponent } from './pages/inventory/inventory-overview/inventory-overview.component';
import { QuotesListComponent } from './pages/quotes/quotes-list.component';
import { QuoteBuilderComponent } from './pages/quotes/quote-builder.component';
import { QuoteDetailComponent } from './pages/quotes/quote-detail.component';
import { QuotePreviewComponent } from './pages/quotes/quote-preview.component';

import { BookingsListComponent } from './pages/bookings/bookings-list.component';
import { BookingDetailComponent } from './pages/bookings/booking-detail.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'ideal-customer', component: IdealCustomerComponent },
  { path: 'user-roles', component: UserRolesComponent },
  { path: 'workflow-demo', component: WorkflowDemoComponent },
  { path: 'ai-copilot', component: AiCopilotComponent },
  { path: 'leads', component: LeadsListComponent },
  { path: 'leads/:id', component: LeadDetailComponent },
  { path: 'customers', component: CustomersListComponent },
  { path: 'customers/:id', component: CustomerDetailComponent },
  { path: 'events', component: EventsListComponent },
  { path: 'events/:id', component: EventDetailComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'products/new', component: ProductFormComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'products/:id/edit', component: ProductFormComponent },
  { path: 'inventory', component: InventoryOverviewComponent },
  { path: 'quotes', component: QuotesListComponent },
  { path: 'quotes/new', component: QuoteBuilderComponent },
  { path: 'quotes/:id', component: QuoteDetailComponent },
  { path: 'quotes/:id/edit', component: QuoteBuilderComponent },
  { path: 'quotes/:id/preview', component: QuotePreviewComponent },
  { path: 'bookings', component: BookingsListComponent },
  { path: 'bookings/:id', component: BookingDetailComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    children: [
      { path: '', component: OverviewComponent },
      { path: 'ai-copilot', component: AiCopilotComponent },
      { path: 'workflow', component: WorkflowDemoComponent },
      { path: 'leads', component: LeadsListComponent },
      { path: 'leads/:id', component: LeadDetailComponent },
      { path: 'customers', component: CustomersListComponent },
      { path: 'customers/:id', component: CustomerDetailComponent },
      { path: 'events', component: EventsListComponent },
      { path: 'events/:id', component: EventDetailComponent },
      { path: 'products', component: ProductListComponent },
      { path: 'products/new', component: ProductFormComponent },
      { path: 'products/:id', component: ProductDetailComponent },
      { path: 'products/:id/edit', component: ProductFormComponent },
      { path: 'inventory', component: InventoryOverviewComponent },
      { path: 'quotes', component: QuotesListComponent },
      { path: 'quotes/new', component: QuoteBuilderComponent },
      { path: 'quotes/:id', component: QuoteDetailComponent },
      { path: 'quotes/:id/edit', component: QuoteBuilderComponent },
      { path: 'quotes/:id/preview', component: QuotePreviewComponent },
      { path: 'bookings', component: BookingsListComponent },
      { path: 'bookings/:id', component: BookingDetailComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];

