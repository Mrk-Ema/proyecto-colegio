import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Inicio } from './inicio/inicio';
import { Usuarios } from './usuarios/usuarios';
import { Layout } from './layout/layout';
import { Perfil } from './perfil/perfil';
import { PerfilEditar } from './perfil-editar/perfil-editar';
import { CambioPassword } from './cambio-password/cambio-password';
import { Recuperar } from './recuperar/recuperar';
import { Anios } from './anios/anios';
import { AnioForm } from './anio-form/anio-form';
import { AnioDetalle } from './anio-detalle/anio-detalle';
import { AnioEstructura } from './anio-estructura/anio-estructura';
import { Carreras } from './carreras/carreras';
import { CarreraForm } from './carrera-form/carrera-form';
import { Cursos } from './cursos/cursos';
import { CursoForm } from './curso-form/curso-form';
import { Grados } from './grados/grados';
import { GradoForm } from './grado-form/grado-form';
import { Secciones } from './secciones/secciones';
import { SeccionForm } from './seccion-form/seccion-form';
import { CurriculoComp } from './curriculo/curriculo';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', component: Login },
  { path: 'recuperar', component: Recuperar },
  { path: '', component: Layout,
    canActivate: [roleGuard],
    children: [
      { path: 'inicio', component: Inicio },
      { path: 'perfil', component: Perfil },
      { path: 'perfil/editar', component: PerfilEditar },
      { path: 'cambio-password', component: CambioPassword },
      { path: 'usuarios', component: Usuarios, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'anios', component: Anios, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'anios/nuevo', component: AnioForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'anios/:anio/editar', component: AnioForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'anios/:anio/estructura', component: AnioEstructura, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'anios/:anio', component: AnioDetalle, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'carreras', component: Carreras, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'carreras/nuevo', component: CarreraForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'carreras/:id/editar', component: CarreraForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'cursos', component: Cursos, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'cursos/nuevo', component: CursoForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'cursos/:id/editar', component: CursoForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'grados', component: Grados, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'grados/nuevo', component: GradoForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'grados/:id/editar', component: GradoForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'secciones', component: Secciones, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'secciones/nuevo', component: SeccionForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'secciones/:id/editar', component: SeccionForm, data: { roles: ['Super Admin', 'Admin'] } },
      { path: 'curriculo', component: CurriculoComp, data: { roles: ['Super Admin', 'Admin'] } },
    ],
  },
  { path: '**', redirectTo: '' },
];
