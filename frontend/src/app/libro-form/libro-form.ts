import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Libro } from '../services/libro';

@Component({
  selector: 'colegio-libro-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './libro-form.html',
})
export class LibroForm implements OnInit {
  esEditar = false;
  id: number | null = null;
  titulo: string = '';
  autor: string = '';
  editorial: string = '';
  cantidad: number | null = null;
  error: string = '';

  constructor(
    private api: Libro,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.esEditar = true;
      this.id = Number(param);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.titulo = r.titulo ?? '';
          this.autor = r.autor ?? '';
          this.editorial = r.editorial ?? '';
          this.cantidad = r.cantidadTotal ?? null;
        },
        error: () => (this.error = 'No se encontró el libro'),
      });
    }
  }

  guardar() {
    this.error = '';
    if (!this.titulo.trim()) {
      this.error = 'Título obligatorio';
      return;
    }
    if (this.cantidad == null || this.cantidad <= 0) {
      this.error = 'La cantidad debe ser mayor a cero';
      return;
    }
    const datos = {
      titulo: this.titulo.trim(),
      autor: this.autor.trim() || null,
      editorial: this.editorial.trim() || null,
      cantidad: this.cantidad,
    };
    if (this.esEditar) {
      this.api.modificar(this.id!, datos).subscribe({
        next: () => this.router.navigate(['/libros']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar el libro'),
      });
    } else {
      this.api.crear(datos).subscribe({
        next: () => this.router.navigate(['/libros']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo registrar el libro'),
      });
    }
  }
}
