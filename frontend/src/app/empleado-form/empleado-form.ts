import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Empleado } from '../services/empleado';

@Component({
  selector: 'colegio-empleado-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './empleado-form.html',
})
export class EmpleadoForm implements OnInit {
  id: number | null = null;
  esEditar = false;
  nombres: string = '';
  apellidos: string = '';
  dpi: string = '';
  fechaNacimiento: string = '';
  telefono: string = '';
  direccion: string = '';
  puesto: string = '';
  fechaContratacion: string = '';
  salario: number | null = null;
  correo: string = '';
  puestos: string[] = [];
  mensaje: string = '';
  error: string = '';

  constructor(private api: Empleado, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.puestos = this.api.puestos(localStorage.getItem('rol') ?? '');
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.nombres = r.nombres ?? '';
          this.apellidos = r.apellidos ?? '';
          this.dpi = r.dpi ?? '';
          this.fechaNacimiento = r.fechaNacimiento ?? '';
          this.telefono = r.telefono ?? '';
          this.direccion = r.direccion ?? '';
          this.puesto = r.puesto ?? '';
          this.fechaContratacion = r.fechaContratacion ?? '';
          this.salario = r.salario ?? null;
          this.correo = r.correo ?? '';
        },
        error: () => (this.error = 'No se encontró el empleado'),
      });
    }
  }

  pideCorreo(): boolean {
    return !!this.puesto && this.api.requiereCorreo(this.puesto);
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombres.trim() || !this.apellidos.trim() || !this.dpi.trim()
      || !this.fechaNacimiento || !this.puesto || !this.fechaContratacion || this.salario == null) {
      this.error = 'Completa nombres, apellidos, DPI, nacimiento, puesto, contratación y salario';
      return;
    }
    if (!/^[0-9]{13}$/.test(this.dpi.trim())) {
      this.error = 'DPI inválido (13 dígitos)';
      return;
    }
    if (this.telefono && !/^[0-9]{8}$/.test(this.telefono)) {
      this.error = 'Teléfono inválido (8 dígitos)';
      return;
    }
    if (this.salario <= 0) {
      this.error = 'Salario inválido';
      return;
    }
    if (this.pideCorreo()) {
      if (!this.correo.trim()) {
        this.error = 'El correo es obligatorio para este puesto';
        return;
      }
      if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(this.correo.trim())) {
        this.error = 'Correo inválido';
        return;
      }
    }
    const datos: any = {
      nombres: this.nombres.trim(),
      apellidos: this.apellidos.trim(),
      dpi: this.dpi.trim(),
      fechaNacimiento: this.fechaNacimiento,
      telefono: this.telefono.trim() || null,
      direccion: this.direccion.trim() || null,
      puesto: this.puesto,
      fechaContratacion: this.fechaContratacion,
      salario: this.salario,
      correo: this.correo.trim() || null,
    };
    if (!this.esEditar) {
      this.api.crear(datos).subscribe({
        next: () => this.router.navigate(['/empleados']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, datos).subscribe({
        next: () => this.router.navigate(['/empleados', this.id]),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
