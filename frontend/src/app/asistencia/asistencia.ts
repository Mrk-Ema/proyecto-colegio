import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Asistencia } from '../services/asistencia';

@Component({
  selector: 'colegio-asistencia',
  imports: [FormsModule, RouterLink],
  templateUrl: './asistencia.html',
})
export class AsistenciaTomar implements OnInit {
  idAsignacion: number = 0;
  error: string = '';
  aviso: string = '';
  fecha: string = new Date().toISOString().slice(0, 10);
  datos: any[] = [];
  /** Estado de asistencia por estudiante. */
  estados: Record<number, string> = {};
  guardando = false;

  constructor(
    private api: Asistencia,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.idAsignacion = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarNomina();
  }

  cargarNomina() {
    this.api.nomina(this.idAsignacion, 1, 100).subscribe({
      next: (r) => {
        this.datos = r.datos;
        for (const e of r.datos) {
          this.estados[e.idInscripcion] = 'Presente';
        }
      },
      error: () => (this.error = 'No se pudo cargar la nómina'),
    });
  }

  guardar() {
    this.error = '';
    this.aviso = '';
    if (!this.datos.length) {
      this.error = 'No hay estudiantes en la nómina';
      return;
    }
    const asistencias = this.datos.map((e) => ({
      idInscripcion: e.idInscripcion,
      estado: this.estados[e.idInscripcion] ?? 'Presente',
    }));
    this.guardando = true;
    this.api
      .tomarAsistencia({ idAsignacion: this.idAsignacion, fecha: this.fecha, asistencias })
      .subscribe({
        next: (r) => {
          this.guardando = false;
          this.aviso = r.mensaje + ': ' + r.registrados + ' estudiantes.';
        },
        error: (e) => {
          this.guardando = false;
          this.error = e.error?.error ?? 'No se pudo registrar la asistencia';
        },
      });
  }
}
