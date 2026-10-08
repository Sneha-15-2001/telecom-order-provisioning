import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../core/api-endpoints';
import { Page } from './customer-api.service';

export interface InvResource {
  id: number;
  resourceNumber: string;
  resourceType: string;
  identifier: string;
  status: string;
  orderId?: number;
}

export interface Reservation {
  id: number;
  reservationNumber: string;
  resourceId: number;
  resourceNumber: string;
  orderId: number;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private http = inject(HttpClient);
  private base = `${API_ENDPOINTS.inventory}/api/inventory`;

  list(page = 0, size = 10, type = '', status = ''): Observable<Page<InvResource>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (type) params = params.set('type', type);
    if (status) params = params.set('status', status);
    return this.http.get<Page<InvResource>>(`${this.base}/resources`, { params });
  }

  available(type: string, limit = 10): Observable<InvResource[]> {
    const params = new HttpParams().set('type', type).set('limit', limit);
    return this.http.get<InvResource[]>(`${this.base}/available`, { params });
  }

  reserve(resourceId: number, orderId: number, customerId?: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reserve`, { resourceId, orderId, customerId });
  }

  confirm(id: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reservations/${id}/confirm`, {});
  }

  cancel(id: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reservations/${id}/cancel`, {});
  }

  reservations(status = '', page = 0, size = 10): Observable<Page<Reservation>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Reservation>>(`${this.base}/reservations`, { params });
  }

  history(id: number): Observable<{ event: string; comment: string; timestamp: string }[]> {
    return this.http.get<{ event: string; comment: string; timestamp: string }[]>(
      `${this.base}/resources/${id}/history`,
    );
  }
}
