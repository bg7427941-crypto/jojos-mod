# Prueba 0.8.0 — Intercepción precisa

Instala Minecraft 26.3, NeoForge 26.3.0.95, GeckoLib 5.5.7 y el mod 0.8.0.
Todos los clientes y el servidor deben actualizar: el protocolo de red es 5.
El JAR se obtiene con `./gradlew build` en `build/libs/jojosmod-0.8.0.jar`.

## Desbloqueo en supervivencia

Despierta Star Platinum y completa precisión, ráfaga y guardia contra tres enemigos
hostiles distintos por categoría. Desde la voluntad inicial 1, el despertar lleva
a 3 y cada desafío completo añade 1. Con 5 se obtiene Star Finger; con 6 se obtiene
la **intercepción precisa**, asignada por defecto a **B**. El panel y `/stand progress`
muestran ambos desbloqueos. Las teclas se pueden cambiar en Controles.

Para una prueba rápida con permisos de operador: `/standtest willpower 6` con
Star Platinum asignado. Esto sirve para comprobar la habilidad; no prueba el
recorrido natural. Las partidas de 0.7.0 con voluntad 6 ya la tienen disponible.

## Uso y límites

Invoca con R, mira hacia la flecha y pulsa B justo antes del impacto. La preparación
dura 2 ticks (0,1 s). La ventana acepta impactos desde el tick 2 hasta el 9,
sin incluir el 10: **0,1 ≤ tiempo < 0,5 s** a 20 TPS. La acción termina a los
12 ticks (0,6 s) si no acierta. El cooldown de **3 s** comienza al pulsar B,
tanto al acertar como al fallar o ser interrumpido.

Solo neutraliza **una flecha normal, con efectos o espectral** de Minecraft, dentro
de un cono de 120° frente a la mirada. La flecha desaparece antes de aplicar su
daño, fuego o efectos. No devuelve munición ni concede voluntad.
Las flechas con **Perforación** atraviesan la intercepción y ofrecen una respuesta
contra esta defensa. Tridentes, pociones, bolas de fuego y otros tipos siguen sus reglas
habituales. Los tiros propios o aliados no consumen la intercepción; los disparos
de jugadores también respetan PvP y fuego amigo.

No se puede usar a la vez que otra acción. Recibir daño de salud antes de atrapar
la flecha interrumpe la preparación. Desinvocar, morir, cambiar de dimensión o perder
el Stand cancela la acción. No es una reducción general de daño: fuera de la
ventana, desde atrás y contra ataques cuerpo a cuerpo se conserva el daño habitual.

La referencia es la precisión de Star Platinum al interceptar una bala al inicio
de Stardust Crusaders. Las flechas, requisito de voluntad, duración y cooldown son
adaptaciones de jugabilidad del mod.

## Comprobaciones para el jugador

1. Enfréntate a un esqueleto con espacio para observar su disparo. Comprueba que
   anticipar y acertar se sienten distintos, y que no basta con repetir B sin parar.
2. Revisa la pose de preparación y el cierre de la mano al acertar, con sus destellos,
   sonidos y subtítulos. Valora lectura del gesto y volumen en primera y tercera persona.
3. Deja llegar flechas antes y después de la ventana, y desde atrás; deben seguir
   dañando. Prueba también una flecha con efectos y una espectral.
4. Recibe un golpe durante la preparación y desinvoca antes del impacto: la acción
   debe cancelarse y conservar su cooldown.
5. Con un amigo y PvP activado, disparad flechas y alternad quién intercepta.
   Uno no debe proteger al otro. Dos flechas seguidas deben superar una única
   activación después de que esta haya neutralizado la primera.
6. Guarda, reconecta y revisa los desafíos anteriores. No deben perderse ni volver
   a dar sus recompensas. La intercepción no añade un formato persistente nuevo.

Las pruebas automáticas acompañan estas comprobaciones; la dificultad de reaccionar,
la latencia con amigos y el timbre necesitan valoración en una partida real.
