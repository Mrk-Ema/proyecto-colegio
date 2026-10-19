import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Actividad {
  private api = 'http://localhost:8080/backend/api/v1/actividades';

  constructor(private http: HttpClient) {}

  listar(page = 1, size = 10, nombre = '', estado = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nombre) {
      p = p.set('nombre', nombre);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  crear(datos: {
    nombre: string;
    descripcion?: string | null;
    costo: number;
    alcance: string;
    grados: number[];
  }) {
    return this.http.post<{ idActividad: number; mensaje: string }>(this.api, datos);
  }

  modificar(
    id: number,
    datos: {
      nombre: string;
      descripcion?: string | null;
      costo: number;
      alcance: string;
      grados: number[];
    },
  ) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  desactivar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/desactivar`, {});
  }

  activar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/activar`, {});
  }
}