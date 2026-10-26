import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EstudianteConsulta } from '../services/estudiante-consulta';

@Component({
  selector: 'colegio-mis-notas',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './mis-notas.html',
})
export class MisNotas implements OnInit {
  datos: any = null;
  error: string = '';
  anios: number[] = [];
  anio: number | null = null;
  curso: number | null = null;
  cursos: any[] = [];

  constructor(private api: EstudianteConsulta) {}

  ngOnInit() {
    this.api.notas(null).subscribe({
      next: (r) => {
        this.datos = r;
        this.anios = r.anios ?? [];
        this.anio = this.anios.length ? this.anios[0] : null;
        if (this.anio != null) {
          this.cargar();
        }
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudieron cargar las notas'),
    });
  }

  cargar() {
    if (this.anio == null) {
      return;
    }
    this.error = '';
    this.api.notas(this.anio, this.curso).subscribe({
      next: (r) => (this.datos = r),
      error: (e) => (this.error = e.error?.error ?? 'No se pudieron cargar las notas'),
    });
  }

  // No helper methods needed; the template accesses c.notas[activityId].valor directly.
}
