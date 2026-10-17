import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Estudiante } from '../services/estudiante';
import { Inscripcion } from '../services/inscripcion';

@Component({
  selector: 'colegio-inscripcion-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './inscripcion-form.html',
})
export class InscripcionForm implements OnInit {
  esEditar = false;
  id: number | null = null;
  anios: any[] = [];
  grados: any[] = [];
  secciones: any[] = [];
  anio: number | null = null;
  idGrado: number | null = null;
  idSeccion: number | null = null;
  /** Estudiante elegido en la inscripción. */
  idEstudiante: number | null = null;
  estudiante: any = null;
  /** Texto del buscador de estudiantes: nombre o carné. */
  busqueda: string = '';
  resultados: any[] = [];
  buscando = false;
  error: string = '';
  aviso: string = '';

  constructor(
    private api: Inscripcion,
    private apiEstudiante: Estudiante,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit() {
    this.api.anios().subscribe({
      next: (r) => {
        this.anios = r.anios;
        this.anio = r.porDefecto;
      },
      error: () => (this.error = 'No se pudieron cargar los años lectivos'),
    });
    const param = this.route.snapshot.paramMap.get('id');
    if (param) {
      this.esEditar = true;
      this.id = Number(param);
      this.cargarInscripcion(this.id);
    }
  }

  /** En edición solo se permite cambiar la sección, así que el resto queda bloqueado. */
  cargarInscripcion(id: number) {
    this.api.obtener(id).subscribe({
      next: (f) => {
        this.anio = f.anio;
        this.idEstudiante = f.idEstudiante;
        this.estudiante = { carnet: f.carnet, nombres: f.nombres, apellidos: f.apellidos };
        this.idGrado = f.idGrado;
        this.cargarGrados();
        this.cargarSecciones(f.idSeccion);
      },
      error: (e) => (this.error = e.error?.error ?? 'No se encontró la inscripción'),
    });
  }

  cargarGrados() {
    if (this.anio == null) {
      this.grados = [];
      return;
    }
    this.api.grados(this.anio).subscribe({
      next: (g) => (this.grados = g),
      error: () => (this.error = 'No se pudieron cargar los grados del año'),
    });
  }

  cargarSecciones(elegida: number | null = null) {
    if (this.idGrado == null) {
      this.secciones = [];
      return;
    }
    this.api.secciones(this.idGrado).subscribe({
      next: (s) => {
        this.secciones = s;
        if (elegida != null) {
          this.idSeccion = elegida;
        }
      },
      error: () => (this.error = 'No se pudieron cargar las secciones'),
    });
  }

  /** Buscador de estudiantes Activos por nombre o carné. */
  buscar() {
    this.buscando = true;
    this.resultados = [];
    this.apiEstudiante.listar(1, 20, this.busqueda.trim(), '', 'Activo').subscribe({
      next: (r) => {
        this.resultados = r.datos;
        this.buscando = false;
        if (!r.datos.length) {
          this.aviso = 'No hay estudiantes Activos que coincidan con la búsqueda.';
        }
      },
      error: () => {
        this.buscando = false;
        this.error = 'No se pudo buscar el estudiante';
      },
    });
  }

  elegirEstudiante(f: any) {
    this.idEstudiante = f.idEstudiante;
    this.estudiante = f;
    this.resultados = [];
    this.busqueda = '';
    this.aviso = '';
  }

  quitarEstudiante() {
    this.idEstudiante = null;
    this.estudiante = null;
  }

  /** Nombre del grado de la inscripción, para mostrarlo bloqueado en modo edición. */
  nombreGrado(): string {
    return this.grados.find((g) => g.idGrado === this.idGrado)?.grado ?? '';
  }

  guardar() {
    this.error = '';
    if (!this.esEditar && this.idEstudiante == null) {
      this.error = 'Selecciona un estudiante';
      return;
    }
    if (this.anio == null) {
      this.error = 'Selecciona el año lectivo';
      return;
    }
    if (this.idGrado == null) {
      this.error = 'Selecciona el grado';
      return;
    }
    if (this.idSeccion == null) {
      this.error = 'Selecciona la sección';
      return;
    }
    if (this.esEditar) {
      this.api.modificar(this.id!, this.idSeccion).subscribe({
        next: (r) => this.router.navigate(['/inscripciones'], { state: { mensaje: r.mensaje } }),
        error: (e) => (this.error = e.error?.error ?? 'No se pudo modificar la inscripción'),
      });
    } else {
      this.api
        .crear({
          idEstudiante: this.idEstudiante!,
          anio: this.anio,
          idGrado: this.idGrado,
          idSeccion: this.idSeccion,
        })
        .subscribe({
          next: (r) => this.router.navigate(['/inscripciones'], { state: { mensaje: r.mensaje } }),
          error: (e) => (this.error = e.error?.error ?? 'No se pudo registrar la inscripción'),
        });
    }
  }
}