import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../core/api-endpoints';
import { Page } from './customer-api.service';

export interface ProvRequest {
  id: number;
  requestNumber: string;
  orderId: number;
  serviceType: string;
  msisdn?: string;
  resourceNumber?: string;
  planCode?: string;
  status: string;
  attempts: number;
  lastError?: string;
}

@Injectable({ providedIn: 'root' })
export class ProvisioningApiService {
  private http = inject(HttpClient);
  private base = `${API_ENDPOINTS.provisioning}/api/provisioning`;

  list(page = 0, size = 10, status = ''): Observable<Page<ProvRequest>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<ProvRequest>>(this.base, { params });
  }

  byOrder(orderId: number): Observable<ProvRequest[]> {
    return this.http.get<ProvRequest[]>(`${this.base}/order/${orderId}`);
  }

  create(body: unknown): Observable<ProvRequest> {
    return this.http.post<ProvRequest>(this.base, body);
  }

  action(id: number, op: string): Observable<ProvRequest> {
    return this.http.post<ProvRequest>(`${this.base}/${id}/${op}`, {});
  }
}
