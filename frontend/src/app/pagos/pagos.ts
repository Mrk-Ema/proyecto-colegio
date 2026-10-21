import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Pago } from '../services/pago';

@Component({
  selector: 'colegio-pagos',
  imports: [FormsModule, DecimalPipe, RouterLink],
  templateUrl: './pagos.html',
})
export class Pagos implements OnInit, OnDestroy {
  datos: any[] = [];
  error: string = '';
  aviso: string = '';
  /** Carnet o ID con el que se busca; es obligatorio para ver los pagos. */
  carnet: string = '';
  estudiante: any = null;
  filtroConcepto: string = '';
  filtroEstado: string = '';
  filtroDesde: string = '';
  filtroHasta: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;
  buscando = false;

  constructor(
    private api: Pago,
    private router: Router,
  ) {}

  ngOnInit() {
    const guardado = this.api.leerBusqueda();
    if (guardado) {
      this.carnet = guardado;
      this.buscar();
    }
  }

  /** Si se sale del módulo, el estudiante buscado se olvida. */
  ngOnDestroy() {
    this.api.olvidarSiSalioDe(this.router.url);
  }

  buscar() {
    if (!this.carnet.trim()) {
      this.error = 'Ingresa el carnet del estudiante';
      this.estudiante = null;
      this.datos = [];
      return;
    }
    this.error = '';
    this.buscando = true;
    this.api.contexto(this.carnet).subscribe({
      next: (r) => {
        this.buscando = false;
        this.estudiante = r.estudiante;
        this.api.guardarBusqueda(this.carnet);
        this.page = 1;
        this.cargar();
      },
      error: (e) => {
        this.buscando = false;
        this.estudiante = null;
        this.datos = [];
        this.error = e.error?.error ?? 'No se encontró el estudiante';
      },
    });
  }

  cargar() {
    this.api
      .listar(
        this.page,
        this.size,
        this.carnet,
        this.filtroConcepto,
        this.filtroEstado,
        this.filtroDesde,
        this.filtroHasta,
      )
      .subscribe({
        next: (r) => {
          this.datos = r.datos;
          this.total = r.total;
          this.totalPaginas = r.totalPaginas;
        },
        error: (e) => (this.error = e.error?.error ?? 'No se pudieron listar los pagos'),
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

  /** Solo la Secretaria registra y anula pagos. */
  esSecretaria(): boolean {
    return (localStorage.getItem('rol') ?? '') === 'Secretaria';
  }

  anular(f: any) {
    const motivo = prompt(
      `¿Anular el pago ${f.numeroCorrelativo} por ${f.monto}? Deja de contar en saldos, solvencias e ingresos. Escriba el motivo:`,
    );
    if (motivo === null) {
      return;
    }
    if (!motivo.trim()) {
      this.error = 'El motivo de la anulación es obligatorio';
      return;
    }
    this.error = '';
    this.api.anular(f.idPago, motivo.trim()).subscribe({
      next: (r) => {
        this.aviso = r.mensaje;
        this.cargar();
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo anular el pago'),
    });
  }

  nombreConcepto(c: string): string {
    return c === 'Inscripcion' ? 'Inscripción' : c === 'Mensualidad' ? 'Mensualidad' : 'Actividad';
  }
}
