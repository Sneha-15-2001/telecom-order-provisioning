import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../core/api-endpoints';
import { Page } from './customer-api.service';

export interface Notification {
  id: number;
  notificationNumber: string;
  orderId?: number;
  channel: string;
  recipient: string;
  body: string;
  status: string;
  providerMessageId?: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  private http = inject(HttpClient);
  private base = `${API_ENDPOINTS.notification}/api/notifications`;

  list(page = 0, size = 10, status = ''): Observable<Page<Notification>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Notification>>(this.base, { params });
  }

  byOrder(orderId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/order/${orderId}`);
  }

  notify(body: unknown): Observable<Notification> {
    return this.http.post<Notification>(`${this.base}/notify`, body);
  }

  send(id: number): Observable<Notification> {
    return this.http.post<Notification>(`${this.base}/${id}/send`, {});
  }

  retry(id: number): Observable<Notification> {
    return this.http.post<Notification>(`${this.base}/${id}/retry`, {});
  }
}
