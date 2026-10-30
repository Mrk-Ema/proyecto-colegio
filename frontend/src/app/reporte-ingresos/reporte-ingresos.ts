import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reporte } from '../services/reporte';

@Component({
  selector: 'colegio-reporte-ingresos',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './reporte-ingresos.html',
})
export class ReporteIngresos implements OnInit {
  filtroDesde: string = '';
  filtroHasta: string = '';
  datos: any = null;
  error: string = '';
  cargando = false;
  exportando = false;

  constructor(private api: Reporte) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.error = '';
    this.cargando = true;
    this.datos = null;
    this.api.ingresos(this.filtroDesde, this.filtroHasta).subscribe({
      next: (r) => {
        this.cargando = false;
        this.datos = r;
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.error?.error ?? 'No se pudo generar el reporte';
      },
    });
  }

  filtrar() {
    this.cargar();
  }

  limpiar() {
    this.filtroDesde = '';
    this.filtroHasta = '';
    this.cargar();
  }

  exportarPdf() {
    this.error = '';
    this.exportando = true;
    this.api.ingresosPdf(this.filtroDesde, this.filtroHasta).subscribe({
      next: (blob) => {
        this.exportando = false;
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          const a = document.createElement('a');
          a.href = url;
          a.download = 'reporte-ingresos.pdf';
          a.click();
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: (e) => {
        this.exportando = false;
        this.error = e.error?.error ?? 'No se pudo exportar el PDF';
      },
    });
  }

  nombreConcepto(c: string): string {
    return c === 'Inscripcion' ? 'Inscripción' : c === 'Mensualidad' ? 'Mensualidad' : 'Actividad';
  }
}
