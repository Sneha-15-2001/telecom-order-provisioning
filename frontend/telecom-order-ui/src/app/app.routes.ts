import { Routes } from '@angular/router';
import { CustomerDetailComponent } from './pages/customer-detail.component';
import { CustomerFormComponent } from './pages/customer-form.component';
import { CustomerListComponent } from './pages/customer-list.component';
import { DashboardComponent } from './pages/dashboard.component';
import { IncidentBoardComponent } from './pages/incident-board.component';
import { InventoryComponent } from './pages/inventory.component';
import { NotificationListComponent } from './pages/notification-list.component';
import { OrderDetailComponent } from './pages/order-detail.component';
import { OrderFormComponent } from './pages/order-form.component';
import { OrderListComponent } from './pages/order-list.component';
import { ProvisioningComponent } from './pages/provisioning.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'customers', component: CustomerListComponent },
  { path: 'customers/new', component: CustomerFormComponent },
  { path: 'customers/:id', component: CustomerDetailComponent },
  { path: 'orders', component: OrderListComponent },
  { path: 'orders/new', component: OrderFormComponent },
  { path: 'orders/:id', component: OrderDetailComponent },
  { path: 'inventory', component: InventoryComponent },
  { path: 'incidents', component: IncidentBoardComponent },
  { path: 'provisioning', component: ProvisioningComponent },
  { path: 'notifications', component: NotificationListComponent },
  { path: '**', redirectTo: '' },
];
