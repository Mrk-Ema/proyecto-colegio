import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Solvencia {
  private api = 'http://localhost:8080/backend/api/v1/solvencias';

  constructor(private http: HttpClient) {}

  /** Datos del estudiante y los años en los que está inscrito. */
  contexto(carnet: string) {
    return this.http.get<any>(`${this.api}/contexto`, {
      params: new HttpParams().set('carnet', carnet.trim()),
    });
  }

  /** Calcula el saldo y el detalle de un estudiante en un año. */
  calcular(carnet: string, anio: number) {
    const p = new HttpParams().set('carnet', carnet.trim()).set('anio', anio);
    return this.http.get<any>(this.api, { params: p });
  }

  /** Genera el PDF de la solvencia (solo si el saldo es cero). */
  pdf(carnet: string, anio: number) {
    const p = new HttpParams().set('carnet', carnet.trim()).set('anio', anio);
    return this.http.get(`${this.api}/pdf`, { params: p, responseType: 'blob' });
  }
}