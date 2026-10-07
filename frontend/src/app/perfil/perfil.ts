import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

@Component({
  selector: 'colegio-perfil',
  imports: [RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  datos: any = null;
  error: string = '';

  constructor(private auth: Autenticacion) {}

  ngOnInit() {
    this.auth.obtenerPerfil().subscribe({
      next: (res) => (this.datos = res),
      error: () => (this.error = 'No se pudo cargar el perfil'),
    });
  }
}
