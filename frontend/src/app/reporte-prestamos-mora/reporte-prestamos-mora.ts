import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reporte } from '../services/reporte';

@Component({
  selector: 'colegio-reporte-prestamos-mora',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './reporte-prestamos-mora.html',
})
export class ReportePrestamosMora implements OnInit {
  filtroDesde: string = '';
  filtroHasta: string = '';
  filtroLibro: number | null = null;
  datos: any[] = [];
  error: string = '';
  aviso: string = '';
  cargando = false;
  exportando = false;
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;
  multaPorDia: number = 0;

  constructor(private api: Reporte) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.error = '';
    this.cargando = true;
    this.api.prestamosEnMora(this.filtroDesde, this.filtroHasta, this.filtroLibro, this.page, this.size).subscribe({
      next: (r) => {
        this.cargando = false;
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
        this.multaPorDia = r.multaPorDia;
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
    this.filtroLibro = null;
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
    this.error = '';
    this.exportando = true;
    this.api.prestamosEnMoraPdf(this.filtroDesde, this.filtroHasta, this.filtroLibro).subscribe({
      next: (blob) => {
        this.exportando = false;
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          const a = document.createElement('a');
          a.href = url;
          a.download = 'prestamos-en-mora.pdf';
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
}
