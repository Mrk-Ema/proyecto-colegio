import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Inicio } from './inicio/inicio';
import { Usuarios } from './usuarios/usuarios';


export const routes: Routes = [
  { path: '', component: Login },
  { path: 'inicio', component: Inicio },
  { path: 'usuarios', component: Usuarios },
  { path: '**', redirectTo: '' },
];
