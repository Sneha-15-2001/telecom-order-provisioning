/** Live ticket shape from GET /api/incidents (no hardcoded tickets anywhere). */
export interface Ticket {
  id: string;
  title: string;
  status: string;
  priority: string;
  reporter: string;
  created?: string;
  order: string;
  error: string;
  text: string;
}
