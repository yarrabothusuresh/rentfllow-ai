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

import { InvoicesListComponent } from './pages/invoices/invoices-list.component';
import { InvoiceDetailComponent } from './pages/invoices/invoice-detail.component';

import { PortalLayoutComponent } from './portal/portal-layout.component';
import { PortalLoginComponent } from './portal/portal-login.component';
import { PortalDashboardComponent } from './portal/portal-dashboard.component';
import { PortalEventsListComponent } from './portal/portal-events-list.component';
import { PortalEventDetailComponent } from './portal/portal-event-detail.component';
import { PortalQuotesListComponent } from './portal/portal-quotes-list.component';
import { PortalQuoteDetailComponent } from './portal/portal-quote-detail.component';
import { PortalBookingsListComponent } from './portal/portal-bookings-list.component';
import { PortalBookingDetailComponent } from './portal/portal-booking-detail.component';
import { PortalInvoicesListComponent } from './portal/portal-invoices-list.component';
import { PortalInvoiceDetailComponent } from './portal/portal-invoice-detail.component';
import { PortalProfileComponent } from './portal/portal-profile.component';
import { PortalRequestsComponent } from './portal/portal-requests.component';

import { NotificationsListComponent } from './pages/notifications/notifications-list/notifications-list.component';
import { WarehouseDashboardComponent } from './pages/warehouse/warehouse-dashboard.component';
import { WarehouseOrdersListComponent } from './pages/warehouse/warehouse-orders-list.component';
import { WarehouseOrderDetailComponent } from './pages/warehouse/warehouse-order-detail.component';
import { WarehousePickListComponent } from './pages/warehouse/warehouse-pick-list.component';
import { WarehousePackingComponent } from './pages/warehouse/warehouse-packing.component';
import { WarehouseShortagesComponent } from './pages/warehouse/warehouse-shortages.component';

import { InventoryDashboardComponent } from './pages/inventory/inventory-dashboard.component';
import { InventoryAvailabilityCalendarComponent } from './pages/inventory/inventory-availability-calendar.component';
import { InventoryReservationsListComponent } from './pages/inventory/inventory-reservations-list.component';
import { InventoryProductAvailabilityComponent } from './pages/inventory/inventory-product-availability.component';
import { InventoryConflictsComponent } from './pages/inventory/inventory-conflicts.component';

import { DeliveryDashboardComponent } from './pages/delivery/delivery-dashboard.component';
import { DeliveryListComponent } from './pages/delivery/delivery-list.component';
import { DeliveryDetailComponent } from './pages/delivery/delivery-detail.component';
import { DeliveryCalendarComponent } from './pages/delivery/delivery-calendar.component';
import { DeliveryRoutesComponent } from './pages/delivery/delivery-routes.component';
import { DriverListComponent } from './pages/delivery/driver-list.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'portal/login', component: PortalLoginComponent },
  {
    path: 'portal',
    component: PortalLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: PortalDashboardComponent },
      { path: 'events', component: PortalEventsListComponent },
      { path: 'events/:id', component: PortalEventDetailComponent },
      { path: 'quotes', component: PortalQuotesListComponent },
      { path: 'quotes/:id', component: PortalQuoteDetailComponent },
      { path: 'bookings', component: PortalBookingsListComponent },
      { path: 'bookings/:id', component: PortalBookingDetailComponent },
      { path: 'invoices', component: PortalInvoicesListComponent },
      { path: 'invoices/:id', component: PortalInvoiceDetailComponent },
      { path: 'profile', component: PortalProfileComponent },
      { path: 'requests', component: PortalRequestsComponent },
      { path: 'notifications', component: NotificationsListComponent }
    ]
  },
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
  { path: 'invoices', component: InvoicesListComponent },
  { path: 'invoices/:id', component: InvoiceDetailComponent },
  { path: 'notifications', component: NotificationsListComponent },
  { path: 'warehouse/dashboard', component: WarehouseDashboardComponent },
  { path: 'warehouse/orders', component: WarehouseOrdersListComponent },
  { path: 'warehouse/orders/:id', component: WarehouseOrderDetailComponent },
  { path: 'warehouse/pick', component: WarehousePickListComponent },
  { path: 'warehouse/packing', component: WarehousePackingComponent },
  { path: 'warehouse/shortages', component: WarehouseShortagesComponent },
  { path: 'inventory/dashboard', component: InventoryDashboardComponent },
  { path: 'inventory/availability', component: InventoryAvailabilityCalendarComponent },
  { path: 'inventory/reservations', component: InventoryReservationsListComponent },
  { path: 'inventory/reservations/:id', component: InventoryReservationsListComponent },
  { path: 'inventory/conflicts', component: InventoryConflictsComponent },
  { path: 'inventory/products/:id/availability', component: InventoryProductAvailabilityComponent },
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
      { path: 'inventory', component: InventoryDashboardComponent },
      { path: 'inventory/dashboard', component: InventoryDashboardComponent },
      { path: 'inventory/availability', component: InventoryAvailabilityCalendarComponent },
      { path: 'inventory/reservations', component: InventoryReservationsListComponent },
      { path: 'inventory/reservations/:id', component: InventoryReservationsListComponent },
      { path: 'inventory/conflicts', component: InventoryConflictsComponent },
      { path: 'inventory/products/:id/availability', component: InventoryProductAvailabilityComponent },
      { path: 'quotes', component: QuotesListComponent },
      { path: 'quotes/new', component: QuoteBuilderComponent },
      { path: 'quotes/:id', component: QuoteDetailComponent },
      { path: 'quotes/:id/edit', component: QuoteBuilderComponent },
      { path: 'quotes/:id/preview', component: QuotePreviewComponent },
      { path: 'bookings', component: BookingsListComponent },
      { path: 'bookings/:id', component: BookingDetailComponent },
      { path: 'invoices', component: InvoicesListComponent },
      { path: 'invoices/:id', component: InvoiceDetailComponent },
      { path: 'notifications', component: NotificationsListComponent },
      { path: 'warehouse/dashboard', component: WarehouseDashboardComponent },
      { path: 'warehouse/orders', component: WarehouseOrdersListComponent },
      { path: 'warehouse/orders/:id', component: WarehouseOrderDetailComponent },
      { path: 'warehouse/pick', component: WarehousePickListComponent },
      { path: 'warehouse/packing', component: WarehousePackingComponent },
      { path: 'warehouse/shortages', component: WarehouseShortagesComponent },
      { path: 'delivery', component: DeliveryDashboardComponent },
      { path: 'delivery/dashboard', component: DeliveryDashboardComponent },
      { path: 'delivery/list', component: DeliveryListComponent },
      { path: 'delivery/calendar', component: DeliveryCalendarComponent },
      { path: 'delivery/routes', component: DeliveryRoutesComponent },
      { path: 'delivery/drivers', component: DriverListComponent },
      { path: 'delivery/:id', component: DeliveryDetailComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];

