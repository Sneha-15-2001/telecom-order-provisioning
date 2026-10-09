import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface Msg {
  from: 'you' | 'bot';
  text: string;
}

/**
 * Floating ops-chatbot inside the NexaTel shell (automation vision).
 * Talks to the Python chatbot (:8090/api/chat): investigate tickets,
 * write RCAs, check health, reproduce incidents — conversationally.
 */
@Component({
  imports: [FormsModule],
  selector: 'app-chat-widget',
  standalone: true,
  templateUrl: './chat-widget.component.html',
})
export class ChatWidgetComponent {
  private http = inject(HttpClient);

  open = signal(false);
  busy = signal(false);
  draft = signal('');
  messages = signal<Msg[]>([
    {
      from: 'bot',
      text: "Hi, I'm the NexaTel ops assistant. Try: “is everything up?”, “reproduce INC-10104”, or paste any ticket like “INC-10101 order ORD-… stuck”.",
    },
  ]);

  quick = ['is everything up?', 'reproduce INC-10104', 'rca of INC-10101 Order ORD-68D80E09 stuck in PAYMENT_PENDING'];

  toggle(): void {
    this.open.update((o) => !o);
  }

  ask(text: string): void {
    const msg = text.trim();
    if (!msg || this.busy()) return;
    this.messages.update((m) => [...m, { from: 'you', text: msg }]);
    this.draft.set('');
    this.busy.set(true);
    this.http
      .post<{ reply: string }>('http://localhost:8090/api/chat', { message: msg, mode: 'llm' })
      .subscribe({
        next: (r) => {
          this.messages.update((m) => [...m, { from: 'bot', text: r.reply }]);
          this.busy.set(false);
        },
        error: () => {
          this.messages.update((m) => [
            ...m,
            { from: 'bot', text: 'Investigator backend (:8090) is unreachable — start it first.' },
          ]);
          this.busy.set(false);
        },
      });
  }
}
