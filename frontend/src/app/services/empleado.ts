import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Pagina } from './carrera';

@Injectable({
  providedIn: 'root',
})
export class Empleado {
  private api = 'http://localhost:8080/backend/api/v1/empleados';

  puestos(rol: string): string[] {
    const operativos = ['Maestro', 'Secretaria', 'Bibliotecario', 'Mantenimiento', 'Limpieza'];
    return rol === 'Super Admin' ? [...operativos, 'Admin', 'Super Admin'] : operativos;
  }

  requiereCorreo(puesto: string): boolean {
    return puesto !== 'Mantenimiento' && puesto !== 'Limpieza';
  }

  listar(page = 1, size = 10, nombre = '', puesto = '', estado = '') {
    let p = new HttpParams().set('page', page).set('size', size);
    if (nombre) {
      p = p.set('nombre', nombre);
    }
    if (puesto) {
      p = p.set('puesto', puesto);
    }
    if (estado) {
      p = p.set('estado', estado);
    }
    return this.http.get<Pagina<any>>(this.api, { params: p });
  }

  constructor(private http: HttpClient) {}

  obtener(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  crear(datos: any) {
    return this.http.post<{ idEmpleado: number }>(this.api, datos);
  }

  modificar(id: number, datos: any) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}`, datos);
  }

  desactivar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/desactivar`, {});
  }

  activar(id: number) {
    return this.http.put<{ mensaje: string }>(`${this.api}/${id}/activar`, {});
  }
}
