import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

@Component({
  selector: 'colegio-recuperar',
  imports: [FormsModule, RouterLink],
  templateUrl: './recuperar.html',
  styleUrl: './recuperar.css',
})
export class Recuperar {
  correo: string = '';
  fechaNacimiento: string = '';
  nueva: string = '';
  confirmar: string = '';
  mensaje: string = '';
  error: string = '';

  constructor(private auth: Autenticacion) {}

  enviar() {
    this.mensaje = '';
    this.error = '';
    if (this.nueva !== this.confirmar) {
      this.error = 'La confirmación no coincide';
      return;
    }
    this.auth.recuperar(this.correo, this.fechaNacimiento, this.nueva).subscribe({
      next: (res) => {
        this.mensaje = res.mensaje;
        this.correo = '';
        this.fechaNacimiento = '';
        this.nueva = '';
        this.confirmar = '';
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo completar la restauracion'),
    });
  }
}
