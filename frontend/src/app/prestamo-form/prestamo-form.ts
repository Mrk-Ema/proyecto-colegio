import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Prestamo } from '../services/prestamo';
import { Libro } from '../services/libro';

@Component({
  selector: 'colegio-prestamo-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './prestamo-form.html',
})
export class PrestamoForm implements OnInit {
  libros: any[] = [];
  tipoPrestatario: string = 'estudiante';
  idPrestatario: number | null = null;
  idLibro: number | null = null;
  error: string = '';
  aviso: string = '';
  guardando = false;

  constructor(
    private api: Prestamo,
    private apiLibro: Libro,
    private router: Router,
  ) {}

  ngOnInit() {
    this.apiLibro.listar(1, 100, '', '', 'Activo').subscribe({
      next: (r) => (this.libros = r.datos),
      error: () => (this.error = 'No se pudieron cargar los libros'),
    });
  }

  guardar() {
    this.error = '';
    this.aviso = '';
    if (this.idLibro == null) {
      this.error = 'Selecciona el libro';
      return;
    }
    if (this.idPrestatario == null) {
      this.error = 'Ingresa el ID del prestatario';
      return;
    }
    this.guardando = true;
    this.api
      .registrar({
        idLibro: this.idLibro,
        tipoPrestatario: this.tipoPrestatario,
        idPrestatario: this.idPrestatario,
      })
      .subscribe({
        next: (r) => {
          this.guardando = false;
          this.aviso = r.mensaje + '. Fecha esperada: ' + r.fechaEsperada;
          setTimeout(() => this.router.navigate(['/prestamos']), 1200);
        },
        error: (e) => {
          this.guardando = false;
          this.error = e.error?.error ?? 'No se pudo registrar el préstamo';
        },
      });
  }
}
