import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Multa {
  private api = 'http://localhost:8080/backend/api/v1/multas';

  constructor(private http: HttpClient) {}

  listar(
    page = 1,
    size = 10,
    prestatario: number | null = null,
    tipoPrestatario = '',
    estado = '',
    desde = '',
    hasta = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (prestatario != null) {
      p = p.set('prestatario', prestatario);
    }
    if (tipoPrestatario) {
      p = p.set('tipoPrestatario', tipoPrestatario);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  pagar(id: number) {
    return this.http.put<{ mensaje: string; numeroCorrelativo: string; monto: number }>(
      `${this.api}/${id}/pagar`,
      {},
    );
  }
}