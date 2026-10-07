import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Autenticacion {
  private api = 'http://localhost:8080/backend/api/v1/usuarios';

  constructor(private http: HttpClient) {}

  login(correo: string, password: string) {
    return this.http
      .post<{ token: string; rol: string }>(`${this.api}/login`, { correo, password })
      .pipe(
        tap((res) => {
          localStorage.setItem('token', res.token);
          localStorage.setItem('rol', res.rol);
        }),
      );
  }
}
