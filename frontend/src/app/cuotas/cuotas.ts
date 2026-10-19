import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Cuota } from '../services/cuota';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-cuotas',
  imports: [FormsModule],
  templateUrl: './cuotas.html',
})
export class Cuotas implements OnInit {
  anios: any[] = [];
  anio: number | null = null;
  grados: any[] = [];
  /** Montos que se están editando, indexados por id de grado. */
  montos: Record<number, { inscripcion: number | null; mensualidad: number | null }> = {};
  error: string = '';
  aviso: string = '';
  guardando = false;

  constructor(
    private api: Cuota,
    private apiAnio: Anio,
  ) {}

  ngOnInit() {
    this.apiAnio.listar(1, 100).subscribe({
      next: (r) => {
        this.anios = r.datos;
        const disponible = this.anios.find((a: any) => a.estado !== 'Cerrado');
        this.anio = disponible?.anio ?? null;
        this.cargar();
      },
      error: () => (this.error = 'No se pudieron cargar los años lectivos'),
    });
  }

  cargar() {
    this.aviso = '';
    if (this.anio == null) {
      this.grados = [];
      return;
    }
    this.api.listar(this.anio).subscribe({
      next: (g) => {
        this.grados = g;
        this.montos = {};
        for (const fila of g) {
          this.montos[fila.idGrado] = {
            inscripcion: fila.montoInscripcion,
            mensualidad: fila.montoMensualidad,
          };
        }
      },
      error: (e) => {
        this.grados = [];
        this.error = e.error?.error ?? 'No se pudieron cargar las cuotas';
      },
    });
  }

  guardar() {
    this.error = '';
    this.aviso = '';
    const filas: any[] = [];
    for (const g of this.grados) {
      const m = this.montos[g.idGrado];
      if (m.inscripcion == null && m.mensualidad == null) {
        continue;
      }
      filas.push({
        idGrado: g.idGrado,
        montoInscripcion: m.inscripcion,
        montoMensualidad: m.mensualidad,
      });
    }
    if (!filas.length) {
      this.error = 'Escribe al menos un monto';
      return;
    }
    for (const f of filas) {
      if (f.montoInscripcion == null || f.montoMensualidad == null) {
        this.error = 'Completa el monto de inscripción y el de mensualidad de cada grado';
        return;
      }
    }
    this.guardando = true;
    this.api.guardar(this.anio!, filas).subscribe({
      next: (r) => {
        this.guardando = false;
        this.aviso = r.mensaje + ': ' + r.guardadas + (r.guardadas === 1 ? ' grado.' : ' grados.');
        this.cargar();
      },
      error: (e) => {
        this.guardando = false;
        this.error = e.error?.error ?? 'No se pudieron guardar las cuotas';
      },
    });
  }

  /** Revisa los montos escritos antes de enviar: no negativos y con dos decimales. */
  montosValidos(): boolean {
    for (const g of this.grados) {
      const m = this.montos[g.idGrado];
      for (const v of [m.inscripcion, m.mensualidad]) {
        if (v != null && (v < 0 || !/^\d+(\.\d{1,2})?$/.test(String(v)))) {
          return false;
        }
      }
    }
    return true;
  }
}