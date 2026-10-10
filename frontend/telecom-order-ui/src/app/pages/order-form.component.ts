import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderApiService, type Product } from '../services/order-api.service';
import { CustomerApiService, type Customer } from '../services/customer-api.service';
import { humanize } from '../shared/status-labels';

interface LineItem {
  product: Product;
  quantity: number;
}

/**
 * Order entry.
 *
 * Previously this asked an operator for the customer's internal numeric ID, a
 * product code, a product name and a unit price — all typed by hand. Real order
 * entry looks up the customer and picks from the sellable catalogue, because
 * the price is catalogue data, not something an agent decides at the counter.
 */
@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-order-form',
  templateUrl: './order-form.component.html',
})
export class OrderFormComponent implements OnInit {
  private api = inject(OrderApiService);
  private customers = inject(CustomerApiService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  catalogue = signal<Product[]>([]);
  customerResults = signal<Customer[]>([]);
  customer = signal<Customer | null>(null);
  lines = signal<LineItem[]>([]);
  saving = signal(false);
  error = signal('');

  readonly orderTypes = [
    { value: 'NEW_CONNECTION', label: 'New connection' },
    { value: 'UPGRADE', label: 'Plan upgrade' },
    { value: 'PLAN_CHANGE', label: 'Plan change' },
    { value: 'DEVICE_ONLY', label: 'Device only' },
    { value: 'BROADBAND', label: 'Broadband' },
    { value: 'BULK', label: 'Bulk order' },
  ];

  customerQuery = signal('');
  typeFilter = signal('');
  productQuery = signal('');

  form = this.fb.nonNullable.group({
    orderType: ['NEW_CONNECTION', Validators.required],
  });

  /** Catalogue narrowed by the chosen type, then by the search box. */
  visibleProducts = computed(() => {
    const type = this.typeFilter();
    const q = this.productQuery().toLowerCase().trim();
    return this.catalogue().filter((p) => {
      if (type && p.itemType !== type) return false;
      if (!q) return true;
      return p.name.toLowerCase().includes(q) || p.productCode.toLowerCase().includes(q);
    });
  });

  /** Item types present in the catalogue, for the filter dropdown. */
  itemTypes = computed(() => [...new Set(this.catalogue().map((p) => p.itemType))].sort());

  total = computed(() =>
    this.lines().reduce((sum, l) => sum + l.product.price * l.quantity, 0),
  );

  ngOnInit(): void {
    this.loadCatalogue();
  }

  private loadCatalogue(): void {
    this.api.catalogue().subscribe({
      next: (products) => this.catalogue.set(products),
      error: () => this.error.set('Could not load the product catalogue. Check that order-service is running.'),
    });
  }

  onCustomerSearch(q: string): void {
    this.customerQuery.set(q);
    const term = q.trim();
    // One character is not a search; it is the start of typing.
    if (term.length < 2) {
      this.customerResults.set([]);
      return;
    }
    this.customers.search(term, 0, 8).subscribe({
      next: (page) => this.customerResults.set(page.content),
      error: () => this.customerResults.set([]),
    });
  }

  chooseCustomer(c: Customer): void {
    this.customer.set(c);
    this.customerResults.set([]);
    this.customerQuery.set(`${c.firstName} ${c.lastName}`);
    // An upgrade or plan change only means something for an existing line.
    if (c.status !== 'ACTIVE') {
      this.error.set(`${c.firstName} ${c.lastName} is ${humanize(c.status, 'customer')}. Activate the account before taking an order.`);
    } else {
      this.error.set('');
    }
  }

  clearCustomer(): void {
    this.customer.set(null);
    this.customerQuery.set('');
  }

  addProduct(p: Product): void {
    this.lines.update((lines) => {
      const existing = lines.find((l) => l.product.productCode === p.productCode);
      if (existing) {
        return lines.map((l) => (l === existing ? { ...l, quantity: l.quantity + 1 } : l));
      }
      return [...lines, { product: p, quantity: 1 }];
    });
  }

  setQuantity(line: LineItem, qty: number): void {
    const next = Math.max(1, Math.min(20, Number(qty) || 1));
    this.lines.update((lines) => lines.map((l) => (l === line ? { ...l, quantity: next } : l)));
  }

  remove(line: LineItem): void {
    this.lines.update((lines) => lines.filter((l) => l !== line));
  }

  get canSubmit(): boolean {
    return !!this.customer() && this.lines().length > 0 && this.form.valid && !this.saving();
  }

  reset(): void {
    this.customer.set(null);
    this.customerQuery.set('');
    this.customerResults.set([]);
    this.lines.set([]);
    this.productQuery.set('');
    this.typeFilter.set('');
    this.error.set('');
    this.form.controls.orderType.setValue('NEW_CONNECTION');
  }

  save(): void {
    if (!this.customer()) {
      this.error.set('Choose the customer this order is for.');
      return;
    }
    if (!this.lines().length) {
      this.error.set('Add at least one product to the order.');
      return;
    }
    if (this.form.invalid) {
      this.error.set('Choose an order type.');
      return;
    }

    const c = this.customer()!;
    this.saving.set(true);
    this.error.set('');
    this.api.create({
      customerId: c.id,
      customerNumber: c.customerNumber,
      orderType: this.form.controls.orderType.value,
      items: this.lines().map((l) => ({
        itemType: l.product.itemType,
        productCode: l.product.productCode,
        productName: l.product.name,
        quantity: l.quantity,
        unitPrice: l.product.price,
      })),
    }).subscribe({
      next: (o) => this.router.navigate(['/orders', o.id]),
      error: (e) => {
        this.saving.set(false);
        this.error.set(e.error?.message ?? 'The order could not be created.');
      },
    });
  }
}