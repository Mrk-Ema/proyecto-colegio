import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Anio } from '../services/anio';
import { Carrera } from '../services/carrera';
import { Grado } from '../services/grado';

@Component({
  selector: 'colegio-grados',
  imports: [FormsModule, RouterLink],
  templateUrl: './grados.html',
  styleUrl: './grados.css',
})
export class Grados implements OnInit {
  datos: any[] = [];
  error: string = '';
  nombre: string = '';
  nivel: number | null = null;
  carrera: number | null = null;
  estado: string = '';
  anio: string = '';
  niveles: any[] = [];
  carreras: any[] = [];
  anios: any[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Grado,
    private apiAnio: Anio,
    private apiCarrera: Carrera,
  ) {}

  ngOnInit() {
    this.cargar();
    this.api.listarNiveles().subscribe({ next: (n) => (this.niveles = n) });
    this.apiCarrera.listar(1, 100, '', 'Activo').subscribe({ next: (r) => (this.carreras = r.datos) });
    this.apiAnio.listar().subscribe({ next: (a) => (this.anios = a) });
  }

  cargar() {
    this.api.listarPaginado(this.page, this.size, this.nombre, this.nivel, this.carrera, this.estado, this.anio).subscribe({
      next: (r) => {
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
      },
      error: () => (this.error = 'No se pudo listar'),
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

  desactivar(g: any) {
    if (!confirm(`¿Desactivar ${g.nombre}? Deja de estar disponible para años nuevos, los años ya creados lo conservan.`)) {
      return;
    }
    this.error = '';
    this.api.desactivar(g.idGrado).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar'),
    });
  }

  activar(g: any) {
    if (!confirm(`¿Activar ${g.nombre}?`)) {
      return;
    }
    this.error = '';
    this.api.activar(g.idGrado).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }
}
