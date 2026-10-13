import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Anio } from '../services/anio';
import { Asignacion } from '../services/asignacion';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-asignacion-form',
  imports: [FormsModule],
  templateUrl: './asignacion-form.html',
  styleUrl: './asignacion-form.css',
})
export class AsignacionForm implements OnInit {
  anio: number | null = null;
  idGrado: number | null = null;
  idSeccion: number | null = null;
  idCurso: number | null = null;
  idEmpleado: number | null = null;
  anios: any[] = [];
  grados: any[] = [];
  secciones: any[] = [];
  pendientes: any[] = [];
  maestros: any[] = [];
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Asignacion,
    private apiAnio: Anio,
    private apiSec: Seccion,
    private router: Router,
  ) {}

  ngOnInit() {
    this.apiAnio.listar(1, 100).subscribe({
      next: (r) => (this.anios = r.datos.filter((x: any) => x.estado !== 'Cerrado')),
    });
    this.apiAnio.listarGrados().subscribe({ next: (g) => (this.grados = g) });
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

  cambioGrado() {
    this.idSeccion = null;
    this.secciones = [];
    this.pendientes = [];
    this.idCurso = null;
    if (this.idGrado == null) {
      return;
    }
    this.apiSec.listar(1, 100, null, null, this.idGrado, '', 'Activo').subscribe({
      next: (r) => (this.secciones = r.datos),
      error: () => (this.error = 'No se pudo cargar secciones'),
    });
  }

  cargarPendientes() {
    this.error = '';
    this.pendientes = [];
    this.idCurso = null;
    if (this.anio == null || this.idSeccion == null) {
      return;
    }
    this.api.sinMaestro(this.anio, this.idSeccion).subscribe({
      next: (r) => (this.pendientes = r.datos ?? []),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo cargar pendientes'),
    });
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (this.anio == null || this.idSeccion == null || this.idCurso == null || this.idEmpleado == null) {
      this.error = 'Seleccione año, sección, curso y maestro';
      return;
    }
    this.api.crear({ anio: this.anio, idSeccion: this.idSeccion, idCurso: this.idCurso, idEmpleado: this.idEmpleado }).subscribe({
      next: () => this.router.navigate(['/asignaciones']),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
    });
  }
}
