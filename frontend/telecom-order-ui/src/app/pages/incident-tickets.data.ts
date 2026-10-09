export interface Ticket {
  id: string;
  title: string;
  status: string;
  priority: string;
  reporter: string;
  created: string;
  order: string;
  error: string;
  text: string;
}

export const TICKETS: Ticket[] = [
  { id: 'INC-10101', title: 'Order stuck in PAYMENT_PENDING after UPI payment', status: 'Open', priority: 'P2', reporter: 'Customer Care', created: '2026-10-07 14:35', order: 'ORD-68D80E09', error: 'No PAYMENT_VALIDATED event', text: 'INC-10101 Order ORD-68D80E09 stuck in PAYMENT_PENDING although payment was recorded.' },
  { id: 'INC-10102', title: 'eSIM activation failing in night batch', status: 'Open', priority: 'P2', reporter: 'Activation Team', created: '2026-10-07 02:14', order: 'order 2', error: 'MISSING_MSISDN', text: 'INC-10102 eSIM provisioning PRV-D7AC40C1 FAILED for order 2.' },
  { id: 'INC-10103', title: 'MSISDN blocked by dead reservation', status: 'Open', priority: 'P3', reporter: 'Inventory Ops', created: '2026-10-06 11:02', order: 'order 9999', error: 'Hold ACTIVE past TTL', text: 'INC-10103 reservation RSV-22B3887F stuck ACTIVE for order 9999; MSISDN RES-B2979633 blocked RESERVED.' },
  { id: 'INC-10104', title: 'Customer never got ORDER_COMPLETED SMS', status: 'Open', priority: 'P3', reporter: 'Customer Care', created: '2026-10-07 16:48', order: 'order 2', error: '550 recipient rejected', text: 'INC-10104 ORDER_COMPLETED SMS for order 2 never delivered although the order completed.' },
  { id: 'INC-10105', title: 'Duplicate ORDER_COMPLETED SMS', status: 'Open', priority: 'P3', reporter: 'Customer (complaint)', created: '2026-10-07 18:20', order: 'order 2', error: 'Same event sent twice', text: 'INC-10105 Duplicate ORDER_COMPLETED SMS for order 2 within a minute.' },
  { id: 'INC-10106', title: 'Order auto-failed, customer claims account fine', status: 'Open', priority: 'P2', reporter: 'Enterprise Sales', created: '2026-10-07 10:05', order: 'ORD-8495EEAF', error: 'CUSTOMER_NOT_ACTIVE:SUSPENDED', text: 'INC-10106 Order ORD-8495EEAF FAILED at validation: customer CUS-DEMO003 SUSPENDED.' },
  { id: 'INC-10107', title: 'Order parked in RETRYING for hours', status: 'Open', priority: 'P3', reporter: 'Ops Dashboard', created: '2026-10-07 21:12', order: 'ORD-475DC248', error: 'No revalidation after retry', text: 'INC-10107 Order ORD-475DC248 stuck in RETRYING after a failed payment, never revalidated.' },
  { id: 'INC-10108', title: 'Order queue page slow at scale', status: 'Open', priority: 'P2', reporter: 'Storefront Team', created: '2026-10-06 09:30', order: '—', error: 'N+1 selects on order_item', text: 'INC-10108 GET /api/orders is slow: N+1 selects on order_item, one per order in the page, breaching response SLA.' },
  { id: 'INC-10109', title: 'Discount vanished after edit, bill shock', status: 'Open', priority: 'P3', reporter: 'Customer (complaint)', created: '2026-10-07 13:44', order: 'ORD-038A154B', error: 'Promo cleared on modify', text: 'INC-10109 Promo FESTIVE10 vanished from order ORD-038A154B after modify; payable jumped (overcharge complaint).' },
  { id: 'INC-10110', title: 'SIM stuck RESERVED, hold can never confirm', status: 'Open', priority: 'P2', reporter: 'Inventory Ops', created: '2026-10-07 08:15', order: 'order 8888', error: 'Reservation expired, 400 forever', text: 'INC-10110 reservation RSV-C5396D47 can never confirm after TTL; SIM stuck RESERVED.' },
  { id: 'INC-10111', title: 'Corporate bulk: 2 of 3 connections live', status: 'Open', priority: 'P2', reporter: 'Enterprise Sales', created: '2026-10-07 15:26', order: 'bulk batch', error: '1 item rejected (customerId null)', text: 'INC-10111 Bulk corporate order partially failed: 2 created, 1 item rejected (customerId null).' },
  { id: 'INC-10112', title: 'Port booked, broadband dead', status: 'Open', priority: 'P2', reporter: 'Field Team', created: '2026-10-07 12:40', order: 'order 7777', error: 'MISSING_RESOURCE (unlinked port)', text: 'INC-10112 Broadband activation FAILED (MISSING_RESOURCE) although fiber port is reserved for order 7777.' },
  { id: 'INC-10113', title: '"Lucky number" refused at store', status: 'Open', priority: 'P2', reporter: 'Store Agent', created: '2026-10-07 11:18', order: '—', error: 'MSISDN already in use', text: 'INC-10113 MSISDN 919000007771 refused for customer 2: already in use by customer 1.' },
  { id: 'INC-10114', title: 'Festival flyer promo rejected', status: 'Open', priority: 'P3', reporter: 'Customer Care', created: '2026-10-07 17:55', order: 'order 76', error: 'Outside validity window', text: 'INC-10114 Promo EXPIRED5 rejected for order 76: outside validity window, flyer still circulating.' },
];
