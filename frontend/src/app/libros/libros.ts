import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Libro } from '../services/libro';

@Component({
  selector: 'colegio-libros',
  imports: [FormsModule, RouterLink],
  templateUrl: './libros.html',
})
export class Libros implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroTitulo: string = '';
  filtroAutor: string = '';
  filtroEstado: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Libro) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.listar(this.page, this.size, this.filtroTitulo, this.filtroAutor, this.filtroEstado).subscribe({
      next: (r) => {
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
      },
      error: () => (this.error = 'No se pudo listar los libros'),
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

  desactivar(f: any) {
    if (!confirm(`¿Desactivar "${f.titulo}"? Deja de ofrecerse para nuevos préstamos.`)) {
      return;
    }
    this.error = '';
    this.api.desactivar(f.idLibro).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar el libro'),
    });
  }

  activar(f: any) {
    if (!confirm(`¿Activar "${f.titulo}"? Volverá a ofrecerse para préstamos.`)) {
      return;
    }
    this.error = '';
    this.api.activar(f.idLibro).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar el libro'),
    });
  }

  puedeEditar(): boolean {
    const rol = localStorage.getItem('rol') ?? '';
    return rol === 'Bibliotecario';
  }
}
