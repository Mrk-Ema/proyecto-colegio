import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Reporte {
  private api = 'http://localhost:8080/backend/api/v1/reportes';

  constructor(private http: HttpClient) {}

  solventes(anio: number, grado: number | null = null, seccion: number | null = null, page = 1, size = 10) {
    let p = new HttpParams().set('anio', anio).set('page', page).set('size', size);
    if (grado != null) {
      p = p.set('grado', grado);
    }
    if (seccion != null) {
      p = p.set('seccion', seccion);
    }
    return this.http.get<any>(`${this.api}/solventes-morosos`, { params: p });
  }

  solventesPdf(anio: number, grado: number | null = null, seccion: number | null = null) {
    let p = new HttpParams().set('anio', anio);
    if (grado != null) {
      p = p.set('grado', grado);
    }
    if (seccion != null) {
      p = p.set('seccion', seccion);
    }
    return this.http.get(`${this.api}/solventes-morosos/pdf`, { params: p, responseType: 'blob' });
  }

  rendimiento(anio: number, grado: number, seccion: number) {
    const p = new HttpParams().set('anio', anio).set('grado', grado).set('seccion', seccion);
    return this.http.get<any>(`${this.api}/rendimiento`, { params: p });
  }

  rendimientoPdf(anio: number, grado: number, seccion: number) {
    const p = new HttpParams().set('anio', anio).set('grado', grado).set('seccion', seccion);
    return this.http.get(`${this.api}/rendimiento/pdf`, { params: p, responseType: 'blob' });
  }

  historialPagos(idEstudiante: number, desde = '', hasta = '', page = 1, size = 10) {
    let p = new HttpParams().set('idEstudiante', idEstudiante).set('page', page).set('size', size);
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get<any>(`${this.api}/historial-pagos`, { params: p });
  }

  historialPagosPdf(idEstudiante: number, desde = '', hasta = '') {
    let p = new HttpParams().set('idEstudiante', idEstudiante);
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get(`${this.api}/historial-pagos/pdf`, { params: p, responseType: 'blob' });
  }

  ingresos(desde = '', hasta = '') {
    let p = new HttpParams();
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get<any>(`${this.api}/ingresos`, { params: p });
  }

  ingresosPdf(desde = '', hasta = '') {
    let p = new HttpParams();
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    return this.http.get(`${this.api}/ingresos/pdf`, { params: p, responseType: 'blob' });
  }

  prestamosEnMora(desde = '', hasta = '', libro: number | null = null, page = 1, size = 10) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    if (libro != null) {
      p = p.set('libro', libro);
    }
    return this.http.get<any>(`${this.api}/prestamos-mora`, { params: p });
  }

  prestamosEnMoraPdf(desde = '', hasta = '', libro: number | null = null) {
    let p = new HttpParams();
    if (desde) {
      p = p.set('desde', desde);
    }
    if (hasta) {
      p = p.set('hasta', hasta);
    }
    if (libro != null) {
      p = p.set('libro', libro);
    }
    return this.http.get(`${this.api}/prestamos-mora/pdf`, { params: p, responseType: 'blob' });
  }
}
