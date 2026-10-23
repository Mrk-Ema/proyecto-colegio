import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Pago {
  private api = 'http://localhost:8080/backend/api/v1/pagos';

  /**
   * Última búsqueda del estudiante. Vive solo en memoria y va atada al token
   * de la sesión, así que nunca le aparece a otro usuario ni a otra sesión.
   */
  private busqueda: { token: string; carnet: string } | null = null;

  constructor(private http: HttpClient) {}

  guardarBusqueda(carnet: string) {
    this.busqueda = { token: localStorage.getItem('token') ?? '', carnet: carnet.trim() };
  }

  leerBusqueda(): string | null {
    const token = localStorage.getItem('token') ?? '';
    if (this.busqueda && this.busqueda.token === token) {
      return this.busqueda.carnet;
    }
    this.busqueda = null;
    return null;
  }

  /** Al salir del módulo de pagos el estudiante se olvida. */
  olvidarSiSalioDe(url: string) {
    if (!url.split('?')[0].startsWith('/pagos')) {
      this.busqueda = null;
    }
  }

  /** Listado de los pagos de un estudiante; el carnet es obligatorio. */
  listar(
    page = 1,
    size = 10,
    carnet = '',
    concepto = '',
    estado = '',
    desde = '',
    hasta = '',
  ) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (carnet.trim()) {
      p = p.set('carnet', carnet.trim());
    }
    if (concepto) {
      p = p.set('concepto', concepto);
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

  /** Datos del estudiante para cobrar: inscripción, cuota y actividades. */
  contexto(carnet: string, anio: number | null = null) {
    let p = new HttpParams().set('carnet', carnet.trim());
    if (anio != null) {
      p = p.set('anio', anio);
    }
    return this.http.get<any>(`${this.api}/contexto`, { params: p });
  }

  registrar(datos: { carnet: string; anio: number; concepto: string; mes?: string | null }) {
    return this.http.post<{ idPago: number; numeroCorrelativo: string; mensaje: string }>(
      this.api,
      datos,
    );
  }

  registrarActividad(datos: { carnet: string; anio: number; idActividad: number }) {
    return this.http.post<{ idPago: number; numeroCorrelativo: string; mensaje: string }>(
      `${this.api}/actividad`,
      datos,
    );
  }

  anular(id: number, motivo: string) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/anular`, { motivo });
  }

  /** Emite el comprobante en PDF y lo descarga en el navegador. */
  comprobante(id: number) {
    return this.http.get(`${this.api}/${id}/comprobante`, { responseType: 'blob' });
  }
}
