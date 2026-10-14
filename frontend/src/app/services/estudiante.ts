import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Estudiante {
  private api = 'http://localhost:8080/backend/api/v1/estudiantes';

  constructor(private http: HttpClient) {}

  listar(
    page = 1,
    size = 10,
    nombre = '',
    carne = '',
    estado = '',
    grado: number | null = null,
    seccion: number | null = null,
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nombre) {
      p = p.set('nombre', nombre);
    }
    if (carne) {
      p = p.set('carne', carne);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    if (grado != null) {
      p = p.set('grado', grado);
    }
    if (seccion != null) {
      p = p.set('seccion', seccion);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  expediente(id: number) {
    return this.http.get<any>(`${this.api}/${id}/expediente`);
  }

  crear(datos: any) {
    return this.http.post<{ idEstudiante: number; carnet: string }>(this.api, datos);
  }

  modificar(id: number, datos: any) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  cambiarEstado(id: number, estado: string) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/estado`, { estado });
  }
}
