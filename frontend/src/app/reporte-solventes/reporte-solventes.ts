import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reporte } from '../services/reporte';
import { Anio } from '../services/anio';
import { Grado } from '../services/grado';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-reporte-solventes',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './reporte-solventes.html',
})
export class ReporteSolventes implements OnInit {
  anios: any[] = [];
  anio: number | null = null;
  grados: any[] = [];
  secciones: any[] = [];
  grado: number | null = null;
  seccion: number | null = null;
  datos: any[] = [];
  error: string = '';
  aviso: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;
  mesesTranscurridos: number = 0;
  cargando = false;
  exportando = false;

  constructor(
    private api: Reporte,
    private apiAnio: Anio,
    private apiGrado: Grado,
    private apiSeccion: Seccion,
  ) {}

  ngOnInit() {
    this.apiAnio.listar(1, 100).subscribe({
      next: (r) => {
        this.anios = r.datos;
        const disponible = this.anios.find((a: any) => a.estado !== 'Cerrado');
        this.anio = disponible?.anio ?? (this.anios.length ? this.anios[0].anio : null);
        if (this.anio != null) {
          this.cargarGrados();
          this.cargar();
        }
      },
      error: () => (this.error = 'No se pudieron cargar los años lectivos'),
    });
  }

  cargarGrados() {
    this.apiGrado.listarActivos().subscribe({
      next: (g) => (this.grados = g),
      error: () => (this.error = 'No se pudieron cargar los grados'),
    });
  }

  cargarSecciones() {
    if (this.grado == null) {
      this.secciones = [];
      this.seccion = null;
      return;
    }
    this.apiSeccion.listar(1, 100, null, null, this.grado, '', 'Activo').subscribe({
      next: (r) => (this.secciones = r.datos),
      error: () => (this.error = 'No se pudieron cargar las secciones'),
    });
  }

  cargar() {
    if (this.anio == null) {
      return;
    }
    this.error = '';
    this.cargando = true;
    this.api.solventes(this.anio, this.grado, this.seccion, this.page, this.size).subscribe({
      next: (r) => {
        this.cargando = false;
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
        this.mesesTranscurridos = r.mesesTranscurridos;
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.error?.error ?? 'No se pudo generar el reporte';
      },
    });
  }

  filtrar() {
    this.page = 1;
    this.cargar();
  }

  limpiar() {
    this.grado = null;
    this.seccion = null;
    this.secciones = [];
    this.filtrar();
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

  exportarPdf() {
    if (this.anio == null) {
      return;
    }
    this.error = '';
    this.exportando = true;
    this.api.solventesPdf(this.anio, this.grado, this.seccion).subscribe({
      next: (blob) => {
        this.exportando = false;
        const url = URL.createObjectURL(blob);
        const ventana = window.open(url, '_blank');
        if (!ventana) {
          const a = document.createElement('a');
          a.href = url;
          a.download = `solventes-morosos-${this.anio}.pdf`;
          a.click();
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: (e) => {
        this.exportando = false;
        this.error = e.error?.error ?? 'No se pudo exportar el PDF';
      },
    });
  }
}
