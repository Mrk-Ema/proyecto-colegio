import { Component, OnInit } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anio-detalle',
  imports: [RouterLink],
  templateUrl: './anio-detalle.html',
  styleUrl: './anio-detalle.css',
})
export class AnioDetalle implements OnInit {
  datos: any = null;
  error: string = '';

  constructor(private api: Anio, private route: ActivatedRoute) {}

  ngOnInit() {
    const anio = Number(this.route.snapshot.paramMap.get('anio'));
    this.api.obtener(anio).subscribe({
      next: (res) => (this.datos = res),
      error: () => (this.error = 'No se encontró'),
    });
  }
}
