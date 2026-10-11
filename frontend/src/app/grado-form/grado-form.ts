import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';
import { Carrera } from '../services/carrera';
import { Grado } from '../services/grado';

@Component({
  selector: 'colegio-grado-form',
  imports: [FormsModule],
  templateUrl: './grado-form.html',
  styleUrl: './grado-form.css',
})
export class GradoForm implements OnInit {
  id: number | null = null;
  nombre: string = '';
  idNivel: number | null = null;
  idCarrera: number | null = null;
  destinoTipo: string = '';
  destinoAnio: string = '';
  niveles: any[] = [];
  carreras: any[] = [];
  anios: any[] = [];
  esEditar = false;
  bloqueado = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Grado,
    private apiAnio: Anio,
    private apiCarrera: Carrera,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.api.listarNiveles().subscribe({ next: (n) => (this.niveles = n) });
    this.apiCarrera.listar(1, 100, '', 'Activo').subscribe({ next: (r) => (this.carreras = r.datos) });
    this.apiAnio.listar().subscribe({ next: (a) => (this.anios = a) });
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.nombre = r.nombre ?? '';
          this.idNivel = r.idNivel ?? null;
          this.idCarrera = r.idCarrera ?? null;
          this.bloqueado = true;
        },
        error: () => (this.error = 'No se pudo cargar el grado'),
      });
    }
  }

  esDiversificado(): boolean {
    const n = this.niveles.find((x) => x.idNivel === this.idNivel);
    return !!n && n.nombre.toLowerCase().includes('diversif');
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    if (this.idNivel == null) {
      this.error = 'Nivel obligatorio';
      return;
    }
    let carrera = this.esDiversificado() ? this.idCarrera : null;
    if (!this.esEditar) {
      let destino: string | null = null;
      if (this.destinoTipo === 'defecto') {
        destino = 'defecto';
      } else if (this.destinoTipo === 'anio' && this.destinoAnio) {
        destino = this.destinoAnio;
      }
      this.api.crear({ nombre: this.nombre.trim(), idNivel: this.idNivel, idCarrera: carrera, destinoAnio: destino }).subscribe({
        next: () => this.router.navigate(['/grados']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, { nombre: this.nombre.trim(), idNivel: this.idNivel, idCarrera: carrera }).subscribe({
        next: () => this.router.navigate(['/grados']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
