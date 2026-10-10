import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Carrera } from '../services/carrera';

@Component({
  selector: 'colegio-carrera-form',
  imports: [FormsModule],
  templateUrl: './carrera-form.html',
  styleUrl: './carrera-form.css',
})
export class CarreraForm implements OnInit {
  id: number | null = null;
  nombre: string = '';
  descripcion: string = '';
  duracionAnios: number | null = null;
  esEditar = false;
  mensaje: string = '';
  error: string = '';

  constructor(
    private api: Carrera,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.nombre = r.nombre ?? '';
          this.descripcion = r.descripcion ?? '';
          this.duracionAnios = r.duracionAnios ?? null;
        },
        error: () => (this.error = 'No se pudo cargar la carrera'),
      });
    }
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombre.trim()) {
      this.error = 'Nombre obligatorio';
      return;
    }
    const datos = {
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim(),
      duracionAnios: this.duracionAnios,
    };
    if (!this.esEditar) {
      this.api.crear(datos).subscribe({
        next: () => this.router.navigate(['/carreras']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, datos).subscribe({
        next: () => this.router.navigate(['/carreras']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
