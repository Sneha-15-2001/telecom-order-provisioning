import { Component, Input, OnChanges, signal } from '@angular/core';

/** Animated count-up number for KPI cards. Usage: <app-count [value]="stats().orders" />. */
@Component({
  selector: 'app-count',
  standalone: true,
  template: `<span>{{ shown() }}</span>`,
})
export class CountUpComponent implements OnChanges {
  @Input({ required: true }) value = 0;

  shown = signal(0);
  private raf = 0;

  ngOnChanges(): void {
    cancelAnimationFrame(this.raf);
    const from = this.shown();
    const to = this.value;
    if (from === to) {
      this.shown.set(to);
      return;
    }
    const start = performance.now();
    const dur = 900;
    const tick = (t: number) => {
      const k = Math.min(1, (t - start) / dur);
      const eased = 1 - Math.pow(1 - k, 3);
      this.shown.set(Math.round(from + (to - from) * eased));
      if (k < 1) this.raf = requestAnimationFrame(tick);
    };
    this.raf = requestAnimationFrame(tick);
  }
}
