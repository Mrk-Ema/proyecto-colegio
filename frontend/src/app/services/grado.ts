import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Grado {
  private api = 'http://localhost:8080/backend/api/v1/grados';
  private apiNiveles = 'http://localhost:8080/backend/api/v1/niveles';

  constructor(private http: HttpClient) {}

  listarPaginado(
    page = 1,
    size = 10,
    nombre = '',
    nivel: number | null = null,
    carrera: number | null = null,
    estado = '',
    anio = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nombre) {
      p = p.set('nombre', nombre);
    }
    if (nivel != null) {
      p = p.set('nivel', nivel);
    }
    if (carrera != null) {
      p = p.set('carrera', carrera);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    if (anio) {
      p = p.set('anio', anio);
    }
    return this.http.get<Pagina<any>>(`${this.api}/paginado`, { params: p });
  }

  listarActivos() {
    return this.http.get<any[]>(this.api);
  }

  listarNiveles() {
    return this.http.get<any[]>(this.apiNiveles);
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  crear(datos: { nombre: string; idNivel: number; idCarrera?: number | null; destinoAnio?: string | null }) {
    return this.http.post<{ mensaje: string; idGrado: number }>(this.api, datos);
  }

  modificar(id: number, datos: { nombre: string; idNivel?: number | null; idCarrera?: number | null }) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  desactivar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/desactivar`, {});
  }

  activar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/activar`, {});
  }
}
