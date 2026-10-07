import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const rol = localStorage.getItem('rol');
  const permitidos: string[] = route.data?.['roles'] ?? [];
  if (!token) return router.navigate(['/login']);
  if (permitidos.length && !permitidos.includes(rol ?? '')) return router.navigate(['/inicio']);
  return true;
};
