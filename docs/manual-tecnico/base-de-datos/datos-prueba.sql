#Datos base minimos (Iteracion 1)
INSERT INTO nivel (nombre) VALUES
  ('Pre-primaria'), ('Primaria'), ('Basico'), ('Diversificado');

INSERT INTO parametro_biblioteca (dias_prestamo, multa_por_dia)
  VALUES (7, 5.00);

INSERT INTO carrera (nombre, descripcion, duracion_años)
VALUES ('Bachillerato en Ciencias y Letras', 'Carrera de nivel diversificado', 2);

INSERT INTO grado (nombre, id_nivel, id_carrera)
SELECT g.nombre, n.id_nivel, NULL
FROM (SELECT 'Párvulos 1' nombre, 'Pre-primaria' nivel 
      UNION ALL SELECT 'Párvulos 2','Pre-primaria'
      UNION ALL SELECT 'Párvulos 3','Pre-primaria'
      UNION ALL SELECT 'Primero Primaria','Primaria' 
      UNION ALL SELECT 'Segundo Primaria','Primaria'
      UNION ALL SELECT 'Tercero Primaria','Primaria' 
      UNION ALL SELECT 'Cuarto Primaria','Primaria'
      UNION ALL SELECT 'Quinto Primaria','Primaria' 
      UNION ALL SELECT 'Sexto Primaria','Primaria'
      UNION ALL SELECT 'Primero Básico','Basico' 
      UNION ALL SELECT 'Segundo Básico','Basico'
      UNION ALL SELECT 'Tercero Básico','Basico') g
JOIN nivel n ON n.nombre = g.nivel;

INSERT INTO grado (nombre, id_nivel, id_carrera)
SELECT g.nombre, n.id_nivel, c.id_carrera
FROM (SELECT 'Cuarto Bachillerato' nombre 
      UNION ALL SELECT 'Quinto Bachillerato') g
JOIN nivel n ON n.nombre = 'Diversificado'
JOIN carrera c ON c.nombre = 'Bachillerato en Ciencias y Letras';

INSERT INTO curso (nombre) VALUES
 ('Matemática'), ('Comunicación y Lenguaje'), ('Ciencias Naturales'),
 ('Ciencias Sociales'), ('Educación Física'), ('Inglés');

INSERT INTO estructura_año (id_año_lectivo, id_grado)
SELECT NULL, id_grado FROM grado;

INSERT INTO curriculo (id_estructura, id_curso)
SELECT e.id_estructura, c.id_curso
FROM estructura_año e CROSS JOIN curso c
WHERE e.id_año_lectivo IS NULL;


INSERT INTO usuario (correo, contraseña_hash, rol, estado)
  VALUES ('superadmin@colegio.edu', '123', 'Super Admin', 'Activo');
