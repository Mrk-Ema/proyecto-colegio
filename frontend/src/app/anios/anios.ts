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

  constructor(private api: Anio) {}

  ngOnInit() {
    this.api.listar().subscribe({
      next: (res) => (this.anios = res),
      error: () => (this.error = 'No se pudo listar'),
    });
  }
}
