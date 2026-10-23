import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Asistencia } from '../services/asistencia';

@Component({
  selector: 'colegio-mis-cursos',
  imports: [FormsModule, RouterLink],
  templateUrl: './mis-cursos.html',
})
export class MisCursos implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroAnio: number | null = null;
  anios: any[] = [];
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: Asistencia) {}

  ngOnInit() {
    this.api.anios().subscribe({
      next: (r) => {
        this.anios = r;
        const disponible = this.anios.find((a: any) => a.estado !== 'Cerrado');
        this.filtroAnio = disponible?.anio ?? null;
        this.cargar();
      },
      error: () => (this.error = 'No se pudieron cargar los años lectivos'),
    });
  }

  cargar() {
    this.api.cursos(this.filtroAnio).subscribe({
      next: (r) => (this.datos = r),
      error: () => (this.error = 'No se pudieron cargar los cursos'),
    });
  }

  filtrar() {
    this.cargar();
  }
}
