import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anio-estructura',
  imports: [FormsModule],
  templateUrl: './anio-estructura.html',
  styleUrl: './anio-estructura.css',
})
export class AnioEstructura implements OnInit {
  anio!: number;
  fechaInicio: string = '';
  fechaCierre: string = '';
  grados: any[] = [];
  marcados = new Set<number>();
  mensaje: string = '';
  error: string = '';
  mensajeFechas: string = '';
  errorFechas: string = '';

  constructor(private api: Anio, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.anio = Number(this.route.snapshot.paramMap.get('anio'));
    this.api.listarGrados().subscribe({
      next: (g) => {
        this.grados = g;
        this.api.obtener(this.anio).subscribe({
          next: (d) => {
            this.fechaInicio = d.fechaInicio ?? '';
            this.fechaCierre = d.fechaCierre ?? '';
            (d.estructura ?? []).forEach((e: any) => this.marcados.add(e.idGrado));
          },
          error: () => (this.error = 'No se encontró el año'),
        });
      },
    });
  }

  alternar(id: number) {
    this.marcados.has(id) ? this.marcados.delete(id) : this.marcados.add(id);
  }

  estaMarcado(id: number): boolean {
    return this.marcados.has(id);
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
    const ids = [...this.marcados];
    if (!ids.length) {
      this.error = 'Marca al menos un grado';
      return;
    }
    this.api.actualizarEstructura(this.anio, ids).subscribe({
      next: () => this.router.navigate(['/anios', this.anio]),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo actualizar'),
    });
  }

  guardarFechas() {
    this.mensajeFechas = '';
    this.errorFechas = '';
    if (!this.fechaInicio || !this.fechaCierre) {
      this.errorFechas = 'Completa fecha de inicio y fecha de cierre';
      return;
    }
    const prefijo = String(this.anio);
    if (!this.fechaInicio.startsWith(prefijo) || !this.fechaCierre.startsWith(prefijo)) {
      this.errorFechas = `Las fechas deben pertenecer al año ${this.anio}`;
      return;
    }
    this.api.modificar(this.anio, { fechaInicio: this.fechaInicio, fechaCierre: this.fechaCierre }).subscribe({
      next: () => (this.mensajeFechas = 'Fechas actualizadas'),
      error: (e) => (this.errorFechas = e.error?.error ?? 'No se pudo actualizar'),
    });
  }
}
