import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Asistencia } from '../services/asistencia';

@Component({
  selector: 'colegio-historial-asistencia',
  imports: [FormsModule, RouterLink],
  templateUrl: './historial-asistencia.html',
})
export class HistorialAsistencia implements OnInit {
  idAsignacion: number = 0;
  datos: any[] = [];
  error: string = '';
  filtroDesde: string = '';
  filtroHasta: string = '';
  filtroEstado: string = '';
  filtroEstudiante: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Asistencia,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.idAsignacion = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  cargar() {
    this.api
      .listar(
        this.idAsignacion,
        this.page,
        this.size,
        this.filtroDesde,
        this.filtroHasta,
        this.filtroEstado,
        this.filtroEstudiante,
      )
      .subscribe({
        next: (r) => {
          this.datos = r.datos;
          this.total = r.total;
          this.totalPaginas = r.totalPaginas;
        },
        error: () => (this.error = 'No se pudo cargar el historial'),
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

  corregir(f: any) {
    const nuevo = prompt(
      `Estado actual: ${f.estado}. Nuevo estado (Presente, Ausente, Tardío, Permiso):`,
      f.estado,
    );
    if (nuevo === null) {
      return;
    }
    const estado = nuevo.trim();
    if (
      estado !== 'Presente' &&
      estado !== 'Ausente' &&
      estado !== 'Tardío' &&
      estado !== 'Permiso'
    ) {
      this.error = 'Estado inválido';
      return;
    }
    this.error = '';
    this.api.corregir(f.idAsistencia, estado).subscribe({
      next: (r) => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo corregir la asistencia'),
    });
  }
}
