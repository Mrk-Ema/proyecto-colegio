import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anios',
  imports: [RouterLink],
  templateUrl: './anios.html',
  styleUrl: './anios.css',
})
export class Anios implements OnInit {
  anios: any[] = [];
  error: string = '';
  mensaje: string = '';

  constructor(private api: Anio) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.listar().subscribe({
      next: (res) => (this.anios = res),
      error: () => (this.error = 'No se pudo listar'),
    });
  }

  activar(a: any) {
    if (!confirm(`¿Activar el año ${a.anio}? El año Activo actual deberá estar cerrado.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.activar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo activar'),
    });
  }

  cerrar(a: any) {
    if (!confirm(`¿Cerrar el año ${a.anio}? No se podrá reabrir ni modificar.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.cerrar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo cerrar'),
    });
  }

  eliminar(a: any) {
    if (!confirm(`¿Eliminar el año ${a.anio}? Se borra con su estructura. No se puede deshacer.`)) return;
    this.error = '';
    this.mensaje = '';
    this.api.eliminar(a.anio).subscribe({
      next: () => this.cargar(),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo eliminar'),
    });
  }
}
