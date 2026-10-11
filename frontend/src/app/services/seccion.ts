import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Seccion {
  private api = 'http://localhost:8080/backend/api/v1/secciones';

  constructor(private http: HttpClient) {}

  listar(
    page = 1,
    size = 10,
    nivel: number | null = null,
    carrera: number | null = null,
    grado: number | null = null,
    nombre = '',
    estado = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nivel != null) {
      p = p.set('nivel', nivel);
    }
    if (carrera != null) {
      p = p.set('carrera', carrera);
    }
    if (grado != null) {
      p = p.set('grado', grado);
    }
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

  crear(datos: { nombre: string; idGrado: number }) {
    return this.http.post<{ mensaje: string }>(this.api, datos);
  }

  modificar(id: number, datos: { nombre: string }) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  desactivar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/desactivar`, {});
  }

  activar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/activar`, {});
  }
}
