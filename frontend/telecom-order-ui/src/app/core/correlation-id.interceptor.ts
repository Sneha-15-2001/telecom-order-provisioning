import { HttpInterceptorFn } from '@angular/common/http';
import { CORRELATION_ID_HEADER } from './api-endpoints';

/**
 * Attaches an `X-Correlation-ID` header to every outbound backend request.
 *
 * A UUID is generated per request from the browser. When the future
 * order workflows (Phase 10) chain calls across services, this ID lets
 * the AI investigator (Phase 14) correlate logs end to end.
 */
export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.headers.has(CORRELATION_ID_HEADER)) {
    return next(req);
  }
  const correlationId =
    typeof crypto !== 'undefined' && 'randomUUID' in crypto
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.floor(Math.random() * 1_000_000_000)}`;
  return next(req.clone({ setHeaders: { [CORRELATION_ID_HEADER]: correlationId } }));
};
