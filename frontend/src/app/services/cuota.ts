import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Cuota {
  private api = 'http://localhost:8080/backend/api/v1/cuotas';

  constructor(private http: HttpClient) {}

  /** Grados del año con los montos ya configurados (los vacíos vienen en null). */
  listar(anio: number) {
    return this.http.get<any[]>(this.api, { params: new HttpParams().set('anio', anio) });
  }

  guardar(anio: number, cuotas: any[]) {
    return this.http.put<{ mensaje: string; guardadas: number }>(this.api, { anio, cuotas });
  }
}