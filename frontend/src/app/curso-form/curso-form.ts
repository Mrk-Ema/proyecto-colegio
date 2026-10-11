import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';
import { Curso } from '../services/curso';

@Component({
  selector: 'colegio-curso-form',
  imports: [FormsModule],
  templateUrl: './curso-form.html',
  styleUrl: './curso-form.css',
})
export class CursoForm implements OnInit {
  id: number | null = null;
  nombre: string = '';
  grados: any[] = [];
  esEditar = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Curso,
    private apiAnio: Anio,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => (this.nombre = r.nombre ?? ''),
        error: () => (this.error = 'No se pudo cargar el curso'),
      });
    } else {
      this.apiAnio.listarGrados().subscribe({
        next: (g) => (this.grados = g.map((x: any) => ({ ...x, marcado: false }))),
      });
    }
  }

  alternar(g: any) {
    g.marcado = !g.marcado;
  }

  esDiversificado(nivel: string): boolean {
    return (nivel ?? '').toLowerCase().includes('diversif');
  }

  grupos(): any[] {
    const niveles: any[] = [];
    const porNivel = new Map<string, any[]>();
    for (const g of this.grados) {
      if (!porNivel.has(g.nivel)) {
        porNivel.set(g.nivel, []);
      }
      porNivel.get(g.nivel)!.push(g);
    }
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
          niveles.push({ titulo: `${nivel} — ${carrera}`, filas: this.trozos(gs, 3) });
        }
      } else {
        niveles.push({ titulo: nivel, filas: this.trozos(lista, 3) });
      }
    }
    return niveles;
  }

  private trozos<T>(arr: T[], n: number): T[][] {
    const r: T[][] = [];
    for (let i = 0; i < arr.length; i += n) {
      r.push(arr.slice(i, i + n));
    }
    return r;
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    if (!this.esEditar) {
      const ids = this.grados.filter((g) => g.marcado).map((g) => g.idGrado);
      this.api.crear({ nombre: this.nombre.trim(), grados: ids }).subscribe({
        next: () => this.router.navigate(['/cursos']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, { nombre: this.nombre.trim() }).subscribe({
        next: () => this.router.navigate(['/cursos']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
