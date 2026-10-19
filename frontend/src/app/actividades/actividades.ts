import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Actividad } from '../services/actividad';

@Component({
  selector: 'colegio-actividades',
  imports: [FormsModule, DecimalPipe, RouterLink],
  templateUrl: './actividades.html',
})
export class Actividades implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroNombre: string = '';
  filtroEstado: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;
  /** Actividad cuyos grados están desplegados en una fila aparte. */
  abierto: number | null = null;

  constructor(private api: Actividad) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.listar(this.page, this.size, this.filtroNombre, this.filtroEstado).subscribe({
      next: (r) => {
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
      },
      error: () => (this.error = 'No se pudo listar las actividades'),
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

  /** Solo el Admin y el Super Admin gestionan actividades; la Secretaria solo consulta. */
  puedeEditar(): boolean {
    const rol = localStorage.getItem('rol') ?? '';
    return rol === 'Admin' || rol === 'Super Admin';
  }

  abrirGrados(f: any) {
    this.abierto = this.abierto === f.idActividad ? null : f.idActividad;
  }

  /** Nombres de los grados del alcance, agrupados por nivel. */
  detalleGrados(f: any): string {
    if (!f.grados?.length) {
      return '';
    }
    const grupos = new Map<string, string[]>();
    for (const g of f.grados) {
      const nivel = g.nivel ?? 'Grados';
      if (!grupos.has(nivel)) {
        grupos.set(nivel, []);
      }
      grupos.get(nivel)!.push(g.grado);
    }
    return [...grupos.entries()]
      .map(([nivel, grados]) => `${nivel}: ${grados.join(', ')}`)
      .join(' · ');
  }

  desactivar(f: any) {
    if (
      !confirm(
        `¿Desactivar "${f.nombre}"? Deja de ofrecerse al registrar pagos, pero los pagos ya realizados se conservan.`,
      )
    ) {
      return;
    }
    this.error = '';
    this.api.desactivar(f.idActividad).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar la actividad'),
    });
  }

  activar(f: any) {
    if (!confirm(`¿Activar "${f.nombre}"? Volverá a ofrecerse al registrar pagos.`)) {
      return;
    }
    this.error = '';
    this.api.activar(f.idActividad).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar la actividad'),
    });
  }
}