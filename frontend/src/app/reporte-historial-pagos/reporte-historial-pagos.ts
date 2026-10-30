import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reporte } from '../services/reporte';

@Component({
  selector: 'colegio-reporte-historial-pagos',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './reporte-historial-pagos.html',
})
export class ReporteHistorialPagos implements OnInit {
  idEstudiante: number | null = null;
  filtroDesde: string = '';
  filtroHasta: string = '';
  datos: any[] = [];
  estudiante: any = null;
  saldoPendiente: any = null;
  error: string = '';
  cargando = false;
  exportando = false;
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Reporte) {}

  ngOnInit() {}

  cargar() {
    if (this.idEstudiante == null) {
      this.error = 'Ingresa el ID del estudiante';
      return;
    }
    this.error = '';
    this.cargando = true;
    this.datos = [];
    this.api.historialPagos(this.idEstudiante, this.filtroDesde, this.filtroHasta, this.page, this.size).subscribe({
      next: (r) => {
        this.cargando = false;
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
        this.estudiante = r.estudiante;
        this.saldoPendiente = r.saldoPendiente;
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.error?.error ?? 'No se pudo generar el reporte';
      },
    });
  }

  filtrar() {
    this.page = 1;
    this.cargar();
  }

  limpiar() {
    this.filtroDesde = '';
    this.filtroHasta = '';
    this.filtrar();
  }

  anterior() {
    if (this.page > 1) {
      this.page--;
      this.cargar();
    }
  }

  siguiente() {
    if (this.page < this.totalPaginas) {
      this.page++;
      this.cargar();
    }
  }

  exportarPdf() {
    if (this.idEstudiante == null) {
      return;
    }
    this.error = '';
    this.exportando = true;
    this.api.historialPagosPdf(this.idEstudiante, this.filtroDesde, this.filtroHasta).subscribe({
      next: (blob) => {
        this.exportando = false;
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          const a = document.createElement('a');
          a.href = url;
          a.download = `historial-pagos-${this.idEstudiante}.pdf`;
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
