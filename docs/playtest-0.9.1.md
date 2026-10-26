# Prueba 0.9.1 — Star Platinum sobre cuerpo de jugador

Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7. El JAR se genera con
`./gradlew build` en `build/libs/jojosmod-0.9.1.jar`. El protocolo continúa en 6.
Para comparar la presentación con un amigo, ambos deben usar los recursos 0.9.1.

## Qué cambia

Star Platinum utiliza una base de jugador clásico: cabeza 8×8×8, torso continuo
8×12×4 y brazos y piernas 4×12×4. La musculatura se apoya más en pintura. Sobre
esa base se añaden cabello, diadema, gema, hombreras, guantes, faldón y botas.
La adaptación de Roundabout al estilo de Minecraft sirve de referencia visual;
los detalles y la textura se mantienen propios.

El conjunto tiene 80 cubos, 29 huesos y trece animaciones. Se han recolocado las
poses de guardia y de la mano que intercepta, y la defensa de Star Finger, para
dar espacio a la cabeza y a los guantes mayores. Los sonidos incluidos siguen
siendo los catorce efectos sintetizados de 0.9.0. El [catálogo del anime](star-platinum-anime-audio-catalog.md)
continúa como recopilación de referencias.

## Revisión del jugador

1. Invoca con R y mira el modelo en tercera persona frontal, lateral y trasera.
   Compara la cabeza, torso y extremidades con el cuerpo de tu personaje.
2. Revisa la silueta, el rostro, las hombreras, la musculatura pintada y la ropa.
   Prueba cerca y a varios bloques de distancia, con iluminación normal.
3. Activa C: los guantes deben quedar delante del rostro y la vuelta al reposo
   debe conservar manos, brazos y hombros unidos.
4. Alterna Z y X contra enemigos. Los contactos conservan sus tiempos: 0,2 s para
   Z y ocho pulsos de X entre 0,2 y 1,25 s. Comprueba que los brazos anchos no
   oculten demasiado la acción ni atraviesen el torso.
5. Con voluntad 5, prueba V; con 6, B. Los dos dedos se muestran solo durante
   Star Finger y la mano de intercepción deja espacio delante de la cara.
6. Con voluntad 4, pulsa J desde suelo firme. Revisa la flexión de rodillas,
   botas y faldón durante el impulso y el aterrizaje.
7. Con un amigo, observa sus poses mientras usa las mismas acciones. Cada cliente
   dibuja sus propios recursos; ambos necesitan la revisión para ver el mismo modelo.

Las habilidades, el daño, la progresión y los cooldowns conservan las reglas
descritas en la [ficha de combate](star-platinum-design.md). La fuente editable
está en [Blockbench](../art/star_platinum/star_platinum.bbmodel), con el atlas
incrustado y todos los clips. La [guía de edición](blockbench-geckolib.md) explica
la exportación y cómo conservar cambios manuales.

## Comprobaciones realizadas

`build` pasa. Se comprobaron las medidas de la base clásica, los 80 cubos y 29
huesos, las trece animaciones, sus contactos, la dirección de Star Finger y el
espacio entre guante y cabeza durante la intercepción. Las UV quedan dentro del
atlas, la textura incrustada de Blockbench coincide con la del mod y la generación
produce archivos idénticos. Los catorce OGG anteriores se conservan byte a byte.

El JAR contiene los recursos y su procedencia actualizados y excluye las clases
de pruebas. El cliente cargó el conjunto y se observó el modelo frontal junto al
personaje del jugador. Esta revisión visual no cambia la lógica de combate; no se
volvió a ejecutar la batería de GameTests. La revisión completa de las poses y
la sesión con dos clientes quedan para la prueba descrita arriba.
