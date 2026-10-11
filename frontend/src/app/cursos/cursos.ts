import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Curso } from '../services/curso';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-cursos',
  imports: [FormsModule, RouterLink],
  templateUrl: './cursos.html',
  styleUrl: './cursos.css',
})
export class Cursos implements OnInit {
  datos: any[] = [];
  error: string = '';
  nombre: string = '';
  estado: string = '';
  grado: number | null = null;
  grados: any[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Curso,
    private apiAnio: Anio,
  ) {}

  ngOnInit() {
    this.cargar();
    this.apiAnio.listarGrados().subscribe({
      next: (g) => (this.grados = g),
    });
  }

  cargar() {
    this.api.listar(this.page, this.size, this.nombre, this.estado, this.grado).subscribe({
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

  desactivar(c: any) {
    if (!confirm(`¿Desactivar ${c.nombre}? Deja de copiarse a años nuevos, los años ya creados lo conservan.`)) {
      return;
    }
    this.error = '';
    this.api.desactivar(c.idCurso).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar'),
    });
  }

  activar(c: any) {
    if (!confirm(`¿Activar ${c.nombre}?`)) {
      return;
    }
    this.error = '';
    this.api.activar(c.idCurso).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }
}
