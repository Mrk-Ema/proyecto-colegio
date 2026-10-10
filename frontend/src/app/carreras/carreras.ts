import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Carrera } from '../services/carrera';

@Component({
  selector: 'colegio-carreras',
  imports: [FormsModule, RouterLink],
  templateUrl: './carreras.html',
  styleUrl: './carreras.css',
})
export class Carreras implements OnInit {
  datos: any[] = [];
  error: string = '';
  nombre: string = '';
  estado: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Carrera) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.listar(this.page, this.size, this.nombre, this.estado).subscribe({
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

  desactivar(c: any) {
    if (!confirm(`¿Desactivar ${c.nombre}? No se crean grados nuevos ni se inscribe en los existentes.`)) {
      return;
    }
    this.error = '';
    this.api.desactivar(c.idCarrera).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo desactivar'),
    });
  }

  activar(c: any) {
    if (!confirm(`¿Activar ${c.nombre}?`)) {
      return;
    }
    this.error = '';
    this.api.activar(c.idCarrera).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }
}
