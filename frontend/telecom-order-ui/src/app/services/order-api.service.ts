import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../core/api-endpoints';
import { Page } from './customer-api.service';

export interface OrderItem {
  id?: number;
  itemType: string;
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  msisdn?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  customerNumber: string;
  orderType: string;
  status: string;
  priority: string;
  totalAmount: number;
  discountAmount: number;
  payableAmount: number;
  promoCode?: string;
  items: OrderItem[];
}

export interface Product {
  id: number;
  productCode: string;
  name: string;
  itemType: string;
  price: number;
  description?: string;
  dataGb?: number;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class OrderApiService {
  private http = inject(HttpClient);
  private base = `${API_ENDPOINTS.order}/api/orders`;

  /** Sellable catalogue — order entry picks from this instead of typing codes. */
  catalogue(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_ENDPOINTS.order}/api/products`);
  }

  list(page = 0, size = 10, status = ''): Observable<Page<Order>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Order>>(this.base, { params });
  }

  get(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.base}/${id}`);
  }

  create(body: unknown): Observable<Order> {
    return this.http.post<Order>(this.base, body);
  }

  action(id: number, op: string, body: unknown = {}): Observable<Order> {
    return this.http.post<Order>(`${this.base}/${id}/${op}`, body);
  }

  history(id: number): Observable<{ event: string; fromStatus: string; toStatus: string; comment: string; timestamp: string }[]> {
    return this.http.get<{ event: string; fromStatus: string; toStatus: string; comment: string; timestamp: string }[]>(
      `${this.base}/${id}/history`,
    );
  }

  timeline(id: number): Observable<{ events: { timestamp: string; title: string; detail: string }[] }> {
    return this.http.get<{ events: { timestamp: string; title: string; detail: string }[] }>(
      `${this.base}/${id}/timeline`,
    );
  }

  recordPayment(id: number, amount: number, method: string): Observable<unknown> {
    return this.http.post(`${this.base}/${id}/payments`, { amount, method });
  }

  validatePayment(id: number): Observable<{ valid: boolean; orderStatus: string }> {
    return this.http.post<{ valid: boolean; orderStatus: string }>(
      `${this.base}/${id}/payment/validate`, {},
    );
  }

  applyPromo(id: number, promoCode: string): Observable<Order> {
    return this.http.post<Order>(`${this.base}/${id}/promotion/apply`, { promoCode });
  }

  promotions(): Observable<{ promoCode: string; description: string }[]> {
    return this.http.get<{ promoCode: string; description: string }[]>(
      `${API_ENDPOINTS.order}/api/promotions`,
    );
  }
}
