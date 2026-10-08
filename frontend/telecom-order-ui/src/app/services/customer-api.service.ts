import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../core/api-endpoints';

export interface Customer {
  id: number;
  customerNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  customerType: string;
  status: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class CustomerApiService {
  private http = inject(HttpClient);
  private base = `${API_ENDPOINTS.customer}/api/customers`;

  list(page = 0, size = 10, status = ''): Observable<Page<Customer>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Customer>>(this.base, { params });
  }

  search(q: string, page = 0, size = 10): Observable<Page<Customer>> {
    const params = new HttpParams().set('q', q).set('page', page).set('size', size);
    return this.http.get<Page<Customer>>(`${this.base}/search`, { params });
  }

  get(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.base}/${id}`);
  }

  create(body: unknown): Observable<Customer> {
    return this.http.post<Customer>(this.base, body);
  }

  suspend(id: number): Observable<Customer> {
    return this.http.post<Customer>(`${this.base}/${id}/suspend`, {});
  }

  reactivate(id: number): Observable<Customer> {
    return this.http.post<Customer>(`${this.base}/${id}/reactivate`, {});
  }

  validate(id: number): Observable<{ valid: boolean; status: string; reasons: string[] }> {
    return this.http.post<{ valid: boolean; status: string; reasons: string[] }>(
      `${this.base}/${id}/validate`, {},
    );
  }

  eligibility(id: number): Observable<{ eligible: boolean; reasons: string[] }> {
    return this.http.get<{ eligible: boolean; reasons: string[] }>(`${this.base}/${id}/eligibility`);
  }

  history(id: number): Observable<{ events: { type: string; description: string; timestamp: string }[] }> {
    return this.http.get<{ events: { type: string; description: string; timestamp: string }[] }>(
      `${this.base}/${id}/history`,
    );
  }

  subscriptions(id: number): Observable<unknown[]> {
    return this.http.get<unknown[]>(`${this.base}/${id}/subscriptions`);
  }
}
