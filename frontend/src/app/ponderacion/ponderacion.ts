import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Ponderacion } from '../services/ponderacion';
import { Asistencia } from '../services/asistencia';

interface FilaActividad {
  idActividadEvaluable: number | null;
  nombre: string;
  puntos: number | null;
}

@Component({
  selector: 'colegio-ponderacion',
  imports: [FormsModule, RouterLink],
  templateUrl: './ponderacion.html',
})
export class PonderacionComponent implements OnInit {
  idAsignacion: number = 0;
  modoModificar = false;
  actividades: FilaActividad[] = [];
  tieneNotas = false;
  total = 0;
  error: string = '';
  aviso: string = '';
  guardando = false;
  eliminando = false;
  cicloActivo = true;

  constructor(
    private api: Ponderacion,
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
      next: (r) => {
        if (r.length) {
          this.modoModificar = true;
          this.actividades = r.map((a: any) => ({
            idActividadEvaluable: a.idActividadEvaluable,
            nombre: a.nombre,
            puntos: a.puntos,
          }));
          this.tieneNotas = r.some((a: any) => a.tieneNotas);
        } else {
          this.modoModificar = false;
          this.actividades = [];
          this.tieneNotas = false;
        }
        this.calcularTotal();
      },
      error: (e) => {
        this.error = e.error?.error ?? 'No se pudo cargar la ponderación';
      },
    });
  }

  agregarFila() {
    this.actividades.push({ idActividadEvaluable: null, nombre: '', puntos: null });
  }

  quitarFila(i: number) {
    this.actividades.splice(i, 1);
    this.calcularTotal();
  }

  calcularTotal() {
    this.total = this.actividades.reduce((s, a) => s + (a.puntos ?? 0), 0);
  }

  /** Validaciones previas al envío. */
  validar(): string | null {
    if (!this.actividades.length) {
      return 'Agrega al menos una actividad';
    }
    const nombres = new Set<string>();
    for (const a of this.actividades) {
      if (!a.nombre.trim()) {
        return 'El nombre de la actividad es obligatorio';
      }
      if (nombres.has(a.nombre.trim().toLowerCase())) {
        return 'El nombre de la actividad no se repite';
      }
      nombres.add(a.nombre.trim().toLowerCase());
      if (a.puntos == null || a.puntos <= 0) {
        return 'Los puntos deben ser mayores a cero';
      }
    }
    if (this.total !== 100) {
      return 'El total debe ser exactamente 100';
    }
    return null;
  }

  guardar(confirmar = false) {
    this.error = '';
    this.aviso = '';
    const err = this.validar();
    if (err) {
      this.error = err;
      return;
    }
    this.guardando = true;
    const payload = {
      idAsignacion: this.idAsignacion,
      actividades: this.actividades.map((a) => ({
        idActividadEvaluable: a.idActividadEvaluable,
        nombre: a.nombre.trim(),
        puntos: a.puntos ?? 0,
      })),
      confirmar,
    };
    this.api.guardar(payload).subscribe({
      next: (r) => {
        this.guardando = false;
        this.aviso = r.mensaje;
        this.modoModificar = true;
        this.cargar();
      },
      error: (e) => {
        this.guardando = false;
        const msg = e.error?.error ?? 'No se pudo guardar la ponderación';
        // Si el backend pide confirmación del reescalado, preguntar
        if (msg.includes('se recalcularán') || msg.includes('Confirmar')) {
          if (confirm(msg)) {
            this.guardar(true);
          }
        } else {
          this.error = msg;
        }
      },
    });
  }

  eliminar() {
    if (!confirm('¿Eliminar la ponderación completa? El curso volverá a "Sin definir".')) {
      return;
    }
    this.error = '';
    this.aviso = '';
    this.eliminando = true;
    this.api.eliminar(this.idAsignacion).subscribe({
      next: (r) => {
        this.eliminando = false;
        this.aviso = r.mensaje;
        this.modoModificar = false;
        this.actividades = [];
        this.calcularTotal();
      },
      error: (e) => {
        this.eliminando = false;
        this.error = e.error?.error ?? 'No se pudo eliminar la ponderación';
      },
    });
  }

  irANotas() {
    this.router.navigate(['/notas', this.idAsignacion]);
  }
}
