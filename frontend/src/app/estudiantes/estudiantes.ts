import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Estudiante } from '../services/estudiante';
import { Grado } from '../services/grado';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-estudiantes',
  imports: [FormsModule, RouterLink],
  templateUrl: './estudiantes.html',
})
export class Estudiantes implements OnInit {
  filas: any[] = [];
  error: string = '';
  mensaje: string = '';
  filtroNombre: string = '';
  filtroCarne: string = '';
  filtroEstado: string = '';
  filtroGrado: number | null = null;
  filtroSeccion: number | null = null;
  grados: any[] = [];
  secciones: any[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Estudiante, private apiGrado: Grado, private apiSeccion: Seccion) {}

  ngOnInit() {
    this.apiGrado.listarActivos().subscribe({ next: (g) => (this.grados = g) });
    this.cargarSecciones();
    this.cargar();
  }

  cargarSecciones() {
    this.apiSeccion.listar(1, 100, null, null, this.filtroGrado, '', 'Activo').subscribe({
      next: (r) => (this.secciones = r.datos),
    });
  }

  alCambiarGrado() {
    this.filtroSeccion = null;
    this.cargarSecciones();
  }

  cargar() {
    this.api
      .listar(this.page, this.size, this.filtroNombre, this.filtroCarne, this.filtroEstado, this.filtroGrado, this.filtroSeccion)
      .subscribe({
        next: (r) => {
          this.filas = r.datos;
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

  cambiarEstado(f: any, nuevo: string) {
    if (!nuevo || nuevo === f.estado) return;
    if (!confirm(`¿Cambiar estado de ${f.nombres} ${f.apellidos} a ${nuevo}?`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.cambiarEstado(f.idEstudiante, nuevo).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo cambiar el estado'),
    });
  }
}
