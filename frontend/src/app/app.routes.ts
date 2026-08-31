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
import { PortalRegisterComponent } from './portal/portal-register.component';
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
import { PortalMessagesComponent } from './portal/portal-messages.component';
import { PortalAddressesComponent } from './portal/portal-addresses.component';

import { PublicCatalogComponent } from './rentals/public-catalog.component';
import { PublicProductDetailComponent } from './rentals/public-product-detail.component';
import { RentalCartComponent } from './rentals/rental-cart.component';
import { QuoteRequestComponent } from './rentals/quote-request.component';
import { CheckoutComponent } from './rentals/checkout.component';
import { CheckoutSuccessComponent } from './rentals/checkout-success.component';
import { QuoteRequestSuccessComponent } from './rentals/quote-request-success.component';

import { CustomerRequestsDashboardComponent } from './customers/customer-requests-dashboard.component';
import { Customer360Component } from './customers/customer-360.component';
import { NotificationsListComponent } from './pages/notifications/notifications-list/notifications-list.component';

import { WarehouseDashboardComponent } from './pages/warehouse/warehouse-dashboard.component';
import { WarehouseOrdersListComponent } from './pages/warehouse/warehouse-orders-list.component';
import { WarehouseOrderDetailComponent } from './pages/warehouse/warehouse-order-detail.component';
import { WarehousePickListComponent } from './pages/warehouse/warehouse-pick-list.component';
import { WarehousePackingComponent } from './pages/warehouse/warehouse-packing.component';
import { WarehouseShortagesComponent } from './pages/warehouse/warehouse-shortages.component';
import { WarehouseCheckoutComponent } from './pages/warehouse/warehouse-checkout.component';

// Day 23 Warehouse Operations 2.0 Components
import { WarehouseMyWorkComponent } from './pages/warehouse/warehouse-my-work.component';
import { WarehousePickListsComponent } from './pages/warehouse/warehouse-pick-lists.component';
import { WarehousePickListDetailComponent } from './pages/warehouse/warehouse-pick-list-detail.component';
import { WarehouseMobilePickComponent } from './pages/warehouse/warehouse-mobile-pick.component';
import { WarehousePackListsComponent } from './pages/warehouse/warehouse-pack-lists.component';
import { WarehousePackListDetailComponent } from './pages/warehouse/warehouse-pack-list-detail.component';
import { WarehouseLoadListsComponent } from './pages/warehouse/warehouse-load-lists.component';
import { WarehouseLoadListDetailComponent } from './pages/warehouse/warehouse-load-list-detail.component';
import { WarehouseExceptionsComponent } from './pages/warehouse/warehouse-exceptions.component';
import { WarehouseSubstitutionsComponent } from './pages/warehouse/warehouse-substitutions.component';
import { WarehouseContainersComponent } from './pages/warehouse/warehouse-containers.component';

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

import { ReturnsDashboardComponent } from './pages/returns/returns-dashboard.component';
import { ReturnsListComponent } from './pages/returns/returns-list.component';
import { ReturnDetailComponent } from './pages/returns/return-detail.component';
import { ReturnsInspectionComponent } from './pages/returns/returns-inspection.component';
import { DamageDashboardComponent } from './pages/returns/damage-dashboard.component';

import { DamageClaimsDashboardComponent } from './pages/claims/damage-claims-dashboard.component';
import { DamageClaimsListComponent } from './pages/claims/damage-claims-list.component';
import { DamageClaimDetailComponent } from './pages/claims/damage-claim-detail.component';
import { RepairsDashboardComponent } from './pages/maintenance/repairs-dashboard.component';
import { RepairDetailComponent } from './pages/maintenance/repair-detail.component';
import { ReplacementsListComponent } from './pages/replacements/replacements-list.component';
import { PortalDamageClaimsComponent } from './pages/claims/portal-damage-claims.component';

