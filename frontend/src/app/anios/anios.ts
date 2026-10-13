import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anios',
  imports: [FormsModule, RouterLink],
  templateUrl: './anios.html',
  styleUrl: './anios.css',
})
export class Anios implements OnInit {
  anios: any[] = [];
  error: string = '';
  mensaje: string = '';
  filtroAnio: number | null = null;
  filtroEstado: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Anio) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.listar(this.page, this.size, this.filtroAnio, this.filtroEstado).subscribe({
      next: (r) => {
        this.anios = r.datos;
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

  activar(a: any) {
    if (!confirm(`¿Activar el año ${a.anio}? El año Activo actual deberá estar cerrado.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.activar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }

  cerrar(a: any) {
    if (!confirm(`¿Cerrar el año ${a.anio}? No se podrá reabrir ni modificar.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.cerrar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo cerrar'),
    });
  }

  eliminar(a: any) {
    if (!confirm(`¿Eliminar el año ${a.anio}? Se borra con su estructura. No se puede deshacer.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.eliminar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo eliminar'),
    });
  }
}
