/**
 * Backend base URLs for the five microservices.
 *
 * Phase 1: constants only (dashboard links). Live HttpClient usage
 * against these endpoints arrives in Phase 9. The Angular dev server
 * proxies `/api/<service>` paths via proxy.conf.json to avoid CORS
 * during local development.
 */
export const API_ENDPOINTS = {
  customer: 'http://localhost:8081',
  order: 'http://localhost:8082',
  inventory: 'http://localhost:8083',
  provisioning: 'http://localhost:8084',
  notification: 'http://localhost:8085',
} as const;

export const CORRELATION_ID_HEADER = 'X-Correlation-ID';
