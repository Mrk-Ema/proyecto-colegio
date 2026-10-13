import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Asignacion {
  private api = 'http://localhost:8080/backend/api/v1/asignaciones';

  constructor(private http: HttpClient) {}

  listar(
    page = 1,
    size = 10,
    anio: number | null = null,
    grado: number | null = null,
    seccion: number | null = null,
    curso: number | null = null,
    maestro = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (anio != null) {
      p = p.set('anio', anio);
    }
    if (grado != null) {
      p = p.set('grado', grado);
    }
    if (seccion != null) {
      p = p.set('seccion', seccion);
    }
    if (curso != null) {
      p = p.set('curso', curso);
    }
    if (maestro) {
      p = p.set('maestro', maestro);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  sinMaestro(anio: number, seccion: number) {
    const p = new HttpParams().set('anio', anio).set('seccion', seccion).set('sinMaestro', true);
    return this.http.get<{ datos: any[] }>(this.api, { params: p });
  }

  maestros() {
    return this.http.get<any[]>(`${this.api}/maestros`);
  }

  crear(datos: { anio: number; idSeccion: number; idCurso: number; idEmpleado: number }) {
    return this.http.post<{ mensaje: string }>(this.api, datos);
  }

  reasignar(id: number, datos: { idEmpleado: number }) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/reasignar`, datos);
  }

  quitar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/quitar`, {});
  }
}
