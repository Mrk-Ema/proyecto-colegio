import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

@Component({
  selector: 'colegio-cambio-password',
  imports: [FormsModule],
  templateUrl: './cambio-password.html',
  styleUrl: './cambio-password.css',
})
export class CambioPassword {
  actual: string = '';
  nueva: string = '';
  confirmar: string = '';
  mensaje: string = '';
  error: string = '';

  constructor(private auth: Autenticacion, private router: Router) {}

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (this.nueva !== this.confirmar) {
      this.error = 'La confirmación no coincide';
      return;
    }
    this.auth.cambiarPassword(this.actual, this.nueva).subscribe({
      next: () => {
        this.mensaje = 'Contraseña actualizada';
        this.actual = '';
        this.nueva = '';
        this.confirmar = '';
      },
      error: (e) => (this.error = e.error?.error ?? 'No se pudo actualizar'),
    });
  }

  volver() {
    this.router.navigate(['/perfil']);
  }
}