// Day 21 Rental Calendar & Advanced Availability Components
import { RentalCalendarComponent } from './pages/calendar/rental-calendar.component';
import { CalendarDashboardComponent } from './pages/calendar/calendar-dashboard.component';
import { DriverCalendarComponent } from './pages/calendar/driver-calendar.component';
import { VehicleCalendarComponent } from './pages/calendar/vehicle-calendar.component';
import { WarehouseCalendarComponent } from './pages/calendar/warehouse-calendar.component';
import { InventoryCalendarComponent } from './pages/calendar/inventory-calendar.component';
import { ConflictDashboardComponent } from './pages/calendar/conflict-dashboard.component';

// Day 22 Inventory 2.0 Components
import { InventoryDashboard2Component } from './pages/inventory/inventory-dashboard-2.component';
import { ProductInventoryComponent } from './pages/inventory/product-inventory.component';
import { SerializedAssetListComponent } from './pages/inventory/serialized-asset-list.component';
import { AssetDetailComponent } from './pages/inventory/asset-detail.component';
import { StockReceiveComponent } from './pages/inventory/stock-receive.component';
import { StockAdjustComponent } from './pages/inventory/stock-adjust.component';
import { StockTransfersComponent } from './pages/inventory/stock-transfers.component';
import { ScanScreenComponent } from './pages/inventory/scan-screen.component';
import { CycleCountComponent } from './pages/inventory/cycle-count.component';
import { InventoryHistoryComponent } from './pages/inventory/inventory-history.component';
import { WarehouseCheckoutComponent } from './pages/warehouse/warehouse-checkout.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'rentals', component: PublicCatalogComponent },
  { path: 'rentals/cart', component: RentalCartComponent },
  { path: 'rentals/request-quote', component: QuoteRequestComponent },
  { path: 'rentals/checkout', component: CheckoutComponent },
  { path: 'rentals/:productId', component: PublicProductDetailComponent },
  { path: 'checkout/success', component: CheckoutSuccessComponent },
  { path: 'quote-request/success', component: QuoteRequestSuccessComponent },
  { path: 'store/:tenantSlug', component: PublicCatalogComponent },
  { path: 'customer-requests', component: CustomerRequestsDashboardComponent },

  { path: 'portal/login', component: PortalLoginComponent },
  { path: 'portal/register', component: PortalRegisterComponent },
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
      { path: 'damage-claims', component: PortalDamageClaimsComponent },
      { path: 'messages', component: PortalMessagesComponent },
      { path: 'addresses', component: PortalAddressesComponent },
      { path: 'profile', component: PortalProfileComponent },
      { path: 'requests', component: PortalRequestsComponent },
      { path: 'notifications', component: NotificationsListComponent }
    ]
  },
  { path: 'ideal-customer', component: IdealCustomerComponent },
  { path: 'user-roles', component: UserRolesComponent },

  // Redirect top-level legacy/direct admin paths to /dashboard/... to ensure Sidebar Left Menu is ALWAYS present
  { path: 'workflow-demo', redirectTo: 'dashboard/workflow', pathMatch: 'full' },
  { path: 'ai-copilot', redirectTo: 'dashboard/ai-copilot', pathMatch: 'full' },
  { path: 'leads', redirectTo: 'dashboard/leads', pathMatch: 'full' },
  { path: 'customers', redirectTo: 'dashboard/customers', pathMatch: 'full' },
  { path: 'events', redirectTo: 'dashboard/events', pathMatch: 'full' },
  { path: 'products', redirectTo: 'dashboard/products', pathMatch: 'full' },
  { path: 'products/new', redirectTo: 'dashboard/products/new', pathMatch: 'full' },
  { path: 'inventory', redirectTo: 'dashboard/inventory', pathMatch: 'full' },
  { path: 'quotes', redirectTo: 'dashboard/quotes', pathMatch: 'full' },
  { path: 'quotes/new', redirectTo: 'dashboard/quotes/new', pathMatch: 'full' },
  { path: 'bookings', redirectTo: 'dashboard/bookings', pathMatch: 'full' },
  { path: 'invoices', redirectTo: 'dashboard/invoices', pathMatch: 'full' },
  { path: 'notifications', redirectTo: 'dashboard/notifications', pathMatch: 'full' },

  // Warehouse sub-routes redirects
  { path: 'warehouse', redirectTo: 'dashboard/warehouse/dashboard', pathMatch: 'full' },
  { path: 'warehouse/dashboard', redirectTo: 'dashboard/warehouse/dashboard', pathMatch: 'full' },
  { path: 'warehouse/my-work', redirectTo: 'dashboard/warehouse/my-work', pathMatch: 'full' },
  { path: 'warehouse/pick-lists', redirectTo: 'dashboard/warehouse/pick-lists', pathMatch: 'full' },
  { path: 'warehouse/pack-lists', redirectTo: 'dashboard/warehouse/pack-lists', pathMatch: 'full' },
  { path: 'warehouse/load-lists', redirectTo: 'dashboard/warehouse/load-lists', pathMatch: 'full' },
  { path: 'warehouse/exceptions', redirectTo: 'dashboard/warehouse/exceptions', pathMatch: 'full' },
  { path: 'warehouse/substitutions', redirectTo: 'dashboard/warehouse/substitutions', pathMatch: 'full' },
  { path: 'warehouse/containers', redirectTo: 'dashboard/warehouse/containers', pathMatch: 'full' },
  { path: 'warehouse/orders', redirectTo: 'dashboard/warehouse/orders', pathMatch: 'full' },
  { path: 'warehouse/pick', redirectTo: 'dashboard/warehouse/pick', pathMatch: 'full' },
  { path: 'warehouse/packing', redirectTo: 'dashboard/warehouse/packing', pathMatch: 'full' },
  { path: 'warehouse/shortages', redirectTo: 'dashboard/warehouse/shortages', pathMatch: 'full' },

  // Delivery sub-routes redirects
  { path: 'delivery', redirectTo: 'dashboard/delivery/dashboard', pathMatch: 'full' },
  { path: 'delivery/dashboard', redirectTo: 'dashboard/delivery/dashboard', pathMatch: 'full' },
  { path: 'delivery/list', redirectTo: 'dashboard/delivery/list', pathMatch: 'full' },
  { path: 'delivery/calendar', redirectTo: 'dashboard/delivery/calendar', pathMatch: 'full' },

  // Returns sub-routes redirects
  { path: 'returns', redirectTo: 'dashboard/returns', pathMatch: 'full' },
  { path: 'returns/dashboard', redirectTo: 'dashboard/returns/dashboard', pathMatch: 'full' },
  { path: 'returns/inspection', redirectTo: 'dashboard/returns/inspection', pathMatch: 'full' },

  // Calendar sub-routes redirects
  { path: 'calendar', redirectTo: 'dashboard/calendar', pathMatch: 'full' },
  { path: 'calendar/dashboard', redirectTo: 'dashboard/calendar/dashboard', pathMatch: 'full' },
  { path: 'calendar/drivers', redirectTo: 'dashboard/calendar/drivers', pathMatch: 'full' },
  { path: 'calendar/vehicles', redirectTo: 'dashboard/calendar/vehicles', pathMatch: 'full' },
  { path: 'calendar/warehouse', redirectTo: 'dashboard/calendar/warehouse', pathMatch: 'full' },
  { path: 'calendar/inventory', redirectTo: 'dashboard/calendar/inventory', pathMatch: 'full' },
  { path: 'calendar/conflicts', redirectTo: 'dashboard/calendar/conflicts', pathMatch: 'full' },

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
      { path: 'customers/:id/360', component: Customer360Component },
      { path: 'events', component: EventsListComponent },
      { path: 'events/:id', component: EventDetailComponent },
      { path: 'products', component: ProductListComponent },
      { path: 'products/new', component: ProductFormComponent },
      { path: 'products/:id', component: ProductDetailComponent },
      { path: 'products/:id/edit', component: ProductFormComponent },
      { path: 'inventory', component: InventoryDashboard2Component },
      { path: 'inventory/dashboard', component: InventoryDashboard2Component },
      { path: 'inventory/v2-dashboard', component: InventoryDashboard2Component },
      { path: 'inventory/product-tracking', component: ProductInventoryComponent },
      { path: 'inventory/products/:productId/assets', component: SerializedAssetListComponent },
      { path: 'inventory/assets/:assetId', component: AssetDetailComponent },
      { path: 'inventory/receive', component: StockReceiveComponent },
      { path: 'inventory/adjust', component: StockAdjustComponent },
      { path: 'inventory/transfers', component: StockTransfersComponent },
      { path: 'inventory/scan', component: ScanScreenComponent },
      { path: 'inventory/count', component: CycleCountComponent },
      { path: 'inventory/history', component: InventoryHistoryComponent },
      { path: 'inventory/availability', component: InventoryAvailabilityCalendarComponent },
      { path: 'inventory/reservations', component: InventoryReservationsListComponent },
      { path: 'inventory/reservations/:id', component: InventoryReservationsListComponent },
      { path: 'inventory/conflicts', component: InventoryConflictsComponent },
      { path: 'inventory/products/:id/availability', component: InventoryProductAvailabilityComponent },
      { path: 'inventory/damage', component: DamageDashboardComponent },
      { path: 'returns', component: ReturnsListComponent },
      { path: 'returns/dashboard', component: ReturnsDashboardComponent },
      { path: 'returns/inspection', component: ReturnsInspectionComponent },
      { path: 'returns/damage', component: DamageDashboardComponent },
      { path: 'returns/:id', component: ReturnDetailComponent },
      { path: 'damage-claims', component: DamageClaimsListComponent },
      { path: 'damage-claims/dashboard', component: DamageClaimsDashboardComponent },
      { path: 'damage-claims/:id', component: DamageClaimDetailComponent },
      { path: 'maintenance', component: RepairsDashboardComponent },
      { path: 'maintenance/dashboard', component: RepairsDashboardComponent },
      { path: 'maintenance/:id', component: RepairDetailComponent },
      { path: 'replacements', component: ReplacementsListComponent },
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
      { path: 'warehouse/my-work', component: WarehouseMyWorkComponent },
      { path: 'warehouse/pick-lists', component: WarehousePickListsComponent },
      { path: 'warehouse/pick-lists/:id', component: WarehousePickListDetailComponent },
      { path: 'warehouse/pick-lists/:id/mobile', component: WarehouseMobilePickComponent },
      { path: 'warehouse/pack-lists', component: WarehousePackListsComponent },
      { path: 'warehouse/pack-lists/:id', component: WarehousePackListDetailComponent },
      { path: 'warehouse/load-lists', component: WarehouseLoadListsComponent },
      { path: 'warehouse/load-lists/:id', component: WarehouseLoadListDetailComponent },
      { path: 'warehouse/exceptions', component: WarehouseExceptionsComponent },
      { path: 'warehouse/substitutions', component: WarehouseSubstitutionsComponent },
      { path: 'warehouse/containers', component: WarehouseContainersComponent },
      { path: 'warehouse/orders', component: WarehouseOrdersListComponent },
      { path: 'warehouse/orders/:id', component: WarehouseOrderDetailComponent },
      { path: 'warehouse/pick', component: WarehousePickListComponent },
      { path: 'warehouse/packing', component: WarehousePackingComponent },
      { path: 'warehouse/shortages', component: WarehouseShortagesComponent },
      { path: 'warehouse/checkout', component: WarehouseCheckoutComponent },
      { path: 'delivery', component: DeliveryDashboardComponent },
      { path: 'delivery/dashboard', component: DeliveryDashboardComponent },
      { path: 'delivery/list', component: DeliveryListComponent },
      { path: 'delivery/calendar', component: DeliveryCalendarComponent },
      { path: 'delivery/routes', component: DeliveryRoutesComponent },
      { path: 'delivery/drivers', component: DriverListComponent },
      { path: 'delivery/:id', component: DeliveryDetailComponent },

      // Calendar inside Dashboard sub-routes
      { path: 'calendar', component: RentalCalendarComponent },
      { path: 'calendar/dashboard', component: CalendarDashboardComponent },
      { path: 'calendar/drivers', component: DriverCalendarComponent },
      { path: 'calendar/vehicles', component: VehicleCalendarComponent },
      { path: 'calendar/warehouse', component: WarehouseCalendarComponent },
      { path: 'calendar/inventory', component: InventoryCalendarComponent },
      { path: 'calendar/conflicts', component: ConflictDashboardComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
