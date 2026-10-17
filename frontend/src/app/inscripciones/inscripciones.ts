import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Inscripcion } from '../services/inscripcion';

@Component({
  selector: 'colegio-inscripciones',
  imports: [FormsModule, RouterLink],
  templateUrl: './inscripciones.html',
})
export class Inscripciones implements OnInit {
  datos: any[] = [];
  error: string = '';
  mensaje: string = '';
  anios: any[] = [];
  niveles: any[] = [];
  grados: any[] = [];
  secciones: any[] = [];
  filtroAnio: number | null = null;
  filtroNivel: number | null = null;
  filtroGrado: number | null = null;
  filtroSeccion: number | null = null;
  filtroEstudiante: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Inscripcion,
  ) {}

  ngOnInit() {
    this.api.anios().subscribe({
      next: (r) => {
        this.anios = r.anios;
        if (r.porDefecto != null) {
          this.filtroAnio = r.porDefecto;
          this.cargarGrados();
        }
      },
      error: () => (this.error = 'No se pudieron cargar los años lectivos'),
    });
    this.api.niveles().subscribe({ next: (n) => (this.niveles = n) });
    this.cargar();
  }

  /** Los grados del filtro dependen del año elegido. */
  cargarGrados() {
    if (this.filtroAnio == null) {
      this.grados = [];
      return;
    }
    this.api.grados(this.filtroAnio).subscribe({
      next: (g) => {
        this.grados = g;
        if (this.filtroGrado != null && !g.some((x: any) => x.idGrado === this.filtroGrado)) {
          this.filtroGrado = null;
          this.filtroSeccion = null;
        }
      },
      error: () => (this.error = 'No se pudieron cargar los grados del año'),
    });
  }

  cargarSecciones() {
    if (this.filtroGrado == null) {
      this.secciones = [];
      return;
    }
    this.api.secciones(this.filtroGrado).subscribe({
      next: (s) => {
        this.secciones = s;
        if (this.filtroSeccion != null && !s.some((x: any) => x.idSeccion === this.filtroSeccion)) {
          this.filtroSeccion = null;
        }
      },
      error: () => (this.error = 'No se pudieron cargar las secciones'),
    });
  }

  cargar() {
    this.api
      .listar(
        this.page,
        this.size,
        this.filtroAnio,
        this.filtroNivel,
        this.filtroGrado,
        this.filtroSeccion,
        this.filtroEstudiante,
      )
      .subscribe({
        next: (r) => {
          this.datos = r.datos;
          this.total = r.total;
          this.totalPaginas = r.totalPaginas;
        },
        error: () => (this.error = 'No se pudo listar las inscripciones'),
      });
  }

  filtrar() {
    this.page = 1;
    this.cargar();
  }

  limpiar() {
    this.filtroAnio = null;
    this.filtroNivel = null;
    this.filtroGrado = null;
    this.filtroSeccion = null;
    this.filtroEstudiante = '';
    this.grados = [];
    this.secciones = [];
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

  esSuperAdmin(): boolean {
    return (localStorage.getItem('rol') ?? '') === 'Super Admin';
  }
}