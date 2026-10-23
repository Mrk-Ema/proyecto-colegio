import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Asistencia } from '../services/asistencia';

@Component({
  selector: 'colegio-nomina',
  imports: [FormsModule, RouterLink],
  templateUrl: './nomina.html',
})
export class Nomina implements OnInit {
  idAsignacion: number = 0;
  datos: any[] = [];
  error: string = '';
  filtroNombre: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(
    private api: Asistencia,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.idAsignacion = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  cargar() {
    this.api.nomina(this.idAsignacion, this.page, this.size, this.filtroNombre).subscribe({
      next: (r) => {
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
      },
      error: () => (this.error = 'No se pudo cargar la nómina'),
    });
  }

  filtrar() {
    this.page = 1;
    this.cargar();
  }

  anterior() {
    if (this.page > 1) {
      this.page--;
      this.cargar();
    }
  }

  siguiente() {
    if (this.page < this.totalPaginas) {
      this.page++;
      this.cargar();
    }
  }
}
