import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { Multa } from '../services/multa';

@Component({
  selector: 'colegio-multas',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './multas.html',
})
export class Multas implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroPrestatario: number | null = null;
  filtroTipo: string = '';
  filtroEstado: string = '';
  filtroDesde: string = '';
  filtroHasta: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;
  aviso: string = '';

  constructor(private api: Multa) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api
      .listar(this.page, this.size, this.filtroPrestatario, this.filtroTipo, this.filtroEstado, this.filtroDesde, this.filtroHasta)
      .subscribe({
        next: (r) => {
          this.datos = r.datos;
          this.total = r.total;
          this.totalPaginas = r.totalPaginas;
        },
        error: () => (this.error = 'No se pudieron listar las multas'),
      });
  }

  filtrar() {
    this.page = 1;
    this.cargar();
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

  pagar(f: any) {
    if (!confirm(`¿Registrar el pago de la multa por ${f.monto}?`)) {
      return;
    }
    this.error = '';
    this.api.pagar(f.idMulta).subscribe({
      next: (r) => {
        this.aviso = r.mensaje + ': ' + r.numeroCorrelativo;
        this.cargar();
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo registrar el pago'),
    });
  }

  puedeEditar(): boolean {
    const rol = localStorage.getItem('rol') ?? '';
    return rol === 'Bibliotecario';
  }
}
