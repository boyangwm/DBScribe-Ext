INSERT INTO modulo2.usuario(
            usbid, contrasena, tipo, nombre)
    VALUES  ('jefea', 'jefea', 'jefelab', 'Jefe A'),
            ('jefeb', 'jefeb', 'jefelab', 'Jefe B'),
            ('solicitantea', 'solicitante', 'profesor', 'Solicitante A'),
            ('solicitanteb', 'solicitante', 'profesor', 'Solicitante B'),
            ('10-11252', 'esteban', 'pregrado', 'Esteban Oliveros');

INSERT INTO modulo2.laboratorio(
            codlab, nombre, jefe)
    VALUES  ('laba', 'laboratorio a', 'jefea'),
            ('labb', 'laboratorio b', 'jefeb');

INSERT INTO modulo2.seccion(
            codsec, nombre, codlab, jefe)
    VALUES  ('seca', 'laboratorio a', 'laba', 'jefea'),
            ('secb', 'laboratorio b', 'labb', 'jefeb');

INSERT INTO modulo2.trabaja(
            trabajador, seccion, laboratorio)
    VALUES  ('jefea','seca','laba'),
            ('jefeb','secb','labb');

INSERT INTO modulo2.gestiona(
            seccion, laboratorio, gestor)
    VALUES  ('seca', 'laba', 'jefea'),
            ('secb', 'labb', 'jefeb');
