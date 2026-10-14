import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Estudiante } from '../services/estudiante';

@Component({
  selector: 'colegio-estudiante-detalle',
  imports: [RouterLink],
  templateUrl: './estudiante-detalle.html',
})
export class EstudianteDetalle implements OnInit {
  datos: any = null;
  error: string = '';

  constructor(private api: Estudiante, private route: ActivatedRoute) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.expediente(id).subscribe({
      next: (r) => (this.datos = r),
      error: () => (this.error = 'No se encontró el estudiante'),
    });
  }
}
