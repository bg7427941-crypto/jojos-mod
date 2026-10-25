# Prueba 0.6.0 — Star Finger, voluntad y sprites

Requiere Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7, tanto en el cliente
como en el servidor. Todos los participantes deben usar la misma versión del mod.
Compila con `./gradlew build` y usa `build/libs/jojosmod-0.6.0.jar`.
Haz una copia de tu mundo antes de actualizar la instancia de prueba.

## Progresión sin comandos de voluntad

1. Con un personaje sin Stand, consigue una flecha Stardust en un cofre nuevo de
   templo del desierto (20 % por cofre). Para preparar la prueba puedes usar
   `/give @s jojosmod:stardust_arrow`.
2. Usa la flecha. El primer despertar concede +2; la voluntad inicial pasa de 1 a 3.
   Juega en **supervivencia** para entrenar. Invoca con **R**.
3. Comprueba el panel superior izquierdo y `/stand progress`, disponible sin OP.
   Pulsa **V**: con voluntad 3 debe explicar el requisito de 5.
4. Usa **Z** y causa daño de salud a tres enemigos hostiles distintos: precisión
   pasa de 0/3 a 3/3 y concede +1. Repetir el mismo enemigo no vuelve a contar.
5. Completa otro desafío: **X** contra tres enemigos distintos, o **C** reduciendo
   ataques físicos frontales de tres enemigos distintos. Un mismo enemigo puede
   contar una vez en cada categoría. Animales y jugadores no cuentan.
6. Al llegar a voluntad 5 debe aparecer el desbloqueo de Star Finger. Completar
   el tercer desafío permite llegar a 6; los premios no se repiten.

Para una prueba controlada, puedes invocar husks con `/summon minecraft:husk ~ ~ ~3`.
Hazlo en una zona despejada. La guardia cubre 120° frente a tu mirada, dura hasta
2 s, reduce daño un 60 % y ralentiza el movimiento; recibir ataques aún causa daño.
No entrenes en creativo: sus acciones no conceden progreso.

## Star Finger y presentación

- Con voluntad ≥5 y el Stand invocado, apunta a un objetivo situado a unos 5
  bloques y pulsa **V**. Deben verse dos dedos extensibles, una estela y un impacto.
  Causa 4 puntos sin armadura tras 0,4 s, termina en 0,9 s y tiene cooldown de 6 s.
- Comprueba que falla fuera de alcance y ante una pared. Fallar, recibir daño de
  salud durante la preparación o desinvocar conserva el cooldown.
- Mientras ejecutas V, prueba Z/X/C: las acciones no deben superponerse.
- Revisa en primera y tercera persona la extensión, la retracción y la vuelta al
  reposo. Los dedos extensibles no deben aparecer al caminar o usar Z/X/C.
- Revisa los ocho sprites en la pestaña creativa, la mano y el suelo: dos flechas,
  fragmento, discos vacío/cargado y tres núcleos. Deben distinguirse sobre fondos
  claros y oscuros, sin texturas ausentes.
- Cambia idioma, escala de interfaz y las teclas. El panel debe mostrar voluntad,
  progreso y la tecla actual de Star Finger; se oculta con F1, F3 y los menús.

El audio incluido sigue usando muestras CC0. El [paquete de audio local](anime-audio.md)
permite sustituir eventos con clips autorizados, sin cambiar código. No incluye
grabaciones del anime. El evento de inicio de ráfaga suena una vez por X; evita
poner voces largas en el swing que se reproduce por cada pulso.

## Persistencia y juego con un amigo

Guarda con un desafío parcial, sal y vuelve a entrar. El contador y la voluntad
deben permanecer; vuelve a invocar con R. Repite después de morir y de guardar el
Stand en un disco con el guardián. Ninguna operación debe repetir los premios.

En un servidor, ambos jugadores pueden entrenar contra enemigos compartidos:
cada uno avanza por sus propias acciones, sin requerir el golpe final. Comprobad
que el panel muestra solo el progreso propio y que el otro jugador ve Star Finger.
Con PvP desactivado o fuego amigo bloqueado, V debe respetar esas reglas.

La sensación de alcance, claridad de los dedos, legibilidad del panel y ritmo de
progreso requieren valoración humana. La parada temporal y los encuentros de DIO
siguen pendientes; estos desafíos constituyen el primer tramo de progresión.
