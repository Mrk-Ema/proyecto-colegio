import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Anio {
  private api = 'http://localhost:8080/backend/api/v1/anios';
  private apiGrados = 'http://localhost:8080/backend/api/v1/grados';

  constructor(private http: HttpClient) {}

  listar() {
    return this.http.get<any[]>(this.api);
  }

  obtener(anio: number) {
    return this.http.get<any>(`${this.api}/${anio}`);
  }

  crear(datos: { anio: number; fechaInicio: string; fechaCierre: string; grados: number[] }) {
    return this.http.post<{ mensaje: string }>(this.api, datos);
  }

  modificar(anio: number, datos: { fechaInicio: string; fechaCierre: string }) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${anio}`, datos);
  }

  activar(anio: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${anio}/activar`, {});
  }

  cerrar(anio: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${anio}/cerrar`, {});
  }

  eliminar(anio: number) {
    return this.http.delete<{ mensaje: string }>(`${this.api}/${anio}`);
  }

  actualizarEstructura(anio: number, grados: number[]) {
    return this.http.put<{ mensaje: string }>(
      'http://localhost:8080/backend/api/v1/estructura/' + anio,
      { grados },
    );
  }

  listarGrados() {
    return this.http.get<any[]>(this.apiGrados);
  }
}
