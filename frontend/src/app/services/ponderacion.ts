import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Ponderacion {
  private api = 'http://localhost:8080/backend/api/v1/maestro/ponderacion';

  constructor(private http: HttpClient) {}

  /** Lista de actividades de la ponderación de un curso. */
  listar(idAsignacion: number) {
    const p = new HttpParams().set('idAsignacion', idAsignacion);
    return this.http.get<any[]>(this.api, { params: p });
  }

  /** Crea o modifica la ponderación. */
  guardar(datos: {
    idAsignacion: number;
    actividades: { idActividadEvaluable?: number | null; nombre: string; puntos: number }[];
    confirmar?: boolean;
  }) {
    return this.http.post<{ mensaje: string }>(this.api, datos);
  }

  /** Elimina la ponderación completa (solo si no hay notas). */
  eliminar(idAsignacion: number) {
    const p = new HttpParams().set('idAsignacion', idAsignacion);
    return this.http.delete<{ mensaje: string }>(this.api, { params: p });
  }
}