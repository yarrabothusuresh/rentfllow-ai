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
      { path: 'events/:id', component: EventDetailComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
