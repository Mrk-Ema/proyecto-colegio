import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Actividad } from '../services/actividad';
import { Grado } from '../services/grado';

const ALCANCE_TODOS = 'Todo el colegio';
const ALCANCE_GRADOS = 'Grados especificos';

@Component({
  selector: 'colegio-actividad-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './actividad-form.html',
})
export class ActividadForm implements OnInit {
  esEditar = false;
  id: number | null = null;
  nombre: string = '';
  descripcion: string = '';
  costo: number | null = null;
  alcance: string = ALCANCE_TODOS;
  grados: any[] = [];
  seleccionados: number[] = [];
  error: string = '';

  constructor(
    private api: Actividad,
    private apiGrado: Grado,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.apiGrado.listarActivos().subscribe({
      next: (r: any[]) => (this.grados = r),
      error: () => (this.error = 'No se pudieron cargar los grados'),
    });
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.esEditar = true;
      this.id = Number(param);
      this.cargar(Number(param));
    }
  }

  cargar(id: number) {
    this.api.obtener(id).subscribe({
      next: (a) => {
        this.nombre = a.nombre ?? '';
        this.descripcion = a.descripcion ?? '';
        this.costo = a.costo;
        this.alcance = a.alcance;
        this.seleccionados = (a.grados ?? []).map((g: any) => g.idGrado);
      },
      error: (e) => (this.error = e.error?.error ?? 'No se encontró la actividad'),
    });
  }

  /** Lista de grados agrupada por nivel, como en las otras pantallas. */
  gruposGrados(): { etiqueta: string; opciones: any[] }[] {
    const grupos = new Map<string, any[]>();
    for (const g of this.grados) {
      const etiqueta = g.carrera ? `${g.nivel} — ${g.carrera}` : g.nivel;
      if (!grupos.has(etiqueta)) {
        grupos.set(etiqueta, []);
      }
      grupos.get(etiqueta)!.push(g);
    }
    return [...grupos.entries()].map(([etiqueta, opciones]) => ({ etiqueta, opciones }));
  }

  marcada(idGrado: number): boolean {
    return this.seleccionados.includes(idGrado);
  }

  alternar(idGrado: number, marcada: boolean) {
    if (marcada && !this.seleccionados.includes(idGrado)) {
      this.seleccionados.push(idGrado);
    } else if (!marcada) {
      this.seleccionados = this.seleccionados.filter((x) => x !== idGrado);
    }
  }

  guardar() {
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    if (this.costo == null || this.costo < 0) {
      this.error = 'Costo inválido (no puede ser negativo)';
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(String(this.costo))) {
      this.error = 'El costo admite solo dos decimales';
      return;
    }
    if (this.alcance === ALCANCE_GRADOS && !this.seleccionados.length) {
      this.error = 'Selecciona al menos un grado';
      return;
    }
    const datos = {
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim() || null,
      costo: this.costo,
      alcance: this.alcance,
      grados: this.alcance === ALCANCE_GRADOS ? this.seleccionados : [],
    };
    if (this.esEditar) {
      this.api.modificar(this.id!, datos).subscribe({
        next: () => this.router.navigate(['/actividades']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar la actividad'),
      });
    } else {
      this.api.crear(datos).subscribe({
        next: () => this.router.navigate(['/actividades']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear la actividad'),
      });
    }
  }
}