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
    { ruta: '/anios', texto: 'Años lectivos', roles: ['Super Admin'] },
    { ruta: '/carreras', texto: 'Carreras', roles: ['Super Admin', 'Admin'] },
    { ruta: '/cursos', texto: 'Cursos', roles: ['Super Admin', 'Admin'] },
    { ruta: '/grados', texto: 'Grados', roles: ['Super Admin', 'Admin'] },
    { ruta: '/secciones', texto: 'Secciones', roles: ['Super Admin', 'Admin'] },
    { ruta: '/curriculo', texto: 'Currículo', roles: ['Super Admin', 'Admin'] },
    { ruta: '/asignaciones', texto: 'Asignaciones', roles: ['Super Admin', 'Admin'] },
    { ruta: '/estudiantes', texto: 'Estudiantes', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/empleados', texto: 'Empleados', roles: ['Super Admin', 'Admin'] },
    { ruta: '/inscripciones', texto: 'Inscripciones', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/cuotas', texto: 'Cuotas', roles: ['Super Admin', 'Admin'] },
    { ruta: '/actividades', texto: 'Actividades', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/pagos', texto: 'Pagos', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/solvencias', texto: 'Solvencias', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/mis-cursos', texto: 'Mis cursos', roles: ['Super Admin', 'Maestro'] },
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
