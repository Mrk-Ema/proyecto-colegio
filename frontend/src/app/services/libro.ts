import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Libro {
  private api = 'http://localhost:8080/backend/api/v1/libros';

  constructor(private http: HttpClient) {}

  listar(page = 1, size = 10, titulo = '', autor = '', estado = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (titulo) {
      p = p.set('titulo', titulo);
    }
    if (autor) {
      p = p.set('autor', autor);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  crear(datos: { titulo: string; autor?: string | null; editorial?: string | null; cantidad: number }) {
    return this.http.post<{ idLibro: number; mensaje: string }>(this.api, datos);
  }

  modificar(id: number, datos: { titulo: string; autor?: string | null; editorial?: string | null; cantidad: number }) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  desactivar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/desactivar`, {});
  }

  activar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/activar`, {});
  }
}
