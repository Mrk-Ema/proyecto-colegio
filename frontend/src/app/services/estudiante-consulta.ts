import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class EstudianteConsulta {
  private api = 'http://localhost:8080/backend/api/v1/estudiante';

  constructor(private http: HttpClient) {}

  /** Notas del estudiante logueado por año y curso. */
  notas(anio: number | null = null, curso: number | null = null) {
    let p = new HttpParams();
    if (anio != null) {
      p = p.set('anio', anio);
    }
    if (curso != null) {
      p = p.set('curso', curso);
    }
    return this.http.get<any>(this.api + '/notas', { params: p });
  }

  /** Estado de cuenta del estudiante logueado por año. */
  estadoCuenta(anio: number) {
    return this.http.get<any>(this.api + '/estado-cuenta', {
      params: new HttpParams().set('anio', anio),
    });
  }

  /** Préstamos del estudiante logueado. */
  prestamos(page = 1, size = 10, estado = '', desde = '', hasta = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (estado) {
      p = p.set('estado', estado);
    }
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get<Pagina<any>>(this.api + '/prestamos', { params: p });
  }

  /** Multas del estudiante logueado. */
  multas(page = 1, size = 10, estado = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (estado) {
      p = p.set('estado', estado);
    }
    return this.http.get<Pagina<any>>(this.api + '/multas', { params: p });
  }
}