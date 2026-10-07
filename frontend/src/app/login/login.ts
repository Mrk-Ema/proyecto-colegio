import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

@Component({
  selector: 'colegio-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  correo: string = '';
  password: string = '';
  error: string = '';

  constructor(
    private auth: Autenticacion,
    private router: Router,
  ) {}

  ingresar() {
    this.error = '';
    this.auth.login(this.correo, this.password).subscribe({
      next: (res) => this.router.navigate([this.rutaPorRol(res.rol)]),
      error: () => (this.error = 'Credenciales inválidas'),
    });
  }

  private rutaPorRol(rol: string): string {
    if (rol === 'Super Admin' || rol === 'Admin') return '/usuarios';
    return '/inicio';
  }
}
