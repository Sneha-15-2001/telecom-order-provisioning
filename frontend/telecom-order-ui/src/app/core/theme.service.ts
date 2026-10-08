import { Injectable, effect, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

/** App-wide dark/light mode, persisted across reloads (MegaTel-style). */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  theme = signal<Theme>(
    (localStorage.getItem('nexatel-theme') as Theme) === 'dark' ? 'dark' : 'light',
  );

  constructor() {
    document.documentElement.dataset['theme'] = this.theme();
    effect(() => {
      const t = this.theme();
      document.documentElement.dataset['theme'] = t;
      localStorage.setItem('nexatel-theme', t);
    });
  }

  toggle(): void {
    this.theme.update((t) => (t === 'light' ? 'dark' : 'light'));
  }
}
