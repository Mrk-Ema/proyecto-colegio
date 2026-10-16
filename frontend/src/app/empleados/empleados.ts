import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Empleado } from '../services/empleado';

@Component({
  selector: 'colegio-empleados',
  imports: [FormsModule, RouterLink],
  templateUrl: './empleados.html',
})
export class Empleados implements OnInit {
  filas: any[] = [];
  error: string = '';
  mensaje: string = '';
  filtroNombre: string = '';
  filtroPuesto: string = '';
  filtroEstado: string = '';
  puestos: string[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Empleado) {}

  ngOnInit() {
    this.puestos = this.api.puestos(localStorage.getItem('rol') ?? '');
    this.cargar();
  }

  cargar() {
    this.api.listar(this.page, this.size, this.filtroNombre, this.filtroPuesto, this.filtroEstado).subscribe({
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

  desactivar(f: any) {
    if (!confirm(`¿Desactivar a ${f.nombres} ${f.apellidos}? Se bloquea su acceso.`)) return;
    this.error = '';
    this.api.desactivar(f.idEmpleado).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar'),
    });
  }

  activar(f: any) {
    if (!confirm(`¿Activar a ${f.nombres} ${f.apellidos}?`)) return;
    this.error = '';
    this.api.activar(f.idEmpleado).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }
}
