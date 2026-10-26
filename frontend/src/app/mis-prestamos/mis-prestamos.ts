import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EstudianteConsulta } from '../services/estudiante-consulta';

@Component({
  selector: 'colegio-mis-prestamos',
  imports: [FormsModule, RouterLink],
  templateUrl: './mis-prestamos.html',
})
export class MisPrestamos implements OnInit {
  datos: any[] = [];
  error: string = '';
  filtroEstado: string = '';
  filtroDesde: string = '';
  filtroHasta: string = '';
  page: number = 1;
  size: number = 10;
  total: number = 0;
  totalPaginas: number = 0;

  constructor(private api: EstudianteConsulta) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.api.prestamos(this.page, this.size, this.filtroEstado, this.filtroDesde, this.filtroHasta).subscribe({
      next: (r) => {
        this.datos = r.datos;
        this.total = r.total;
        this.totalPaginas = r.totalPaginas;
      },
      error: () => (this.error = 'No se pudieron cargar los préstamos'),
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
