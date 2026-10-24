import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Nota {
  private api = 'http://localhost:8080/backend/api/v1/maestro';

  constructor(private http: HttpClient) {}

  /** Matriz de notas de un curso: actividades, estudiantes y totales. */
  listar(idAsignacion: number) {
    const p = new HttpParams().set('idAsignacion', idAsignacion);
    return this.http.get<any>(`${this.api}/notas`, { params: p });
  }

  /** Ingresa notas para una actividad (batch). */
  ingresar(datos: {
    idAsignacion: number;
    idActividadEvaluable: number;
    notas: { idInscripcion: number; valor: number }[];
  }) {
    return this.http.post<{ mensaje: string; registrados: number }>(`${this.api}/notas`, datos);
  }

  /** Modifica una nota individual. */
  modificar(id: number, valor: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/notas/${id}`, { valor });
  }
}