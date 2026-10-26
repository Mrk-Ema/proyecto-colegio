import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { EstudianteConsulta } from '../services/estudiante-consulta';

@Component({
  selector: 'colegio-mi-estado-cuenta',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './mi-estado-cuenta.html',
})
export class MiEstadoCuenta implements OnInit {
  anios: number[] = [];
  anio: number | null = null;
  resultado: any = null;
  error: string = '';
  buscando = false;

  constructor(private api: EstudianteConsulta) {}

  ngOnInit() {
    this.api.notas(null).subscribe({
      next: (r) => {
        this.anios = r.anios ?? [];
        this.anio = this.anios.length ? this.anios[0] : null;
        if (this.anio != null) {
          this.calcular();
        }
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudieron cargar los datos'),
    });
  }

  calcular() {
    if (this.anio == null) {
      this.error = 'Selecciona el año lectivo';
      return;
    }
    this.error = '';
    this.buscando = true;
    this.resultado = null;
    this.api.estadoCuenta(this.anio).subscribe({
      next: (r) => {
        this.buscando = false;
        this.resultado = r;
      },
      error: (e) => {
        this.buscando = false;
        this.resultado = null;
        this.error = e.error?.error ?? 'No se pudo calcular el estado de cuenta';
      },
    });
  }
}
