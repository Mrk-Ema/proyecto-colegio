import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Anio } from '../services/anio';
import { Asignacion } from '../services/asignacion';
import { Curso } from '../services/curso';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-asignaciones',
  imports: [FormsModule, RouterLink],
  templateUrl: './asignaciones.html',
  styleUrl: './asignaciones.css',
})
export class Asignaciones implements OnInit {
  datos: any[] = [];
  error: string = '';
  anio: number | null = null;
  grado: number | null = null;
  seccion: number | null = null;
  curso: number | null = null;
  maestro: string = '';
  anios: any[] = [];
  grados: any[] = [];
  secciones: any[] = [];
  cursos: any[] = [];
  maestros: any[] = [];
  nuevoMaestro: Record<number, number | null> = {};
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Asignacion,
    private apiAnio: Anio,
    private apiSec: Seccion,
    private apiCur: Curso,
  ) {}

  ngOnInit() {
    this.cargar();
    this.apiAnio.listar(1, 100).subscribe({ next: (r) => (this.anios = r.datos) });
    this.apiAnio.listarGrados().subscribe({ next: (g) => (this.grados = g) });
    this.apiSec.listar(1, 100, null, null, null, '', 'Activo').subscribe({ next: (r) => (this.secciones = r.datos) });
    this.apiCur.listar(1, 100, '', 'Activo').subscribe({
      next: (r) => {
        const v = new Map<number, any>();
        for (const c of r.datos) {
          if (!v.has(c.idCurso)) {
            v.set(c.idCurso, c);
          }
        }
        this.cursos = [...v.values()].sort((a, b) => a.nombre.localeCompare(b.nombre));
      },
    });
    this.api.maestros().subscribe({ next: (m) => (this.maestros = m) });
  }

  esDiversificado(nivel: string): boolean {
    return (nivel ?? '').toLowerCase().includes('diversif');
  }

  gruposGrados(): any[] {
    const porNivel = new Map<string, any[]>();
    for (const g of this.grados) {
      if (!porNivel.has(g.nivel)) {
        porNivel.set(g.nivel, []);
      }
      porNivel.get(g.nivel)!.push(g);
    }
    const grupos: any[] = [];
    for (const [nivel, lista] of porNivel) {
      if (this.esDiversificado(nivel)) {
        const porCarrera = new Map<string, any[]>();
        for (const g of lista) {
          const c = g.carrera ?? 'Sin carrera';
          if (!porCarrera.has(c)) {
            porCarrera.set(c, []);
          }
          porCarrera.get(c)!.push(g);
        }
        for (const [carrera, gs] of porCarrera) {
          grupos.push({ etiqueta: `${nivel} — ${carrera}`, opciones: gs });
        }
      } else {
        grupos.push({ etiqueta: nivel, opciones: lista });
      }
    }
    return grupos;
  }

  cargar() {
    this.api.listar(this.page, this.size, this.anio, this.grado, this.seccion, this.curso, this.maestro).subscribe({
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

  reasignar(a: any) {
    const id = this.nuevoMaestro[a.idAsignacion];
    if (id == null) {
      this.error = 'Seleccione nuevo maestro';
      return;
    }
    if (!confirm(`¿Reasignar ${a.curso} (${a.seccion}) a otro maestro? Se conservan ponderación y notas.`)) {
      return;
    }
    this.error = '';
    this.api.reasignar(a.idAsignacion, { idEmpleado: id }).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo reasignar'),
    });
  }

  quitar(a: any) {
    if (!confirm(`¿Quitar a ${a.maestro} de ${a.curso} (${a.seccion})? Queda sin maestro.`)) {
      return;
    }
    this.error = '';
    this.api.quitar(a.idAsignacion).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo quitar'),
    });
  }
}
