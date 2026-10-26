# Revisión visual 0.9.2 — adaptación de Roundabout

Esta revisión adapta el modelo y atlas de Star Platinum de Roundabout al rig
GeckoLib del proyecto. Se entrega para valorar su apariencia. La procedencia,
los créditos y la licencia están en [las fuentes editables](../art/star_platinum/README.md)
y acompañan el JAR. Las habilidades y la progresión mantienen su comportamiento.

Compila con `./gradlew build`; el archivo es `build/libs/jojosmod-0.9.2.jar`.
Usa Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7. Sustituye el JAR anterior
en `mods`, evitando cargar dos versiones juntas. El protocolo continúa en 6.
Para comparar con un amigo, ambos deben utilizar esta revisión visual.

## Apariencia

Las [vistas de desarrollo](../art/star_platinum/preview.png) muestran frente,
tres cuartos, perfil y espalda. Son renders del modelo de ejecución, no capturas
del juego. Los 79 cubos y el atlas anime 128×128 conservan el rostro, el cabello,
las hombreras y el vestuario de Roundabout; las muñecas y tobillos se articulan
para las animaciones del mod. No se importan sus keyframes ni extremidades
duplicadas de ráfaga.

En un mundo de pruebas, usa `/give @s jojosmod:stardust_arrow` si todavía no
tienes Stand y actívala mirando al aire. Invoca con R y usa F5 para observarlo
junto al jugador desde distintas cámaras. Valora rostro, silueta, proporciones,
color y visibilidad a distancia normal de combate.

## Movimiento

- R: aparición y retirada, sin piezas o dedos extensibles residuales.
- Z y X: hombros y codos acompañan los contactos de los puñetazos.
- C: guantes al frente del rostro, con espacio entre mano y cabeza.
- V, voluntad ≥5: dos dedos de la mano derecha, contacto a 0,4 s y retirada a 0,7 s.
- B, voluntad ≥6: mano preparada y gesto de captura al acertar una flecha.
- J, voluntad ≥4: impulso, piernas recogidas y flexión al aterrizar.

Camina, gira y cancela acciones recibiendo daño o desinvocando. Comprueba que
guantes, botas, faldón y cabello regresan al reposo. La tela plana debe verse
desde ambos lados. La [guía de salto](playtest-0.9.0.md) conserva las pruebas
de gameplay y de servidor dedicado con otro cliente.

Los catorce sonidos sintetizados y los trece clips de animación mantienen los
mismos bytes de 0.9.1. Esta revisión cambia el modelo, atlas y procedencia;
no añade grabaciones del anime.

## Verificación de esta revisión

`./gradlew build --offline --no-daemon` pasa. La conversión se regenera de forma
idéntica y verifica los hashes Git de las tres entradas de Roundabout. Se revisaron
los 79 cubos, 29 huesos, UV dentro del atlas, textura incrustada y nombres y
duraciones de los trece clips. La auditoría de animaciones mantiene los contactos,
la dirección de Star Finger, la ventana de intercepción y la separación entre
guante y cabeza con el rig adaptado. Se inspeccionaron renders de reposo, guardia,
intercepción, Star Finger y salto.

En Minecraft 26.3 se cargó el modelo y atlas, se invocó con R y se observó de
frente junto al jugador con la cámara F5. Las poses de acción se revisaron en
renders; no se completó una sesión con dos clientes reales para esta entrega.

El JAR contiene los recursos verificados, el manifiesto de procedencia y la
licencia completa de Roundabout; excluye las herramientas y clases de QA. Las
animaciones de ejecución y los catorce OGG coinciden byte por byte con 0.9.1. La
batería de GameTests no se repitió para esta revisión de recursos. La valoración
visual en tu PC y la observación con un segundo cliente quedan en esta guía.
