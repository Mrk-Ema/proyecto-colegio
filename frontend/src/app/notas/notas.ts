import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Nota } from '../services/nota';
import { Asistencia } from '../services/asistencia';

@Component({
  selector: 'colegio-notas',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './notas.html',
})
export class Notas implements OnInit {
  idAsignacion: number = 0;
  error: string = '';
  aviso: string = '';
  datos: any = null;
  /** Actividad seleccionada para ingresar notas. */
  actividadSeleccionada: number | null = null;

  /** Busca la actividad seleccionada en los datos cargados. */
  actividadActual(): any {
    return this.datos?.actividades?.find((a: any) => a.idActividadEvaluable === this.actividadSeleccionada);
  }
  /** Notas que se están editando, indexadas por id de inscripción. */
  notasEdicion: Record<number, number | null> = {};
  /** Nota individual que se está modificando: idNota. */
  modificandoId: number | null = null;
  nuevoValor: number | null = null;
  guardando = false;

  constructor(
    private api: Nota,
    private apiAsistencia: Asistencia,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.idAsignacion = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  cargar() {
    this.error = '';
    this.api.listar(this.idAsignacion).subscribe({
      next: (r) => (this.datos = r),
      error: (e) => (this.error = e.error?.error ?? 'No se pudieron cargar las notas'),
    });
  }

  /** Valor de la nota de un estudiante en una actividad. */
  valorNota(e: any, idActividad: number): number | null {
    return e.notas?.[idActividad]?.valor ?? null;
  }

  /** idNota de una nota individual (para modificar). */
  idNota(e: any, idActividad: number): number | null {
    return e.notas?.[idActividad]?.idNota ?? null;
  }

  /** Total acumulado de un estudiante. */
  totalEstudiante(e: any): number {
    return e.total ?? 0;
  }

  /** Prepara la edición de notas para una actividad. */
  prepararIngreso(idActividad: number) {
    this.actividadSeleccionada = idActividad;
    this.notasEdicion = {};
    for (const e of this.datos.estudiantes) {
      const nota = e.notas?.[idActividad];
      this.notasEdicion[e.idInscripcion] = nota?.valor ?? null;
    }
  }

  cancelarIngreso() {
    this.actividadSeleccionada = null;
    this.notasEdicion = {};
  }

  /** Ingresa notas para la actividad seleccionada. */
  ingresarNotas() {
    if (this.actividadSeleccionada == null) {
      this.error = 'Selecciona la actividad';
      return;
    }
    const actividad = this.datos.actividades.find(
      (a: any) => a.idActividadEvaluable === this.actividadSeleccionada,
    );
    if (!actividad) {
      this.error = 'Actividad no encontrada';
      return;
    }
    const notas: { idInscripcion: number; valor: number }[] = [];
    for (const e of this.datos.estudiantes) {
      const v = this.notasEdicion[e.idInscripcion];
      if (v != null) {
        notas.push({ idInscripcion: e.idInscripcion, valor: v });
      }
    }
    if (!notas.length) {
      this.error = 'Ingresa al menos una nota';
      return;
    }
    this.error = '';
    this.guardando = true;
    this.api
      .ingresar({ idAsignacion: this.idAsignacion, idActividadEvaluable: this.actividadSeleccionada, notas })
      .subscribe({
        next: (r) => {
          this.guardando = false;
          this.aviso = r.mensaje + ': ' + r.registrados + ' notas.';
          this.actividadSeleccionada = null;
          this.notasEdicion = {};
          this.cargar();
        },
        error: (e) => {
          this.guardando = false;
          this.error = e.error?.error ?? 'No se pudieron registrar las notas';
        },
      });
  }

  /** Inicia la modificación de una nota individual. */
  iniciarModificacion(idNota: number, valorActual: number | null) {
    this.modificandoId = idNota;
    this.nuevoValor = valorActual;
  }

  /** Guarda la nota modificada. */
  guardarModificacion() {
    if (this.modificandoId == null || this.nuevoValor == null) {
      return;
    }
    this.error = '';
    this.guardando = true;
    this.api.modificar(this.modificandoId, this.nuevoValor).subscribe({
      next: (r) => {
        this.guardando = false;
        this.aviso = r.mensaje;
        this.modificandoId = null;
        this.nuevoValor = null;
        this.cargar();
      },
      error: (e) => {
        this.guardando = false;
        this.error = e.error?.error ?? 'No se pudo modificar la nota';
      },
    });
  }

  cancelarModificacion() {
    this.modificandoId = null;
    this.nuevoValor = null;
  }

  irAPonderacion() {
    this.router.navigate(['/ponderacion', this.idAsignacion]);
  }
}
