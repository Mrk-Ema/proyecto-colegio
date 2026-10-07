import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

@Component({
  selector: 'colegio-perfil-editar',
  imports: [FormsModule],
  templateUrl: './perfil-editar.html',
  styleUrl: './perfil-editar.css',
})
export class PerfilEditar implements OnInit {
  correo: string = '';
  nombres: string = '';
  apellidos: string = '';
  telefono: string = '';
  direccion: string = '';
  mensaje: string = '';
  error: string = '';

  constructor(
    private auth: Autenticacion,
    private router: Router,
  ) {}

  ngOnInit() {
    this.auth.obtenerPerfil().subscribe({
      next: (res) => {
        this.correo = res.correo ?? '';
        this.nombres = res.nombres ?? '';
        this.apellidos = res.apellidos ?? '';
        this.telefono = res.telefono ?? '';
        this.direccion = res.direccion ?? '';
      },
      error: () => (this.error = 'No se pudo cargar el perfil'),
    });
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    const datos: any = {};
    if (this.correo) datos.correo = this.correo;
    if (this.nombres) datos.nombres = this.nombres;
    if (this.apellidos) datos.apellidos = this.apellidos;
    datos.telefono = this.telefono;
    datos.direccion = this.direccion;
    this.auth.actualizarPerfil(datos).subscribe({
      next: () => this.router.navigate(['/perfil']),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo actualizar'),
    });
  }

  soloNumeros(e: KeyboardEvent) {
    if (!/[0-9]/.test(e.key) && e.key !== 'Backspace') e.preventDefault();
  }
}
