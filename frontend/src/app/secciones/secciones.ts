import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Carrera } from '../services/carrera';
import { Grado } from '../services/grado';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-secciones',
  imports: [FormsModule, RouterLink],
  templateUrl: './secciones.html',
  styleUrl: './secciones.css',
})
export class Secciones implements OnInit {
  datos: any[] = [];
  error: string = '';
  nivel: number | null = null;
  carrera: number | null = null;
  grado: number | null = null;
  nombre: string = '';
  estado: string = '';
  niveles: any[] = [];
  carreras: any[] = [];
  grados: any[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Seccion,
    private apiGrado: Grado,
    private apiCarrera: Carrera,
  ) {}

  ngOnInit() {
    this.cargar();
    this.apiGrado.listarNiveles().subscribe({ next: (n) => (this.niveles = n) });
    this.apiCarrera.listar(1, 100, '', 'Activo').subscribe({ next: (r) => (this.carreras = r.datos) });
    this.apiGrado.listarActivos().subscribe({ next: (g) => (this.grados = g) });
  }

  cargar() {
    this.api.listar(this.page, this.size, this.nivel, this.carrera, this.grado, this.nombre, this.estado).subscribe({
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

  desactivar(s: any) {
    if (!confirm(`¿Desactivar sección ${s.nombre}? Deja de aparecer en inscripción, el historial se conserva.`)) {
      return;
    }
    this.error = '';
    this.api.desactivar(s.idSeccion).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar'),
    });
  }

  activar(s: any) {
    if (!confirm(`¿Activar sección ${s.nombre}?`)) {
      return;
    }
    this.error = '';
    this.api.activar(s.idSeccion).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }
}
