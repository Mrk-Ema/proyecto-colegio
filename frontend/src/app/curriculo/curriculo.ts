import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Anio } from '../services/anio';
import { Curso } from '../services/curso';
import { Curriculo } from '../services/curriculo';

@Component({
  selector: 'colegio-curriculo',
  imports: [FormsModule],
  templateUrl: './curriculo.html',
  styleUrl: './curriculo.css',
})
export class CurriculoComp implements OnInit {
  grados: any[] = [];
  anios: any[] = [];
  cursos: any[] = [];
  idGrado: number | null = null;
  contexto: string = 'defecto';
  marcados = new Set<number>();
  existe: boolean = false;
  soloLectura: boolean = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Curriculo,
    private apiAnio: Anio,
    private apiCurso: Curso,
  ) {}

  ngOnInit() {
    this.apiAnio.listarGrados().subscribe({ next: (g) => (this.grados = g) });
    this.apiAnio.listar().subscribe({ next: (a) => (this.anios = a) });
    this.apiCurso.listar(1, 100, '', 'Activo').subscribe({
      next: (r) => {
        const v = new Map<number, any>();
        for (const c of r.datos) {
          if (!v.has(c.idCurso)) {
            v.set(c.idCurso, c);
          }
        }
        this.cursos = [...v.values()].sort((a, b) => a.nombre.localeCompare(b.nombre));
      },
    });
  }

  esCerrado(): boolean {
    const a = this.anios.find((x) => String(x.anio) === this.contexto);
    return !!a && a.estado === 'Cerrado';
  }

  esDiversificado(nivel: string): boolean {
    return (nivel ?? '').toLowerCase().includes('diversif');
  }

  gruposGrados(): any[] {
    const porNivel = new Map<string, any[]>();
    for (const g of this.grados) {
      if (!porNivel.has(g.nivel)) {
        porNivel.set(g.nivel, []);
      }
      porNivel.get(g.nivel)!.push(g);
    }
    const grupos: any[] = [];
    for (const [nivel, lista] of porNivel) {
      if (this.esDiversificado(nivel)) {
        const porCarrera = new Map<string, any[]>();
        for (const g of lista) {
          const c = g.carrera ?? 'Sin carrera';
          if (!porCarrera.has(c)) {
            porCarrera.set(c, []);
          }
          porCarrera.get(c)!.push(g);
        }
        for (const [carrera, gs] of porCarrera) {
          grupos.push({ etiqueta: `${nivel} — ${carrera}`, opciones: gs });
        }
      } else {
        grupos.push({ etiqueta: nivel, opciones: lista });
      }
    }
    return grupos;
  }

  cargar() {
    this.error = '';
    this.mensaje = '';
    this.marcados.clear();
    this.existe = false;
    if (this.idGrado == null) {
      return;
    }
    this.soloLectura = this.esCerrado();
    this.api.consultar(this.idGrado, this.contexto || 'defecto').subscribe({
      next: (r) => {
        (r.cursos ?? []).forEach((c: any) => this.marcados.add(c.idCurso));
        this.existe = true;
      },
      error: (e) => {
        if (e.status === 404) {
          this.existe = false;
          this.mensaje = 'Sin currículo: marca y guarda para crearlo.';
        } else {
          this.error = e.error?.error ?? 'No se pudo consultar';
        }
      },
    });
  }

  filas(): any[][] {
    const r: any[][] = [];
    for (let i = 0; i < this.cursos.length; i += 3) {
      r.push(this.cursos.slice(i, i + 3));
    }
    return r;
  }

  alternar(id: number) {
    this.marcados.has(id) ? this.marcados.delete(id) : this.marcados.add(id);
  }

  estaMarcado(id: number): boolean {
    return this.marcados.has(id);
  }

  guardar() {
    this.error = '';
    const ids = [...this.marcados];
    if (this.idGrado == null) {
      this.error = 'Seleccione grado';
      return;
    }
    if (!ids.length) {
      this.error = 'Marque al menos un curso';
      return;
    }
    const datos = { idGrado: this.idGrado, contexto: this.contexto || 'defecto', cursos: ids };
    const op = this.existe ? this.api.modificar(datos) : this.api.crear(datos);
    op.subscribe({
      next: () => {
        this.mensaje = this.existe ? 'Currículo modificado' : 'Currículo creado';
        this.cargar();
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo guardar'),
    });
  }
}
