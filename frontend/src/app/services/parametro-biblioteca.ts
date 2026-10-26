import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ParametroBiblioteca {
  private api = 'http://localhost:8080/backend/api/v1/parametros-biblioteca';

  constructor(private http: HttpClient) {}

  obtener() {
    return this.http.get<any>(this.api);
  }

  guardar(datos: { diasPrestamo: number; multaPorDia: number }) {
    return this.http.put<{ mensaje: string; diasPrestamo: number; multaPorDia: number }>(this.api, datos);
  }
}