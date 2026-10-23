import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { Solvencia } from '../services/solvencia';

@Component({
  selector: 'colegio-solvencias',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './solvencias.html',
})
export class Solvencias implements OnInit {
  carnet: string = '';
  anios: number[] = [];
  anio: number | null = null;
  resultado: any = null;
  error: string = '';
  aviso: string = '';
  buscando = false;
  generando = false;

  constructor(private api: Solvencia) {}

  ngOnInit() {}

  /** Busca al estudiante y carga los años en los que está inscrito. */
  buscar() {
    if (!this.carnet.trim()) {
      this.error = 'Ingresa el carnet del estudiante';
      this.resultado = null;
      return;
    }
    this.error = '';
    this.aviso = '';
    this.buscando = true;
    this.resultado = null;
    this.api.contexto(this.carnet).subscribe({
      next: (r) => {
        this.buscando = false;
        this.anios = r.anios;
        this.anio = r.porDefecto;
      },
      error: (e) => {
        this.buscando = false;
        this.anios = [];
        this.anio = null;
        this.error = e.error?.error ?? 'No se encontró el estudiante';
      },
    });
  }

  /** Calcula el saldo y el detalle del estudiante en el año elegido. */
  calcular() {
    if (!this.carnet.trim()) {
      this.error = 'Ingresa el carnet del estudiante';
      this.resultado = null;
      return;
    }
    if (this.anio == null) {
      this.error = 'Selecciona el año lectivo';
      return;
    }
    this.error = '';
    this.aviso = '';
    this.buscando = true;
    this.resultado = null;
    this.api.calcular(this.carnet, this.anio).subscribe({
      next: (r) => {
        this.buscando = false;
        this.resultado = r;
      },
      error: (e) => {
        this.buscando = false;
        this.resultado = null;
        this.error = e.error?.error ?? 'No se pudo calcular la solvencia';
      },
    });
  }

  /** Genera y descarga el PDF de la solvencia (solo si el saldo es cero). */
  generarPdf() {
    if (!this.resultado?.puedeGenerarSolvencia) {
      return;
    }
    this.generando = true;
    this.api.pdf(this.carnet, this.anio!).subscribe({
      next: (blob) => {
        this.generando = false;
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          const a = document.createElement('a');
          a.href = url;
          a.download = `solvencia-${this.carnet}.pdf`;
          a.click();
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: (e) => {
        this.generando = false;
        this.error = e.error?.error ?? 'No se pudo generar la solvencia';
      },
    });
  }

  nombreConcepto(c: string): string {
    return c === 'Inscripcion' ? 'Inscripción' : c === 'Mensualidad' ? 'Mensualidad' : 'Actividad';
  }
}
