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
  secciones: any[] = [];
  error: string = '';

  constructor(private api: Anio, private route: ActivatedRoute) {}

  ngOnInit() {
    const anio = Number(this.route.snapshot.paramMap.get('anio'));
    this.api.obtener(anio).subscribe({
      next: (res) => {
        this.datos = res;
        this.api.listarSecciones(anio).subscribe({
          next: (s) => (this.secciones = s.secciones ?? []),
        });
      },
      error: () => (this.error = 'No se encontró'),
    });
  }

  esDiversificado(nivel: string): boolean {
    return (nivel ?? '').toLowerCase().includes('diversif');
  }

  grupos(): any[] {
    const lista: any[] = this.datos?.estructura ?? [];
    const niveles: any[] = [];
    const porNivel = new Map<string, any[]>();
    for (const g of lista) {
      const key = `${g.nivel}|||${g.carrera ?? ''}`;
      if (!porNivel.has(key)) {
        porNivel.set(key, []);
      }
      porNivel.get(key)!.push(g);
    }
    for (const [key, gs] of porNivel) {
      const [nivel, carrera] = key.split('|||');
      const titulo = this.esDiversificado(nivel) && carrera ? `${nivel} — ${carrera}` : nivel;
      const filas: any[][] = [];
      for (let i = 0; i < gs.length; i += 3) {
        filas.push(gs.slice(i, i + 3));
      }
      niveles.push({ titulo, filas });
    }
    return niveles;
  }

  seccionesDe(idGrado: number): any[] {
    return this.secciones.filter((s: any) => s.idGrado === idGrado);
  }
}
