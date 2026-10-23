import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Asistencia {
  private api = 'http://localhost:8080/backend/api/v1/maestro';

  constructor(private http: HttpClient) {}

  /** Años no Cerrados en los que el maestro tiene asignaciones. */
  anios() {
    return this.http.get<any[]>(`${this.api}/anios`);
  }

  /** Asignaciones del maestro en un año no Cerrado. */
  cursos(anio: number | null = null) {
    let p = new HttpParams();
    if (anio != null) {
      p = p.set('anio', anio);
    }
    return this.http.get<any[]>(`${this.api}/cursos`, { params: p });
  }

  /** Inscritos del curso (nómina paginada). */
  nomina(idAsignacion: number, page = 1, size = 10, nombre = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nombre) {
      p = p.set('nombre', nombre);
    }
    return this.http.get<Pagina<any>>(`${this.api}/cursos/${idAsignacion}/nomina`, { params: p });
  }

  /** Toma la asistencia del día para un curso. */
  tomarAsistencia(datos: {
    idAsignacion: number;
    fecha: string;
    asistencias: { idInscripcion: number; estado: string }[];
  }) {
    return this.http.post<{ mensaje: string; registrados: number }>(`${this.api}/asistencia`, datos);
  }

  /** Historial de asistencia con filtros. */
  listar(
    idAsignacion: number,
    page = 1,
    size = 10,
    desde = '',
    hasta = '',
    estado = '',
    estudiante = '',
  ) {
    let p = new HttpParams().set('idAsignacion', idAsignacion).set('page', page).set('size', size);
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    if (estudiante) {
      p = p.set('estudiante', estudiante);
    }
    return this.http.get<Pagina<any>>(`${this.api}/asistencia`, { params: p });
  }

  /** Corrige el estado de una asistencia. */
  corregir(id: number, estado: string) {
    return this.http.put<{ mensaje: string }>(`${this.api}/asistencia/${id}`, { estado });
  }
}