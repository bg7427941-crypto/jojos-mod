# Dominio avanzado de Star Platinum — prueba 0.10.0

Esta entrega añade dos desafíos personales de voluntad y mejoras de enfriamiento.
Conserva las habilidades, modelo, animaciones y los ocho clips de sonido añadidos
manualmente en `main`. No añade parada temporal ni un encuentro con DIO.

## Instalar

1. Compila con Java 25: `./gradlew build` o `gradlew.bat build` en Windows.
2. Sustituye el JAR anterior en `mods` por `build/libs/jojosmod-0.10.0.jar`.
3. Usa Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7.
4. Actualiza cliente y servidor juntos: **protocolo 8**. Los clientes 0.9.3 no son compatibles.

Los clips subidos manualmente ya se incluyen en el JAR. Desactiva otros paquetes
que sustituyan estos sonidos para escuchar esa selección durante la prueba.

## Recorrido de supervivencia

Despierta Star Platinum con una flecha Stardust y completa los desafíos básicos.
El despertar da +2 de voluntad una vez; precisión, ráfaga y guardia dan +1 cada
uno. Desde voluntad inicial 1 se llega a 6. El salto sigue requiriendo 4, Star
Finger 5 e intercepción 6. Consulta `/stand progress` para ver reglas y contadores.

| Desafío avanzado | Condición | Recompensa única |
| --- | --- | --- |
| Star Finger a distancia | Causar daño de salud a 3 hostiles distintos, con el punto de contacto a más de 3 bloques de tus ojos | +1 de voluntad; enfriamiento de V de 6 a 5 s |
| Intercepción | Atrapar correctamente flechas de 3 tiradores hostiles distintos con B | +1 de voluntad; enfriamiento de B de 3 a 2,5 s |

La mejora comienza en la siguiente activación, sin acortar el enfriamiento que
esté corriendo al completar el desafío. No cambia daño, alcance, preparación ni
ventana de captura. Los cinco desafíos permiten llegar naturalmente a voluntad 8.
Ese valor no desbloquea todavía parada temporal.

## Comprobar Star Finger

- Juega en supervivencia con el Stand invocado y voluntad al menos 5.
- Golpea a un husk u otro hostil situado a unos cinco bloques. Debe avanzar una vez.
- Repite contra ese mismo enemigo: el contador no vuelve a avanzar.
- Un impacto cercano, un fallo, una pared o daño absorbido por completo no cuentan.
- Tras tres hostiles distintos, comprueba el +1 de voluntad y la línea «dominado».
- Espera el enfriamiento actual; desde la siguiente activación, V debe recuperarse en 5 s.

La distancia usa el punto donde el rayo toca al enemigo. El centro de una criatura
grande puede estar más lejos aunque su superficie esté dentro de tres bloques.

## Comprobar intercepción

- Juega con voluntad al menos 6 y mira hacia un esqueleto que te esté disparando.
- Activa B para recibir la flecha entre 0,1 y menos de 0,5 s desde la activación.
- El primer tirador cuenta una vez. Usa tres esqueletos distintos para completar.
- Una flecha de un amigo o dispensador puede seguir siendo interceptable según
  las reglas normales, pero no da progreso. Tampoco cuentan criaturas pasivas o aliados.
- Activar B sin atrapar una flecha, atraparla fuera de ventana o recibirla por
  detrás no da progreso. Las flechas con Perforación conservan su comportamiento anterior.
- Comprueba el +1 de voluntad y, desde la siguiente activación, el enfriamiento de 2,5 s.

## Guardado y juego con amigos

1. Deja uno de los desafíos en 1/3 o 2/3; guarda y vuelve a entrar.
2. Muere y recupera tu Stand: contadores y bonificaciones deben conservarse.
3. Dos jugadores pueden progresar con los mismos enemigos por sus propias acciones.
   La recompensa de uno no debe modificar al otro. No se exige el golpe final.
4. Repetir acciones, reconectar o guardar/recuperar un disco no repite premios.
5. Un mundo anterior conserva el despertar y sus tres desafíos; los dos nuevos
   empiezan vacíos, sin volver a conceder recompensas antiguas.

Los GameTests comprueban combate, elegibilidad, enfriamientos, premios, guardado,
copia tras muerte y codecs. La legibilidad del panel, el ritmo de entrenamiento,
la escucha y una partida con dos clientes reales necesitan valoración dentro del juego.
