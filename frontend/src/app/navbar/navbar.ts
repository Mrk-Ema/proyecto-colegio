import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Autenticacion } from '../services/autenticacion';

interface OpcionMenu {
  ruta: string;
  texto: string;
  roles: string[];
}

interface GrupoMenu {
  etiqueta: string;
  items: OpcionMenu[];
}

@Component({
  selector: 'colegio-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  rol: string = '';
  menu: GrupoMenu[] = [
    { etiqueta: '', items: [{ ruta: '/inicio', texto: 'Inicio', roles: ['Super Admin', 'Admin', 'Secretaria', 'Maestro', 'Bibliotecario', 'Estudiante'] }] },
    { etiqueta: '', items: [{ ruta: '/anios', texto: 'Años lectivos', roles: ['Super Admin'] }] },
    { etiqueta: 'Catálogos', items: [
      { ruta: '/carreras', texto: 'Carreras', roles: ['Super Admin', 'Admin'] },
      { ruta: '/cursos', texto: 'Cursos', roles: ['Super Admin', 'Admin'] },
      { ruta: '/grados', texto: 'Grados', roles: ['Super Admin', 'Admin'] },
      { ruta: '/secciones', texto: 'Secciones', roles: ['Super Admin', 'Admin'] },
      { ruta: '/curriculo', texto: 'Currículo', roles: ['Super Admin', 'Admin'] },
      { ruta: '/asignaciones', texto: 'Asignaciones', roles: ['Super Admin', 'Admin'] },
    ]},
    { etiqueta: 'Personas', items: [
      { ruta: '/estudiantes', texto: 'Estudiantes', roles: ['Super Admin', 'Admin', 'Secretaria'] },
      { ruta: '/empleados', texto: 'Empleados', roles: ['Super Admin', 'Admin'] },
      { ruta: '/inscripciones', texto: 'Inscripciones', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    ]},
    { etiqueta: 'Finanzas', items: [
      { ruta: '/cuotas', texto: 'Cuotas', roles: ['Super Admin', 'Admin'] },
      { ruta: '/actividades', texto: 'Actividades', roles: ['Super Admin', 'Admin', 'Secretaria'] },
      { ruta: '/pagos', texto: 'Pagos', roles: ['Super Admin', 'Admin', 'Secretaria'] },
      { ruta: '/solvencias', texto: 'Solvencias', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/reporte-solventes', texto: 'Reporte solventes', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/reporte-rendimiento', texto: 'Reporte rendimiento', roles: ['Super Admin', 'Admin'] },
    { ruta: '/reporte-historial-pagos', texto: 'Historial de pagos', roles: ['Super Admin', 'Admin', 'Secretaria'] },
    { ruta: '/reporte-ingresos', texto: 'Reporte de ingresos', roles: ['Super Admin', 'Admin'] },
    { ruta: '/reporte-prestamos-mora', texto: 'Préstamos en mora', roles: ['Super Admin', 'Admin', 'Bibliotecario'] },
    ]},
    { etiqueta: 'Biblioteca', items: [
      { ruta: '/libros', texto: 'Libros', roles: ['Bibliotecario', 'Estudiante'] },
      { ruta: '/parametros-biblioteca', texto: 'Parámetros', roles: ['Bibliotecario'] },
      { ruta: '/prestamos', texto: 'Préstamos', roles: ['Bibliotecario'] },
      { ruta: '/multas', texto: 'Multas', roles: ['Bibliotecario'] },
    ]},
    { etiqueta: 'Mis datos', items: [
      { ruta: '/mis-notas', texto: 'Mis notas', roles: ['Estudiante'] },
      { ruta: '/mi-estado-cuenta', texto: 'Mi estado de cuenta', roles: ['Estudiante'] },
      { ruta: '/mis-prestamos', texto: 'Mis préstamos', roles: ['Estudiante'] },
      { ruta: '/mis-multas', texto: 'Mis multas', roles: ['Estudiante'] },
      { ruta: '/mis-cursos', texto: 'Mis cursos', roles: ['Maestro'] },
    ]},
    { etiqueta: '', items: [{ ruta: '/perfil', texto: 'Mi perfil', roles: ['Super Admin', 'Admin', 'Secretaria', 'Maestro', 'Bibliotecario', 'Estudiante'] }] },
  ];

  constructor(private auth: Autenticacion, private router: Router) {
    this.rol = localStorage.getItem('rol') ?? '';
  }

  /** Items del grupo que son visibles para el rol actual. */
  itemsVisibles(items: OpcionMenu[]): OpcionMenu[] {
    return items.filter((op) => op.roles.includes(this.rol));
  }

  /** Un grupo se muestra solo si tiene al menos un item visible. */
  grupoVisible(grupo: GrupoMenu): boolean {
    return this.itemsVisibles(grupo.items).length > 0;
  }

  /** Un grupo con 2+ items visibles se renderiza como dropdown. */
  esDropdown(grupo: GrupoMenu): boolean {
    return this.itemsVisibles(grupo.items).length >= 2;
  }

  /** El primer (y único) item visible de un grupo simple. */
  primerVisible(grupo: GrupoMenu): OpcionMenu {
    return this.itemsVisibles(grupo.items)[0];
  }

  visible(op: OpcionMenu): boolean {
    return op.roles.includes(this.rol);
  }

  salir() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}