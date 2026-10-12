import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Curriculo {
  private api = 'http://localhost:8080/backend/api/v1/curriculo';

  constructor(private http: HttpClient) {}

  consultar(idGrado: number, contexto: string) {
    const p = new HttpParams().set('grado', idGrado).set('contexto', contexto || 'defecto');
    return this.http.get<{ cursos: any[] }>(this.api, { params: p });
  }

  crear(datos: { idGrado: number; contexto: string; cursos: number[] }) {
    return this.http.post<{ mensaje: string }>(this.api, datos);
  }

  modificar(datos: { idGrado: number; contexto: string; cursos: number[] }) {
    return this.http.put<{ mensaje: string }>(this.api, datos);
  }
}
