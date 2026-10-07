import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

interface OpcionMenu {
  ruta: string;
  texto: string;
  roles: string[];
}

@Component({
  selector: 'colegio-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  rol: string = '';
  menu: OpcionMenu[] = [
    { ruta: '/inicio', texto: 'Inicio', roles: ['Super Admin', 'Admin', 'Secretaria', 'Maestro', 'Bibliotecario', 'Estudiante'] },
    { ruta: '/usuarios', texto: 'Usuarios', roles: ['Super Admin', 'Admin'] },
    { ruta: '/perfil', texto: 'Mi perfil', roles: ['Super Admin', 'Admin', 'Secretaria', 'Maestro', 'Bibliotecario', 'Estudiante'] },
  ];

  constructor(private auth: Autenticacion, private router: Router) {
    this.rol = localStorage.getItem('rol') ?? '';
  }

  visible(op: OpcionMenu): boolean {
    return op.roles.includes(this.rol);
  }

  salir() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
