import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';
import { Curso } from '../services/curso';

@Component({
  selector: 'colegio-curso-form',
  imports: [FormsModule],
  templateUrl: './curso-form.html',
  styleUrl: './curso-form.css',
})
export class CursoForm implements OnInit {
  id: number | null = null;
  nombre: string = '';
  grados: any[] = [];
  esEditar = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Curso,
    private apiAnio: Anio,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => (this.nombre = r.nombre ?? ''),
        error: () => (this.error = 'No se pudo cargar el curso'),
      });
    } else {
      this.apiAnio.listarGrados().subscribe({
        next: (g) => (this.grados = g.map((x: any) => ({ ...x, marcado: false }))),
      });
    }
  }

  alternar(g: any) {
    g.marcado = !g.marcado;
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    if (!this.esEditar) {
      const ids = this.grados.filter((g) => g.marcado).map((g) => g.idGrado);
      this.api.crear({ nombre: this.nombre.trim(), grados: ids }).subscribe({
        next: () => this.router.navigate(['/cursos']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, { nombre: this.nombre.trim() }).subscribe({
        next: () => this.router.navigate(['/cursos']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
