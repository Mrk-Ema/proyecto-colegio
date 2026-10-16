import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Empleado } from '../services/empleado';

@Component({
  selector: 'colegio-empleado-detalle',
  imports: [RouterLink],
  templateUrl: './empleado-detalle.html',
})
export class EmpleadoDetalle implements OnInit {
  datos: any = null;
  error: string = '';

  constructor(private api: Empleado, private route: ActivatedRoute) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.obtener(id).subscribe({
      next: (r) => (this.datos = r),
      error: () => (this.error = 'No se encontró el empleado'),
    });
  }
}
