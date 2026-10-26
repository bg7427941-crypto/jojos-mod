# Prueba 0.7.0 — Star Platinum creado desde cero

Requiere Minecraft 26.3, NeoForge 26.3.0.95 y GeckoLib 5.5.7 en todos los clientes
y en el servidor. Compila con `./gradlew build` y utiliza `build/libs/jojosmod-0.7.0.jar`.
El protocolo de red es 4; todos los participantes deben actualizar el mod.

Esta entrega cambia modelo, rig, UV, textura, nueve animaciones y diez efectos de
sonido. Las habilidades y la voluntad conservan el balance de la
[guía de progresión 0.6.0](playtest-0.6.0.md). Se sustituyen los recursos importados
por un conjunto creado para este proyecto.

## Aspecto y movimiento

1. Invoca con R y revisa el cuerpo, rostro, cabello, collar, hombreras, guantes,
   faldón y rodillas en primera y tercera persona. La paleta corresponde a Parte 3:
   cuerpo violeta, ojos cian, joya plateada, pañuelo rojo y rodilleras claras.
2. Camina, gira, salta y detente. Revisa articulaciones y transiciones de flotación,
   desplazamiento y reposo; la tela y el cabello deben acompañar el movimiento.
3. Con Z observa preparación, puño a 0,2 s y recuperación. Con X comprueba ocho
   golpes alternos, cada 0,15 s desde el primero.
4. C debe adoptar una guardia frontal con los antebrazos. Comprueba que las manos
   y hombreras no atraviesan el rostro y que la cámara permite ver al enemigo.
5. Con voluntad ≥5, V debe extender **dos dedos de la mano derecha** a los 0,4 s,
   retraerlos antes de 0,7 s y completar la acción a 0,9 s. Fuera de esa acción los
   dedos largos deben quedar ocultos.
6. Repite desde el punto de vista de un amigo, cerca y a cierta distancia, buscando
   texturas invertidas, partes que desaparecen o saltos entre poses.

Valora especialmente rostro, proporciones, sensación de fuerza, claridad de la
guardia y visibilidad al atacar en primera persona. El render de desarrollo y el
boceto conceptual no sustituyen esta valoración dentro de Minecraft.

## Sonido original

Desactiva packs de audio personalizados para escuchar el conjunto incluido.
Activa subtítulos y ajusta Jugadores en Música y sonidos.

- R: llegada y retirada de energía; no hay grabaciones de voces incluidas.
- Z/X: aire y golpes con cuerpo, sin chasquidos al terminar los clips.
- C: tensión defensiva e impacto corto al bloquear.
- V: extensión elástica y retracción.

Usad X o V a la vez con un amigo. Cancelar o desinvocar uno debe interrumpir su
señal, conservando la del otro. Los sonidos deben atenuarse con la distancia.
El volumen y el timbre necesitan valoración con una salida de audio real.

Los [modelos editables](../art/star_platinum/README.md) y la
[síntesis de sonido](../art/sounds/original-audio.md) permiten revisar cómo se ha
creado cada recurso. El [paquete local](anime-audio.md) sigue disponible para quien
quiera probar otras muestras en su cliente.
