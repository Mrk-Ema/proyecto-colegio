CREATE DATABASE IF NOT EXISTS colegio
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE colegio;

-- Personas y acceso
CREATE TABLE usuario (
  id_usuario          INT AUTO_INCREMENT PRIMARY KEY,
  correo              VARCHAR(150) NOT NULL UNIQUE,
  contraseña_hash     VARCHAR(255) NOT NULL,
  rol                 ENUM('Super Admin','Admin','Secretaria','Maestro',
                           'Bibliotecario','Estudiante') NOT NULL,
  estado              ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  fecha_ultimo_acceso DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE empleado (
  id_empleado       INT AUTO_INCREMENT PRIMARY KEY,
  nombres           VARCHAR(100) NOT NULL,
  apellidos         VARCHAR(100) NOT NULL,
  dpi               VARCHAR(13) NOT NULL UNIQUE,
  fecha_nacimiento  DATE NOT NULL,
  telefono          VARCHAR(20) NULL,
  direccion         VARCHAR(255) NULL,
  puesto            VARCHAR(100) NOT NULL,
  fecha_contratacion DATE NOT NULL,
  salario           DECIMAL(10,2) NOT NULL,
  estado            ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  id_usuario        INT NULL UNIQUE, -- NULL = Mantenimiento/Limpieza porque no tienen cuenta 
  CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE estudiante (
  id_estudiante    INT AUTO_INCREMENT PRIMARY KEY,
  carnet           VARCHAR(20) NULL UNIQUE, -- cuando se crea es null pero despues de crear se hace un update y crea el carnet en base al id y año
  nombres          VARCHAR(100) NOT NULL,
  apellidos        VARCHAR(100) NOT NULL,
  fecha_nacimiento DATE NOT NULL,
  direccion        VARCHAR(255) NULL,
  telefono         VARCHAR(20) NULL,
  tipo_sangre      VARCHAR(5) NULL,
  alergias         VARCHAR(255) NULL,
  padecimientos    VARCHAR(255) NULL,
  estado           ENUM('Activo','Inactivo','Graduado') NOT NULL DEFAULT 'Activo',
  id_usuario       INT NULL UNIQUE,
  CONSTRAINT fk_estudiante_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE encargado (
  id_encargado INT AUTO_INCREMENT PRIMARY KEY,
  nombres      VARCHAR(100) NOT NULL,
  apellidos    VARCHAR(100) NOT NULL,
  parentesco   VARCHAR(50) NULL,
  telefono     VARCHAR(20) NULL,
  dpi          VARCHAR(13) NOT NULL UNIQUE,
  id_estudiante INT NOT NULL,
  CONSTRAINT fk_encargado_estudiante FOREIGN KEY (id_estudiante)
    REFERENCES estudiante (id_estudiante)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Estructura academica
CREATE TABLE año_lectivo (
  id_año_lectivo INT PRIMARY KEY,  -- el año normal tipo, 2024, 2025, 2026 y asi...
  fecha_inicio    DATE NOT NULL,
  fecha_cierre    DATE NOT NULL,
  estado          ENUM('Planificado','Activo','Cerrado') NOT NULL DEFAULT 'Planificado'
) ENGINE=InnoDB;

CREATE TABLE nivel (
  id_nivel INT AUTO_INCREMENT PRIMARY KEY,
  nombre   VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE carrera (
  id_carrera     INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  descripcion    VARCHAR(255) NULL,
  duracion_años INT NULL,
  estado         ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE grado (
  id_grado   INT AUTO_INCREMENT PRIMARY KEY,
  nombre     VARCHAR(100) NOT NULL,
  estado     ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  id_nivel   INT NOT NULL,
  id_carrera INT NULL,  -- solo si es diversificado sino null
  CONSTRAINT fk_grado_nivel FOREIGN KEY (id_nivel)
    REFERENCES nivel (id_nivel)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_grado_carrera FOREIGN KEY (id_carrera)
    REFERENCES carrera (id_carrera)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE seccion (
  id_seccion INT AUTO_INCREMENT PRIMARY KEY,
  nombre     VARCHAR(10) NOT NULL,
  estado     ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  id_grado   INT NOT NULL,
  CONSTRAINT fk_seccion_grado FOREIGN KEY (id_grado)
    REFERENCES grado (id_grado)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE curso (
  id_curso INT AUTO_INCREMENT PRIMARY KEY,
  nombre   VARCHAR(100) NOT NULL,
  estado   ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE estructura_año (
  id_estructura   INT AUTO_INCREMENT PRIMARY KEY,
  id_año_lectivo INT NULL,  -- NULL para estructura por defecto
  id_grado        INT NOT NULL,
  CONSTRAINT fk_estructura_año FOREIGN KEY (id_año_lectivo)
    REFERENCES año_lectivo (id_año_lectivo)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_estructura_grado FOREIGN KEY (id_grado)
    REFERENCES grado (id_grado)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_estructura_año_grado (id_año_lectivo, id_grado)
) ENGINE=InnoDB;

CREATE TABLE curriculo (
  id_curriculo INT AUTO_INCREMENT PRIMARY KEY,
  id_estructura INT NOT NULL,
  id_curso     INT NOT NULL,
  CONSTRAINT fk_curriculo_estructura FOREIGN KEY (id_estructura)
    REFERENCES estructura_año (id_estructura)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_curriculo_curso FOREIGN KEY (id_curso)
    REFERENCES curso (id_curso)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_curriculo_estructura_curso (id_estructura, id_curso)
) ENGINE=InnoDB;

CREATE TABLE asignacion_maestro (
  id_asignacion  INT AUTO_INCREMENT PRIMARY KEY,
  id_año_lectivo INT NOT NULL,
  id_seccion     INT NOT NULL,
  id_curso       INT NOT NULL,
  id_empleado    INT NOT NULL,  -- el maestro
  CONSTRAINT fk_asig_año FOREIGN KEY (id_año_lectivo)
    REFERENCES año_lectivo (id_año_lectivo)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_asig_seccion FOREIGN KEY (id_seccion)
    REFERENCES seccion (id_seccion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_asig_curso FOREIGN KEY (id_curso)
    REFERENCES curso (id_curso)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_asig_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado (id_empleado)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_asignacion (id_año_lectivo, id_seccion, id_curso)
) ENGINE=InnoDB;

-- Inscripciones y finanzas
CREATE TABLE inscripcion (
  id_inscripcion   INT AUTO_INCREMENT PRIMARY KEY,
  fecha_inscripcion DATE NOT NULL,
  id_estudiante    INT NOT NULL,
  id_año_lectivo  INT NOT NULL,
  id_seccion       INT NOT NULL,
  CONSTRAINT fk_insc_estudiante FOREIGN KEY (id_estudiante)
    REFERENCES estudiante (id_estudiante)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_insc_año FOREIGN KEY (id_año_lectivo)
    REFERENCES año_lectivo (id_año_lectivo)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_insc_seccion FOREIGN KEY (id_seccion)
    REFERENCES seccion (id_seccion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_inscripcion (id_estudiante, id_año_lectivo)-- un estudiante es para una inscripcion por año
) ENGINE=InnoDB;

CREATE TABLE cuota (
  id_cuota           INT AUTO_INCREMENT PRIMARY KEY,
  monto_inscripcion  DECIMAL(10,2) NOT NULL,
  monto_mensualidad  DECIMAL(10,2) NOT NULL,
  id_año_lectivo    INT NOT NULL,
  id_grado           INT NOT NULL,
  CONSTRAINT fk_cuota_año FOREIGN KEY (id_año_lectivo)
    REFERENCES año_lectivo (id_año_lectivo)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_cuota_grado FOREIGN KEY (id_grado)
    REFERENCES grado (id_grado)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_cuota (id_año_lectivo, id_grado)
) ENGINE=InnoDB;

CREATE TABLE actividad_extracurricular (
  id_actividad INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(150) NOT NULL,
  descripcion  VARCHAR(255) NULL,
  costo        DECIMAL(10,2) NOT NULL,
  alcance      ENUM('Todo el colegio','Grados especificos') NOT NULL,
  estado       ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE actividad_grado (
  id_actividad INT NOT NULL,
  id_grado     INT NOT NULL,
  PRIMARY KEY (id_actividad, id_grado), 
  CONSTRAINT fk_ag_actividad FOREIGN KEY (id_actividad)
    REFERENCES actividad_extracurricular (id_actividad)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_ag_grado FOREIGN KEY (id_grado)
    REFERENCES grado (id_grado)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE pago (
  id_pago             INT AUTO_INCREMENT PRIMARY KEY,
  numero_correlativo  VARCHAR(30) NOT NULL UNIQUE,  
  concepto            ENUM('Inscripcion','Mensualidad','Actividad') NOT NULL,
  mes                 VARCHAR(20) NULL,  -- solo si concepto =  Mensualidad
  monto               DECIMAL(10,2) NOT NULL,
  fecha_pago          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado              ENUM('Registrado','Anulado') NOT NULL DEFAULT 'Registrado',
  motivo_anulacion    VARCHAR(255) NULL,
  fecha_anulacion     DATETIME NULL,
  id_inscripcion      INT NOT NULL,
  id_actividad        INT NULL,  -- solo si concepto = Actividad
  id_usuario_registra INT NOT NULL,
  id_usuario_anula    INT NULL,
  CONSTRAINT fk_pago_inscripcion FOREIGN KEY (id_inscripcion)
    REFERENCES inscripcion (id_inscripcion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_pago_actividad FOREIGN KEY (id_actividad)
    REFERENCES actividad_extracurricular (id_actividad)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_pago_registra FOREIGN KEY (id_usuario_registra)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_pago_anula FOREIGN KEY (id_usuario_anula)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_pago_mes (id_inscripcion, mes)
) ENGINE=InnoDB;

-- Aula del maestro
CREATE TABLE asistencia (
  id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
  fecha         DATE NOT NULL,
  estado        ENUM('Presente','Ausente','Tardio','Permiso') NOT NULL,
  id_asignacion INT NOT NULL,
  id_inscripcion INT NOT NULL,
  CONSTRAINT fk_asis_asignacion FOREIGN KEY (id_asignacion)
    REFERENCES asignacion_maestro (id_asignacion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_asis_inscripcion FOREIGN KEY (id_inscripcion)
    REFERENCES inscripcion (id_inscripcion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_asistencia (fecha, id_asignacion, id_inscripcion)
) ENGINE=InnoDB;

CREATE TABLE actividad_evaluable (
  id_actividad_evaluable INT AUTO_INCREMENT PRIMARY KEY,
  nombre                 VARCHAR(150) NOT NULL,
  puntos                 DECIMAL(6,2) NOT NULL,
  id_asignacion          INT NOT NULL,
  CONSTRAINT fk_ae_asignacion FOREIGN KEY (id_asignacion)
    REFERENCES asignacion_maestro (id_asignacion)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE nota (
  id_nota               INT AUTO_INCREMENT PRIMARY KEY,
  valor                 DECIMAL(6,2) NOT NULL,
  fecha_registro        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_actividad_evaluable INT NOT NULL,
  id_inscripcion        INT NOT NULL,
  CONSTRAINT fk_nota_ae FOREIGN KEY (id_actividad_evaluable)
    REFERENCES actividad_evaluable (id_actividad_evaluable)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_nota_inscripcion FOREIGN KEY (id_inscripcion)
    REFERENCES inscripcion (id_inscripcion)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  UNIQUE KEY uq_nota (id_actividad_evaluable, id_inscripcion)
) ENGINE=InnoDB;

-- Biblioteca
CREATE TABLE libro (
  id_libro             INT AUTO_INCREMENT PRIMARY KEY,
  titulo               VARCHAR(200) NOT NULL,
  autor                VARCHAR(150) NULL,
  editorial            VARCHAR(150) NULL,
  cantidad_total       INT NOT NULL DEFAULT 0,
  cantidad_disponible  INT NOT NULL DEFAULT 0,
  estado               ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  CONSTRAINT chk_libro_cantidades CHECK (cantidad_disponible <= cantidad_total)
) ENGINE=InnoDB;

CREATE TABLE prestamo (
  id_prestamo       INT AUTO_INCREMENT PRIMARY KEY,
  fecha_prestamo    DATE NOT NULL,
  fecha_esperada    DATE NOT NULL,
  fecha_devolucion  DATE NULL,
  estado            ENUM('Activo','Devuelto') NOT NULL DEFAULT 'Activo',
  id_libro          INT NOT NULL,
  id_estudiante     INT NULL,
  id_empleado       INT NULL,
  CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro)
    REFERENCES libro (id_libro)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_prestamo_estudiante FOREIGN KEY (id_estudiante)
    REFERENCES estudiante (id_estudiante)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_prestamo_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado (id_empleado)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE multa (
  id_multa            INT AUTO_INCREMENT PRIMARY KEY,
  dias_atraso         INT NOT NULL DEFAULT 0,
  monto               DECIMAL(10,2) NOT NULL,
  estado              ENUM('Pendiente','Pagada') NOT NULL DEFAULT 'Pendiente',
  fecha_pago          DATETIME NULL,
  numero_correlativo  VARCHAR(30) NULL UNIQUE, 
  id_usuario_cobra    INT NULL,
  id_prestamo         INT NOT NULL UNIQUE,  
  CONSTRAINT fk_multa_cobra FOREIGN KEY (id_usuario_cobra)
    REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_multa_prestamo FOREIGN KEY (id_prestamo)
    REFERENCES prestamo (id_prestamo)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE parametro_biblioteca (
  id_parametro   INT AUTO_INCREMENT PRIMARY KEY,
  dias_prestamo  INT NOT NULL,
  multa_por_dia  DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB;

