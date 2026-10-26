import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ParametroBiblioteca } from '../services/parametro-biblioteca';

@Component({
  selector: 'colegio-parametros-biblioteca',
  imports: [FormsModule, RouterLink],
  templateUrl: './parametros-biblioteca.html',
})
export class ParametrosBiblioteca implements OnInit {
  diasPrestamo: number | null = null;
  multaPorDia: number | null = null;
  error: string = '';
  aviso: string = '';
  guardando = false;

  constructor(private api: ParametroBiblioteca) {}

  ngOnInit() {
    this.api.obtener().subscribe({
      next: (r) => {
        this.diasPrestamo = r.diasPrestamo;
        this.multaPorDia = r.multaPorDia;
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudieron cargar los parámetros'),
    });
  }

  guardar() {
    this.error = '';
    this.aviso = '';
    if (this.diasPrestamo == null || this.diasPrestamo <= 0) {
      this.error = 'Los días de préstamo deben ser mayores a cero';
      return;
    }
    if (this.multaPorDia == null || this.multaPorDia < 0) {
      this.error = 'La multa por día no puede ser negativa';
      return;
    }
    this.guardando = true;
    this.api.guardar({ diasPrestamo: this.diasPrestamo, multaPorDia: this.multaPorDia }).subscribe({
      next: (r) => {
        this.guardando = false;
        this.aviso = r.mensaje;
        this.diasPrestamo = r.diasPrestamo;
        this.multaPorDia = r.multaPorDia;
      },
      error: (e) => {
        this.guardando = false;
        this.error = e.error?.error ?? 'No se pudieron guardar los parámetros';
      },
    });
  }
}
