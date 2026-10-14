import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Estudiante } from '../services/estudiante';

interface EncargadoForm {
  idEncargado?: number;
  nombres: string;
  apellidos: string;
  parentesco: string;
  telefono: string;
  dpi: string;
  abierto: boolean;
}

@Component({
  selector: 'colegio-estudiante-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './estudiante-form.html',
})
export class EstudianteForm implements OnInit {
  id: number | null = null;
  esEditar = false;
  carnet: string = '';
  nombres: string = '';
  apellidos: string = '';
  fechaNacimiento: string = '';
  correo: string = '';
  telefono: string = '';
  direccion: string = '';
  tipoSangre: string = '';
  alergias: string = '';
  padecimientos: string = '';
  encargados: EncargadoForm[] = [];
  mensaje: string = '';
  error: string = '';

  constructor(private api: Estudiante, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    const p = this.route.snapshot.paramMap.get('id');
    if (p) {
      this.esEditar = true;
      this.id = Number(p);
      this.api.obtener(this.id).subscribe({
        next: (r) => {
          this.carnet = r.carnet ?? '';
          this.nombres = r.nombres ?? '';
          this.apellidos = r.apellidos ?? '';
          this.fechaNacimiento = r.fechaNacimiento ?? '';
          this.correo = r.correo ?? '';
          this.telefono = r.telefono ?? '';
          this.direccion = r.direccion ?? '';
          this.tipoSangre = r.tipoSangre ?? '';
          this.alergias = r.alergias ?? '';
          this.padecimientos = r.padecimientos ?? '';
          this.encargados = (r.encargados ?? []).map((e: any) => ({
            idEncargado: e.idEncargado,
            nombres: e.nombres ?? '',
            apellidos: e.apellidos ?? '',
            parentesco: e.parentesco ?? '',
            telefono: e.telefono ?? '',
            dpi: e.dpi ?? '',
            abierto: false,
          }));
        },
        error: () => (this.error = 'No se encontró el estudiante'),
      });
    }
  }

  agregarEncargado() {
    this.encargados.push({ nombres: '', apellidos: '', parentesco: '', telefono: '', dpi: '', abierto: true });
  }

  alternar(i: number) {
    this.encargados[i].abierto = !this.encargados[i].abierto;
  }

  quitarEncargado(i: number) {
    if (this.encargados.length <= 1) {
      this.error = 'Debe quedar al menos un encargado';
      return;
    }
    if (!confirm(`¿Quitar al encargado ${i + 1}?`)) return;
    this.encargados.splice(i, 1);
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    if (!this.nombres.trim() || !this.apellidos.trim() || !this.fechaNacimiento || !this.correo.trim()) {
      this.error = 'Completa nombres, apellidos, fecha de nacimiento y correo';
      return;
    }
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(this.correo.trim())) {
      this.error = 'Correo inválido';
      return;
    }
    if (this.telefono && !/^[0-9]{8}$/.test(this.telefono)) {
      this.error = 'Teléfono inválido (8 dígitos)';
      return;
    }
    if (!this.encargados.length) {
      this.error = 'Agrega al menos un encargado';
      return;
    }
    for (let i = 0; i < this.encargados.length; i++) {
      const e = this.encargados[i];
      if (!e.nombres.trim() || !e.apellidos.trim() || !e.dpi.trim()) {
        this.error = `Encargado ${i + 1}: completa nombres, apellidos y DPI`;
        return;
      }
      if (!/^[0-9]{13}$/.test(e.dpi.trim())) {
        this.error = `Encargado ${i + 1}: DPI inválido (13 dígitos)`;
        return;
      }
      if (e.telefono && !/^[0-9]{8}$/.test(e.telefono)) {
        this.error = `Encargado ${i + 1}: teléfono inválido (8 dígitos)`;
        return;
      }
    }
    const datos: any = {
      nombres: this.nombres.trim(),
      apellidos: this.apellidos.trim(),
      fechaNacimiento: this.fechaNacimiento,
      correo: this.correo.trim(),
      telefono: this.telefono.trim() || null,
      direccion: this.direccion.trim() || null,
      tipoSangre: this.tipoSangre.trim() || null,
      alergias: this.alergias.trim() || null,
      padecimientos: this.padecimientos.trim() || null,
      encargados: this.encargados.map((e) => ({
        ...(e.idEncargado != null ? { idEncargado: e.idEncargado } : {}),
        nombres: e.nombres.trim(),
        apellidos: e.apellidos.trim(),
        parentesco: e.parentesco.trim() || null,
        telefono: e.telefono.trim() || null,
        dpi: e.dpi.trim(),
      })),
    };
    if (!this.esEditar) {
      this.api.crear(datos).subscribe({
        next: (r) => this.router.navigate(['/estudiantes']),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo crear'),
      });
    } else {
      this.api.modificar(this.id!, datos).subscribe({
        next: () => this.router.navigate(['/estudiantes', this.id]),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar'),
      });
    }
  }
}
