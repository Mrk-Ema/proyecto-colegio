import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Anio } from '../services/anio';

@Component({
  selector: 'colegio-anio-estructura',
  imports: [FormsModule],
  templateUrl: './anio-estructura.html',
  styleUrl: './anio-estructura.css',
})
export class AnioEstructura implements OnInit {
  anio!: number;
  grados: any[] = [];
  marcados = new Set<number>();
  mensaje: string = '';
  error: string = '';

  constructor(private api: Anio, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.anio = Number(this.route.snapshot.paramMap.get('anio'));
    this.api.listarGrados().subscribe({
      next: (g) => {
        this.grados = g;
        this.api.obtener(this.anio).subscribe({
          next: (d) => (d.estructura ?? []).forEach((e: any) => this.marcados.add(e.idGrado)),
          error: () => (this.error = 'No se encontró el año'),
        });
      },
    });
  }

  alternar(id: number) {
    this.marcados.has(id) ? this.marcados.delete(id) : this.marcados.add(id);
  }

  estaMarcado(id: number): boolean {
    return this.marcados.has(id);
  }

  guardar() {
    this.mensaje = '';
    this.error = '';
    const ids = [...this.marcados];
    if (!ids.length) {
      this.error = 'Marca al menos un grado';
      return;
    }
    this.api.actualizarEstructura(this.anio, ids).subscribe({
      next: () => this.router.navigate(['/anios', this.anio]),
      error: (e) => (this.error = e.error?.error ?? 'No se pudo actualizar'),
    });
  }
}
