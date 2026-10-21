import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Pago } from '../services/pago';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto',
  'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

@Component({
  selector: 'colegio-pago-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './pago-form.html',
})
export class PagoForm implements OnInit, OnDestroy {
  meses = MESES;
  /** true cuando el pago es de una actividad extracurricular. */
  esActividad = false;
  carnet: string = '';
  anios: number[] = [];
  anio: number | null = null;
  concepto: string = 'Inscripcion';
  mes: string = '';
  idActividad: number | null = null;
  actividades: any[] = [];
  estudiante: any = null;
  inscripcion: any = null;
  cuota: any = null;
  error: string = '';
  aviso: string = '';
  guardando = false;

  constructor(
    private api: Pago,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.esActividad = this.route.snapshot.url.join('/').includes('actividad');
    this.carnet = this.api.leerBusqueda() ?? '';
    this.cargar();
  }

  /** Si se sale del módulo, el estudiante buscado se olvida. */
  ngOnDestroy() {
    this.api.olvidarSiSalioDe(this.router.url);
  }

  cargar() {
    if (!this.carnet.trim()) {
      this.error = 'Ingresa el carnet del estudiante';
      return;
    }
    this.error = '';
    this.api.contexto(this.carnet).subscribe({
      next: (r) => {
        this.estudiante = r.estudiante;
        this.anios = r.anios;
        this.anio = r.anio;
        this.aplicarContexto(r);
      },
      error: (e) => (this.error = e.error?.error ?? 'No se encontró el estudiante'),
    });
  }

  /** Al cambiar de año se vuelve a pedir el contexto para refrescar cuota y actividades. */
  cambiarAnio() {
    this.error = '';
    this.api.contexto(this.carnet, this.anio).subscribe({
      next: (r) => this.aplicarContexto(r),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo consultar el año'),
    });
  }

  aplicarContexto(r: any) {
    this.inscripcion = r.inscripcion;
    this.cuota = r.cuota;
    this.actividades = r.actividades;
    this.idActividad = null;
    this.mes = this.mesActual();
  }

  mesActual(): string {
    return MESES[new Date().getMonth()];
  }

  /** El monto nunca se escribe: sale de la cuota del grado. */
  monto(): number | null {
    if (this.esActividad) {
      const a = this.actividades.find((x: any) => x.idActividad === this.idActividad);
      return a ? a.costo : null;
    }
    if (!this.cuota) {
      return null;
    }
    return this.concepto === 'Mensualidad' ? this.cuota.montoMensualidad : this.cuota.montoInscripcion;
  }

  guardar() {
    this.error = '';
    this.aviso = '';
    if (!this.carnet.trim()) {
      this.error = 'Ingresa el carnet del estudiante';
      return;
    }
    if (this.anio == null) {
      this.error = 'Selecciona el año lectivo';
      return;
    }
    if (this.esActividad) {
      if (this.idActividad == null) {
        this.error = 'Selecciona la actividad';
        return;
      }
      this.guardando = true;
      this.api
        .registrarActividad({
          carnet: this.carnet.trim(),
          anio: this.anio,
          idActividad: this.idActividad,
        })
        .subscribe({
          next: (r) => this.terminar(r),
          error: (e) => {
            this.guardando = false;
            this.error = e.error?.error ?? 'No se pudo registrar el pago';
          },
        });
      return;
    }
    if (this.concepto === 'Mensualidad' && !this.mes) {
      this.error = 'Selecciona el mes que se está pagando';
      return;
    }
    this.guardando = true;
    this.api
      .registrar({
        carnet: this.carnet.trim(),
        anio: this.anio,
        concepto: this.concepto,
        mes: this.concepto === 'Mensualidad' ? this.mes : null,
      })
      .subscribe({
        next: (r) => this.terminar(r),
        error: (e) => {
          this.guardando = false;
          this.error = e.error?.error ?? 'No se pudo registrar el pago';
        },
      });
  }

  terminar(r: any) {
    this.guardando = false;
    this.aviso = `${r.mensaje}: ${r.numeroCorrelativo}`;
    this.api.guardarBusqueda(this.carnet);
    setTimeout(() => this.router.navigate(['/pagos']), 900);
  }
}
