import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const autenticacionInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const esPublica = req.url.includes('/login') || req.url.includes('/recuperar');
  const clon =
    !esPublica && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  const router = inject(Router);
  return next(clon).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('rol');
        router.navigate(['/login']);
      }
      return throwError(() => e);
    }),
  );
};
