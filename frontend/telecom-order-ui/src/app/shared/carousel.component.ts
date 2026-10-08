import { Component, Input, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface Slide {
  eyebrow: string;
  title: string;
  sub: string;
  cta: string;
  link: string;
  cta2?: string;
  link2?: string;
  art: 'burst' | 'rings' | 'waves';
}

/** Auto-playing hero carousel with dots + arrows (MegaTel-style). */
@Component({
  imports: [RouterLink],
  selector: 'app-carousel',
  standalone: true,
  templateUrl: './carousel.component.html',
})
export class CarouselComponent implements OnInit, OnDestroy {
  @Input({ required: true }) slides: Slide[] = [];
  @Input() intervalMs = 6000;

  index = signal(0);
  private timer: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.play();
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  play(): void {
    if (this.timer) clearInterval(this.timer);
    this.timer = setInterval(() => this.next(), this.intervalMs);
  }

  go(i: number): void {
    this.index.set((i + this.slides.length) % this.slides.length);
    this.play();
  }

  next(): void {
    this.go(this.index() + 1);
  }

  prev(): void {
    this.go(this.index() - 1);
  }
}
