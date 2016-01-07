CREATE SCHEMA modulo2;

CREATE TABLE modulo2.usuario
(
    usbid character(25) NOT NULL,
    contrasena character(30) DEFAULT '123',
    tipo character(15) DEFAULT 'usuario',
    nombre character(50) DEFAULT 'Nuevo Usuario',
    CONSTRAINT pk_usuario PRIMARY KEY (usbid)
);

CREATE TABLE modulo2.laboratorio
(
    codlab character(20) NOT NULL,
    nombre character(20) DEFAULT 'Nuevo Laboratorio',
    jefe character(25) NOT NULL,
    CONSTRAINT pk_laboratorio PRIMARY KEY (codlab),
    CONSTRAINT fk_laboratorio_jefe FOREIGN KEY (jefe) REFERENCES modulo2.usuario (usbid)
);

CREATE TABLE modulo2.seccion
(
    codsec character(5) NOT NULL,
    nombre character(70) DEFAULT 'Nueva Seccion',
    codlab character(5) NOT NULL,
    jefe character(25) NOT NULL,
    CONSTRAINT pk_seccion PRIMARY KEY (codsec, codlab),
    CONSTRAINT fk_seccion_jefe FOREIGN KEY (jefe) REFERENCES modulo2.usuario (usbid),
    CONSTRAINT fk_seccion_lab FOREIGN KEY (codlab) REFERENCES modulo2.laboratorio (codlab)
);

CREATE TABLE modulo2.trabaja
(
    trabajador character(25) NOT NULL,
    seccion character(5) NOT NULL,
    laboratorio character(5) NOT NULL,
    CONSTRAINT pk_trabaja PRIMARY KEY (trabajador, seccion, laboratorio),
    CONSTRAINT fk_trabajador FOREIGN KEY (trabajador) REFERENCES modulo2.usuario (usbid),
    CONSTRAINT fk_trabaja_seccion FOREIGN KEY (seccion, laboratorio) REFERENCES modulo2.seccion (codsec,codlab)
);

CREATE TABLE modulo2.gestiona
(
    seccion character(5) NOT NULL,
    laboratorio character(5) NOT NULL,
    gestor character(25) NOT NULL,
    CONSTRAINT pk_gestiona PRIMARY KEY (gestor),
    CONSTRAINT fk_gest_trabaja FOREIGN KEY (gestor,seccion,laboratorio) REFERENCES modulo2.trabaja (trabajador,seccion,laboratorio)
);

CREATE TABLE modulo2.insumo
(
    codigo integer NOT NULL,
    nombre character(100) NOT NULL,
    seccion character(5) NOT NULL,
    laboratorio character(5) NOT NULL,
    existe boolean DEFAULT '1',
    marca character(50) DEFAULT '-',
    modelo character(30) DEFAULT '-',
    CONSTRAINT pk_insumo PRIMARY KEY (codigo),
    CONSTRAINT fk_insumo_sec FOREIGN KEY (seccion,laboratorio) REFERENCES modulo2.seccion (codsec,codlab)
);

CREATE TABLE modulo2.item_insumo
(
    insumo bigint NOT NULL,
    id integer NOT NULL,
    numero integer NOT NULL,
    serial character(20) DEFAULT '-',
    estado character(30) DEFAULT '-',
    visible boolean DEFAULT '1',
    existeItem boolean DEFAULT '1',
    observacion character(140),
    fechaMod timestamp NOT NULL,
    fechaAgr timestamp NOT NULL,
    fechaEli timestamp NOT NULL,
    CONSTRAINT pk_item_insumo PRIMARY KEY (id)
);