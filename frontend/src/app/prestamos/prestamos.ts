import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Prestamo } from '../services/prestamo';

@Component({
  selector: 'colegio-prestamos',
  imports: [FormsModule, RouterLink],
  templateUrl: './prestamos.html',
})
export class Prestamos implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroLibro: number | null = null;
  filtroPrestatario: number | null = null;
  filtroTipo: string = '';
  filtroEstado: string = '';
  filtroDesde: string = '';
  filtroHasta: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Prestamo) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api
      .listar(this.page, this.size, this.filtroLibro, this.filtroPrestatario, this.filtroTipo, this.filtroEstado, this.filtroDesde, this.filtroHasta)
      .subscribe({
        next: (r) => {
          this.datos = r.datos;
          this.total = r.total;
          this.totalPaginas = r.totalPaginas;
        },
        error: () => (this.error = 'No se pudieron listar los préstamos'),
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

  devolver(f: any) {
    if (!confirm(`¿Registrar la devolución de "${f.libro}"?`)) {
      return;
    }
    this.error = '';
    this.api.devolver(f.idPrestamo).subscribe({
      next: (r) => {
        this.aviso = r.mensaje;
        if (r.montoMulta > 0) {
          this.aviso += ` Multa de ${r.montoMulta} por ${r.diasAtraso} día(s) de atraso.`;
        }
        this.cargar();
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo registrar la devolución'),
    });
  }

  puedeEditar(): boolean {
    const rol = localStorage.getItem('rol') ?? '';
    return rol === 'Bibliotecario';
  }

  aviso: string = '';
}
