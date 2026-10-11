import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Grado } from '../services/grado';
import { Seccion } from '../services/seccion';

@Component({
  selector: 'colegio-seccion-form',
  imports: [FormsModule],
  templateUrl: './seccion-form.html',
  styleUrl: './seccion-form.css',
})
export class SeccionForm implements OnInit {
  id: number | null = null;
  nombre: string = '';
  idGrado: number | null = null;
  grados: any[] = [];
  esEditar = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Seccion,
    private apiGrado: Grado,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.apiGrado.listarActivos().subscribe({ next: (g) => (this.grados = g) });
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.nombre = r.nombre ?? '';
          this.idGrado = r.idGrado ?? null;
        },
        error: () => (this.error = 'No se pudo cargar la sección'),
      });
    }
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

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }    if (!this.esEditar) {
      if (this.idGrado == null) {
        this.error = 'Grado obligatorio';
        return;
      }
      this.api.crear({ nombre: this.nombre.trim(), idGrado: this.idGrado }).subscribe({
        next: () => this.router.navigate(['/secciones']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, { nombre: this.nombre.trim() }).subscribe({
        next: () => this.router.navigate(['/secciones']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
