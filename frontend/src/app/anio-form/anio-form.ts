import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anio-form',
  imports: [FormsModule],
  templateUrl: './anio-form.html',
  styleUrl: './anio-form.css',
})
export class AnioForm implements OnInit {
  anio: number | null = null;
  fechaInicio: string = '';
  fechaCierre: string = '';
  grados: any[] = [];
  esEditar = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Anio,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('anio');
    this.api.listarGrados().subscribe({
      next: (g) => (this.grados = g.map((x: any) => ({ ...x, marcado: false }))),
    });
    if (p) {
      this.esEditar = true;
      this.anio = Number(p);
      this.api.obtener(this.anio).subscribe({
        next: (r) => {
          this.fechaInicio = r.fechaInicio ?? '';
          this.fechaCierre = r.fechaCierre ?? '';
        },
      });
    }
  }

  alternar(g: any) {
    g.marcado = !g.marcado;
  }

  minFecha(): string | null {
    return this.anio ? `${this.anio}-01-01` : null;
  }

  maxFecha(): string | null {
    return this.anio ? `${this.anio}-12-31` : null;
  }

  private fechasCoherentes(): boolean {
    if (!this.anio || !this.fechaInicio || !this.fechaCierre) return true;
    const prefijo = String(this.anio);
    return this.fechaInicio.startsWith(prefijo) && this.fechaCierre.startsWith(prefijo);
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.anio || !this.fechaInicio || !this.fechaCierre) {
      this.error = 'Completa año, fecha de inicio y fecha de cierre';
      return;
    }
    
    if (!this.fechasCoherentes()) {
      this.error = `Las fechas deben pertenecer al año ${this.anio}`;
      return;
    }
    if (!this.esEditar) {
      const ids = this.grados.filter((g) => g.marcado).map((g) => g.idGrado);
      if (!ids.length) {
        this.error = 'Marca al menos un grado';
        return;
      }
      this.api
        .crear({
          anio: this.anio!,
          fechaInicio: this.fechaInicio,
          fechaCierre: this.fechaCierre,
          grados: ids,
        })
        .subscribe({
          next: () => this.router.navigate(['/anios']),
          error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
        });
    } else {
      this.api
        .modificar(this.anio!, { fechaInicio: this.fechaInicio, fechaCierre: this.fechaCierre })
        .subscribe({
          next: () => this.router.navigate(['/anios', this.anio]),
          error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
        });
    }
  }
}
