import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { StatusPillComponent } from '../shared/status-pill.component';
import { TICKETS, Ticket } from './incident-tickets.data';

interface Investigation {
  hypothesis: string;
  suspect: { service: string; signal: string; confidence: string };
  fix_hint: string;
  has_error: boolean;
  journey: { service: string; event: string; status: string; time: string }[];
  log_lines: { service: string; line: string }[];
  log_sources: { mode: string; files: string[] };
  db_evidence: Record<string, unknown[]>;
}

interface RcaDoc {
  incident: string;
  confidence: string;
  impact: string;
  affected: { order: string; order_status: string; customer: unknown; service: string };
  root_cause: string;
  contributing_factors: string[];
  fix_type: string;
  recommendation: string;
  timeline: { service: string; event: string; status: string; time: string }[];
}

/**
 * Incident board inside NexaTel (mirrors the React investigator):
 * Jira-style queue → investigate → DATA/CODE verdict → RCA + workaround.
 */
@Component({
  imports: [FormsModule, StatusPillComponent],
  selector: 'app-incident-board',
  templateUrl: './incident-board.component.html',
})
export class IncidentBoardComponent {
  private http = inject(HttpClient);
  private api = 'http://localhost:8090';

  tickets = TICKETS;
  picked: Ticket | null = null;
  draft = signal('');
  busy = signal(false);
  rcaBusy = signal(false);
  error = signal('');
  inv: Investigation | null = null;
  rca: RcaDoc | null = null;

  pick(t: Ticket): void {
    this.picked = t;
    this.draft.set(t.text);
    this.inv = null;
    this.rca = null;
    this.error.set('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  investigate(): void {
    const text = this.draft().trim();
    if (!text || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.inv = null;
    this.rca = null;
    this.http.post<Investigation>(`${this.api}/api/investigate`, { incident_text: text, mode: 'llm' }).subscribe({
      next: (r) => {
        this.inv = r;
        this.busy.set(false);
      },
      error: () => {
        this.error.set('Investigator backend (:8090) is unreachable — start it first.');
        this.busy.set(false);
      },
    });
  }

  makeRca(): void {
    const text = this.draft().trim();
    if (!text || this.rcaBusy()) return;
    this.rcaBusy.set(true);
    this.http.post<{ rca: RcaDoc }>(`${this.api}/api/rca`, { incident_text: text, mode: 'llm' }).subscribe({
      next: (r) => {
        this.rca = r.rca;
        this.rcaBusy.set(false);
      },
      error: () => {
        this.error.set('RCA failed — is :8090 up?');
        this.rcaBusy.set(false);
      },
    });
  }

  isData(): boolean {
    const f = (this.rca?.fix_type ?? this.inv?.fix_hint ?? '').toUpperCase();
    return f.startsWith('DATA');
  }
}
