import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Inscripcion {
  private api = 'http://localhost:8080/backend/api/v1/inscripciones';

  constructor(private http: HttpClient) {}

  /** Años que admiten inscripción (los que no están Cerrados) y el que se propone por defecto. */
  anios() {
    return this.http.get<{ anios: any[]; porDefecto: number | null }>(`${this.api}/anios`);
  }

  /** Grados de la estructura del año, ya filtrados a los que se puede inscribir. */
  grados(anio: number) {
    return this.http.get<any[]>(`${this.api}/grados`, { params: new HttpParams().set('anio', anio) });
  }

  /** Secciones Activas de un grado. */
  secciones(grado: number) {
    return this.http.get<any[]>(`${this.api}/secciones`, {
      params: new HttpParams().set('grado', grado),
    });
  }

  /** Niveles para filtrar el listado. */
  niveles() {
    return this.http.get<any[]>(`${this.api}/niveles`);
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  listar(
    page = 1,
    size = 10,
    anio: number | null = null,
    nivel: number | null = null,
    grado: number | null = null,
    seccion: number | null = null,
    estudiante = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (anio != null) {
      p = p.set('anio', anio);
    }
    if (nivel != null) {
      p = p.set('nivel', nivel);
    }
    if (grado != null) {
      p = p.set('grado', grado);
    }
    if (seccion != null) {
      p = p.set('seccion', seccion);
    }
    if (estudiante.trim()) {
      p = p.set('estudiante', estudiante.trim());
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  crear(datos: { idEstudiante: number; anio: number; idGrado: number; idSeccion: number }) {
    return this.http.post<{ idInscripcion: number; mensaje: string }>(this.api, datos);
  }

  modificar(id: number, idSeccion: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, { idSeccion });
  }
}